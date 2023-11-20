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
@file:OptIn(com.squareup.anvil.annotations.ExperimentalAnvilApi::class)

package tangle.fragment.compiler

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.internal.capitalize
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import tangle.fragment.compiler.FragmentInjectParams.Fragment
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FileGenerator
import tangle.inject.compiler.FqNames
import tangle.inject.compiler.FunSpec
import tangle.inject.compiler.Parameter
import tangle.inject.compiler.addFunction
import tangle.inject.compiler.applyEach
import tangle.inject.compiler.applyIf
import tangle.inject.compiler.asArgumentList
import tangle.inject.compiler.buildFile
import java.io.File

/**
 * This generates a Factory for the Fragment itself, which is delegated to by the Factory
 * interface's implementation.
 *
 * It's equivalent to the factory which is generated for `@Inject` annotations, because it provides
 * all of the dependencies needed in order to create a new instance. This is different from
 * `@AssistedInject` constructor factories.
 *
 * given this Fragment definition:
 * ```
 * @ContributesFragment(Unit::class)
 * class MyFragment @FragmentInject constructor(
 *   val numbers: List<Int>
 * ) : Fragment() {
 *
 *   @FragmentInjectFactory
 *   interface Factory {
 *     fun create(
 *       @TangleParam("name") name: String
 *     ): MyFragment
 *   }
 * }
 * ```
 *
 * This generator will create the following `MyFragment_Factory.kt`:
 * ```
 * public class MyFragment_Factory(
 *   internal val numbers: Provider<@JvmSuppressWildcards List<Int>>
 * ) : Factory<MyFragment> {
 *   public override fun `get`(): MyFragment = newInstance(numbers.get())
 *
 *   public companion object {
 *     @JvmStatic
 *     public fun create(numbers: Provider<@JvmSuppressWildcards List<Int>>): MyFragment_Factory =
 *         MyFragment_Factory(numbers)
 *
 *     @JvmStatic
 *     public fun newInstance(numbers: @JvmSuppressWildcards List<Int>): MyFragment =
 *         MyFragment(numbers)
 *   }
 * }
 * ```
 *
 * @see FragmentAssisted_Factory_Impl_Generator
 */
internal object Fragment_Factory_Generator : FileGenerator<FragmentInjectParams.Fragment> {
  override fun generate(
    codeGenDir: File,
    params: FragmentInjectParams.Fragment
  ): GeneratedFile {
    val packageName = params.packageName
    val fragmentFactoryClassNameString = params.fragmentFactoryClassNameString

    // arguments for the constructor of this class
    val factoryConstructorParams =
      params.constructorParams + params.memberInjectedParams

    val typeSpecBuilder = createTypeSpecBuilder(factoryConstructorParams, params)

    val content = FileSpec.buildFile(packageName, fragmentFactoryClassNameString) {
      typeSpecBuilder
        .applyEach(params.typeParameters) { addTypeVariable(it.typeVariableName) }
        .addSuperinterface(ClassNames.daggerFactory.parameterizedBy(params.fragmentTypeName))
        .applyIf(factoryConstructorParams.isNotEmpty()) {
          primaryConstructor(
            FunSpec.constructorBuilder()
              .applyEach(factoryConstructorParams) { parameter ->
                addParameter(parameter.name, parameter.providerTypeName)
              }
              .build()
          )
        }
        .applyEach(factoryConstructorParams) { parameter ->

          val qualifierAnnotationSpecs = parameter.qualifiers

          addProperty(
            PropertySpec.Companion.builder(parameter.name, parameter.providerTypeName)
              .initializer(parameter.name)
              .addModifiers(KModifier.INTERNAL)
              .applyEach(qualifierAnnotationSpecs) { addAnnotation(it) }
              .build()
          )
        }
        .addFunction("get") {
          addModifiers(KModifier.OVERRIDE)
          returns(returnType = params.fragmentClassName)

          val newInstanceArguments = params.constructorParams.asArgumentList(
            asProvider = true,
            includeModule = false
          )
          if (params.memberInjectedParams.isEmpty()) {
            addStatement(
              "return·newInstance($newInstanceArguments)",
              params.fragmentClassName
            )
          } else {
            addStatement("val instance = newInstance($newInstanceArguments)")

            val memberInjectParameters = params.memberInjectedParams

            memberInjectParameters.forEach { parameter ->

              val propertyName = parameter.name
              val functionName = "inject${propertyName.capitalize()}"

              val param = when {
                parameter.isWrappedInProvider -> parameter.name
                parameter.isWrappedInLazy -> "${FqNames.daggerDoubleCheck}.lazy(${parameter.name})"
                else -> parameter.name + ".get()"
              }

              addStatement("%T.$functionName(instance, $param)", parameter.memberInjectorClass)
            }
            addStatement("return instance")
          }
        }
        .addStatic(factoryConstructorParams.isEmpty()) {
          // creates a new instance of the factory
          addFunction(
            FunSpec("create") {
              addAnnotation(ClassNames.jvmStatic)
              factoryConstructorParams.forEach { param ->
                addParameter(param.name, param.providerTypeName)
              }
              returns(params.fragmentFactoryClassName)

              if (factoryConstructorParams.isEmpty()) {
                addStatement("return·this")
              } else {
                val createArguments = factoryConstructorParams.asArgumentList(
                  asProvider = false,
                  includeModule = false
                )
                addStatement(
                  "return·%T($createArguments)",
                  params.fragmentFactoryClassName
                )
              }
            }
          )
          // creates a new instance of the fragment
          addFunction(
            FunSpec("newInstance") {
              addAnnotation(ClassNames.jvmStatic)
              params.constructorParams.forEach { param ->
                val paramType = when {
                  param.isWrappedInLazy -> param.lazyTypeName
                  else -> param.typeName
                }
                addParameter(param.name, paramType)
              }
              returns(params.fragmentClassName)

              val injectArguments = params.constructorParams.asArgumentList(
                asProvider = false,
                includeModule = false
              )
              addStatement("return·%T($injectArguments)", params.fragmentClassName)
            }
          )
        }
        .build()
        .let { addType(it) }
    }

    return createGeneratedFile(
      codeGenDir = codeGenDir,
      packageName = packageName,
      fileName = fragmentFactoryClassNameString,
      content = content
    )
  }

  /**
   * If this factory doesn't take any arguments at all, then it's an object. If this factory needs a
   * constructor, then obviously it's a class.
   */
  fun createTypeSpecBuilder(
    factoryConstructorParams: List<Parameter>,
    params: Fragment
  ) = if (factoryConstructorParams.isEmpty()) {
    TypeSpec.objectBuilder(params.fragmentFactoryClassName)
  } else {
    TypeSpec.classBuilder(params.fragmentFactoryClassName)
  }

  /**
   * If the whole factory is an object, just add content to it. Otherwise, add a companion object
   * and add the new content there.
   */
  fun TypeSpec.Builder.addStatic(
    alreadyObject: Boolean,
    content: TypeSpec.Builder.() -> TypeSpec.Builder
  ) = apply {
    if (alreadyObject) {
      content()
    } else {
      addType(
        TypeSpec.companionObjectBuilder()
          .content()
          .build()
      )
    }
  }
}
