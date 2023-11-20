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
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.descriptors.resolveClassByFqName
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.FqName
import tangle.inject.compiler.AnnotationSpec
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.buildFile
import tangle.inject.compiler.components.MergeComponentParams
import tangle.inject.compiler.tangleScopeMapProviderComponent
import java.io.File

/**
 * ```
 * @ContributesTo(
 *   AppScope::class,
 *   replaces = []
 * )
 * public interface AppScope_TangleInjectorComponent : TangleScopeMapProviderComponent {
 *   public override val tangleScopeMapProvider:
 *     TangleAppScope_TangleScopeMapProvider_Subcomponent
 * }
 * ```
 */
@OptIn(com.squareup.anvil.annotations.ExperimentalAnvilApi::class)
internal object UserScope_TangleScopeMapProviderComponent_Generator :
  FileGenerator<MergeComponentParams> {
  override fun generate(
    codeGenDir: File,
    params: MergeComponentParams
  ): GeneratedFile? {

    if (params.forSubcomponent) {
      // This module binds the subcomponent factory for the main component's scope.
      // It doesn't need to be re-bound for subcomponents.
      return null
    }

    val packageName = "tangle.inject"

    val baseName = params.providerComponentClassName.simpleName

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
      TypeSpec.interfaceBuilder(className)
        .addSuperinterface(ClassNames.tangleScopeMapProviderComponent)
        .addAnnotation(
          AnnotationSpec(ClassNames.contributesTo) {
            addMember("%T::class", params.scopeClassName)
            addMember(
              "replaces·=·[%L]",
              replaced.joinToString { CodeBlock.of("%T::class", it).toString() }
            )
          }
        )
        .addProperty(
          PropertySpec.builder(
            "scopeMapProvider",
            params.memberInjectToScopeMapProviderSubcomponentClassName
          )
            .addModifiers(KModifier.OVERRIDE)
            .build()
        )
        .build()
        .let { addType(it) }
    }

    return createGeneratedFile(codeGenDir, packageName, classNameString, content)
  }
}
