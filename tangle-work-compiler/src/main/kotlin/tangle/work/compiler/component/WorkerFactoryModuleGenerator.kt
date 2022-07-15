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

package tangle.work.compiler.component

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.descriptors.resolveClassByFqName
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.FqName
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.addFunction
import tangle.inject.compiler.buildFile
import tangle.inject.compiler.generateSimpleNameString
import tangle.work.compiler.tangleWorkerFactory
import java.io.File

object WorkerFactoryModuleGenerator : FileGenerator<MergeComponentParams> {

  override fun generate(
    codeGenDir: File,
    params: MergeComponentParams
  ): GeneratedFile? {

    val moduleFqName =
      FqName(
        "${params.subcomponentModulePackageName}.${params.mergeComponentWorkerFactoryModuleClassName.simpleName}"
      )

    // If the (Dagger) Module for this scope already exists in a different Gradle module,
    // it can't be created again here without creating a duplicate binding
    // for the TangleWorkerFactory.
    val alreadyCreated = listOf(params.module)
      .plus(params.module.allDependencyModules)
      .any { depMod ->
        depMod.resolveClassByFqName(moduleFqName, NoLookupLocation.FROM_BACKEND) != null
      }

    if (alreadyCreated) {
      return null
    }

    val packageName = params.subcomponentModulePackageName

    val className = params.mergeComponentWorkerFactoryModuleClassName

    val content = FileSpec.buildFile(packageName, className.simpleName) {
      TypeSpec.objectBuilder(className)
        .addAnnotation(
          AnnotationSpec.builder(ClassNames.contributesTo)
            .addMember("%T::class", params.scopeClassName)
            .build()
        )
        .addAnnotation(ClassNames.module)
        .addFunction(
          "provide_${ClassNames.tangleWorkerFactory.generateSimpleNameString()}"
        ) {
          returns(ClassNames.tangleWorkerFactory)
          addAnnotation(ClassNames.provides)
          addStatement("return·%T()", ClassNames.tangleWorkerFactory)
          build()
        }
        .build()
        .let { addType(it) }
    }

    return createGeneratedFile(codeGenDir, packageName, className.simpleName, content)
  }
}
