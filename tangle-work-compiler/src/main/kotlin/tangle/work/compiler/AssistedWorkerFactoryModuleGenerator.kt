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

package tangle.work.compiler

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import tangle.inject.compiler.*
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
          .addAnnotation(
            AnnotationSpec.builder(ClassNames.contributesTo)
              .addMember("%T::class", ClassNames.tangleAppScope)
              .build()
          )
          .applyEach(params.workerParamsList) { workerParams ->

            addFunction(
              "multibind_${workerParams.assistedFactoryClassName.simpleNames.joinToString("_")}"
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
