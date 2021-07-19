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

package tangle.viewmodel.compiler

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.*
import com.squareup.anvil.compiler.internal.buildFile
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
import tangle.inject.compiler.*
import tangle.inject.compiler.asClassName
import java.io.File
import javax.inject.Provider

@Suppress("unused")
@AutoService(CodeGenerator::class)
class VMInjectGenerator : CodeGenerator {

  override fun generateCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> {
    val generatedProviders = projectFiles
      .classesAndInnerClass(module)
      .mapNotNull { clazz ->
        clazz.vmInjectConstructor(module)?.let { constructor ->

          generateProviderFile(
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
              createDaggerModule(
                codeGenDir = codeGenDir,
                scopeName = scopeName.asClassName(module),
                packageName = packageName,
                generatedFiles = byScopeName
              ),
              createTangleScopeDaggerModule(
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

  private fun generateProviderFile(
    codeGenDir: File,
    module: ModuleDescriptor,
    clazz: KtClassOrObject,
    constructor: KtConstructor<*>
  ): GeneratedProvider {
    val packageName = clazz.containingKtFile
      .packageFqName
      .safePackageString(dotSuffix = false)
    val factoryClassNameString = "${clazz.generateClassName()}_Provider"

    val factoryClassName = ClassName(packageName, factoryClassNameString)

    val scopeName = clazz.scope(FqNames.contributesViewModel, module)

    val viewModelConstructorParams = constructor.valueParameters.mapToParameters(module)

    val (injectedParams, savedStateParams) = viewModelConstructorParams
      .partition { !it.isTangleParam }

    val typeParameters = clazz.typeVariableNames(module)

    val viewModelClassName = clazz.asClassName().let {
      if (typeParameters.isEmpty()) it else it.parameterizedBy(typeParameters)
    }

    val savedStateParam = injectedParams
      .firstOrNull {
        it.typeName == ClassNames.androidxSavedStateHandle ||
          it.typeName == ClassNames.androidxSavedStateHandle.jvmSuppressWildcards()
      }
      ?: createSavedStateParameter(viewModelConstructorParams)

    val providerConstructorParams =
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
            .applyEach(providerConstructorParams) { parameter ->
              addParameter(parameter.name, parameter.providerTypeName)
            }
            .addAnnotation(ClassNames.inject)
            .build()
        )
        .applyEach(providerConstructorParams) { parameter ->

          val qualifierAnnotationSpecs = parameter.qualifiers

          addProperty(
            PropertySpec.builder(parameter.name, parameter.providerTypeName)
              .initializer(parameter.name)
              .addModifiers(PRIVATE)
              .applyEach(qualifierAnnotationSpecs) { addAnnotation(it) }
              .build()
          )
        }
        .addFunction(
          generateGetFunction(
            savedStateParam,
            viewModelClassName,
            viewModelConstructorParams
          )
        )
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

  private fun createSavedStateParameter(
    viewModelConstructorParams: List<ContructorInjectParameter>
  ): ContructorInjectParameter {
    return ContructorInjectParameter(
      name = viewModelConstructorParams.uniqueName("savedStateHandleProvider"),
      typeName = ClassNames.androidxSavedStateHandle,
      providerTypeName = ClassNames.androidxSavedStateHandle.wrapInProvider(),
      lazyTypeName = ClassNames.androidxSavedStateHandle.wrapInLazy(),
      isWrappedInProvider = true,
      isWrappedInLazy = false,
      tangleParamName = null,
      qualifiers = emptyList()
    )
  }

  private fun generateGetFunction(
    savedStateParam: ContructorInjectParameter,
    viewModelClassName: TypeName,
    params: List<ContructorInjectParameter>
  ): FunSpec {
    val allArguments = params.asArgumentList(
      asProvider = true,
      includeModule = false
    )

    val savedState = params.filter { it.isTangleParam }

    return FunSpec.builder("get")
      .addModifiers(OVERRIDE)
      .returns(viewModelClassName)
      .applyEach(savedState) { param ->

        require(!param.tangleParamName.isNullOrEmpty()) {
          "parameter ${param.name} is annotated with ${FqNames.tangleParam.asString()}, " +
            "but does not have a valid key."
        }

        addStatement(
          "val·%L·=·${savedStateParam.name}.get().get<%T>(%S)",
          param.name,
          param.typeName,
          param.tangleParamName!!
        )

        if (!param.typeName.isNullable) {
          beginControlFlow("checkNotNull(%L)·{", param.name)
          addStatement("\"Required parameter with name `%L` \" +", param.tangleParamName!!)
          addStatement("\"and type `%L` is missing from SavedStateHandle.\"", param.typeName)
          endControlFlow()
        }
      }
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

  private fun createDaggerModule(
    codeGenDir: File,
    scopeName: ClassName,
    packageName: String,
    generatedFiles: List<GeneratedProvider>
  ): GeneratedFile {
    val moduleName = "Tangle_${scopeName.simpleNames.joinToString("_")}_VMInject_Module"

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

  private fun createTangleScopeDaggerModule(
    codeGenDir: File,
    packageName: String,
    generatedFiles: List<GeneratedProvider>
  ): GeneratedFile {
    val moduleName = "Tangle_${ClassNames.tangleScope.simpleName}_VMInject_Module"

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
