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

package tangle.work.compiler

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.addContributesTo
import tangle.inject.compiler.addFunction
import tangle.inject.compiler.applyEach
import tangle.inject.compiler.buildFile
import tangle.inject.compiler.generateSimpleNameString
import java.io.File

object AssistedWorkerFactoryModuleGenerator : FileGenerator<TangleAppScopeModule> {

  override fun generate(
    codeGenDir: File,
    params: TangleAppScopeModule
  ): GeneratedFile {

    val packageName = params.packageName

    val moduleName = "${ClassNames.tangleAppScope.simpleName}_AssistedFactory_Module"

    val content = FileSpec.buildFile(packageName, moduleName) {
      addType(
        TypeSpec
          .interfaceBuilder(ClassName(packageName, moduleName))
          .addAnnotation(ClassNames.module)
          .addContributesTo(ClassNames.tangleAppScope)
          .applyEach(params.workerParamsList) { workerParams ->

            addFunction(
              "multibind_${workerParams.assistedFactoryClassName.generateSimpleNameString()}"
            ) {

              addModifiers(ABSTRACT)
              addParameter("factory", workerParams.assistedFactoryClassName)
              returns(
                ClassNames.assistedWorkerFactory
                  .parameterizedBy(
                    TypeVariableName("out·${ClassNames.androidxListenableWorker.canonicalName}")
                  )
              )
              addAnnotation(ClassNames.binds)
              addAnnotation(ClassNames.intoMap)
              addAnnotation(
                AnnotationSpec.builder(ClassNames.stringKey)
                  .addMember(
                    "%S",
                    workerParams
                      .workerClassName
                      .canonicalName
                      .replace("..", ".")
                  )
                  .build()
              )
              addAnnotation(ClassNames.tangleAssistedWorkerFactoryMap)
            }
          }
          .build()

      )
    }
    return createGeneratedFile(codeGenDir, packageName, moduleName, content)
  }
}
