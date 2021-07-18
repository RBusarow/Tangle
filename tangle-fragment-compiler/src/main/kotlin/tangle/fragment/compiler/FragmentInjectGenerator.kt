package tangle.fragment.compiler

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.nonStaticOuterClasses
import org.jetbrains.kotlin.resolve.DescriptorUtils
import tangle.fragment.compiler.FragmentInjectParams.Factory
import tangle.fragment.compiler.FragmentInjectParams.Fragment
import tangle.inject.compiler.*
import tangle.inject.compiler.asClassName
import java.io.File

@Suppress("unused")
@AutoService(CodeGenerator::class)
class FragmentInjectGenerator : CodeGenerator {

  override fun isApplicable(context: AnvilContext): Boolean = true

  override fun generateCode(
    codeGenDir: File,
    module: ModuleDescriptor,
    projectFiles: Collection<KtFile>
  ): Collection<GeneratedFile> {
    val paramsList = projectFiles
      .flatMap { file ->

        val factoryParams = file.classesAndInnerClasses(module)
          .filter { it.hasAnnotation(FqNames.fragmentInjectFactory, module) }
          .map { factoryInterface ->

            factoryInterface.nonStaticOuterClasses()
              .firstOrNull { it.fragmentInjectConstructor(module) != null }
              ?.let { fragmentClass ->
                Factory.create(
                  module,
                  factoryInterface,
                  fragmentClass,
                  fragmentClass.fragmentInjectConstructor(module)!!
                )
              } ?: throw TangleCompilationException(
              "The @${FqNames.fragmentInjectFactory.shortName().asString()}-annotated interface " +
                "`${factoryInterface.fqName}` must be defined inside a Fragment " +
                "which is annotated with `@${FqNames.fragmentInject.shortName().asString()}`."
            )
          }

        val alreadyParsedFragments = factoryParams.map { it.fragmentClassName }.toSet()

        file.classesAndInnerClasses(module)
          .filterNot { it.asClassName() in alreadyParsedFragments }
          .forEach { clazz ->

            if (clazz.asClassName() in alreadyParsedFragments) return@forEach

            require(
              clazz.fragmentInjectConstructor(module) == null,
              { clazz.requireClassDescriptor(module) }
            ) {
              "@${FqNames.fragmentInject.shortName().asString()} must only be applied " +
                "to the constructor of a Fragment, and that fragment must have a corresponding " +
                "${
                FqNames.fragmentInjectFactory.shortName()
                  .asString()
                }-annotated factory interface."
            }
          }

        listOf(factoryParams, factoryParams.map { it.fragmentParams })
      }
      .flatten()
      .toList()

    val generated = paramsList.map { params ->

      when (params) {
        is Factory -> createFactoryImplementation(codeGenDir, params, params.fragmentParams, module)
        is Fragment -> createFragmentFactory(codeGenDir, module, params)
      }
    }

    if (paramsList.isEmpty()) {
      return emptyList()
    }

    val daggerModules = paramsList
      .groupBy { it.packageName }
      .flatMap { (packageName, byPackageName) ->
        byPackageName
          .groupBy { it.scopeName }
          .map { (scopeName, byScopeName) ->
            createDaggerModule(
              scopeClassName = scopeName.asClassName(module),
              codeGenDir = codeGenDir,
              packageName = packageName,
              paramsList = byScopeName
            )
          }
      }

