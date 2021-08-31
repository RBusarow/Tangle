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

package tangle.viewmodel.compiler

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.classesAndInnerClasses
import com.squareup.anvil.compiler.internal.hasAnnotation
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.nonStaticOuterClasses
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.TangleCodeGenerator
import tangle.inject.compiler.TangleCompilationException
import tangle.inject.compiler.vmInjectConstructor
import tangle.viewmodel.compiler.params.Factory
import tangle.viewmodel.compiler.params.TangleScopeModule
import tangle.viewmodel.compiler.params.ViewModelParams
import java.io.File

@Suppress("UNUSED")
@AutoService(CodeGenerator::class)
class VMInjectCodeGenerator : TangleCodeGenerator() {

  override fun generateTangleCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> {

    val paramsList = projectFiles
      .flatMap { file ->

        val factoryParams = file.classesAndInnerClasses(module)
          .filter { it.hasAnnotation(FqNames.vmInjectFactory, module) }
          .map { factoryInterface ->

            factoryInterface.nonStaticOuterClasses()
              .firstOrNull { it.vmInjectConstructor(module) != null }
              ?.let { viewModelClass ->

                val factory = Factory.create(
                  module,
                  factoryInterface,
                  viewModelClass
                )

                val viewModel = ViewModelParams.create(
                  module,
                  viewModelClass,
                  viewModelClass.vmInjectConstructor(module)!!,
                  factory
                )

                viewModel
              } ?: throw TangleCompilationException(
              "The @${FqNames.vmInjectFactory.shortName().asString()}-annotated interface " +
                "`${factoryInterface.fqName}` must be defined inside a ViewModel " +
                "which is annotated with `@${FqNames.vmInject.shortName().asString()}`."
            )
          }

        val alreadyParsedViewModels = factoryParams
          .map { it.viewModelClassName }
          .toSet()

        val viewModelParams = file.classesAndInnerClasses(module)
          .filterNot { it.asClassName() in alreadyParsedViewModels }
          .mapNotNull { clazz ->

            if (clazz.asClassName() in alreadyParsedViewModels) return@mapNotNull null

            val constructor = clazz.vmInjectConstructor(module) ?: return@mapNotNull null

            ViewModelParams.create(module, clazz, constructor, null)
          }

        factoryParams + viewModelParams
      }
      .toList()

    val generated = paramsList.map { params ->
      with(ViewModelFactoryGenerator()) { generate(codeGenDir, params) }
    }

    val tangleScopeModules = with(ViewModelTangleScopeModuleGenerator()) {
      paramsList
        .flatMap { listOfNotNull(it, it.factory) }
        .groupBy { it.packageName }
        .map { (packageName, byPackageName) ->

          TangleScopeModule(
            packageName = packageName,
            viewModelParamsList = byPackageName
          )
        }
        .map { generate(codeGenDir, it) }
    }

    // creates `@Provides` functions for any ViewModels which don't require an assisted factory
    val tangleAppScopeModules = with(ViewModelTangleAppScopeModuleGenerator()) {
      paramsList
        .groupBy { it.packageName }
        .map { (packageName, byPackageName) ->

          TangleScopeModule(
            packageName = packageName,
            viewModelParamsList = byPackageName
          )
        }
        .map { generate(codeGenDir, it) }
    }

    return generated + tangleScopeModules + tangleAppScopeModules
  }
}
