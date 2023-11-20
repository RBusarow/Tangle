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

package tangle.inject.compiler.components

import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.requireFqName
import com.squareup.anvil.compiler.internal.safePackageString
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import tangle.inject.compiler.AnnotationSpec
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.generateSimpleNameString

@OptIn(ExperimentalAnvilApi::class)
data class MergeComponentParams(
  val module: ModuleDescriptor,
  val packageName: String,
  val scopeFqName: FqName,
  val scopeClassName: ClassName,
  val componentClass: KtClassOrObject,
  val componentClassName: ClassName,
  val providerComponentClassName: ClassName,
  val memberInjectToScopeMapProviderSubcomponentClassName: ClassName,
  val mergeComponentTangleInjectorModuleClassName: ClassName,
  val scopeToComponentModuleClassName: ClassName,
  val originalComponentClassName: ClassName,
  val scopeQualifier: AnnotationSpec,
  val forSubcomponent: Boolean
) {
  companion object {
    fun create(
      module: ModuleDescriptor,
      scopeFqName: FqName,
      clazz: KtClassOrObject,
      forSubcomponent: Boolean
    ): MergeComponentParams {

      val packageName = clazz.containingKtFile.packageFqName.safePackageString()

      val scopeClassName = scopeFqName.asClassName(module)

      val localScope = scopeClassName.generateSimpleNameString()

      val componentClassName =
        ClassName(packageName, "${localScope}_TangleInjectorComponent")
      val providerComponentClassName =
        ClassName(packageName, "${localScope}_TangleScopeMapProviderComponent")

      val scopeQualifier = AnnotationSpec(ClassNames.named) {
        addMember("%S", "${clazz.requireFqName().asString()}--${scopeClassName.canonicalName}")
      }

      val tangleAppScope = ClassNames.tangleAppScope.generateSimpleNameString()

      val memberInjectToScopeMapProviderClassName = ClassName(
        packageName, "${tangleAppScope}_TangleScopeMapProvider_Subcomponent"
      )

      val mergeComponentTangleInjectorModuleClassName = ClassName(
        packageName, "${localScope}_Tangle_TangleInjector_Module"
      )

      val originalComponentName = clazz.requireFqName()
      val originalComponentClassName = originalComponentName.asClassName(module)

      val scopeToComponentModuleName = "${localScope}_to_Component_Module"
      val scopeToComponentModuleClassName = ClassName(packageName, scopeToComponentModuleName)

      return MergeComponentParams(
        module = module,
        packageName = packageName,
        scopeFqName = scopeFqName,
        scopeClassName = scopeClassName,
        componentClass = clazz,
        componentClassName = componentClassName,
        providerComponentClassName = providerComponentClassName,
        memberInjectToScopeMapProviderSubcomponentClassName = memberInjectToScopeMapProviderClassName,
        mergeComponentTangleInjectorModuleClassName = mergeComponentTangleInjectorModuleClassName,
        scopeToComponentModuleClassName = scopeToComponentModuleClassName,
        originalComponentClassName = originalComponentClassName,
        scopeQualifier = scopeQualifier,
        forSubcomponent = forSubcomponent
      )
    }
  }
}
