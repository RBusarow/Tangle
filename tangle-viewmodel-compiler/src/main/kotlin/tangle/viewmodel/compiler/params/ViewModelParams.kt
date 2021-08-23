/*
 * Copyright (C) 2021 Rick Busarow
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

package tangle.viewmodel.compiler.params

import com.squareup.anvil.compiler.internal.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.jvm.jvmSuppressWildcards
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import tangle.inject.compiler.*
import tangle.viewmodel.compiler.androidxSavedStateHandle

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
  val savedStateParam: Parameter?,
  val factory: Factory?
) : ViewModelInjectParams {

  override val factoryFunctionName: String
    get() = factory?.factoryFunctionName ?: DEFAULT_FACTORY_FUNCTION_NAME

  companion object {

    const val DEFAULT_FACTORY_FUNCTION_NAME = "create"

    fun create(
      module: ModuleDescriptor,
      viewModelClass: KtClassOrObject,
      constructor: KtConstructor<*>,
      factory: Factory?
    ): ViewModelParams {

      val packageName = viewModelClass.containingKtFile
        .packageFqName
        .safePackageString(dotSuffix = false)

      val viewModelClassDescriptor = viewModelClass.requireClassDescriptor(module)

      val viewModelFactoryClassNameString = "${viewModelClass.generateClassName()}_Factory"
      val viewModelFactoryClassName = ClassName(packageName, viewModelFactoryClassNameString)

      val memberInjectParameters = viewModelClassDescriptor.memberInjectedParameters(module)

      val viewModelConstructorParams = constructor.valueParameters.mapToParameters(module)

      viewModelConstructorParams.forEach { param ->
        require(
          value = !(param.isAssisted && param.isTangleParam),
          declarationDescriptor = { viewModelClassDescriptor }
        ) {
          "${viewModelClassDescriptor.name}'s constructor parameter `${param.name}` is annotated " +
            "with both `${FqNames.vmAssisted}` (meaning it's passed directly from a Factory) " +
            "and `${FqNames.tangleParam}` (meaning it's passed via SavedStateHandle).  Only one " +
            "of these annotations can be applied to a single property."
        }
      }

      val (daggerConstructorParams, savedStateParams) = viewModelConstructorParams
        .filterNot { it.isAssisted }
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
        savedStateParam = finalSavedStateParam,
        factory = factory
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
        isAssisted = false
      )
    }
  }
}
