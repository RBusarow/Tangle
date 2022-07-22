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

package tangle.viewmodel.compiler.components

import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.anvil.compiler.internal.safePackageString
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import tangle.inject.compiler.AnnotationSpec
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.find
import tangle.inject.compiler.generateSimpleNameString
import tangle.viewmodel.compiler.tangleViewModelScope

data class MergeComponentParams(
  val module: ModuleDescriptor,
  val packageName: String,
  val subcomponentModulePackageName: String,
  val scopeFqName: FqName,
  val scopeClassName: ClassName,
  val componentClass: ClassReference,
  val keysSubcomponentClassName: ClassName,
  val keysSubcomponentFactoryClassName: ClassName,
  val mapSubcomponentClassName: ClassName,
  val mapSubcomponentFactoryClassName: ClassName,
  val componentClassName: ClassName,
  val mergeComponentModuleClassName: ClassName,
  val subcomponentModuleClassName: ClassName,
  val scopeQualifier: AnnotationSpec
) {
  companion object {
    fun create(clazz: ClassReference, module: ModuleDescriptor): MergeComponentParams {
      val packageName = clazz.packageFqName.safePackageString()

      val scopeClass = clazz.annotations.find(FqNames.mergeComponent)!!.scope()
      val scopeFqName = scopeClass.fqName
      val scopeClassName = scopeClass.asClassName()

      val scopeQualifier = AnnotationSpec(ClassNames.named) {
        addMember("%S", "${clazz.fqName.asString()}--${scopeClassName.canonicalName}")
      }

      val localScope = scopeClassName.generateSimpleNameString()
      val tangleViewModelScope = ClassNames.tangleViewModelScope.generateSimpleNameString()
      val tangleAppScope = ClassNames.tangleAppScope.generateSimpleNameString()

      val keysSubcomponentClassName =
        ClassName(packageName, "${tangleAppScope}_Tangle_ViewModel_Keys_Subcomponent")

      val keysSubcomponentFactoryClassName = keysSubcomponentClassName.nestedClass("Factory")

      val mapSubcomponentClassName =
        ClassName(packageName, "${tangleViewModelScope}_Tangle_ViewModel_Map_Subcomponent")

      val mapSubcomponentFactoryClassName = mapSubcomponentClassName.nestedClass("Factory")

      val componentClassName =
        ClassName(packageName, "${localScope}_Tangle_ViewModel_Component")

      val mergeComponentModuleClassName =
        ClassName(packageName, "${localScope}_Tangle_ViewModel_Module")

      val subcomponentModulePackageName = "tangle.viewmodel"

      val subcomponentModuleClassName =
        ClassName(
          subcomponentModulePackageName,
          "${localScope}_Tangle_ViewModel_SubcomponentFactory_Module"
        )

      return MergeComponentParams(
        module = module,
        packageName = packageName,
        subcomponentModulePackageName = subcomponentModulePackageName,
        scopeFqName = scopeFqName,
        scopeClassName = scopeClassName,
        componentClass = clazz,
        keysSubcomponentClassName = keysSubcomponentClassName,
        keysSubcomponentFactoryClassName = keysSubcomponentFactoryClassName,
        mapSubcomponentClassName = mapSubcomponentClassName,
        mapSubcomponentFactoryClassName = mapSubcomponentFactoryClassName,
        componentClassName = componentClassName,
        mergeComponentModuleClassName = mergeComponentModuleClassName,
        subcomponentModuleClassName = subcomponentModuleClassName,
        scopeQualifier = scopeQualifier
      )
    }
  }
}
