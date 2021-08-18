package tangle.viewmodel.compiler

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeSpec.Builder
import tangle.inject.compiler.*
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
          .applyEach(viewModelParams) { params ->
            addViewModelBinder(params)
          }
          .applyEach(factoryParams) { factory ->
            addViewModelFactoryBinder(factory)
          }
          .apply {
            if (viewModelParams.isNotEmpty()) {
              addType(
                TypeSpec.companionObjectBuilder()
                  .applyEach(viewModelParams) { viewModelParam ->

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
      "multibind_${factoryParams.viewModelClassName.simpleNames.joinToString("_")}"
    ) {

      addModifiers(ABSTRACT)
      addParameter("factory", factoryParams.viewModelFactoryClassName)
      returns( factoryParams.factoryInterfaceClassName)
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
