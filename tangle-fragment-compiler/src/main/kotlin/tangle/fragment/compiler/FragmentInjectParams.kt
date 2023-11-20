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
@file:OptIn(com.squareup.anvil.annotations.ExperimentalAnvilApi::class)

package tangle.fragment.compiler

import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.MemberFunctionReference
import com.squareup.anvil.compiler.internal.reference.TypeParameterReference
import com.squareup.anvil.compiler.internal.reference.TypeReference
import com.squareup.anvil.compiler.internal.reference.allSuperTypeClassReferences
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.anvil.compiler.internal.reference.generateClassName
import com.squareup.anvil.compiler.internal.safePackageString
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality.ABSTRACT
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import tangle.inject.compiler.ConstructorInjectParameter
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.MemberInjectParameter
import tangle.inject.compiler.TangleCompilationException
import tangle.inject.compiler.find
import tangle.inject.compiler.generateSimpleNameString
import tangle.inject.compiler.mapToParameters
import tangle.inject.compiler.memberInjectedParameters
import tangle.inject.compiler.require
import tangle.inject.compiler.requireTangleParamName

internal sealed class FragmentInjectParams {
  abstract val packageName: String
  abstract val scopeName: FqName
  abstract val fragmentClassName: ClassName

  data class Fragment(
    override val packageName: String,
    override val scopeName: FqName,
    override val fragmentClassName: ClassName,
    val fragmentClass: ClassReference,
    val fragmentFactoryClassNameString: String,
    val fragmentFactoryClassName: ClassName,
    val constructor: MemberFunctionReference,
    val constructorParams: List<ConstructorInjectParameter>,
    val memberInjectedParams: List<MemberInjectParameter>,
    val typeParameters: List<TypeParameterReference>,
    val fragmentClassSimpleName: String,
    val fragmentTypeName: TypeName
  ) : FragmentInjectParams() {
    companion object {
      fun create(
        module: ModuleDescriptor,
        fragmentClass: ClassReference,
        constructor: MemberFunctionReference
      ): Fragment {
        val packageName = fragmentClass.packageFqName
          .safePackageString(dotSuffix = false)

        val fragmentClassDescriptor = fragmentClass

        val fragmentFactoryClassID = fragmentClass.generateClassName(suffix = "_Factory")
        val fragmentFactoryClassName = fragmentFactoryClassID
          .asClassName()

        val contributesFragmentAnnotation = fragmentClass.annotations
          .find(FqNames.contributesFragment)!!

        val scopeName = contributesFragmentAnnotation.scope().fqName

        val memberInjectParameters = fragmentClassDescriptor.memberInjectedParameters()

        val allFragmentConstructorParams = constructor.parameters
          .mapToParameters(module)

        val typeParameters = fragmentClass.typeParameters

        val fragmentClassSimpleName = fragmentClass.asClassName()
          .simpleNames
          .joinToString("_")

        val fragmentClassName = fragmentClass.asClassName()

        val fragmentTypeName = fragmentClassName.let {
          if (typeParameters.isEmpty()) it else it.parameterizedBy(
            typeParameters.map { it.typeVariableName }
          )
        }
        return Fragment(
          packageName = packageName,
          scopeName = scopeName,
          fragmentClassName = fragmentClassName,
          fragmentClass = fragmentClassDescriptor,
          fragmentFactoryClassNameString = fragmentFactoryClassName.simpleName,
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
    val factoryClass: ClassReference,
    val factoryInterfaceClassName: ClassName,
    val fragmentFactoryClassName: ClassName,
    val factoryImplClassName: ClassName,
    val typeParameters: List<TypeParameterReference>,
    val tangleParams: List<TangleParameter>,
    val functionName: String
  ) : FragmentInjectParams() {
    data class TangleParameter(
      val key: String,
      val name: String,
      val type: TypeReference,
      val typeName: TypeName
    )

    companion object {
      fun create(
        module: ModuleDescriptor,
        factoryInterface: ClassReference,
        fragmentClass: ClassReference,
        constructor: MemberFunctionReference
      ): Factory {
        val packageName = factoryInterface.packageFqName.safePackageString(dotSuffix = false)

        val contributesAnnotation = fragmentClass.annotations.find(FqNames.contributesFragment)

        require(
          value = contributesAnnotation != null,
          fragmentClass
        ) {
          "@${FqNames.fragmentInject.shortName().asString()}-annotated Fragments must also " +
            "have a `${FqNames.contributesFragment.asString()}` class annotation."
        }

        val scopeClass = fragmentClass.annotations.find(FqNames.contributesFragment)!!.scope()
        val scopeName = scopeClass.fqName

        val fragmentFactoryClassName = fragmentClass.generateClassName(suffix = "_Factory")
          .asClassName()

        val functions = factoryInterface.functions

        require(functions.size == 1, factoryInterface) {
          "@${FqNames.fragmentInjectFactory.shortName().asString()}-annotated types must have " +
            "exactly one abstract function -- without a default implementation -- " +
            "which returns the ${FqNames.fragmentInject.shortName().asString()} Fragment type."
        }

        val function = functions[0]

        val typeParameters = factoryInterface.typeParameters

        val functionParameters = function.parameters

        val factoryInterfaceClassName = factoryInterface.asClassName()
        val factoryImplSimpleName =
          "${factoryInterfaceClassName.generateSimpleNameString()}_Impl"
        val factoryImplClassName = ClassName(packageName, factoryImplSimpleName)

        val tangleParams = functionParameters.map {
          TangleParameter(
            it.requireTangleParamName(),
            it.name,
            it.type(),
            it.type().asTypeName()
          )
        }

        tangleParams.checkForBundleSafe(factoryInterface)

        val functionName = function.name

        val fragmentParams =
          Fragment.create(module, fragmentClass, constructor)

        return Factory(
          packageName = packageName,
          scopeName = scopeName,
          fragmentClassName = fragmentParams.fragmentClassName,
          fragmentParams = fragmentParams,
          factoryClass = factoryInterface,
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

      internal fun List<TangleParameter>.checkForBundleSafe(classReference: ClassReference) {
        fun TangleParameter.superTypeFqNames() = type.asClassReference()
          .allSuperTypeClassReferences(false)
          .map { it.fqName }

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
            classReference,
            "Tangle found Fragment runtime arguments which cannot " +
              "be inserted into a Bundle: $listString"
          )
        }
      }
    }
  }
}
