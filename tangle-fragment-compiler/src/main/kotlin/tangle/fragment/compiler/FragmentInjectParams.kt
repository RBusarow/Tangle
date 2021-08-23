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

package tangle.fragment.compiler

import com.squareup.anvil.compiler.internal.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality.ABSTRACT
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.supertypes
import tangle.inject.compiler.*

internal sealed class FragmentInjectParams {
  abstract val packageName: String
  abstract val scopeName: FqName
  abstract val fragmentClassName: ClassName

  data class Fragment(
    override val packageName: String,
    override val scopeName: FqName,
    override val fragmentClassName: ClassName,
    val fragmentClassDescriptor: ClassDescriptor,
    val fragmentFactoryClassNameString: String,
    val fragmentFactoryClassName: ClassName,
    val constructor: KtConstructor<*>,
    val constructorParams: List<ConstructorInjectParameter>,
    val memberInjectedParams: List<MemberInjectParameter>,
    val typeParameters: List<TypeVariableName>,
    val fragmentClassSimpleName: String,
    val fragmentTypeName: TypeName
  ) : FragmentInjectParams() {
    companion object {
      fun create(
        module: ModuleDescriptor,
        fragmentClass: KtClassOrObject,
        constructor: KtConstructor<*>
      ): Fragment {
        val packageName = fragmentClass.containingKtFile
          .packageFqName
          .safePackageString(dotSuffix = false)

        val fragmentClassDescriptor = fragmentClass.requireClassDescriptor(module)

        val fragmentFactoryClassNameString = "${fragmentClass.generateClassName()}_Factory"
        val fragmentFactoryClassName = ClassName(packageName, fragmentFactoryClassNameString)

        val scopeName = fragmentClass.scope(FqNames.contributesFragment, module)

        val memberInjectParameters = fragmentClassDescriptor.memberInjectedParameters(module)

        val allFragmentConstructorParams = constructor.valueParameters
          .mapToParameters(module)

        val typeParameters = fragmentClass.typeVariableNames(module)

        val fragmentClassSimpleName = fragmentClass.asClassName()
          .simpleNames
          .joinToString("_")

        val fragmentClassName = fragmentClass.asClassName()

        val fragmentTypeName = fragmentClassName.let {
          if (typeParameters.isEmpty()) it else it.parameterizedBy(typeParameters)
        }
        return Fragment(
          packageName = packageName,
          scopeName = scopeName,
          fragmentClassName = fragmentClassName,
          fragmentClassDescriptor = fragmentClassDescriptor,
          fragmentFactoryClassNameString = fragmentFactoryClassNameString,
          fragmentFactoryClassName = fragmentFactoryClassName,
          constructor = constructor,
          constructorParams = allFragmentConstructorParams,
          memberInjectedParams = memberInjectParameters,
          typeParameters = typeParameters,
          fragmentClassSimpleName = fragmentClassSimpleName,
          fragmentTypeName = fragmentTypeName
        )
      }
    }
  }

  data class Factory(
    override val packageName: String,
    override val scopeName: FqName,
    override val fragmentClassName: ClassName,
    val fragmentParams: Fragment,
    val factoryDescriptor: ClassDescriptor,
    val factoryInterface: KtClassOrObject,
    val factoryInterfaceClassName: ClassName,
    val fragmentFactoryClassName: ClassName,
    val factoryImplClassName: ClassName,
    val typeParameters: List<TypeVariableName>,
    val tangleParams: List<TangleParameter>,
    val functionName: String
  ) : FragmentInjectParams() {
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
        fragmentClass: KtClass,
        constructor: KtConstructor<*>
      ): Factory {
        val packageName = factoryInterface.containingKtFile
          .packageFqName
          .safePackageString(dotSuffix = false)

        val contributesAnnotation = fragmentClass.findAnnotation(
          FqNames.contributesFragment, module
        )

        require(
          value = contributesAnnotation != null,
          declarationDescriptor = { fragmentClass.requireClassDescriptor(module) }
        ) {
          "@${FqNames.fragmentInject.shortName().asString()}-annotated Fragments must also " +
            "have a `${FqNames.contributesFragment.asString()}` class annotation."
        }

        val scopeName = fragmentClass.scope(FqNames.contributesFragment, module)

        val fragmentFactoryClassName =
          ClassName(packageName, "${fragmentClass.generateClassName()}_Factory")

        val factoryDescriptor = factoryInterface.requireClassDescriptor(module)

        val functions = factoryDescriptor.functions()

        require(functions.size == 1, factoryDescriptor) {
          "@${FqNames.fragmentInjectFactory.shortName().asString()}-annotated types must have " +
            "exactly one abstract function -- without a default implementation -- " +
            "which returns the ${FqNames.fragmentInject.shortName().asString()} Fragment type."
        }

        val function = functions[0]

        val typeParameters = factoryInterface.typeVariableNames(module)

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

        tangleParams.checkForBundleSafe(factoryDescriptor)

        val functionName = function.name.asString()

        val fragmentParams =
          Fragment.create(module, fragmentClass, constructor)

        return Factory(
          packageName = packageName,
          scopeName = scopeName,
          fragmentClassName = fragmentParams.fragmentClassName,
          fragmentParams = fragmentParams,
          factoryDescriptor = factoryDescriptor,
          factoryInterface = factoryInterface,
          factoryInterfaceClassName = factoryInterfaceClassName,
          fragmentFactoryClassName = fragmentFactoryClassName,
          factoryImplClassName = factoryImplClassName,
          typeParameters = typeParameters,
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

      internal fun List<TangleParameter>.checkForBundleSafe(descriptor: ClassDescriptor) {
        fun TangleParameter.superTypeFqNames() = kotlinType.supertypes()
          .asSequence()
          .map { it.classDescriptorForType().fqNameSafe }

        val notBundleSafe = filter { tangleParameter ->
          !BundleSafe.contains(tangleParameter.typeName) &&
            tangleParameter.superTypeFqNames().none { BundleSafe.contains(it) }
        }

        if (notBundleSafe.isNotEmpty()) {
          val listString = notBundleSafe.joinToString(
            separator = ",\n",
            prefix = "[",
            postfix = "]"
          ) { "${it.name}: ${it.typeName}" }

          throw TangleCompilationException(
            descriptor,
            "Tangle found Fragment runtime arguments which cannot " +
              "be inserted into a Bundle: $listString"
          )
        }
      }
    }
  }
}
