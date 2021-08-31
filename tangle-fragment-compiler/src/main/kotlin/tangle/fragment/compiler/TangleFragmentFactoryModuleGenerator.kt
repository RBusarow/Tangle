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

package tangle.fragment.compiler

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.classesAndInnerClasses
import com.squareup.anvil.compiler.internal.hasAnnotation
import com.squareup.anvil.compiler.internal.scope
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.resolveClassByFqName
import org.jetbrains.kotlin.incremental.components.NoLookupLocation.FROM_BACKEND
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import tangle.inject.compiler.*
import java.io.File

@Suppress("unused")
@AutoService(CodeGenerator::class)
class TangleFragmentFactoryModuleGenerator : TangleCodeGenerator() {

  override fun generateTangleCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> = projectFiles
    .flatMap { it.classesAndInnerClasses(module) }
    .filter { it.hasAnnotation(FqNames.mergeComponent, module) }
    // fast path for excluding duplicate binding modules if they're in the same source
    .distinctBy { it.scope(FqNames.mergeComponent, module) }
    .mapNotNull { generateComponent(codeGenDir, module, it) }

  private fun generateComponent(
    codeGenDir: File,
    module: ModuleDescriptor,
    clazz: KtClassOrObject
  ): GeneratedFile? {
    // Every single instance of this generated Module will have the same FqName,
    // except for the scope name which is prepended to the interface name.
    // This is crucial because we can only ever have one TangleFragmentFactory binding declaration
    // per scope, even if they're in different Gradle modules.  We use this known constant FqName
    // to look for instances of the module within the classpath,
    // and skip generation if one already exists.
    // Modules for a different scope are okay.
    val packageName = "tangle.fragment"

    val scopeFqName = clazz.scope(FqNames.mergeComponent, module)
    val scopeClassName = scopeFqName.asClassName(module)
    val scopeClassNameString = scopeClassName.generateSimpleNameString()

    val moduleClassNameString = "${scopeClassNameString}_Tangle_FragmentFactory_Module"
    val moduleFqName = FqName("$packageName.$moduleClassNameString")
    val moduleClassName = ClassName(packageName, moduleClassNameString)

    // If the (Dagger) Module for this scope already exists in a different Gradle module,
    // it can't be created again here without creating a duplicate binding
    // for the TangleFragmentFactory.
    val alreadyCreated = listOf(module)
      .plus(module.allDependencyModules)
      .any { depMod ->
        depMod.resolveClassByFqName(moduleFqName, FROM_BACKEND) != null
      }

    if (alreadyCreated) {
      return null
    }

    val content = FileSpec.buildFile(packageName, moduleClassNameString) {
      TypeSpec.interfaceBuilder(moduleClassName)
        .addAnnotation(ClassNames.module)
        .addAnnotation(
          AnnotationSpec.builder(ClassNames.contributesTo)
            .addMember("%T::class", scopeFqName.asClassName(module))
            .build()
        )
        .addFunction("bindProviderMap") {
          addAnnotation(ClassNames.multibinds)
          addModifiers(ABSTRACT)
          returns(ClassNames.fragmentMap)
        }
        .addFunction("bindTangleProviderMap") {
          addAnnotation(ClassNames.multibinds)
          addAnnotation(ClassNames.tangleFragmentProviderMap)
          addModifiers(ABSTRACT)
          returns(ClassNames.fragmentMap)
        }
        .addType(
          TypeSpec.companionObjectBuilder()
            .addFunction("provide_${ClassNames.tangleFragmentFactory.simpleName}") {
              addAnnotation(ClassNames.provides)
              addParameter("providerMap", ClassNames.fragmentProviderMap)
              addParameter(
                ParameterSpec.builder("tangleProviderMap", ClassNames.fragmentProviderMap)
                  .addAnnotation(ClassNames.tangleFragmentProviderMap)
                  .build()
              )
              returns(ClassNames.tangleFragmentFactory)
              addStatement(
                "return·%T(providerMap,·tangleProviderMap)",
                ClassNames.tangleFragmentFactory
              )
            }
            .build()
        )
        .build()
        .let { addType(it) }
    }

    return createGeneratedFile(codeGenDir, packageName, moduleClassNameString, content)
  }
}
