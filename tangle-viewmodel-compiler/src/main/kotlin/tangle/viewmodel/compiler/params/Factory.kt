package tangle.viewmodel.compiler.params

import com.squareup.anvil.compiler.internal.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.types.KotlinType
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.require

data class Factory(
  override val packageName: String,
  override val scopeName: FqName,
  override val viewModelClassName: ClassName,
  val factoryInterfaceClassName: ClassName,
  val viewModelFactoryClassName: ClassName,
  val functionArguments: List<FunctionParameter>,
  val typeParameters: List<TypeVariableName>,
  override val factoryFunctionName: String
) : ViewModelInjectParams {

  data class FunctionParameter(
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

      val functionArguments = functionParameters.map {
        FunctionParameter(
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
        factoryInterfaceClassName = factoryInterfaceClassName,
        viewModelFactoryClassName = viewModelFactoryClassName,
        functionArguments = functionArguments,
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
