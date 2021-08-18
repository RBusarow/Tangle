package tangle.viewmodel.compiler

import com.squareup.anvil.compiler.internal.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.jvm.jvmSuppressWildcards
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality.ABSTRACT
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.types.KotlinType
import tangle.inject.compiler.*

data class TangleScopeModule(
  val packageName: String,
  val viewModelParamsList: List<ViewModelParams>
)

sealed interface ViewModelInjectParams {
  val packageName: String
  val scopeName: FqName
  val viewModelClassName: ClassName
}

data class ViewModelParams(
  override val packageName: String,
  override val scopeName: FqName,
  override val viewModelClassName: ClassName,
  val viewModelClassDescriptor: ClassDescriptor,
  val viewModelConstructorParams: List<ConstructorInjectParameter>,
  val viewModelFactoryClassNameString: String,
  val viewModelFactoryClassName: ClassName,
  val viewModelFactoryConstructorParams: List<Parameter>,
  val constructor: KtConstructor<*>,
  val memberInjectedParams: List<MemberInjectParameter>,
  val typeParameters: List<TypeVariableName>,
  val viewModelClassSimpleName: String,
  val viewModelTypeName: TypeName,
  val savedStateParam: Parameter?
) : ViewModelInjectParams {
  companion object {
    fun create(
      module: ModuleDescriptor,
      viewModelClass: KtClassOrObject,
      constructor: KtConstructor<*>
    ): ViewModelParams {

      val packageName = viewModelClass.containingKtFile
        .packageFqName
        .safePackageString(dotSuffix = false)

      val viewModelClassDescriptor = viewModelClass.requireClassDescriptor(module)

      val viewModelFactoryClassNameString = "${viewModelClass.generateClassName()}_Factory"
      val viewModelFactoryClassName = ClassName(packageName, viewModelFactoryClassNameString)

      // val scopeName = viewModelClass.scope(FqNames.contributesViewModel, module)

      val memberInjectParameters = viewModelClassDescriptor.memberInjectedParameters(module)

      val viewModelConstructorParams = constructor.valueParameters.mapToParameters(module)

      val (daggerConstructorParams, savedStateParams) = viewModelConstructorParams
        .partition { !it.isTangleParam }

      val tempSavedStateParam = daggerConstructorParams
        .firstOrNull {
          it.typeName == ClassNames.androidxSavedStateHandle ||
            it.typeName == ClassNames.androidxSavedStateHandle.jvmSuppressWildcards()
        }
        ?: createSavedStateParameter(viewModelConstructorParams)

      val needsExtraSavedStateParam =
        tempSavedStateParam !in daggerConstructorParams && savedStateParams.isNotEmpty()

      val factoryConstructorParams = if (needsExtraSavedStateParam) {
        daggerConstructorParams + memberInjectParameters + tempSavedStateParam
      } else {
        daggerConstructorParams + memberInjectParameters
      }

      val typeParameters = viewModelClass.typeVariableNames(module)

      val viewModelClassSimpleName = viewModelClass.asClassName()
        .simpleNames
        .joinToString("_")

      val viewModelClassName = viewModelClass.asClassName()

      val viewModelTypeName = viewModelClassName.let {
        if (typeParameters.isEmpty()) it else it.parameterizedBy(typeParameters)
      }

      val finalSavedStateParam = when {
        needsExtraSavedStateParam -> tempSavedStateParam
        savedStateParams.isNotEmpty() -> tempSavedStateParam
        else -> null
      }

      return ViewModelParams(
        packageName = packageName,
        scopeName = FqNames.tangleAppScope,
        viewModelClassName = viewModelClassName,
        viewModelClassDescriptor = viewModelClassDescriptor,
        viewModelConstructorParams = viewModelConstructorParams,
        viewModelFactoryClassNameString = viewModelFactoryClassNameString,
        viewModelFactoryClassName = viewModelFactoryClassName,
        viewModelFactoryConstructorParams = factoryConstructorParams,
        constructor = constructor,
        memberInjectedParams = memberInjectParameters,
        typeParameters = typeParameters,
        viewModelClassSimpleName = viewModelClassSimpleName,
        viewModelTypeName = viewModelTypeName,
        savedStateParam = finalSavedStateParam
      )
    }

    private fun createSavedStateParameter(
      viewModelConstructorParams: List<ConstructorInjectParameter>
    ): ConstructorInjectParameter {
      return ConstructorInjectParameter(
        name = viewModelConstructorParams.uniqueName("savedStateHandleProvider"),
        typeName = ClassNames.androidxSavedStateHandle,
        providerTypeName = ClassNames.androidxSavedStateHandle.wrapInProvider(),
        lazyTypeName = ClassNames.androidxSavedStateHandle.wrapInLazy(),
        isWrappedInProvider = true,
        isWrappedInLazy = false,
        tangleParamName = null,
        qualifiers = emptyList()
      )
    }
  }
}

