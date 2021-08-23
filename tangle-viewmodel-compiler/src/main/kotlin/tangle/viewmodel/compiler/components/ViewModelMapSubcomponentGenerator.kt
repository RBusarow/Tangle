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

package tangle.viewmodel.compiler.components

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.kotlinpoet.*
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.addFunction
import tangle.inject.compiler.buildFile
import tangle.viewmodel.compiler.*
import java.io.File

class ViewModelMapSubcomponentGenerator : FileGenerator<MergeComponentParams> {
  override fun generate(
    codeGenDir: File,
    params: MergeComponentParams
  ): GeneratedFile {

    val packageName = params.packageName

    val className = params.mapSubcomponentClassName

    val content = FileSpec.buildFile(packageName, className.simpleName) {
      TypeSpec.interfaceBuilder(className)
        .addSuperinterface(ClassNames.tangleViewModelMapSubcomponent)
        .addAnnotation(
          AnnotationSpec.builder(ClassNames.mergeSubcomponent)
            .addMember("%T::class", ClassNames.tangleScope)
            .build()
        )
        .addType(
          TypeSpec.interfaceBuilder("Factory")
            .addSuperinterface(ClassNames.tangleViewModelMapSubcomponentFactory)
            .addAnnotation(ClassNames.subcomponentFactory)
            .addFunction("create") {
              returns(className)
              addModifiers(KModifier.ABSTRACT, KModifier.OVERRIDE)
              addParameter(
                ParameterSpec.builder("savedStateHandle", ClassNames.androidxSavedStateHandle)
                  .addAnnotation(ClassNames.bindsInstance)
                  .build()
              )
            }
            .build()
        )
        .build()
        .let { addType(it) }
    }

    return createGeneratedFile(codeGenDir, packageName, className.simpleName, content)
  }
}
