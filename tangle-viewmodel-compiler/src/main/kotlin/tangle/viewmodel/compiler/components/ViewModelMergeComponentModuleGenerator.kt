package tangle.viewmodel.compiler.components

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.TypeSpec
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.addFunction
import tangle.inject.compiler.buildFile
import java.io.File

class ViewModelMergeComponentModuleGenerator : FileGenerator<MergeComponentParams> {
  override fun generate(
    codeGenDir: File,
    params: MergeComponentParams
  ): GeneratedFile {

    val packageName = params.packageName

    val className = params.mergeComponentModuleClassName
    val classNameString = className.simpleName

    val content = FileSpec.buildFile(packageName, classNameString) {
      TypeSpec.interfaceBuilder(className)
        .addAnnotation(ClassNames.module)
        .addAnnotation(
          AnnotationSpec.builder(ClassNames.contributesTo)
            .addMember("%T::class", params.scopeClassName)
            .build()
        )
        .addFunction("bindTangleViewModelProviderMapKeySet") {
          addAnnotation(ClassNames.multibinds)
          addAnnotation(ClassNames.tangleViewModelProviderMapKeySet)
          addModifiers(ABSTRACT)
          returns(ClassNames.viewModelClassSet)
        }
        .addFunction("bindTangleViewModelProviderMap") {
          addAnnotation(ClassNames.multibinds)
          addAnnotation(ClassNames.tangleViewModelProviderMap)
          addModifiers(ABSTRACT)
          returns(ClassNames.viewModelMap)
        }
        .addFunction("bindTangleViewModelFactoryMap") {
          addAnnotation(ClassNames.multibinds)
          addAnnotation(ClassNames.tangleViewModelFactoryMap)
          addModifiers(ABSTRACT)
          returns(ClassNames.anyMap)
        }
        .build()
        .let { addType(it) }
    }

    return createGeneratedFile(codeGenDir, packageName, classNameString, content)
  }
}
