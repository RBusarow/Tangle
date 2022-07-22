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
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.addContributesTo
import tangle.inject.compiler.addFunction
import tangle.inject.compiler.buildFile
import tangle.inject.compiler.memberInject.MemberInjectParams
import tangle.inject.compiler.tangleInjector
import java.io.File

/**
 * Creates a binding and multibinding for TangleInjector implementations
 *
 * ```
 * @Module
 * @ContributesTo(AppScope::class)
 * public interface Tangle_AppScope_TangleInjector_Module {
 *   @Binds
 *   public fun bindReceiverTangleInjector(injector: ReceiverTangleInjector): TangleInjector<Receiver>
 *
 *   @Binds
 *   @IntoMap
 *   @ClassKey(AppScope::class)
 *   public fun multibindReceiverTangleInjectorIntoMap(injector: ReceiverTangleInjector):
 *     TangleInjector<*>
 * }
 * ```
 */
internal object TangleInjector_ModuleGenerator : FileGenerator<MemberInjectParams> {

  override fun generate(
    codeGenDir: File,
    params: MemberInjectParams
  ): GeneratedFile {

    val scopeClassName = params.scopeClassName
    val packageName = params.packageName

    val moduleName = params.userScopeModuleName

    val content = FileSpec.buildFile(packageName, moduleName) {
      addType(
        TypeSpec.interfaceBuilder(params.userScopeModuleClassName)
          .addAnnotation(ClassNames.module)
          .addContributesTo(scopeClassName)
          .addFunction("bind${params.injectorName}") {
            addAnnotation(ClassNames.binds)
            addModifiers(KModifier.ABSTRACT)
            addParameter("injector", params.injectorClassName)
            returns(ClassNames.tangleInjector.parameterizedBy(params.injectedClassName))
          }
          .addFunction("multibind${params.injectorName}IntoMap") {
            addAnnotation(ClassNames.binds)
            addAnnotation(ClassNames.intoMap)
              .addAnnotation(
                AnnotationSpec.builder(ClassNames.classKey)
                  .addMember("%T::class", params.injectedClassName)
                  .build()
              )
            addModifiers(KModifier.ABSTRACT)
            addParameter("injector", params.injectorClassName)
            returns(ClassNames.tangleInjector.parameterizedBy(TypeVariableName("*")))
          }
          .build()
      )
    }

    return createGeneratedFile(
      codeGenDir = codeGenDir,
      packageName = packageName,
      fileName = moduleName,
      content = content
    )
  }
}
