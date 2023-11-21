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
@file:OptIn(com.squareup.anvil.annotations.ExperimentalAnvilApi::class)

package tangle.work.compiler

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.internal.reference.classAndInnerClassReferences
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.TangleCodeGenerator
import tangle.inject.compiler.assistedInjectConstructor
import java.io.File

@Suppress("UNUSED")
@AutoService(CodeGenerator::class)
class TangleWorkerCodeGenerator : TangleCodeGenerator() {

  override fun generateTangleCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> {
    val workerParamsList = projectFiles
      .classAndInnerClassReferences(module)
      .filter { it.isAnnotatedWith(FqNames.tangleWorker) }
      .mapNotNull { workerClass ->

        val constructor = workerClass.assistedInjectConstructor() ?: return@mapNotNull null

        WorkerParams.create(module, workerClass, constructor)
      }
      .toList()

    val factoryInterfaces = workerParamsList
      .map { AssistedWorkerFactoryGenerator.generate(codeGenDir, it) }

    val moduleParams = workerParamsList
      .groupBy { it.packageName }
      .map { (packageName, byPackageName) ->

        TangleAppScopeModule(
          packageName = packageName,
          workerParamsList = byPackageName
        )
      }

    val moduleInterfaces = moduleParams
      .map { AssistedWorkerFactoryModuleGenerator.generate(codeGenDir, it) }

    return factoryInterfaces + moduleInterfaces
  }
}

data class TangleAppScopeModule(
  val packageName: String,
  val workerParamsList: List<WorkerParams>
)
