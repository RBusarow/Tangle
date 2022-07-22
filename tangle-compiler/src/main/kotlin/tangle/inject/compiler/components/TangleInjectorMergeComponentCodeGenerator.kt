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

package tangle.inject.compiler.components

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.internal.reference.classAndInnerClassReferences
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.TangleCodeGenerator
import tangle.inject.compiler.components.fileGen.TangleAppScope_TangleInjectorMapProvider_Subcomponent_Generator
import tangle.inject.compiler.components.fileGen.TangleAppScope_UserScope_to_Component_Module_Generator
import tangle.inject.compiler.components.fileGen.UserScope_Default_InjectedClassToScopeClass_Module_Generator
import tangle.inject.compiler.components.fileGen.UserScope_Default_TangleInjectorMap_Module_Generator
import tangle.inject.compiler.components.fileGen.UserScope_Default_TangleScopeToComponentMap_Module_Generator
import tangle.inject.compiler.components.fileGen.UserScope_TangleInjectorComponent_Generator
import tangle.inject.compiler.components.fileGen.UserScope_TangleScopeMapProviderComponent_Generator
import java.io.File

@Suppress("UNUSED")
@AutoService(CodeGenerator::class)
class TangleInjectorMergeComponentCodeGenerator : TangleCodeGenerator() {

  val fileGenerators = listOf(
    UserScope_TangleInjectorComponent_Generator,
    UserScope_Default_TangleInjectorMap_Module_Generator,
    UserScope_Default_TangleScopeToComponentMap_Module_Generator,
    UserScope_Default_InjectedClassToScopeClass_Module_Generator,
    UserScope_TangleScopeMapProviderComponent_Generator,
    TangleAppScope_TangleInjectorMapProvider_Subcomponent_Generator,
    TangleAppScope_UserScope_to_Component_Module_Generator
  )

  override fun generateTangleCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> = projectFiles
    .classAndInnerClassReferences(module)
    .mapNotNull { clazz ->

      val (annotation, forSubcomponent) = clazz.annotations
        .find { it.fqName == FqNames.mergeComponent }
        ?.let { it to false }
        ?: clazz.annotations.find { it.fqName == FqNames.mergeSubcomponent }
          ?.let { it to true }
        ?: return@mapNotNull null

      val scopeFqName = annotation.scope(parameterIndex = 0).fqName

      // don't generate code for the internal scopes
      when (scopeFqName) {
        FqNames.tangleAppScope -> return@mapNotNull null
        FqNames.tangleViewModelScope -> return@mapNotNull null
      }

      MergeComponentParams.create(module, scopeFqName, clazz.clazz, forSubcomponent)
    }
    .distinctBy { it.scopeFqName }
    .flatMap { params ->
      fileGenerators.mapNotNull { generator ->
        generator.generate(codeGenDir, params)
      }
    }
    .toList()
}
