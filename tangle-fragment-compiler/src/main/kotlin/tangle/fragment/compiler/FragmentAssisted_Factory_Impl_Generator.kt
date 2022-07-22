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

package tangle.fragment.compiler

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import tangle.fragment.compiler.FragmentInjectParams.Factory
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.MemberNames
import tangle.inject.compiler.applyEach
import tangle.inject.compiler.buildFile
import tangle.inject.compiler.uniqueName
import tangle.inject.compiler.wrapInProvider
import java.io.File

/**
 * Similar to an Assisted factory. This is generated from the `@FragmentInjectFactory` annotation.
 *
 * It just delegates to the generated `*_Factory`, except it constructs a Bundle and sets it in the
 * Fragment instance before returning.
 *
 * ```
 * public class MyFragment_Factory_Impl(
 *   public val delegateFactory: MyFragment_Factory
 * ) : MyFragment.Factory {
 *     public override fun create(name: String): MyFragment {
 *         val bundle = bundleOf(
 *           "name" to name
 *         )
 *     return delegateFactory.get().apply {
 *       this@apply.arguments = bundle
 *     }
 *   }
 *
 *   public companion object {
 *     @JvmStatic
 *     public fun create(delegateFactory: MyFragment_Factory): Provider<MyFragment.Factory> =
 *       InstanceFactory.create(MyFragment_Factory_Impl(delegateFactory))
 *   }
 * }
 * ```
 */
internal object FragmentAssisted_Factory_Impl_Generator : FileGenerator<Factory> {

  override fun generate(
    codeGenDir: File,
    params: Factory
  ): GeneratedFile {

    val factoryParams = params
    val fragmentParams = params.fragmentParams

    val packageName = factoryParams.packageName
    val fragmentFactoryClassName = fragmentParams.fragmentFactoryClassName
    val fragmentTypeName = fragmentParams.fragmentClassName
    val factoryClass = factoryParams.factoryClass

    val factoryInterfaceClassName = factoryParams.factoryInterfaceClassName
    val factoryImplClassName = factoryParams.factoryImplClassName

    val typeParameters = factoryParams.typeParameters

    val delegateFactoryName = "delegateFactory"

    val tangleParams = factoryParams.tangleParams

    val content = FileSpec.buildFile(packageName, factoryImplClassName.simpleName) {
      TypeSpec.classBuilder(factoryImplClassName)
        .apply {
          if (factoryClass.isInterface()) {
            addSuperinterface(factoryInterfaceClassName)
          } else {
            superclass(factoryInterfaceClassName)
          }
        }
        .applyEach(typeParameters) { addTypeVariable(it.typeVariableName) }
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter(delegateFactoryName, fragmentFactoryClassName)
            .build()
        )
        .addProperty(
          PropertySpec.builder(delegateFactoryName, fragmentFactoryClassName)
            .initializer(delegateFactoryName)
            .build()
        )
        .addFunction(
          FunSpec.builder(factoryParams.functionName)
            .addModifiers(OVERRIDE)
            .applyEach(tangleParams) { param ->
              addParameter(param.name, param.typeName)
            }
            .returns(returnType = fragmentTypeName)
            .apply {
              val allNames = factoryParams.tangleParams.map { it.name }

              val bundleName = allNames.uniqueName("bundle")

              val bundleOfArguments = tangleParams.joinToString(
                separator = ",\n",
                prefix = "(\n",
                postfix = "\n)"
              ) { param ->
                CodeBlock.of("  %S·to·%L", param.key, param.name).toString()
              }

              addStatement(
                "val·%L·=·%M%L",
                bundleName,
                MemberNames.bundleOf,
                bundleOfArguments
              )

              beginControlFlow("return·$delegateFactoryName.get().apply·{", fragmentTypeName)
              addStatement("this@apply.arguments·=·%L", bundleName)
              endControlFlow()
            }
            .build()
        )
        .addType(
          TypeSpec.companionObjectBuilder()
            .addFunction(
              FunSpec
                .builder("create")
                .addAnnotation(ClassNames.jvmStatic)
                .addParameter(delegateFactoryName, fragmentFactoryClassName)
                .returns(factoryInterfaceClassName.wrapInProvider())
                .addStatement(
                  "return·%T.create(%T($delegateFactoryName))",
                  ClassNames.instanceFactory,
                  factoryImplClassName
                )
                .build()
            )
            .build()
        )
        .build()
        .let { addType(it) }
    }
    return createGeneratedFile(
      codeGenDir = codeGenDir,
      packageName = packageName,
      fileName = factoryImplClassName.simpleName,
      content = content
    )
  }
}
