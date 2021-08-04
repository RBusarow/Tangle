package tangle.viewmodel.compiler.components

import com.squareup.anvil.compiler.api.GeneratedFile
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

    val packageName = params.packageName

    val className = params.subcomponentModuleClassName

    val mapSubcomponentClassName = params.mapSubcomponentClassName

    val keysSubcomponentClassName = params.keysSubcomponentClassName

    val content = FileSpec.buildFile(packageName, className.simpleName) {
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
          "bind${params.mapSubcomponentFactoryClassName.simpleNames.joinToString("_")}IntoSet"
        ) {
          addModifiers(KModifier.ABSTRACT)
          returns(ClassNames.tangleViewModelMapSubcomponentFactory)
          addParameter("factory", params.mapSubcomponentFactoryClassName)
          addAnnotation(ClassNames.binds)
          addAnnotation(ClassNames.intoSet)
          build()
        }
        .addFunction(
          "bind${params.keysSubcomponentFactoryClassName.simpleNames.joinToString("_")}IntoSet"
        ) {
          addModifiers(KModifier.ABSTRACT)
          returns(ClassNames.tangleViewModelKeysSubcomponentFactory)
          addParameter("factory", params.keysSubcomponentFactoryClassName)
          addAnnotation(ClassNames.binds)
          addAnnotation(ClassNames.intoSet)
          build()
        }
        .build()
        .let { addType(it) }
    }

    return createGeneratedFile(codeGenDir, packageName, className.simpleName, content)
  }
}

/*
@ContributesTo(Unit::class)
@Named("kotlin.Unit")
@Module(subcomponents = [UnitTangleViewModelMapSubcomponent::class, UnitTangleViewModelKeysSubcomponent::class])
public interface UnitTangleViewModelSubcomponentModule {
  @Binds
  public
      fun bindUnitTangleViewModelMapSubcomponent_Factory(factory: UnitTangleViewModelMapSubcomponent.Factory):
      TangleViewModelMapSubcomponent.Factory

  @Binds
  public
      fun bindUnitTangleViewModelKeysSubcomponent_Factory(factory: UnitTangleViewModelKeysSubcomponent.Factory):
      TangleViewModelKeysSubcomponent.Factory
}
 */
