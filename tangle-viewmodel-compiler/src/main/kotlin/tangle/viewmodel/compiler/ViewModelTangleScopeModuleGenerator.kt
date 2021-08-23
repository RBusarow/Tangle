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
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeSpec.Builder
import tangle.inject.compiler.*
import tangle.viewmodel.compiler.params.Factory
import tangle.viewmodel.compiler.params.TangleScopeModule
import tangle.viewmodel.compiler.params.ViewModelParams
import java.io.File

class ViewModelTangleScopeModuleGenerator : FileGenerator<TangleScopeModule> {

  override fun generate(
    codeGenDir: File,
    params: TangleScopeModule
  ): GeneratedFile {

    val packageName = params.packageName

    val moduleName = "${ClassNames.tangleScope.simpleName}_VMInject_Module"

    val viewModelParams = params.viewModelParamsList.filterIsInstance<ViewModelParams>()
    val factoryParams = params.viewModelParamsList.filterIsInstance<Factory>()

    val providedViewModels = viewModelParams.filter { it.factory == null }

    val content = FileSpec.buildFile(packageName, moduleName) {
      addType(
        TypeSpec
          .interfaceBuilder(ClassName(packageName, moduleName))
          .addAnnotation(ClassNames.module)
          .addAnnotation(
            AnnotationSpec.builder(ClassNames.contributesTo)
              .addMember("%T::class", ClassNames.tangleScope)
              .build()
          )
          .applyEach(providedViewModels) { params ->
            addViewModelBinder(params)
          }
          .applyEach(factoryParams) { factory ->
            addViewModelFactoryBinder(factory)
          }
          .apply {
            if (providedViewModels.isNotEmpty()) {
              addType(
                TypeSpec.companionObjectBuilder()
                  .applyEach(providedViewModels) { viewModelParam ->

                    addFunction(
                      "provide_${viewModelParam.viewModelFactoryClassName.simpleNames.joinToString("_")}"
                    ) {

                      addParameter("factory", viewModelParam.viewModelFactoryClassName)
                      returns(viewModelParam.viewModelClassName)
                      addAnnotation(ClassNames.provides)
                      addStatement("return·factory.create()")
                      build()
                    }
                  }
                  .build()
              )
            }
          }
          .build()

      )
    }
    return createGeneratedFile(codeGenDir, packageName, moduleName, content)
  }

  private fun Builder.addViewModelBinder(viewModelParams: ViewModelParams) = apply {
    addFunction(
      "multibind_${viewModelParams.viewModelClassName.simpleNames.joinToString("_")}"
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

  private fun Builder.addViewModelFactoryBinder(factoryParams: Factory) = apply {
    addFunction(
      "multibind_${factoryParams.viewModelFactoryClassName.simpleNames.joinToString("_")}"
    ) {

      addModifiers(ABSTRACT)
      addParameter("factory", factoryParams.viewModelFactoryClassName)
      returns(ClassNames.any)
      addAnnotation(ClassNames.binds)
      addAnnotation(ClassNames.intoMap)
      addAnnotation(
        AnnotationSpec.builder(ClassNames.classKey)
          .addMember("%T::class", factoryParams.factoryInterfaceClassName)
          .build()
      )
      addAnnotation(ClassNames.tangleViewModelFactoryMap)
    }
  }
}
