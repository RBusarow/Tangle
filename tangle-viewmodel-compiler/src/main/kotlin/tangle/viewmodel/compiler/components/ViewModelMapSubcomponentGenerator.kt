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
