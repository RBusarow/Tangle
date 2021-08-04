package tangle.viewmodel.compiler.components

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import tangle.inject.compiler.AnnotationSpec
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.buildFile
import java.io.File

class ViewModelComponentGenerator : FileGenerator<MergeComponentParams> {
  override fun generate(
    codeGenDir: File,
    params: MergeComponentParams
  ): GeneratedFile {

    val packageName = params.packageName

    val className = params.componentClassName
    val classNameString = className.simpleName

    val content = FileSpec.buildFile(packageName, classNameString) {
      TypeSpec.interfaceBuilder(className)
        .addSuperinterface(ClassNames.tangleViewModelComponent)
        .addAnnotation(
          AnnotationSpec(ClassNames.contributesTo) {
            addMember("%T::class", params.scopeClassName)
          }
        )
        .build()
        .let { addType(it) }
    }

    return createGeneratedFile(codeGenDir, packageName, classNameString, content)
  }
}
