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
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.applyEach
import tangle.inject.compiler.asArgumentList
import tangle.inject.compiler.buildFile
import java.io.File

object AssistedWorkerFactoryGenerator : FileGenerator<WorkerParams> {

  override fun generate(
    codeGenDir: File,
    params: WorkerParams
  ): GeneratedFile {

    val content = FileSpec.buildFile(
      params.packageName, params.workerClassNameString
    ) {
      addType(
        TypeSpec.classBuilder(params.assistedFactoryClassName)
          .apply {
            addSuperinterface(
              ClassNames.assistedWorkerFactory
                .parameterizedBy(params.workerClassName)
            )

            val constructorParams = params.constructorParams
              .filterNot { it.isDaggerAssisted }

            primaryConstructor(
              FunSpec.constructorBuilder().apply {
                addAnnotation(ClassNames.inject)

                constructorParams.forEach { param ->
                  addParameter(
                    ParameterSpec.builder(param.name, param.providerTypeName)
                      .applyEach(param.qualifiers) { addAnnotation(it) }
                      .build()
                  )
                }
              }
                .build()
            )
            constructorParams.forEach { param ->

              addProperty(
                PropertySpec.builder(param.name, param.providerTypeName)
                  .initializer(param.name)
                  .addModifiers(PRIVATE)
                  .build()
              )
            }

            val delegateFactoryCreateArgs = constructorParams.asArgumentList(
              asProvider = false,
              includeModule = false
            )

            val contextFirst = params.assistedArgs
              .first()
              .typeName == ClassNames.androidContext

            val getArguments = if (contextFirst) {
              "context,·params"
            } else {
              "params,·context"
            }

            addFunction(
              FunSpec.builder("create")
                .addModifiers(OVERRIDE)
                .returns(params.workerClassName)
                .addParameter("context", ClassNames.androidContext)
                .addParameter("params", ClassNames.androidxWorkerParameters)
                .addStatement(
                  "val·delegateFactory·=·%T.create($delegateFactoryCreateArgs)",
                  params.delegateFactoryClassName
                )
                .addStatement("return·delegateFactory.get($getArguments)")
                .build()
            )
          }
          .build()
      )
    }
    return createGeneratedFile(
      codeGenDir, params.packageName, params.assistedFactoryClassNameString, content
    )
  }
}
