package tangle.viewmodel.compiler

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.addFunction
import tangle.inject.compiler.applyEach
import tangle.inject.compiler.buildFile
import java.io.File

class ViewModelTangleAppScopeModuleGenerator : FileGenerator<TangleScopeModule> {

  override fun generate(
    codeGenDir: File,
    params: TangleScopeModule
  ): GeneratedFile {

    val packageName = params.packageName

    val moduleName = "${ClassNames.tangleAppScope.simpleName}_VMInject_Module"

    val content = FileSpec.buildFile(packageName, moduleName) {
      addType(
        TypeSpec.objectBuilder(ClassName(packageName, moduleName))
          .addAnnotation(ClassNames.module)
          .addAnnotation(
            AnnotationSpec.builder(ClassNames.contributesTo)
              .addMember("%T::class", ClassNames.tangleAppScope)
              .build()
          )
          .applyEach(params.viewModelParamsList) { viewModelParams ->

            addFunction(
              "provide${viewModelParams.viewModelClassName.simpleNames.joinToString("_")}Key"
            ) {
              returns(ClassNames.javaClassOutVM)
              addAnnotation(ClassNames.intoSet)
              addAnnotation(ClassNames.provides)
              addAnnotation(ClassNames.tangleViewModelProviderMapKeySet)
              addStatement("return·%T::class.java", viewModelParams.viewModelClassName)
              build()
            }
          }
          .build()

      )
    }
    return createGeneratedFile(codeGenDir, packageName, moduleName, content)
  }
}
