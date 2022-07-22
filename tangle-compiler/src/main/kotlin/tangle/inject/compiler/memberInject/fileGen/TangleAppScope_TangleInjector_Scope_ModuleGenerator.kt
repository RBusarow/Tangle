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
import com.squareup.kotlinpoet.TypeSpec
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.addContributesTo
import tangle.inject.compiler.addFunction
import tangle.inject.compiler.buildFile
import tangle.inject.compiler.generateSimpleNameString
import tangle.inject.compiler.memberInject.MemberInjectParams
import tangle.inject.compiler.tangleScopeMap
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
internal object TangleAppScope_TangleInjector_Scope_ModuleGenerator : FileGenerator<MemberInjectParams> {

  override fun generate(
    codeGenDir: File,
    params: MemberInjectParams
  ): GeneratedFile {

    val packageName = params.packageName

    val moduleName = params.tangleAppScopeModuleClassName.simpleName

    val content = FileSpec.buildFile(packageName, moduleName) {
      addType(
        TypeSpec.objectBuilder(params.tangleAppScopeModuleClassName)
          .addAnnotation(ClassNames.module)
          .addContributesTo(ClassNames.tangleAppScope)
          .addFunction("provide${params.injectedClassName.generateSimpleNameString()}Scope") {
            addAnnotation(ClassNames.provides)
            addAnnotation(ClassNames.tangleScopeMap)
            addAnnotation(ClassNames.intoMap)
              .addAnnotation(
                AnnotationSpec.builder(ClassNames.classKey)
                  .addMember("%T::class", params.injectedClassName)
                  .build()
              )
            returns(ClassNames.javaClassWildcard)
            addStatement("return·%T::class.java", params.scopeClassName)
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
