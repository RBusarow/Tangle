/*
 * Copyright (C) 2022 Rick Busarow
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tangle.viewmodel.compiler

import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.FunctionReference
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.anvil.compiler.internal.reference.generateClassName
import com.squareup.anvil.compiler.internal.safePackageString
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
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.ConstructorInjectParameter
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.MemberInjectParameter
import tangle.inject.compiler.Parameter
import tangle.inject.compiler.find
import tangle.inject.compiler.generateSimpleNameString
import tangle.inject.compiler.mapToParameters
import tangle.inject.compiler.memberInjectedParameters
import tangle.inject.compiler.require
import tangle.inject.compiler.requireTangleParamName
import tangle.inject.compiler.uniqueName
import tangle.inject.compiler.wrapInLazy
import tangle.inject.compiler.wrapInProvider

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
  val viewModelClassDescriptor: ClassReference,
  val viewModelConstructorParams: List<ConstructorInjectParameter>,
  val viewModelFactoryClassNameString: String,
  val viewModelFactoryClassName: ClassName,
  val viewModelFactoryConstructorParams: List<Parameter>,
  val constructor: FunctionReference,
  val memberInjectedParams: List<MemberInjectParameter>,
  val typeParameters: List<TypeVariableName>,
  val viewModelClassSimpleName: String,
  val viewModelTypeName: TypeName,
  val savedStateParam: Parameter?
) : ViewModelInjectParams {
  companion object {
    fun create(
      module: ModuleDescriptor,
      viewModelClass: ClassReference,
      constructor: FunctionReference
    ): ViewModelParams {
      val packageName = viewModelClass.packageFqName.safePackageString()

      val viewModelFactoryClassName = viewModelClass.generateClassName(suffix = "_Factory")
        .asClassName()

      val memberInjectParameters = viewModelClass.memberInjectedParameters()

      val viewModelConstructorParams = constructor.parameters
        .mapToParameters(module)

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

      val typeParameters = viewModelClass.typeParameters.map { it.typeVariableName }

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
        viewModelClassDescriptor = viewModelClass,
        viewModelConstructorParams = viewModelConstructorParams,
        viewModelFactoryClassNameString = viewModelFactoryClassName.simpleName,
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
        qualifiers = emptyList(),
        isDaggerAssisted = false
      )
    }
  }
}

data class Factory(
  override val packageName: String,
  override val scopeName: FqName,
  override val viewModelClassName: ClassName,
  val viewModelParams: ViewModelParams,
  val factoryInterface: ClassReference,
  val factoryInterfaceClassName: ClassName,
  val viewModelFactoryClassName: ClassName,
  val factoryImplClassName: ClassName,
  val tangleParams: List<TangleParameter>,
  val functionName: String
) : ViewModelInjectParams {
  data class TangleParameter(
    val key: String,
    val name: String,
    val typeName: TypeName
  )

  companion object {
    fun create(
      module: ModuleDescriptor,
      factoryInterface: ClassReference,
      viewModelClass: ClassReference,
      constructor: FunctionReference
    ): Factory {
      val packageName = factoryInterface.packageFqName.safePackageString(dotSuffix = false)

      val contributesAnnotation = viewModelClass.annotations.find(FqNames.contributesViewModel)

      require(
        value = contributesAnnotation != null,
        classReference = viewModelClass
      ) {
        "@${FqNames.vmInject.shortName().asString()}-annotated ViewModels must also " +
          "have a `${FqNames.contributesViewModel.asString()}` class annotation."
      }

      val scopeClass = viewModelClass.annotations.find(FqNames.mergeComponent)!!.scope()
      val scopeFqName = scopeClass.fqName

      val viewModelFactoryClassName = viewModelClass.generateClassName(suffix = "_Factory")
        .asClassName()

      val functions = factoryInterface.functions

      require(functions.size == 1, factoryInterface) {
        "@${FqNames.vmInjectFactory.shortName().asString()}-annotated types must have " +
          "exactly one abstract function -- without a default implementation -- " +
          "which returns the ${FqNames.vmInject.shortName().asString()} ViewModel type."
      }

      val function = functions[0]

      val functionParameters = function.parameters

      val factoryInterfaceClassName = factoryInterface.asClassName()
      val factoryImplSimpleName =
        "${factoryInterfaceClassName.generateSimpleNameString()}_Impl"
      val factoryImplClassName = ClassName(packageName, factoryImplSimpleName)

      val tangleParams = functionParameters.map {
        TangleParameter(
          it.requireTangleParamName(),
          it.name,
          it.type().asTypeName()
        )
      }

      val functionName = function.name

      val viewModelParams =
        ViewModelParams.create(module, viewModelClass, constructor)

      return Factory(
        packageName = packageName,
        scopeName = scopeFqName,
        viewModelClassName = viewModelParams.viewModelClassName,
        viewModelParams = viewModelParams,
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
