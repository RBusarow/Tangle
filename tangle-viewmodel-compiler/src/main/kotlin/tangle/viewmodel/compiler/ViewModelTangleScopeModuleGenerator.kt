package tangle.viewmodel.compiler

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.TypeSpec
import tangle.inject.compiler.*
import java.io.File

class ViewModelTangleScopeModuleGenerator : FileGenerator<TangleScopeModule> {

  override fun generate(
    codeGenDir: File,
    params: TangleScopeModule
  ): GeneratedFile {

    val packageName = params.packageName

    val moduleName = "${ClassNames.tangleScope.simpleName}_VMInject_Module"

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
          .applyEach(params.viewModelParamsList) { viewModelParams ->

            addFunction(
              "multibind${viewModelParams.viewModelClassName.simpleNames.joinToString("_")}"
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

                addFunction(
                  "provide${viewModelParams.viewModelFactoryClassName.simpleNames.joinToString("_")}"
                ) {

                  addParameter("factory", viewModelParams.viewModelFactoryClassName)
                  returns(viewModelParams.viewModelClassName)
                  addAnnotation(ClassNames.provides)
                  addStatement("return·factory.create()")
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
