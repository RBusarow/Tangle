package tangle.viewmodel.compiler.components

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.internal.classesAndInnerClasses
import com.squareup.anvil.compiler.internal.hasAnnotation
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import tangle.inject.compiler.FqNames
import java.io.File

@Suppress("UNUSED")
@AutoService(CodeGenerator::class)
class ViewModelMergeComponentCodeGenerator : CodeGenerator {

  val fileGenerators = listOf(
    ViewModelMapSubcomponentGenerator(),
    ViewModelKeysSubcomponentGenerator(),
    ViewModelSubcomponentModuleGenerator(),
    ViewModelMergeComponentModuleGenerator(),
    ViewModelComponentGenerator()
  )

  override fun isApplicable(context: AnvilContext): Boolean = true

  override fun generateCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> = projectFiles
    .flatMap { it.classesAndInnerClasses(module) }
    .filter { it.hasAnnotation(FqNames.mergeComponent, module) }
    .map { MergeComponentParams.create(it, module) }
    // .distinctBy { it.scopeFqName }
    .flatMap { params ->
      fileGenerators.map { generator ->
        generator.generate(codeGenDir, params)
      }
    }
}
