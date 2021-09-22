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

package tangle.viewmodel.compiler

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.internal.capitalize
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import tangle.inject.compiler.*
import java.io.File

class ViewModelTangleScopeModuleGenerator : FileGenerator<TangleScopeModule> {

  override fun generate(
    codeGenDir: File,
    params: TangleScopeModule
  ): GeneratedFile {

    val packageName = params.packageName

    val moduleName = "${ClassNames.tangleViewModelScope.simpleName}_VMInject_Module"

    val content = FileSpec.buildFile(packageName, moduleName) {
      addType(
        TypeSpec
          .interfaceBuilder(ClassName(packageName, moduleName))
          .addAnnotation(ClassNames.module)
          .addAnnotation(
            AnnotationSpec.builder(ClassNames.contributesTo)
              .addMember("%T::class", ClassNames.tangleViewModelScope)
              .build()
          )
          .applyEach(params.viewModelParamsList) { viewModelParams ->

            addFunction(
              "multibind_${viewModelParams.viewModelClassName.generateSimpleNameString()}"
            ) {

              addModifiers(ABSTRACT)
              addParameter("viewModel", viewModelParams.viewModelClassName)
              returns(ClassNames.androidxViewModel)
              addAnnotation(ClassNames.binds)
              addAnnotation(ClassNames.intoMap)
              addAnnotation(
                AnnotationSpec.builder(ClassNames.classKey)
                  .addMember("%T::class", viewModelParams.viewModelClassName)
                  .build()
              )
              addAnnotation(ClassNames.tangleViewModelProviderMap)
            }
          }
          .addType(
            TypeSpec.companionObjectBuilder()
              .applyEach(params.viewModelParamsList) { viewModelParams ->

                val factoryConstructorParams =
                  viewModelParams.viewModelFactoryConstructorParams + viewModelParams.memberInjectedParams

                addFunction(
                  "provide_${viewModelParams.viewModelClassSimpleName}"
                ) {

                  applyEach(factoryConstructorParams) { parameter ->
                    addParameter(parameter.name, parameter.providerTypeName)
                  }

                  returns(viewModelParams.viewModelClassName)
                  addAnnotation(ClassNames.provides)

                  val constructorArguments =
                    viewModelParams.viewModelConstructorParams.asArgumentList(
                      asProvider = true,
                      includeModule = false
                    )

                  val tangleParams = viewModelParams.viewModelConstructorParams
                    .filter { it.isTangleParam }

                  if (viewModelParams.savedStateParam != null && tangleParams.isNotEmpty()) {
                    tangleParams.forEach { param ->

                      val tangleParamName = param.tangleParamName

                      require(
                        !tangleParamName.isNullOrEmpty(),
                        viewModelParams.viewModelClassDescriptor
                      ) {
                        "parameter ${param.name} is annotated with ${FqNames.tangleParam.asString()}, " +
                          "but does not have a valid key."
                      }

                      addStatement(
                        "val·%L·=·${viewModelParams.savedStateParam.name}.get().get<%T>(%S)",
                        param.name,
                        param.typeName,
                        tangleParamName
                      )
                      if (!param.typeName.isNullable) {
                        beginControlFlow("checkNotNull(%L)·{", param.name)
                        addStatement(
                          "%S",
                          buildCodeBlock {
                            add(
                              "Required parameter with name `%L` and type `%L` is missing from SavedStateHandle.",
                              tangleParamName,
                              param.typeName
                            )
                          }
                        )
                        endControlFlow()
                      }
                    }
                  }

                  if (viewModelParams.memberInjectedParams.isEmpty()) {
                    addStatement(
                      "return·%T($constructorArguments)",
                      viewModelParams.viewModelClassName
                    )
                  } else {

                    addStatement(
                      "val·instance·=·%T($constructorArguments)",
                      viewModelParams.viewModelClassName
                    )

                    val memberInjectParameters = viewModelParams.memberInjectedParams

                    memberInjectParameters.forEach { parameter ->

                      val propertyName = parameter.name
                      val functionName = "inject${propertyName.capitalize()}"

                      val param = when {
                        parameter.isWrappedInProvider -> parameter.name
                        parameter.isWrappedInLazy -> "${FqNames.daggerDoubleCheck}.lazy(${parameter.name})"
                        else -> parameter.name // + ".get()"
                      }

                      addStatement(
                        "%T.$functionName(instance, $param)",
                        parameter.memberInjectorClass
                      )
                    }
                    addStatement("return·instance")
                  }

                  build()
                }
              }
              .build()
          )
          .build()

      )
    }
    return createGeneratedFile(codeGenDir, packageName, moduleName, content)
  }
}
