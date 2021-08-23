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
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.internal.classesAndInnerClasses
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import tangle.inject.compiler.vmInjectConstructor
import java.io.File

@Suppress("UNUSED")
@AutoService(CodeGenerator::class)
class VMInjectCodeGenerator : CodeGenerator {

  override fun isApplicable(context: AnvilContext): Boolean = true

  override fun generateCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> {

    val viewModelParamsList = projectFiles
      .flatMap { it.classesAndInnerClasses(module) }
      .mapNotNull {
        val constructor = it.vmInjectConstructor(module) ?: return@mapNotNull null
        it to constructor
      }
      .map { (viewModelClass, constructor) ->
        ViewModelParams.create(module, viewModelClass, constructor)
      }

    val viewModels = with(ViewModelFactoryGenerator()) {
      viewModelParamsList
        .map { generate(codeGenDir, it) }
    }

    val moduleParams = viewModelParamsList
      .groupBy { it.packageName }
      .map { (packageName, byPackageName) ->

        TangleScopeModule(
          packageName = packageName,
          viewModelParamsList = byPackageName
        )
      }
    val tangleScopeModules = with(ViewModelTangleScopeModuleGenerator()) {
      moduleParams
        .map { generate(codeGenDir, it) }
    }
    val tangleAppScopeModules = with(ViewModelTangleAppScopeModuleGenerator()) {
      moduleParams
        .map { generate(codeGenDir, it) }
    }

    return viewModels + tangleScopeModules + tangleAppScopeModules
  }
}
