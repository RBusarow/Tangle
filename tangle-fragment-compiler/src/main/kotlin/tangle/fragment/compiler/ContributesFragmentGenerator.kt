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

import com.google.auto.service.AutoService
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.*
import com.squareup.kotlinpoet.*
import dagger.Binds
import dagger.Provides
import dagger.multibindings.IntoMap
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import tangle.fragment.compiler.FragmentInjectParams.Fragment
import tangle.inject.compiler.*
import tangle.inject.compiler.asClassName
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
class ContributesFragmentGenerator : CodeGenerator {

  override fun isApplicable(context: AnvilContext): Boolean = true

  override fun generateCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> {
    val bindings = projectFiles
      .classesAndInnerClass(module)
      .mapNotNull { clazz ->

        val annotation = clazz.findAnnotation(FqNames.contributesFragment, module)
          ?: return@mapNotNull null

        val classDescriptor = clazz.requireClassDescriptor(module)

        require(classDescriptor.isFragment(), classDescriptor) {
          "The annotation `${annotation.text}` can only be applied " +
            "to classes which extend ${FqNames.androidxFragment.asString()}"
        }

        val packageName = clazz.containingKtFile
          .packageFqName
          .safePackageString(dotSuffix = false)

        var fragmentInject = false

        val constructor = clazz.fragmentInjectConstructor(module)
          ?.also { fragmentInject = true }
          ?: clazz.injectConstructor(module)

        require(constructor != null, classDescriptor) {
          "Classes annotated with `${annotation.text}` must have a constructor annotated with " +
            "`@${FqNames.inject.asString()}` or `@${FqNames.fragmentInject.asString()}`."
        }

        val injectedParams = Fragment.create(module, clazz, constructor).constructorParams

        Binding(
          injectedParams = injectedParams,
          packageName = packageName,
          scopeName = annotation.scope(module),
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
    val moduleName = "Tangle_${scopeClassName.simpleNames.joinToString("_")}_Fragment_Module"

    val fragmentInjected = bindingList.filter { it.fragmentInject }

    val companionObject = if (fragmentInjected.isNotEmpty()) {
      TypeSpec.companionObjectBuilder()
        .applyEach(fragmentInjected) { binding ->

          val args = binding.injectedParams.asArgumentList(
            asProvider = false,
            includeModule = false
          )

          val fragmentFactoryClassNameString =
            "${binding.fragmentClass.generateClassName()}_Factory"

          val factoryImplClassName = ClassName(binding.packageName, fragmentFactoryClassNameString)

          addFunction(
            FunSpec.builder(name = "provide_${binding.fragmentClassName.simpleNames.joinToString("_")}")
              .addAnnotation(ClassNames.provides)
              .addAnnotation(ClassNames.tangleFragmentProviderMap)
              .applyEach(binding.injectedParams) { argument ->
                val paramType = when {
                  argument.isWrappedInLazy -> argument.lazyTypeName
                  else -> argument.typeName
                }
                addParameter(argument.name, paramType)
              }
              .returns(binding.fragmentClassName)
              .addStatement("return·%T.newInstance($args)", factoryImplClassName)
              .build()
          )
        }
        .build()
    } else null

    val content = FileSpec.buildFile(packageName, moduleName) {
      addType(
        TypeSpec.interfaceBuilder(ClassName(packageName, moduleName))
          .addAnnotation(ClassNames.module)
          .addAnnotation(
            AnnotationSpec.builder(ClassNames.contributesTo)
              .addMember("%T::class", scopeClassName)
              .build()
          )
          .applyEach(bindingList) { binding ->

            val fragmentKeySpec = AnnotationSpec.builder(ClassNames.tangleFragmentKey)
              .addMember("%T::class", binding.fragmentClassName)
              .build()

            addFunction(
              FunSpec.builder(name = "bind_${binding.fragmentClassName.simpleNames.joinToString("_")}")
                .addModifiers(KModifier.ABSTRACT)
                .returns(ClassNames.androidxFragment)
                .apply {
                  if (binding.fragmentInject) {
                    addParameter(
                      ParameterSpec.builder("fragment", binding.fragmentClassName)
                        .addAnnotation(ClassNames.tangleFragmentProviderMap)
                        .build()
                    )
                  } else {
                    receiver(binding.fragmentClassName)
                  }
                }
                .addAnnotation(ClassNames.binds)
                .addAnnotation(ClassNames.intoMap)
                .addAnnotation(fragmentKeySpec)
                .apply {
                  if (binding.fragmentInject) {
                    addAnnotation(ClassNames.tangleFragmentProviderMap)
                  }
                }
                .build()
            )
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
    val fragmentClass: KtClassOrObject,
    val fragmentClassName: ClassName,
    val fragmentInject: Boolean
  )
}
