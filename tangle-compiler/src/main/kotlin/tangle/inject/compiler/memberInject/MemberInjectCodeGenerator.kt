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

package tangle.inject.compiler.memberInject

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.internal.reference.classAndInnerClassReferences
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.TangleCodeGenerator
import tangle.inject.compiler.assistedInjectConstructor
import tangle.inject.compiler.fragmentInjectConstructor
import tangle.inject.compiler.injectConstructor
import tangle.inject.compiler.memberInject.fileGen.TangleAppScope_TangleInjector_Scope_ModuleGenerator
import tangle.inject.compiler.memberInject.fileGen.TangleInjectorGenerator
import tangle.inject.compiler.memberInject.fileGen.TangleInjector_ModuleGenerator
import tangle.inject.compiler.require
import tangle.inject.compiler.vmInjectConstructor
import java.io.File

@Suppress("UNUSED")
@OptIn(com.squareup.anvil.annotations.ExperimentalAnvilApi::class)
@AutoService(CodeGenerator::class)
class MemberInjectCodeGenerator : TangleCodeGenerator() {

  val fileGenerators = listOf(
    TangleInjector_ModuleGenerator,
    TangleAppScope_TangleInjector_Scope_ModuleGenerator,
    TangleInjectorGenerator
  )

  override fun generateTangleCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> {
    return projectFiles
      .classAndInnerClassReferences(module)
      .mapNotNull { clazz ->

        val annotationEntry = clazz.annotations.find { it.fqName == FqNames.tangleScope }
          ?: return@mapNotNull null

        val injectConstructor = clazz.injectConstructor()
          ?: clazz.fragmentInjectConstructor()
          ?: clazz.assistedInjectConstructor()
          ?: clazz.vmInjectConstructor()

        require(
          injectConstructor == null,
          psi = { clazz.clazz }
        ) {
          "@TangleScope cannot be applied to classes which use injected constructors."
        }

        MemberInjectParams.create(clazz, annotationEntry)
      }
      .flatMap { params ->
        fileGenerators.mapNotNull { generator ->
          generator.generate(codeGenDir, params)
        }
      }
      .toList()
  }
}
