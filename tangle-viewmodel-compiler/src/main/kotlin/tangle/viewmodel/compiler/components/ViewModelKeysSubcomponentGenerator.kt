package tangle.viewmodel.compiler.components

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.kotlinpoet.*
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.addFunction
import tangle.inject.compiler.buildFile
import tangle.viewmodel.compiler.*
import java.io.File
import tangle.viewmodel.compiler.*

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
