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

package tangle.viewmodel.compiler.components

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.addFunction
import tangle.inject.compiler.buildFile
import tangle.viewmodel.compiler.tangleViewModelKeysSubcomponent
import tangle.viewmodel.compiler.tangleViewModelKeysSubcomponentFactory
import java.io.File

class ViewModelKeysSubcomponentGenerator : FileGenerator<MergeComponentParams> {
  override fun generate(
    codeGenDir: File,
    params: MergeComponentParams
  ): GeneratedFile {

    val packageName = params.packageName

    val className = params.keysSubcomponentClassName

    val content = FileSpec.buildFile(packageName, className.simpleName) {
      TypeSpec.interfaceBuilder(className)
        .addSuperinterface(ClassNames.tangleViewModelKeysSubcomponent)
        .addAnnotation(
          AnnotationSpec.builder(ClassNames.mergeSubcomponent)
            .addMember("%T::class", ClassNames.tangleAppScope)
            .build()
        )
        .addType(
          TypeSpec.interfaceBuilder("Factory")
            .addSuperinterface(ClassNames.tangleViewModelKeysSubcomponentFactory)
            .addAnnotation(ClassNames.subcomponentFactory)
            .addFunction("create") {
              returns(className)
              addModifiers(KModifier.ABSTRACT, KModifier.OVERRIDE)
            }
            .build()
        )
        .build()
        .let { addType(it) }
    }

    return createGeneratedFile(codeGenDir, packageName, className.simpleName, content)
  }
}
