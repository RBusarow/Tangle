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
import com.squareup.anvil.compiler.api.GeneratedFile
import java.io.File

fun interface FileGenerator<T> {

  fun generate(codeGenDir: File, params: T): GeneratedFile?

  /**
   * Write [content] into a new file for the given [packageName] and [fileName]. [fileName] usually
   * refers to the class name.
   */
  @ExperimentalAnvilApi
  @Suppress("unused")
  fun createGeneratedFile(
    codeGenDir: File,
    packageName: String,
    fileName: String,
    content: String
  ): GeneratedFile {
    val directory = File(codeGenDir, packageName.replace('.', File.separatorChar))
    val file = File(directory, "$fileName.kt")
    check(file.parentFile.exists() || file.parentFile.mkdirs()) {
      "Could not generate package directory: ${file.parentFile}"
    }
    file.writeText(content)

    return GeneratedFile(file, content)
  }
}
