package tangle.viewmodel.compiler.components

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
    fun create(clazz: KtClassOrObject, module: ModuleDescriptor): MergeComponentParams {

      val packageName = clazz.containingKtFile.packageFqName.safePackageString()

      val scopeFqName = clazz.scope(FqNames.mergeComponent, module)
      val scopeClassName = scopeFqName.asClassName(module)
      val scopeQualifier = AnnotationSpec(ClassNames.named) {
        addMember("%S", "${clazz.requireFqName().asString()}--${scopeClassName.canonicalName}")
      }

      val base = scopeClassName.generateSimpleNameString()

      val keysSubcomponentClassName =
        ClassName(packageName, "${base}_Tangle_ViewModel_Keys_Subcomponent")

      val keysSubcomponentFactoryClassName = keysSubcomponentClassName.nestedClass("Factory")

      val mapSubcomponentClassName =
        ClassName(packageName, "${base}_Tangle_ViewModel_Map_Subcomponent")

      val mapSubcomponentFactoryClassName = mapSubcomponentClassName.nestedClass("Factory")

      val componentClassName =
        ClassName(packageName, "${base}_Tangle_ViewModel_Component")

      val mergeComponentModuleClassName =
        ClassName(packageName, "${base}_Tangle_ViewModel_Module")

      val subcomponentModulePackageName = "tangle.viewmodel"

      val subcomponentModuleClassName =
        ClassName(
          subcomponentModulePackageName,
          "${base}_Tangle_ViewModel_SubcomponentFactory_Module"
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