    return generated + daggerModules
  }

  private fun createFragmentFactory(
    codeGenDir: File,
    module: ModuleDescriptor,
    fragmentParams: FragmentInjectParams.Fragment
  ): GeneratedFile {
    val packageName = fragmentParams.packageName
    val fragmentFactoryClassNameString = fragmentParams.fragmentFactoryClassNameString

    val typeSpecBuilder = if (fragmentParams.injectedParams.isEmpty()) {
      TypeSpec.objectBuilder(fragmentParams.fragmentFactoryClassName)
    } else {
      TypeSpec.classBuilder(fragmentParams.fragmentFactoryClassName)
    }

    fun TypeSpec.Builder.addStatic(
      action: TypeSpec.Builder.() -> TypeSpec.Builder
    ) = apply {
      if (fragmentParams.injectedParams.isEmpty()) {
        action()
      } else {
        addType(
          TypeSpec.companionObjectBuilder()
            .action()
            .build()
        )
      }
    }

    val createFunction = FunSpec
      .builder("create")
      .addAnnotation(ClassNames.jvmStatic)
      .applyEach(fragmentParams.injectedParams) { param ->
        addParameter(param.name, param.providerTypeName)
      }
      .returns(fragmentParams.fragmentFactoryClassName)
      .apply {
        if (fragmentParams.injectedParams.isEmpty()) {
          addStatement("return·this")
        } else {
          val createArguments = fragmentParams.injectedParams.asArgumentList(false, false)
          addStatement(
            "return·%T($createArguments)",
            fragmentParams.fragmentFactoryClassName
          )
        }
      }
      .build()

    val newInstanceFunction = FunSpec
      .builder("newInstance")
      .addAnnotation(ClassNames.jvmStatic)
      .applyEach(fragmentParams.injectedParams) { param ->
        addParameter(param.name, param.typeName)
      }
      .returns(fragmentParams.fragmentClassName)
      .apply {
        val injectArguments = fragmentParams.injectedParams.asArgumentList(false, false)
        addStatement("return·%T($injectArguments)", fragmentParams.fragmentClassName)
      }
      .build()

    val content = FileSpec.buildFile(packageName, fragmentFactoryClassNameString) {
      typeSpecBuilder
        .applyEach(fragmentParams.typeParameters) { addTypeVariable(it) }
        .addSuperinterface(ClassNames.daggerFactory.parameterizedBy(fragmentParams.fragmentTypeName))
        .apply {
          if (fragmentParams.injectedParams.isNotEmpty()) {
            primaryConstructor(
              FunSpec.constructorBuilder()
                .applyEach(fragmentParams.injectedParams) { parameter ->
                  addParameter(parameter.name, parameter.providerTypeName)
                }
                .build()
            )
          }
        }
        .applyEach(fragmentParams.injectedParams) { parameter ->

          val qualifierAnnotationSpecs = parameter.annotationEntries
            .filter { it.isQualifier(module) }
            .map { it.toAnnotationSpec(module) }

          addProperty(
            PropertySpec.builder(parameter.name, parameter.providerTypeName)
              .initializer(parameter.name)
              .addModifiers(INTERNAL)
              .applyEach(qualifierAnnotationSpecs) { addAnnotation(it) }
              .build()
          )
        }
        .addFunction(
          FunSpec.builder("get")
            .addModifiers(OVERRIDE)
            .returns(returnType = fragmentParams.fragmentClassName)
            .apply {
              val getterArguments = fragmentParams.injectedParams.asArgumentList(
                asProvider = true,
                includeModule = false
              )
              addStatement("return·newInstance($getterArguments)", fragmentParams.fragmentClassName)
            }
            .build()
        )
        .addStatic {
          addFunction(createFunction)
          addFunction(newInstanceFunction)
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

  private fun createFactoryImplementation(
    codeGenDir: File,
    factoryParams: FragmentInjectParams.Factory,
    fragmentParams: FragmentInjectParams.Fragment,
    module: ModuleDescriptor
  ): GeneratedFile {
    val packageName = factoryParams.packageName
    val fragmentFactoryClassName = fragmentParams.fragmentFactoryClassName
    val fragmentTypeName = fragmentParams.fragmentClassName
    val factoryDescriptor = factoryParams.factoryDescriptor

    val factoryInterfaceClassName = factoryParams.factoryInterfaceClassName
    val factoryImplClassName = factoryParams.factoryImplClassName

    val typeParameters = factoryParams.factoryInterface.typeVariableNames(module)

    val delegateFactoryName = "delegateFactory"

    val tangleParams = factoryParams.tangleParams

    val content = FileSpec.buildFile(packageName, factoryImplClassName.simpleName) {
      TypeSpec.classBuilder(factoryImplClassName)
        .apply {
          if (DescriptorUtils.isInterface(factoryDescriptor)) {
            addSuperinterface(factoryInterfaceClassName)
          } else {
            superclass(factoryInterfaceClassName)
          }
        }
        .applyEach(typeParameters) { addTypeVariable(it) }
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

  private fun createDaggerModule(
    scopeClassName: ClassName,
    codeGenDir: File,
    packageName: String,
    paramsList: List<FragmentInjectParams>
  ): GeneratedFile {
    val moduleName = "Tangle_${scopeClassName.simpleNames.joinToString("_")}_FragmentInject_Module"

    val factoryImpls = paramsList.filterIsInstance<FragmentInjectParams.Factory>()

    val content = FileSpec.buildFile(packageName, moduleName) {
      addType(
        TypeSpec.objectBuilder(ClassName(packageName, moduleName))
          .addAnnotation(ClassNames.module)
          .addAnnotation(
            AnnotationSpec.Companion.builder(ClassNames.contributesTo)
              .addMember("%T::class", scopeClassName)
              .build()
          )
          .applyEach(factoryImpls) { params ->

            val args = params.fragmentParams.injectedParams.asArgumentList(
              asProvider = false,
              includeModule = false
            )

            addFunction(
              FunSpec.builder(
                "provide${params.factoryInterfaceClassName.simpleNames.joinToString("_")}"
              )
                .addAnnotation(ClassNames.provides)
                .applyEach(params.fragmentParams.injectedParams) { argument ->
                  addParameter(argument.name, argument.typeName.wrapInProvider())
                }
                .returns(params.factoryInterfaceClassName)
                .addStatement(
                  "return·%T.create(%T.create($args)).get()",
                  params.factoryImplClassName,
                  params.fragmentFactoryClassName
                )
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
}
