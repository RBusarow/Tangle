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

package tangle.fragment.compiler

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.anvil.compiler.internal.reference.classAndInnerClassReferences
import com.squareup.anvil.compiler.internal.reference.generateClassName
import com.squareup.anvil.compiler.internal.safePackageString
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.utils.addToStdlib.applyIf
import tangle.fragment.compiler.FragmentInjectParams.Fragment
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.ConstructorInjectParameter
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.TangleCodeGenerator
import tangle.inject.compiler.addContributesTo
import tangle.inject.compiler.addFunction
import tangle.inject.compiler.applyEach
import tangle.inject.compiler.asArgumentList
import tangle.inject.compiler.buildFile
import tangle.inject.compiler.find
import tangle.inject.compiler.fragmentInjectConstructor
import tangle.inject.compiler.generateSimpleNameString
import tangle.inject.compiler.injectConstructor
import tangle.inject.compiler.isFragment
import tangle.inject.compiler.require
import java.io.File

/**
 * Generates a module to create qualified bindings for the Fragment itself
 *
 * ```
 * @Module
 * @ContributesTo(Unit::class)
 * public interface Tangle_Unit_Fragment_Module {
 *   @Binds
 *   @IntoMap
 *   @FragmentKey(MyFragment::class)
 *   @TangleFragmentProviderMap
 *   public fun bind_MyFragment(@TangleFragmentProviderMap fragment: MyFragment): Fragment
 *
 *   public companion object {
 *     @Provides
 *     @TangleFragmentProviderMap
 *     public fun provide_MyFragment(numbers: @JvmSuppressWildcards List<Int>): MyFragment =
 *         MyFragment_Factory.newInstance(numbers)
 *   }
 * }
 * ```
 */
@Suppress("unused")
@AutoService(CodeGenerator::class)
class ContributesFragmentGenerator : TangleCodeGenerator() {

  override fun generateTangleCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> {
    val bindings = projectFiles
      .classAndInnerClassReferences(module)
      .mapNotNull { clazz ->

        val annotation = clazz.annotations.find(FqNames.contributesFragment)
          ?: return@mapNotNull null

        require(clazz.isFragment(), clazz) {
          "The annotation `${annotation.annotation.text}` can only be applied " +
            "to classes which extend ${FqNames.androidxFragment.asString()}"
        }

        val packageName = clazz.packageFqName
          .safePackageString(dotSuffix = false)

        var fragmentInject = false

        val constructor = clazz.fragmentInjectConstructor()
          ?.also { fragmentInject = true }
          ?: clazz.injectConstructor()

        require(constructor != null, clazz) {
          "Classes annotated with `${annotation.fqName}` must have a constructor annotated with " +
            "`@${FqNames.inject.asString()}` or `@${FqNames.fragmentInject.asString()}`."
        }

        val injectedParams = Fragment.create(module, clazz, constructor).constructorParams

        Binding(
          injectedParams = injectedParams,
          packageName = packageName,
          scopeName = annotation.scope().fqName,
          fragmentClass = clazz,
          fragmentClassName = clazz.asClassName(),
          fragmentInject = fragmentInject
        )
      }
      .toList()

    return bindings
      .groupBy { it.packageName }
      .flatMap { (packageName, byPackageName) ->
        byPackageName
          .groupBy { it.scopeName }
          .map { (scopeName, byScopeName) ->
            createDaggerModule(
              scopeClassName = scopeName.asClassName(module),
              codeGenDir = codeGenDir,
              packageName = packageName,
              bindingList = byScopeName
            )
          }
      }
  }

  private fun createDaggerModule(
    scopeClassName: ClassName,
    codeGenDir: File,
    packageName: String,
    bindingList: List<Binding>
  ): GeneratedFile {
    val moduleName = "Tangle_${scopeClassName.generateSimpleNameString()}_Fragment_Module"

    val fragmentInjected = bindingList.filter { it.fragmentInject }

    val companionObject = if (fragmentInjected.isNotEmpty()) {
      TypeSpec.companionObjectBuilder()
        .applyEach(fragmentInjected) { binding ->

          val args = binding.injectedParams.asArgumentList(
            asProvider = false,
            includeModule = false
          )

          val fragmentFactoryClassName = binding.fragmentClass
            .generateClassName(suffix = "_Factory")
            .asClassName()

          addFunction("provide_${binding.fragmentClassName.generateSimpleNameString()}") {
            addAnnotation(ClassNames.provides)
            addAnnotation(ClassNames.tangleFragmentProviderMap)
            applyEach(binding.injectedParams) { argument ->
              val paramType = when {
                argument.isWrappedInLazy -> argument.lazyTypeName
                else -> argument.typeName
              }
              addParameter(argument.name, paramType)
            }
            returns(binding.fragmentClassName)
            addStatement("return·%T.newInstance($args)", fragmentFactoryClassName)
          }
            .build()
        }
        .build()
    } else null

    fun Binding.bindingFunSpec(): FunSpec {
      val fragmentKeySpec = AnnotationSpec.builder(ClassNames.tangleFragmentKey)
        .addMember("%T::class", fragmentClassName)
        .build()

      return FunSpec.builder(name = "bind_${fragmentClassName.generateSimpleNameString()}")
        .addModifiers(KModifier.ABSTRACT)
        .returns(ClassNames.androidxFragment)
        .apply {
          if (fragmentInject) {
            addParameter(
              ParameterSpec.builder("fragment", fragmentClassName)
                .addAnnotation(ClassNames.tangleFragmentProviderMap)
                .build()
            )
          } else {
            receiver(fragmentClassName)
          }
        }
        .addAnnotation(ClassNames.binds)
        .addAnnotation(ClassNames.intoMap)
        .addAnnotation(fragmentKeySpec)
        .applyIf(fragmentInject) {
          addAnnotation(ClassNames.tangleFragmentProviderMap)
        }
        .build()
    }

    val content = FileSpec.buildFile(packageName, moduleName) {
      addType(
        TypeSpec.interfaceBuilder(ClassName(packageName, moduleName))
          .addAnnotation(ClassNames.module)
          .addContributesTo(scopeClassName)
          .applyEach(bindingList) { binding ->
            addFunction(binding.bindingFunSpec())
          }
          .apply {
            if (companionObject != null) {
              addType(companionObject)
            }
          }
          .build()
      )
    }

    return createGeneratedFile(
      codeGenDir = codeGenDir,
      packageName = packageName,
      fileName = moduleName,
      content = content
    )
  }

  internal data class Binding(
    val injectedParams: List<ConstructorInjectParameter>,
    val packageName: String,
    val scopeName: FqName,
    val fragmentClass: ClassReference,
    val fragmentClassName: ClassName,
    val fragmentInject: Boolean
  )
}
