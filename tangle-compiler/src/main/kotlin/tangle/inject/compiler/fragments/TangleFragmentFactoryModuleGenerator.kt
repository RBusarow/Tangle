package tangle.inject.compiler.fragments

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.classesAndInnerClasses
import com.squareup.anvil.compiler.internal.generateClassName
import com.squareup.anvil.compiler.internal.hasAnnotation
import com.squareup.anvil.compiler.internal.safePackageString
import com.squareup.anvil.compiler.internal.scope
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.asClassName
import tangle.inject.compiler.buildFile
import java.io.File

@Suppress("unused")
@AutoService(CodeGenerator::class)
class TangleFragmentFactoryModuleGenerator : CodeGenerator {

  override fun isApplicable(context: AnvilContext): Boolean = true

  override fun generateCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> = projectFiles
    .flatMap { it.classesAndInnerClasses(module) }
    .filter { it.hasAnnotation(FqNames.mergeComponent, module) }
    .map { generateComponent(codeGenDir, module, it) }

  private fun generateComponent(
    codeGenDir: File,
    module: ModuleDescriptor,
    clazz: KtClassOrObject
  ): GeneratedFile {
    val packageName = clazz.containingKtFile.packageFqName.safePackageString()
    val className = "${clazz.generateClassName()}Tangle_FragmentFactory_Module"

    val scope = clazz.scope(FqNames.mergeComponent, module)

    val componentClassName = ClassName(packageName, className)

    val content = FileSpec.buildFile(packageName, className) {
      TypeSpec.interfaceBuilder(componentClassName)
        .addAnnotation(ClassNames.module)
        .addAnnotation(
          AnnotationSpec.builder(ClassNames.contributesTo)
            .addMember("%T::class", scope.asClassName(module))
            .build()
        )
        .addFunction(
          FunSpec.builder("bind${ClassNames.tangleFragmentFactory.simpleName}")
            .addAnnotation(ClassNames.binds)
            .addModifiers(ABSTRACT)
            .addParameter("fragmentFactory", ClassNames.tangleFragmentFactory)
            .returns(ClassNames.androidxFragmentFactory)
            .build()
        )
        .addFunction(
          FunSpec.builder("bindProviderMap")
            .addAnnotation(ClassNames.multibinds)
            .addModifiers(ABSTRACT)
            .returns(ClassNames.fragmentMap)
            .build()
        )
        .addFunction(
          FunSpec.builder("bindTangleProviderMap")
            .addAnnotation(ClassNames.multibinds)
            .addAnnotation(ClassNames.tangleFragmentProviderMap)
            .addModifiers(ABSTRACT)
            .returns(ClassNames.fragmentMap)
            .build()
        )
        .addType(
          TypeSpec.companionObjectBuilder()
            .addFunction(
              FunSpec.builder("provide${ClassNames.tangleFragmentFactory.simpleName}")
                .addAnnotation(ClassNames.provides)
                .addParameter("providerMap", ClassNames.fragmentProviderMap)
                .addParameter(
                  ParameterSpec.builder("tangleProviderMap", ClassNames.fragmentProviderMap)
                    .addAnnotation(ClassNames.tangleFragmentProviderMap)
                    .build()
                )
                .returns(ClassNames.tangleFragmentFactory)
                .addStatement(
                  "return·%T(providerMap,·tangleProviderMap)",
                  ClassNames.tangleFragmentFactory
                )
                .build()
            )
            .build()
        )
        .build()
        .let { addType(it) }
    }

    return createGeneratedFile(codeGenDir, packageName, className, content)
  }
}
