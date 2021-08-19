package tangle.viewmodel.compiler.params

import com.squareup.anvil.compiler.internal.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.types.KotlinType
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.require

data class Factory(
  override val packageName: String,
  override val scopeName: FqName,
  override val viewModelClassName: ClassName,
  val factoryDescriptor: ClassDescriptor,
  val factoryInterface: KtClassOrObject,
  val factoryInterfaceClassName: ClassName,
  override val viewModelFactoryClassName: ClassName,
  val factoryImplClassName: ClassName,
  val assistedParams: List<AssistedParameter>,
  val typeParameters: List<TypeVariableName>,
  override val factoryFunctionName: String
) : ViewModelInjectParams {
  data class AssistedParameter(
    val name: String,
    val kotlinType: KotlinType,
    val typeName: TypeName
  )

  companion object {
    fun create(
      module: ModuleDescriptor,
      factoryInterface: KtClassOrObject,
      viewModelClass: KtClass
    ): Factory {
      val packageName = factoryInterface.containingKtFile
        .packageFqName
        .safePackageString(dotSuffix = false)

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

      val assistedParams = functionParameters.map {
        AssistedParameter(
          it.name.asString(),
          it.type,
          it.type.asTypeName()
        )
      }

      val functionName = function.name.asString()

      val typeParameters = factoryInterface.typeVariableNames(module)

      return Factory(
        packageName = packageName,
        scopeName = FqNames.tangleAppScope,
        viewModelClassName = viewModelClass.asClassName(),
        factoryDescriptor = factoryDescriptor,
        factoryInterface = factoryInterface,
        factoryInterfaceClassName = factoryInterfaceClassName,
        viewModelFactoryClassName = viewModelFactoryClassName,
        factoryImplClassName = factoryImplClassName,
        assistedParams = assistedParams,
        typeParameters = typeParameters,
        factoryFunctionName = functionName
      )
    }

    private fun ClassDescriptor.functions(): List<FunctionDescriptor> = unsubstitutedMemberScope
      .getContributedDescriptors(DescriptorKindFilter.FUNCTIONS)
      .asSequence()
      .filterIsInstance<FunctionDescriptor>()
      .filter { it.modality == Modality.ABSTRACT }
      .filter {
        it.visibility == DescriptorVisibilities.PUBLIC ||
          it.visibility == DescriptorVisibilities.PROTECTED
      }
      .toList()
  }
}
