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

package tangle.inject.compiler

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.jvm.jvmSuppressWildcards
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtFile
import tangle.inject.annotations.TangleScope
import java.io.File
import javax.inject.Provider

@Suppress("unused", "LongMethod")
@AutoService(CodeGenerator::class)
class VMInjectGenerator : CodeGenerator {
  companion object {
    internal const val savedStateHandleParamName = "savedStateHandle"
  }

  override fun generateCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> {
    val generatedProviders = projectFiles
      .classesAndInnerClass(module)
      .mapNotNull { clazz ->
        clazz.vmInjectConstructor(module)?.let { constructor ->

          generateFactoryFile(
            codeGenDir = codeGenDir,
            module = module,
            clazz = clazz,
            constructor = constructor
          )
        }
      }
      .toList()

    if (generatedProviders.isEmpty()) {
      return emptyList()
    }

    val daggerModules = generatedProviders
      .groupBy { it.packageName }
      .flatMap { (packageName, byPackageName) ->
        byPackageName
          .groupBy { it.scopeName }
          .flatMap { (scopeName, byScopeName) ->
            listOf(
              generateDaggerModule(
                codeGenDir = codeGenDir,
                scopeName = scopeName.asClassName(module),
                packageName = packageName,
                generatedFiles = byScopeName
              ),
              generateTangleDaggerModule(
                codeGenDir = codeGenDir,
                packageName = packageName,
                generatedFiles = byScopeName
              )
            )
          }
      }

    return generatedProviders.map { it.generatedFile } + daggerModules
  }

  override fun isApplicable(context: AnvilContext): Boolean = true

  private fun generateFactoryFile(
    codeGenDir: File,
    module: ModuleDescriptor,
    clazz: KtClassOrObject,
    constructor: KtConstructor<*>
  ): GeneratedProvider {
    val packageName = clazz.containingKtFile
      .packageFqName
      .safePackageString(dotSuffix = false)
    val factoryClassNameString = "${clazz.generateClassName()}_Factory"

    val factoryClassName = ClassName(packageName, factoryClassNameString)

    val scopeName = clazz.scope(FqNames.contributesViewModel, module)

    val viewModelConstructorParams = constructor.valueParameters.mapToParameter(module)

    val (injectedParams, savedStateParams) = viewModelConstructorParams
      .partition { !it.isFromSavedState }

    val typeParameters = clazz.typeVariableNames(module)

    val viewModelClassName = clazz.asClassName().let {
      if (typeParameters.isEmpty()) it else it.parameterizedBy(typeParameters)
    }

    val savedStateParam = injectedParams
      .firstOrNull { it.typeName == ClassNames.androidxSavedStateHandle }
      ?: Parameter(
        name = savedStateHandleParamName,
        typeName = ClassNames.androidxSavedStateHandle,
        providerTypeName = ClassNames.androidxSavedStateHandle.wrapInProvider(),
        lazyTypeName = ClassNames.androidxSavedStateHandle.wrapInLazy(),
        isWrappedInProvider = true,
        isWrappedInLazy = false,
        isFromSavedState = false,
        fromSavedStateName = null,
        isAssisted = false,
        assistedIdentifier = "",
        annotationEntries = emptyList()
      )

    val factoryConstructorParams =
      if (savedStateParam !in injectedParams && savedStateParams.isNotEmpty()) {
        injectedParams + savedStateParam
      } else {
        injectedParams
      }

    val content = FileSpec.buildFile(packageName, factoryClassNameString) {
      TypeSpec.classBuilder(factoryClassName)
        .applyEach(typeParameters) { addTypeVariable(it) }
        .addSuperinterface(Provider::class.asClassName().parameterizedBy(viewModelClassName))
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .applyEach(factoryConstructorParams) { parameter ->
              addParameter(parameter.name, parameter.providerTypeName)
            }
            .addAnnotation(ClassNames.inject)
            .build()
        )
        .apply {
          if (savedStateParams.isNotEmpty()) {
            addProperty(
              PropertySpec.builder(savedStateHandleParamName, ClassNames.providerSavedStateHandle)
                .initializer(savedStateHandleParamName)
                .addModifiers(PRIVATE)
                .build()
            )
          }
        }
        .applyEach(injectedParams) { parameter ->

          val qualifierAnnotationSpecs = parameter.annotationEntries
            .filter { it.isQualifier(module) }
            .map { it.toAnnotationSpec(module) }

          addProperty(
            PropertySpec.builder(parameter.name, parameter.providerTypeName)
              .initializer(parameter.name)
              .addModifiers(PRIVATE)
              .applyEach(qualifierAnnotationSpecs) { addAnnotation(it) }
              .build()
          )
        }
        .addFunction(generateGetFunction(viewModelClassName, viewModelConstructorParams))
        .build()
        .let { addType(it) }
    }

