package tangle.viewmodel.compiler

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.internal.*
import com.squareup.kotlinpoet.*
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import tangle.inject.compiler.*
import java.io.File

@Suppress("UNUSED")
@AutoService(CodeGenerator::class)
class VMInjectCodeGenerator : CodeGenerator {

  override fun isApplicable(context: AnvilContext): Boolean = true

  override fun generateCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> {

    val viewModelParamsList = projectFiles
      .flatMap { it.classesAndInnerClasses(module) }
      .mapNotNull {
        val constructor = it.vmInjectConstructor(module) ?: return@mapNotNull null
        it to constructor
      }
      .map { (viewModelClass, constructor) ->
        ViewModelParams.create(module, viewModelClass, constructor)
      }

    val viewModels = with(ViewModelFactoryGenerator()) {
      viewModelParamsList
        .map { generate(codeGenDir, it) }
    }

    val moduleParams = viewModelParamsList
      .groupBy { it.packageName }
      .map { (packageName, byPackageName) ->

        TangleScopeModule(
          packageName = packageName,
          viewModelParamsList = byPackageName
        )
      }
    val tangleScopeModules = with(ViewModelTangleScopeModuleGenerator()) {
      moduleParams
        .map { generate(codeGenDir, it) }
    }
    val tangleAppScopeModules = with(ViewModelTangleAppScopeModuleGenerator()) {
      moduleParams
        .map { generate(codeGenDir, it) }
    }

    return viewModels + tangleScopeModules + tangleAppScopeModules
  }
}
