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

package tangle.inject.compiler.memberInject.fileGen

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.addFunction
import tangle.inject.compiler.buildFile
import tangle.inject.compiler.memberInject.MemberInjectParams
import tangle.inject.compiler.memberInjector
import tangle.inject.compiler.tangleInjector
import java.io.File

/**
 * ```
 * @ContributesTo(SomeScope::class)
 * public interface Tangle_MyActivityInjector {
 *   public fun inject(target: MyActivity): Unit
 * }
 * ```
 */
@OptIn(com.squareup.anvil.annotations.ExperimentalAnvilApi::class)
internal object TangleInjectorGenerator : FileGenerator<MemberInjectParams> {

  override fun generate(
    codeGenDir: File,
    params: MemberInjectParams
  ): GeneratedFile {

    val packageName = params.packageName

    val content = FileSpec.buildFile(packageName, params.injectorName) {
      addType(
        TypeSpec.classBuilder(params.injectorClassName)
          .primaryConstructor(
            FunSpec.constructorBuilder()
              .addAnnotation(ClassNames.inject)
              .apply {
                if (params.hasInjectedMembers) {
                  addParameter(
                    ParameterSpec.Companion.builder(
                      "injector",
                      ClassNames.memberInjector.parameterizedBy(params.injectedClassName)
                    )
                      .build()
                  )
                }
              }
              .build()
          ).apply {
            if (params.hasInjectedMembers) {
              addProperty(
                PropertySpec.builder(
                  "injector",
                  ClassNames.memberInjector.parameterizedBy(params.injectedClassName)
                )
                  .initializer("injector")
                  .addModifiers(KModifier.INTERNAL)
                  .build()
              )
            }
          }
          .addSuperinterface(ClassNames.tangleInjector.parameterizedBy(params.injectedClassName))
          .addFunction("inject") {
            addParameter("target", params.injectedClassName)
            addModifiers(OVERRIDE)
              .apply {
                if (params.hasInjectedMembers) {
                  addStatement("injector.injectMembers(target)")
                }
              }
          }
          .build()
      )
    }

    return createGeneratedFile(
      codeGenDir = codeGenDir,
      packageName = packageName,
      fileName = params.injectorName,
      content = content
    )
  }
}
