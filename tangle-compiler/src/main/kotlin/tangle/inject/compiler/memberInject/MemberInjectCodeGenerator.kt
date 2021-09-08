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

package tangle.inject.compiler.memberInject

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.internal.classesAndInnerClass
import com.squareup.anvil.compiler.internal.findAnnotation
import com.squareup.anvil.compiler.internal.requireClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import tangle.inject.compiler.*
import tangle.inject.compiler.memberInject.fileGen.TangleAppScope_TangleInjector_Scope_ModuleGenerator
import tangle.inject.compiler.memberInject.fileGen.TangleInjectorGenerator
import tangle.inject.compiler.memberInject.fileGen.TangleInjector_ModuleGenerator
import java.io.File

@Suppress("UNUSED")
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
      .classesAndInnerClass(module)
      .mapNotNull { clazz ->

        val annotationEntry = clazz.findAnnotation(FqNames.tangleScope, module)
          ?: return@mapNotNull null

        val injectConstructor = clazz.injectConstructor(module)
          ?: clazz.fragmentInjectConstructor(module)
          ?: clazz.assistedInjectConstructor(module)
          ?: clazz.vmInjectConstructor(module)

        require(injectConstructor == null,
          { clazz.requireClassDescriptor(module) }
        ) {
          "@TangleScope cannot be applied to classes which use injected constructors."
        }

        MemberInjectParams.create(module, clazz, annotationEntry)
      }
      .flatMap { params ->
        fileGenerators.mapNotNull { generator ->
          generator.generate(codeGenDir, params)
        }
      }
      .toList()
  }
}
