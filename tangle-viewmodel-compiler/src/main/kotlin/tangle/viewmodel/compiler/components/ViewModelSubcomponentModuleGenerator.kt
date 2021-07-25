package tangle.viewmodel.compiler.components

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.internal.generateClassName
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.addFunction
import tangle.inject.compiler.buildFile
import java.io.File

class ViewModelSubcomponentModuleGenerator : FileGenerator<MergeComponentParams> {

  override fun generate(
    codeGenDir: File,
    params: MergeComponentParams
  ): GeneratedFile {

    val clazz = params.componentClass
    val packageName = params.packageName

    val className = "${clazz.generateClassName()}TangleViewModelSubcomponentModule"

    val mapSubcomponentClassName = params.mapSubcomponentClassName

    val keysSubcomponentClassName = params.keysSubcomponentClassName

    val content = FileSpec.buildFile(packageName, className) {
      TypeSpec.interfaceBuilder(className)
        .addAnnotation(
          AnnotationSpec.builder(ClassNames.contributesTo)
            .addMember("%T::class", params.scopeClassName)
            .build()
        )
        .addAnnotation(
          AnnotationSpec.builder(ClassNames.module)
            .addMember(
              "subcomponents·=·[%T::class,·%T::class]",
              mapSubcomponentClassName,
              keysSubcomponentClassName
            )
            .build()
        )
        .addFunction(
          "bind${params.mapSubcomponentFactoryClassName.simpleNames.joinToString("_")}"
        ) {
          addModifiers(KModifier.ABSTRACT)
          returns(ClassNames.tangleViewModelMapSubcomponentFactory)
          addParameter("factory", params.mapSubcomponentFactoryClassName)
          addAnnotation(ClassNames.binds)
          build()
        }
        .addFunction(
          "bind${params.keysSubcomponentFactoryClassName.simpleNames.joinToString("_")}"
        ) {
          addModifiers(KModifier.ABSTRACT)
          returns(ClassNames.tangleViewModelKeysSubcomponentFactory)
          addParameter("factory", params.keysSubcomponentFactoryClassName)
          addAnnotation(ClassNames.binds)
          build()
        }
        .build()
        .let { addType(it) }
    }

    return createGeneratedFile(codeGenDir, packageName, className, content)
  }
}
