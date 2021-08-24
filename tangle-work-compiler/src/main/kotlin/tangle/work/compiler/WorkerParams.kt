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

package tangle.work.compiler

import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.requireClassDescriptor
import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import tangle.inject.compiler.ConstructorInjectParameter
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.mapToParameters
import tangle.inject.compiler.require

data class WorkerParams(
  val module: ModuleDescriptor,
  val packageName: String,
  val workerClassName: ClassName,
  val workerClassNameString: String,
  val delegateFactoryClassName: ClassName,
  val assistedFactoryClassNameString: String,
  val assistedFactoryClassName: ClassName,
  val constructorParams: List<ConstructorInjectParameter>,
  val assistedArgs: List<ConstructorInjectParameter>
) {
  companion object {

    val expectedAssistedArgTypes = listOf(
      FqNames.context.asString(), FqNames.workerParameters.asString()
    )

    fun create(
      module: ModuleDescriptor,
      workerClass: KtClassOrObject,
      constructor: KtConstructor<*>
    ): WorkerParams {

      val workerClassClassName = workerClass.asClassName()
      val workerClassNameString = workerClassClassName.simpleName
      val packageName = workerClassClassName.packageName

      val assistedFactoryClassNameString = "${workerClassNameString}_AssistedFactory"
      val assistedFactoryClassName = ClassName(packageName, assistedFactoryClassNameString)

      val constructorParams = constructor.valueParameters
        .mapToParameters(module)

      val assistedArgs = constructorParams.filter { it.isDaggerAssisted }

      val argTypes = assistedArgs
        .map { it.typeName.toString() }
        .sorted()

      require(
        argTypes == expectedAssistedArgTypes,
        { workerClass.requireClassDescriptor(module) }
      ) {

        val actual = assistedArgs.map { "${it.name}: ${it.typeName}" }

        val expected = listOf(
          "context: ${FqNames.context.asString()}", "params: ${FqNames.workerParameters.asString()}"
        )

        """@TangleWorker-annotated classes may only have Context and WorkerParameters as @Assisted-annotated parameters.
        |
        |required assisted constructor parameters
        |${expected.joinToString("\n\t", "\t")}
        |
        |actual assisted constructor parameters
        |${actual.joinToString("\n\t", "\t")}
      """.trimMargin()
      }

      return WorkerParams(
        module = module,
        packageName = packageName,
        workerClassName = workerClassClassName,
        workerClassNameString = workerClassNameString,
        delegateFactoryClassName = ClassName(packageName, workerClassNameString + "_Factory"),
        assistedFactoryClassNameString = assistedFactoryClassNameString,
        assistedFactoryClassName = assistedFactoryClassName,
        constructorParams = constructorParams,
        assistedArgs = assistedArgs
      )
    }
  }
}
