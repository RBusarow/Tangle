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
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.internal.*
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.nonStaticOuterClasses
import tangle.fragment.compiler.FragmentInjectParams.Factory
import tangle.fragment.compiler.FragmentInjectParams.Fragment
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.TangleCompilationException
import tangle.inject.compiler.asClassName
import tangle.inject.compiler.fragmentInjectConstructor
import tangle.inject.compiler.require
import java.io.File

@Suppress("unused")
@AutoService(CodeGenerator::class)
class FragmentInjectGenerator : CodeGenerator {

  override fun isApplicable(context: AnvilContext): Boolean = true

  override fun generateCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> {
    val paramsList = projectFiles
      .flatMap { file ->

        val factoryParams = file.classesAndInnerClasses(module)
          .filter { it.hasAnnotation(FqNames.fragmentInjectFactory, module) }
          .map { factoryInterface ->

            factoryInterface.nonStaticOuterClasses()
              .firstOrNull { it.fragmentInjectConstructor(module) != null }
              ?.let { fragmentClass ->
                Factory.create(
                  module,
                  factoryInterface,
                  fragmentClass,
                  fragmentClass.fragmentInjectConstructor(module)!!
                )
              } ?: throw TangleCompilationException(
              "The @${FqNames.fragmentInjectFactory.shortName().asString()}-annotated interface " +
                "`${factoryInterface.fqName}` must be defined inside a Fragment " +
                "which is annotated with `@${FqNames.fragmentInject.shortName().asString()}`."
            )
          }

        val alreadyParsedFragments = factoryParams.map { it.fragmentClassName }.toSet()

        file.classesAndInnerClasses(module)
          .filterNot { it.asClassName() in alreadyParsedFragments }
          .forEach { clazz ->

            if (clazz.asClassName() in alreadyParsedFragments) return@forEach

            require(
              clazz.fragmentInjectConstructor(module) == null,
              { clazz.requireClassDescriptor(module) }
            ) {
              "@${FqNames.fragmentInject.shortName().asString()} must only be applied " +
                "to the constructor of a Fragment, and that fragment must have a corresponding " +
                FqNames.fragmentInjectFactory.shortName().asString() +
                "-annotated factory interface."
            }
          }

        listOf(factoryParams, factoryParams.map { it.fragmentParams })
      }
      .flatten()
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
