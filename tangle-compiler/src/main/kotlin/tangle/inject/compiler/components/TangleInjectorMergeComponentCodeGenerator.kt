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
import com.squareup.anvil.compiler.internal.classesAndInnerClasses
import com.squareup.anvil.compiler.internal.findAnnotation
import com.squareup.anvil.compiler.internal.scope
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.TangleCodeGenerator
import tangle.inject.compiler.components.fileGen.*
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
    .flatMap { it.classesAndInnerClasses(module) }
    .mapNotNull { clazz ->

      val (annotation, forSubcomponent) = clazz.findAnnotation(FqNames.mergeComponent, module)
        ?.let { it to false }
        ?: clazz.findAnnotation(FqNames.mergeSubcomponent, module)
          ?.let { it to true }
        ?: return@mapNotNull null

      val scopeFqName = annotation.scope(module)

      // don't generate code for the internal scopes
      when (scopeFqName) {
        FqNames.tangleAppScope -> return@mapNotNull null
        FqNames.tangleViewModelScope -> return@mapNotNull null
      }

      MergeComponentParams.create(module, scopeFqName, clazz, forSubcomponent)
    }
    .distinctBy { it.scopeFqName }
    .flatMap { params ->
      fileGenerators.mapNotNull { generator ->
        generator.generate(codeGenDir, params)
      }
    }
}
