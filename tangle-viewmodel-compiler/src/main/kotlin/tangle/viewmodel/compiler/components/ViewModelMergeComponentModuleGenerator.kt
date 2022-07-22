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

package tangle.viewmodel.compiler.components

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.TypeSpec
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.addContributesTo
import tangle.inject.compiler.addFunction
import tangle.inject.compiler.buildFile
import tangle.viewmodel.compiler.tangleViewModelProviderMap
import tangle.viewmodel.compiler.tangleViewModelProviderMapKeySet
import tangle.viewmodel.compiler.viewModelClassSet
import tangle.viewmodel.compiler.viewModelMap
import java.io.File

class ViewModelMergeComponentModuleGenerator : FileGenerator<MergeComponentParams> {
  override fun generate(
    codeGenDir: File,
    params: MergeComponentParams
  ): GeneratedFile {

    val packageName = params.packageName

    val className = params.mergeComponentModuleClassName
    val classNameString = className.simpleName

    val content = FileSpec.buildFile(packageName, classNameString) {
      TypeSpec.interfaceBuilder(className)
        .addAnnotation(ClassNames.module)
        .addContributesTo(params.scopeClassName)
        .addFunction("bindTangleViewModelProviderMapKeySet") {
          addAnnotation(ClassNames.multibinds)
          addAnnotation(ClassNames.tangleViewModelProviderMapKeySet)
          addModifiers(ABSTRACT)
          returns(ClassNames.viewModelClassSet)
        }
        .addFunction("bindTangleViewModelProviderMap") {
          addAnnotation(ClassNames.multibinds)
          addAnnotation(ClassNames.tangleViewModelProviderMap)
          addModifiers(ABSTRACT)
          returns(ClassNames.viewModelMap)
        }
        .build()
        .let { addType(it) }
    }

    return createGeneratedFile(codeGenDir, packageName, classNameString, content)
  }
}
