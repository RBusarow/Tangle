/*
 * Copyright (C) 2021 Rick Busarow
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tangle.viewmodel.compiler

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FqNames
import java.io.File

@Suppress("unused")
@AutoService(CodeGenerator::class)
class TangleViewModelSubcomponentModuleGenerator : CodeGenerator {

  override fun isApplicable(context: AnvilContext): Boolean = true

  override fun generateCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> = projectFiles
    .flatMap { it.classesAndInnerClasses(module) }
    .filter { it.hasAnnotation(FqNames.mergeComponent, module) }
    .map { generateModule(codeGenDir, module, it) }

  private fun generateModule(
    codeGenDir: File,
    module: ModuleDescriptor,
    clazz: KtClassOrObject
  ): GeneratedFile {
    val packageName = clazz.containingKtFile.packageFqName.safePackageString()
    val className = "${clazz.generateClassName()}TangleModule"

    val scope = clazz.scope(FqNames.mergeComponent, module)

    val subcomponentClassName = ClassName(
      packageName,
      "${clazz.generateClassName()}TangleSubcomponent"
    )
    val subcomponentFactoryClassName = subcomponentClassName.nestedClass("Factory")

    val content = FileSpec.buildFile(packageName, className) {
      TypeSpec.interfaceBuilder(className)
        .addAnnotation(
          AnnotationSpec.Companion.builder(ClassNames.contributesTo)
            .addMember("%T::class", scope.asClassName(module))
            .build()
        )
        .addAnnotation(
          AnnotationSpec.Companion.builder(ClassNames.module)
            .addMember("subcomponents·=·[%T::class]", subcomponentClassName)
            .build()
        )
        .addFunction(
          FunSpec
            .builder(name = "bind${subcomponentFactoryClassName.simpleNames.joinToString("_")}")
            .addModifiers(ABSTRACT)
            .returns(ClassNames.tangleViewModelSubcomponentFactory)
            .addParameter("factory", subcomponentFactoryClassName)
            .addAnnotation(ClassNames.binds)
            .build()
        )
        .build()
        .let { addType(it) }
    }

    return createGeneratedFile(codeGenDir, packageName, className, content)
  }
}