    return GeneratedProvider(
      packageName = packageName,
      viewModelClassName = clazz.asClassName(),
      providerImplClassName = factoryClassName,
      scopeName = scopeName,
      generatedFile = createGeneratedFile(
        codeGenDir = codeGenDir,
        packageName = packageName,
        fileName = factoryClassNameString,
        content = content
      )
    )
  }

  private fun generateGetFunction(
    viewModelClassName: TypeName,
    params: List<Parameter>
  ): FunSpec {
    val allArguments = params.asArgumentList(
      asProvider = true,
      includeModule = false
    )

    return FunSpec.builder("get")
      .addModifiers(OVERRIDE)
      .returns(viewModelClassName)
      .addStatement("return·%T($allArguments)", viewModelClassName)
      .build()
  }

  private fun generateCreateFunction(
    factoryClassName: TypeName,
    factoryConstructorParams: List<Parameter>
  ): FunSpec {
    val allArguments = factoryConstructorParams.asArgumentList(
      asProvider = false,
      includeModule = false
    )
    return FunSpec.builder("create")
      .addAnnotation(ClassNames.jvmStatic)
      .applyEach(factoryConstructorParams) { parameter ->
        addParameter(parameter.name, parameter.providerTypeName)
      }
      .returns(factoryClassName)
      .addStatement("return·%T($allArguments)", factoryClassName)
      .build()
  }

  private fun generateNewInstanceFunction(
    viewModelClassName: TypeName,
    savedStateParams: List<Parameter>,
    injectedParams: List<Parameter>,
    viewModelConstructorParams: List<Parameter>
  ): FunSpec {
    val allArguments = viewModelConstructorParams.asArgumentList(
      asProvider = true,
      includeModule = false
    )
    return FunSpec.builder("newInstance")
      .addAnnotation(ClassNames.jvmStatic)
      .apply {
        if (savedStateParams.isNotEmpty()) {
          addParameter(
            savedStateHandleParamName,
            ClassNames.providerSavedStateHandle.jvmSuppressWildcards()
          )
        }
        injectedParams.forEach { parameter ->
          addParameter(parameter.name, parameter.providerTypeName)
        }
      }
      .returns(viewModelClassName)
      .addStatement("return·%T($allArguments)", viewModelClassName)
      .build()
  }

  private data class GeneratedProvider(
    val packageName: String,
    val viewModelClassName: ClassName,
    val providerImplClassName: ClassName,
    val scopeName: FqName,
    val generatedFile: GeneratedFile
  )

  private fun generateDaggerModule(
    codeGenDir: File,
    scopeName: ClassName,
    packageName: String,
    generatedFiles: List<GeneratedProvider>
  ): GeneratedFile {
    val moduleName = "Tangle_${scopeName.simpleNames.joinToString("_")}_Module"

    val content = FileSpec.buildFile(packageName, moduleName) {
      addType(
        TypeSpec
          .objectBuilder(ClassName(packageName, moduleName))
          .addAnnotation(ClassNames.module)
          .addAnnotation(
            AnnotationSpec.builder(ClassNames.contributesTo)
              .addMember("%T::class", scopeName)
              .build()
          )
          .applyEach(generatedFiles) { generatedFile ->

            addFunction(
              FunSpec
                .builder(
                  name = "provide${
                  generatedFile.viewModelClassName.simpleNames.joinToString("_")
                  }Key"
                )
                .returns(ClassNames.javaClassOutVM)
                .addAnnotation(ClassNames.intoSet)
                .addAnnotation(ClassNames.provides)
                .addAnnotation(ClassNames.tangleViewModelProviderMapKeySet)
                .addStatement("return·%T::class.java", generatedFile.viewModelClassName)
                .build()
            )
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

  private fun generateTangleDaggerModule(
    codeGenDir: File,
    packageName: String,
    generatedFiles: List<GeneratedProvider>
  ): GeneratedFile {
    val moduleName = "Tangle_${TangleScope::class.simpleName}_Module"

    val content = FileSpec.buildFile(packageName, moduleName) {
      addType(
        TypeSpec
          .interfaceBuilder(ClassName(packageName, moduleName))
          .addAnnotation(ClassNames.module)
          .addAnnotation(
            AnnotationSpec.builder(ClassNames.contributesTo)
              .addMember("%T::class", ClassNames.tangleScope)
              .build()
          )
          .applyEach(generatedFiles) { generatedFile ->

            addFunction(
              FunSpec
                .builder(
                  "multibind${generatedFile.viewModelClassName.simpleNames.joinToString("_")}"
                )
                .addModifiers(ABSTRACT)
                .addParameter("viewModel", generatedFile.viewModelClassName)
                .returns(ClassNames.androidxViewModel)
                .addAnnotation(ClassNames.binds)
                .addAnnotation(ClassNames.intoMap)
                .addAnnotation(
                  AnnotationSpec.builder(ClassNames.tangleViewModelKey)
                    .addMember("%T::class", generatedFile.viewModelClassName)
                    .build()
                )
                .addAnnotation(ClassNames.tangleViewModelProviderMap)
                .build()
            )
          }
          .addType(
            TypeSpec.companionObjectBuilder()
              .applyEach(generatedFiles) { generatedFile ->

                addFunction(
                  FunSpec
                    .builder(
                      name = "provide${
                      generatedFile.viewModelClassName.simpleNames.joinToString("_")
                      }"
                    )
                    .addParameter("provider", generatedFile.providerImplClassName)
                    .returns(generatedFile.viewModelClassName)
                    .addAnnotation(ClassNames.provides)
                    .addStatement("return·provider.get()")
                    .build()
                )
              }
              .build()
          )
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
