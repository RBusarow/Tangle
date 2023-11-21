/*
 * Copyright (C) 2022 Rick Busarow
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

package tangle.inject.compiler.components.fileGen

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.descriptors.resolveClassByFqName
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.FqName
import tangle.inject.compiler.AnnotationSpec
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.FileGenerator.Companion.CONSTANT_PACKAGE_NAME
import tangle.inject.compiler.addFunction
import tangle.inject.compiler.buildFile
import tangle.inject.compiler.components.MergeComponentParams
import tangle.inject.compiler.generateSimpleNameString
import tangle.inject.compiler.memberInject.fileGen.TangleAppScope_TangleInjector_Scope_ModuleGenerator
import tangle.inject.compiler.tangleScopeToComponentMap
import java.io.File

/**
 * ```
 * @Module
 * @ContributesTo(
 *   TangleAppScope::class,
 *   replaces = []
 * )
 * public object AppScope_to_Component_Module {
 *   @Provides
 *   @TangleScopeToComponentMap
 *   @IntoMap
 *   @ClassKey(AppScope::class)
 *   public fun provideAppScopeComponentClassIntoMap(): Class<*> = AppComponent::class.java
 * }
 * ```
 */
@OptIn(com.squareup.anvil.annotations.ExperimentalAnvilApi::class)
internal object TangleAppScope_UserScope_to_Component_Module_Generator : FileGenerator<MergeComponentParams> {

  override fun generate(
    codeGenDir: File,
    params: MergeComponentParams
  ): GeneratedFile {

    val packageName = CONSTANT_PACKAGE_NAME

    val baseName = params.scopeToComponentModuleClassName.simpleName

    var classNameString = baseName

    val replaced = mutableListOf<ClassName>()

    var count = 0
    var unique: Boolean
    do {
      val moduleFqName = FqName("$packageName.$classNameString")

      // If the Subcomponent already exists in a different module or source set,
      // exclude that version in favor of this new one.
      unique = listOf(params.module)
        .plus(params.module.allDependencyModules)
        .firstNotNullOfOrNull { depMod ->
          depMod.resolveClassByFqName(moduleFqName, NoLookupLocation.FROM_BACKEND)
        }
        ?.let { resolved ->
          classNameString = "$baseName${++count}"
          replaced.add(resolved.asClassName())
          false
        }
        ?: true
    } while (!unique)

    val className = ClassName(packageName, classNameString)

    val content = FileSpec.buildFile(packageName, classNameString) {
      addType(
        TypeSpec.objectBuilder(className)
          .addAnnotation(ClassNames.module)
          .addAnnotation(
            AnnotationSpec(ClassNames.contributesTo) {
              addMember("%T::class", ClassNames.tangleAppScope)
              addMember(
                "replaces·=·[%L]",
                replaced.joinToString { CodeBlock.of("%T::class", it).toString() }
              )
            }
          )
          .addFunction("provide${params.scopeClassName.generateSimpleNameString()}ComponentClassIntoMap") {
            addAnnotation(ClassNames.provides)
            addAnnotation(ClassNames.tangleScopeToComponentMap)
            addAnnotation(ClassNames.intoMap)
              .addAnnotation(
                AnnotationSpec(ClassNames.classKey) {
                  addMember("%T::class", params.scopeClassName)
                }
              )
            returns(ClassNames.javaClassWildcard)
            addStatement("return·%T::class.java", params.originalComponentClassName)
          }
          .build()
      )
    }

    return TangleAppScope_TangleInjector_Scope_ModuleGenerator.createGeneratedFile(
      codeGenDir = codeGenDir,
      packageName = packageName,
      fileName = classNameString,
      content = content
    )
  }
}
