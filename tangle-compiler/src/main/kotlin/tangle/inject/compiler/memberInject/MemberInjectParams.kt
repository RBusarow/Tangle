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

import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.requireClassDescriptor
import com.squareup.anvil.compiler.internal.requireFqName
import com.squareup.anvil.compiler.internal.scope
import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClassOrObject
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
      module: ModuleDescriptor,
      clazz: KtClassOrObject,
      annotationEntry: KtAnnotationEntry
    ): MemberInjectParams {

      val packageName = clazz.containingKtFile.packageFqName.asString()

      val injectedClassName = clazz.requireFqName().asClassName(module)

      val injectedParams = clazz.requireClassDescriptor(module)
        .memberInjectedParameters(module)

      val scope = annotationEntry.scope(module)
      val scopeClassName = scope.asClassName(module)

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
