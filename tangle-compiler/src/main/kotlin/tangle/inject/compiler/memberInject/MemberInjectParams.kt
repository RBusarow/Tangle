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

package tangle.inject.compiler.memberInject

import com.squareup.anvil.compiler.internal.reference.AnnotationReference
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.kotlinpoet.ClassName
import tangle.inject.compiler.generateSimpleNameString
import tangle.inject.compiler.memberInjectedParameters

data class MemberInjectParams(
  val scopeClassName: ClassName,
  val packageName: String,
  val injectedClassName: ClassName,
  val userScopeModuleName: String,
  val userScopeModuleClassName: ClassName,
  val tangleAppScopeModuleClassName: ClassName,
  val injectorName: String,
  val injectorClassName: ClassName,
  val hasInjectedMembers: Boolean
) {
  companion object {
    fun create(
      clazz: ClassReference,
      annotationEntry: AnnotationReference
    ): MemberInjectParams {
      val packageName = clazz.packageFqName.asString()

      val injectedClassName = clazz.asClassName()

      val injectedParams = clazz.memberInjectedParameters()

      val scope = annotationEntry.scope(parameterIndex = 0)
      val scopeClassName = scope.asClassName()

      val injectedClassNameBase = injectedClassName.generateSimpleNameString()

      val injectorName = "Tangle_" + injectedClassNameBase + "Injector"

      val injectorClassName = ClassName(packageName, injectorName)

      val moduleName =
        "${scopeClassName.generateSimpleNameString()}_Tangle_${injectedClassNameBase}Injector_Module"

      val tangleAppScopeModuleClassName =
        "TangleAppScope_${injectorName}_Scope_Module"

      val userScopeModuleClassName = ClassName(packageName, moduleName)

      return MemberInjectParams(
        scopeClassName = scopeClassName,
        packageName = packageName,
        injectedClassName = injectedClassName,
        userScopeModuleName = moduleName,
        userScopeModuleClassName = userScopeModuleClassName,
        tangleAppScopeModuleClassName = ClassName(packageName, tangleAppScopeModuleClassName),
        injectorName = injectorName,
        injectorClassName = injectorClassName,
        hasInjectedMembers = injectedParams.isNotEmpty()
      )
    }
  }
}
