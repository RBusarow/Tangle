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

package tangle.inject.compiler

import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

/**
 * Wraps all code generation in [delegateToAnvilUnsafe], which catches Anvil exceptions
 * and rethrows them as TangleCompilationException.
 *
 * Anvil can be pretty sensitive to attempts to resolve types in generated code,
 * and Tangle generates a lot of code which then needs further code-gen.
 *
 * If an Anvil exception occurs during Tangle's compilation, it's probably a Tangle in that Tangle's
 * attempting to use an unsupported Anvil API.
 *
 * In short, this is an attempt to prevent flooding Anvil's issue tracker with Tangle bug reports.
 */
@OptIn(ExperimentalAnvilApi::class)
abstract class TangleCodeGenerator : CodeGenerator {

  final override fun isApplicable(context: AnvilContext): Boolean = true

  final override fun generateCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> {
    return delegateToAnvilUnsafe {
      generateTangleCode(codeGenDir, module, projectFiles)
    }
  }

  abstract fun generateTangleCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile>
}