data class Factory(
  override val packageName: String,
  override val scopeName: FqName,
  override val viewModelClassName: ClassName,
  val viewModelParams: ViewModelParams,
  val factoryDescriptor: ClassDescriptor,
  val factoryInterface: KtClassOrObject,
  val factoryInterfaceClassName: ClassName,
  val viewModelFactoryClassName: ClassName,
  val factoryImplClassName: ClassName,
  val tangleParams: List<TangleParameter>,
  val functionName: String
) : ViewModelInjectParams {
  data class TangleParameter(
    val key: String,
    val name: String,
    val kotlinType: KotlinType,
    val typeName: TypeName
  )

  companion object {
    fun create(
      module: ModuleDescriptor,
      factoryInterface: KtClassOrObject,
      viewModelClass: KtClass,
      constructor: KtConstructor<*>
    ): Factory {
      val packageName = factoryInterface.containingKtFile
        .packageFqName
        .safePackageString(dotSuffix = false)

      val contributesAnnotation = viewModelClass.findAnnotation(
        FqNames.contributesViewModel, module
      )

      require(
        value = contributesAnnotation != null,
        declarationDescriptor = { viewModelClass.requireClassDescriptor(module) }
      ) {
        "@${FqNames.vmInject.shortName().asString()}-annotated ViewModels must also " +
          "have a `${FqNames.contributesViewModel.asString()}` class annotation."
      }

      val scopeName = viewModelClass.scope(FqNames.contributesViewModel, module)

      val viewModelFactoryClassName =
        ClassName(packageName, "${viewModelClass.generateClassName()}_Factory")

      val factoryDescriptor = factoryInterface.requireClassDescriptor(module)

      val functions = factoryDescriptor.functions()

      require(functions.size == 1, factoryDescriptor) {
        "@${FqNames.vmInjectFactory.shortName().asString()}-annotated types must have " +
          "exactly one abstract function -- without a default implementation -- " +
          "which returns the ${FqNames.vmInject.shortName().asString()} ViewModel type."
      }

      val function = functions[0]

      val functionParameters = function.valueParameters

      val factoryInterfaceClassName = factoryInterface.asClassName()
      val factoryImplSimpleName =
        "${factoryInterfaceClassName.simpleNames.joinToString("_")}_Impl"
      val factoryImplClassName = ClassName(packageName, factoryImplSimpleName)

      val tangleParams = functionParameters.map {
        TangleParameter(
          it.requireTangleParamName(),
          it.name.asString(),
          it.type,
          it.type.asTypeName()
        )
      }

      // tangleParams.checkForBundleSafe(factoryDescriptor)

      val functionName = function.name.asString()

      val viewModelParams =
        ViewModelParams.create(module, viewModelClass, constructor)

      return Factory(
        packageName = packageName,
        scopeName = scopeName,
        viewModelClassName = viewModelParams.viewModelClassName,
        viewModelParams = viewModelParams,
        factoryDescriptor = factoryDescriptor,
        factoryInterface = factoryInterface,
        factoryInterfaceClassName = factoryInterfaceClassName,
        viewModelFactoryClassName = viewModelFactoryClassName,
        factoryImplClassName = factoryImplClassName,
        tangleParams = tangleParams,
        functionName = functionName
      )
    }

    private fun ClassDescriptor.functions(): List<FunctionDescriptor> = unsubstitutedMemberScope
      .getContributedDescriptors(DescriptorKindFilter.FUNCTIONS)
      .asSequence()
      .filterIsInstance<FunctionDescriptor>()
      .filter { it.modality == ABSTRACT }
      .filter {
        it.visibility == DescriptorVisibilities.PUBLIC ||
          it.visibility == DescriptorVisibilities.PROTECTED
      }
      .toList()
  }
}
