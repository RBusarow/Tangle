package tangle.viewmodel.compiler

import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.internal.capitalize
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import tangle.inject.compiler.*
import tangle.viewmodel.compiler.params.Factory.FunctionParameter
import tangle.viewmodel.compiler.params.ViewModelParams
import java.io.File

internal class ViewModelFactoryGenerator : FileGenerator<ViewModelParams> {

  @Suppress("ComplexMethod")
  override fun generate(
    codeGenDir: File,
    params: ViewModelParams
  ): GeneratedFile {

    val factoryConstructorParams =
      params.viewModelFactoryConstructorParams + params.memberInjectedParams

    val content = FileSpec.buildFile(params.packageName, params.viewModelFactoryClassNameString) {
      TypeSpec.classBuilder(params.viewModelFactoryClassName)
        .applyEach(params.typeParameters) { addTypeVariable(it) }
        .apply {
          primaryConstructor(
            com.squareup.kotlinpoet.FunSpec.constructorBuilder()
              .addAnnotation(ClassNames.inject)
              .applyEach(factoryConstructorParams) { parameter ->
                addParameter(parameter.name, parameter.providerTypeName)
              }
              .build()
          )

          params.factory
            ?.factoryInterfaceClassName
            ?.also {
              addSuperinterface(it)
            }
        }
        .applyEach(factoryConstructorParams) { parameter ->

          val qualifierAnnotationSpecs = parameter.qualifiers

          addProperty(
            PropertySpec.builder(parameter.name, parameter.providerTypeName)
              .initializer(parameter.name)
              .addModifiers(INTERNAL)
              .applyEach(qualifierAnnotationSpecs) { addAnnotation(it) }
              .build()
          )
        }
        .addFunction(params.factoryFunctionName) {
          if (params.factory != null) {
            addModifiers(OVERRIDE)
          }

          val constructorArguments = params.viewModelConstructorParams
            .asArgumentList(
              asProvider = true,
              includeModule = false
            )

          val constructorAssisted = params.viewModelConstructorParams
            .filter { it.isAssisted }

          requireFactoryExistsIfNeeded(constructorAssisted, params)

          val factoryParams = params.factory
            ?.functionArguments
            .orEmpty()

          requireAssistedArgumentsMatch(constructorAssisted, factoryParams, params)

          factoryParams.forEach { param ->
            addParameter(param.name, param.typeName)
          }

          returns(returnType = params.viewModelClassName)

          val tangleParams = params.viewModelConstructorParams
            .filter { it.isTangleParam }

          if (params.savedStateParam != null && tangleParams.isNotEmpty()) {
            tangleParams.forEach { param ->

              val tangleParamName = param.tangleParamName

              require(
                !tangleParamName.isNullOrEmpty(),
                params.viewModelClassDescriptor
              ) {
                "parameter ${param.name} is annotated with ${FqNames.tangleParam.asString()}, " +
                  "but does not have a valid key."
              }

              addStatement(
                "val·%L·=·${params.savedStateParam.name}.get().get<%T>(%S)",
                param.name,
                param.typeName,
                tangleParamName
              )
              if (!param.typeName.isNullable) {
                beginControlFlow("checkNotNull(%L)·{", param.name)
                addStatement(
                  "%S",
                  buildCodeBlock {
                    add(
                      "Required parameter with name `%L` and type `%L` is missing from SavedStateHandle.",
                      tangleParamName,
                      param.typeName
                    )
                  }
                )
                endControlFlow()
              }
            }
          }

          if (params.memberInjectedParams.isEmpty()) {
            addStatement(
              "return·%T($constructorArguments)",
              params.viewModelClassName
            )
          } else {

            addStatement(
              "val·instance·=·%T($constructorArguments)",
              params.viewModelClassName
            )

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
            addStatement("return·instance")
          }
        }
        .build()
        .let { addType(it) }
    }

    return createGeneratedFile(
      codeGenDir,
      params.packageName,
      params.viewModelFactoryClassNameString,
      content
    )
  }

  private fun requireAssistedArgumentsMatch(
    constructorAssisted: List<ConstructorInjectParameter>,
    factoryParams: List<FunctionParameter>,
    viewModelParams: ViewModelParams
  ) {

    val matchingArguments = constructorAssisted.all { cp ->
      factoryParams.any { fp ->
        fp.name == cp.name && fp.typeName == cp.typeName
      }
    } && constructorAssisted.size == factoryParams.size

    require(matchingArguments, { viewModelParams.viewModelClassDescriptor }) {
      """@VMAssisted-annotated constructor parameters and factory interface function parameters don't match.
        |
        |assisted constructor parameters
        |${constructorAssisted.joinToString("\n\t", "\t") { "${it.name}: ${it.typeName}" }}
        |
        |factory function parameters
        |${factoryParams.joinToString("\n\t", "\t") { "${it.name}: ${it.typeName}" }}
      """.trimMargin()
    }
  }

  private fun requireFactoryExistsIfNeeded(
    constructorAssisted: List<ConstructorInjectParameter>,
    viewModelParams: ViewModelParams
  ) {
    val factoryExistsIfNeeded =
      constructorAssisted.isEmpty() || viewModelParams.factory != null

    require(
      factoryExistsIfNeeded,
      { viewModelParams.viewModelClassDescriptor }
    ) {
      "${viewModelParams.viewModelClassSimpleName}'s constructor has @VMAssisted-annotated " +
        "parameters, but there is no corresponding factory interface.  In order to provide " +
        "assisted parameters, create a Factory interface " +
        "and annotated it with @VMInjectFactory."
    }
  }
}
