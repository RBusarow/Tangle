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
import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.anvil.compiler.internal.reference.classAndInnerClassReferences
import com.squareup.anvil.compiler.internal.reference.toClassReference
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.nonStaticOuterClasses
import tangle.fragment.compiler.FragmentInjectParams.Factory
import tangle.fragment.compiler.FragmentInjectParams.Fragment
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.TangleCodeGenerator
import tangle.inject.compiler.TangleCompilationException
import tangle.inject.compiler.fragmentInjectConstructor
import tangle.inject.compiler.require
import java.io.File

@Suppress("unused")
@AutoService(CodeGenerator::class)
class FragmentInjectGenerator : TangleCodeGenerator() {

  override fun generateTangleCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> {
    val paramsList = projectFiles
      .flatMap { file ->

        val factoryParams = listOf(file).classAndInnerClassReferences(module)
          .filter { it.isAnnotatedWith(FqNames.fragmentInjectFactory) }
          .map { factoryInterface ->

            factoryInterface.clazz
              .nonStaticOuterClasses()
              .map { it.toClassReference(module) }
              .firstOrNull { it.fragmentInjectConstructor() != null }
              ?.let { fragmentClass ->
                Factory.create(
                  module = module,
                  factoryInterface = factoryInterface,
                  fragmentClass = fragmentClass,
                  constructor = fragmentClass.fragmentInjectConstructor()!!
                )
              } ?: throw TangleCompilationException(
              "The @${FqNames.fragmentInjectFactory.shortName().asString()}-annotated interface " +
                "`${factoryInterface.fqName}` must be defined inside a Fragment " +
                "which is annotated with `@${FqNames.fragmentInject.shortName().asString()}`."
            )
          }

        val alreadyParsedFragments = factoryParams.map { it.fragmentClassName }.toSet()

        listOf(file).classAndInnerClassReferences(module)
          .filterNot { it.asClassName() in alreadyParsedFragments }
          .forEach { clazz ->

            if (clazz.asClassName() in alreadyParsedFragments) return@forEach

            require(clazz.fragmentInjectConstructor() == null, clazz) {
              "@${FqNames.fragmentInject.shortName().asString()} must only be applied " +
                "to the constructor of a Fragment, and that fragment must have a corresponding " +
                FqNames.fragmentInjectFactory.shortName().asString() +
                "-annotated factory interface."
            }
          }

        factoryParams.toList().plus(factoryParams.map { it.fragmentParams })
      }
      .toList()

    if (paramsList.isEmpty()) {
      return emptyList()
    }

    val factoryImpls = paramsList
      .filterIsInstance<Factory>()
      .map { FragmentAssisted_Factory_Impl_Generator.generate(codeGenDir, it) }

    val fragmentFactories = paramsList
      .filterIsInstance<Fragment>()
      .map { Fragment_Factory_Generator.generate(codeGenDir, it) }

    val daggerModules = paramsList
      .filterIsInstance<FragmentInjectParams.Factory>()
      .groupBy { it.packageName }
      .flatMap { (packageName, byPackageName) ->
        byPackageName
          .groupBy { it.scopeName }
          .map { (scopeName, byScopeName) ->

            val params = FragmentInject_ModuleGenerator.FragmentBindingModuleParams(
              scopeClassName = scopeName.asClassName(module),
              packageName = packageName,
              factoryParams = byScopeName
            )

            FragmentInject_ModuleGenerator.generate(codeGenDir, params)
          }
      }

    return fragmentFactories + factoryImpls + daggerModules
  }
}
