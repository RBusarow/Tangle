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

package tangle.work.compiler.component

import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.requireFqName
import com.squareup.anvil.compiler.internal.safePackageString
import com.squareup.anvil.compiler.internal.scope
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import tangle.inject.compiler.*

data class MergeComponentParams(
  val module: ModuleDescriptor,
  val packageName: String,
  val subcomponentModulePackageName: String,
  val scopeFqName: FqName,
  val scopeClassName: ClassName,
  val componentClass: KtClassOrObject,
  val mapSubcomponentClassName: ClassName,
  val mapSubcomponentFactoryClassName: ClassName,
  val componentClassName: ClassName,
  val mergeComponentModuleClassName: ClassName,
  val mergeComponentWorkerFactoryModuleClassName: ClassName,
  val subcomponentModuleClassName: ClassName,
  val scopeQualifier: AnnotationSpec
) {
  companion object {
    fun create(clazz: KtClassOrObject, module: ModuleDescriptor): MergeComponentParams {

      val packageName = clazz.containingKtFile.packageFqName.safePackageString()

      val scopeFqName = clazz.scope(FqNames.mergeComponent, module)
      val scopeClassName = scopeFqName.asClassName(module)
      val scopeQualifier = AnnotationSpec(ClassNames.named) {
        addMember("%S", "${clazz.requireFqName().asString()}--${scopeClassName.canonicalName}")
      }

      val localScope = scopeClassName.generateSimpleNameString()
      val tangleAppScope = ClassNames.tangleAppScope.generateSimpleNameString()

      val mapSubcomponentClassName = ClassName(
        packageName, "${tangleAppScope}_Tangle_Worker_Map_Subcomponent"
      )

      val mapSubcomponentFactoryClassName = mapSubcomponentClassName.nestedClass("Factory")

      val componentClassName = ClassName(
        packageName, "${localScope}_Tangle_Worker_Component"
      )

      val mergeComponentModuleClassName = ClassName(
        packageName, "${localScope}_Tangle_Worker_Module"
      )

      val mergeComponentWorkerFactoryModuleClassName = ClassName(
        packageName, "${localScope}_Tangle_WorkerFactory_Module"
      )

      val subcomponentModulePackageName = "tangle.worker"

      val subcomponentModuleClassName = ClassName(
        subcomponentModulePackageName,
        "${localScope}_Tangle_Worker_SubcomponentFactory_Module"
      )

      return MergeComponentParams(
        module = module,
        packageName = packageName,
        subcomponentModulePackageName = subcomponentModulePackageName,
        scopeFqName = scopeFqName,
        scopeClassName = scopeClassName,
        componentClass = clazz,
        mapSubcomponentClassName = mapSubcomponentClassName,
        mapSubcomponentFactoryClassName = mapSubcomponentFactoryClassName,
        componentClassName = componentClassName,
        mergeComponentModuleClassName = mergeComponentModuleClassName,
        mergeComponentWorkerFactoryModuleClassName = mergeComponentWorkerFactoryModuleClassName,
        subcomponentModuleClassName = subcomponentModuleClassName,
        scopeQualifier = scopeQualifier
      )
    }
  }
}
