package tangle.viewmodel.compiler.components

import com.squareup.anvil.compiler.internal.generateClassName
import com.squareup.anvil.compiler.internal.safePackageString
import com.squareup.anvil.compiler.internal.scope
import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.asClassName

data class MergeComponentParams(
  val module: ModuleDescriptor,
  val packageName: String,
  val scopeFqName: FqName,
  val scopeClassName: ClassName,
  val componentClass: KtClassOrObject,
  val keysSubcomponentClassName: ClassName,
  val keysSubcomponentFactoryClassName: ClassName,
  val mapSubcomponentClassName: ClassName,
  val mapSubcomponentFactoryClassName: ClassName,
  val componentClassName: ClassName,
  val mergeComponentModuleClassName: ClassName,
) {
  companion object {
    fun create(clazz: KtClassOrObject, module: ModuleDescriptor): MergeComponentParams {

      val packageName = clazz.containingKtFile.packageFqName.safePackageString()

      val scopeFqName = clazz.scope(FqNames.mergeComponent, module)
      val scopeClassName = scopeFqName.asClassName(module)

      val keysSubcomponentClassName =
        ClassName(packageName, "${clazz.generateClassName()}TangleViewModelKeysSubcomponent")
      val keysSubcomponentFactoryClassName = keysSubcomponentClassName.nestedClass("Factory")
      val mapSubcomponentClassName =
        ClassName(packageName, "${clazz.generateClassName()}TangleViewModelMapSubcomponent")
      val mapSubcomponentFactoryClassName = mapSubcomponentClassName.nestedClass("Factory")
      val componentClassName =
        ClassName(packageName, "${clazz.generateClassName()}TangleViewModelComponent")
      val mergeComponentModuleClassName =
        ClassName(packageName, "${clazz.generateClassName()}TangleViewModelModule")

      return MergeComponentParams(
        module = module,
        packageName = packageName,
        scopeFqName = scopeFqName,
        scopeClassName = scopeClassName,
        componentClass = clazz,
        keysSubcomponentClassName = keysSubcomponentClassName,
        keysSubcomponentFactoryClassName = keysSubcomponentFactoryClassName,
        mapSubcomponentClassName = mapSubcomponentClassName,
        mapSubcomponentFactoryClassName = mapSubcomponentFactoryClassName,
        componentClassName = componentClassName,
        mergeComponentModuleClassName = mergeComponentModuleClassName
      )
    }
  }
}
