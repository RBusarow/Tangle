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

package tangle.inject.compiler

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.classesAndInnerClasses
import com.squareup.anvil.compiler.internal.generateClassName
import com.squareup.anvil.compiler.internal.hasAnnotation
import com.squareup.anvil.compiler.internal.safePackageString
import com.squareup.kotlinpoet.*
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

@Suppress("unused")
@AutoService(CodeGenerator::class)
class TangleSubcomponentGenerator : CodeGenerator {

  override fun isApplicable(context: AnvilContext): Boolean = true

  override fun generateCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> = projectFiles
    .flatMap { it.classesAndInnerClasses(module) }
    .filter { it.hasAnnotation(FqNames.mergeComponent, module) }
    .map { generateSubcomponent(codeGenDir, it) }

  private fun generateSubcomponent(
    codeGenDir: File,
    clazz: KtClassOrObject
  ): GeneratedFile {
    val packageName = clazz.containingKtFile.packageFqName.safePackageString()
    val className = "${clazz.generateClassName()}TangleSubcomponent"

    val subcomponentClassName = ClassName(packageName, className)

    val content = FileSpec.buildFile(packageName, className) {
      TypeSpec.interfaceBuilder(subcomponentClassName)
        .addSuperinterface(ClassNames.tangleSubcomponent)
        .addAnnotation(
          AnnotationSpec.builder(ClassNames.mergeSubomponent)
            .addMember("%T::class", ClassNames.tangleScope)
            .build()
        )
        .addType(
          TypeSpec.interfaceBuilder("Factory")
            .addSuperinterface(ClassNames.tangleSubcomponentFactory)
            .addAnnotation(ClassNames.subcomponentFactory)
            .addFunction(
              FunSpec.builder("create")
                .returns(subcomponentClassName)
                .addModifiers(KModifier.ABSTRACT, KModifier.OVERRIDE)
                .addParameter(
                  ParameterSpec.builder("savedStateHandle", ClassNames.androidxSavedStateHandle)
                    .addAnnotation(ClassNames.bindsInstance)
                    .build()
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
