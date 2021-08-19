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
@file:Suppress("ComplexMethod", "TooManyFunctions", "NestedBlockDepth")

package tangle.inject.compiler

import com.squareup.anvil.compiler.api.AnvilCompilationException
import com.squareup.anvil.compiler.internal.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.jvm.jvmSuppressWildcards
import dagger.Lazy
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.resolveClassByFqName
import org.jetbrains.kotlin.incremental.components.NoLookupLocation.FROM_BACKEND
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import javax.inject.Provider
import com.squareup.anvil.compiler.internal.asClassName as anvilAsClassName
import com.squareup.anvil.compiler.internal.requireFqName as anvilRequireFqName
import com.squareup.anvil.compiler.internal.requireTypeName as anvilRequireTypeName

fun KtAnnotationEntry.scope(module: ModuleDescriptor): FqName {
  return findAnnotationArgument<KtClassLiteralExpression>(name = "scope", index = 0)
    .let { classLiteralExpression ->
      if (classLiteralExpression == null) {
        throw TangleCompilationException(
          "Couldn't find scope for ${fqNameOrNull(module)}.",
          element = this
        )
      }

      classLiteralExpression.requireFqName(module)
    }
}

fun KtClassOrObject.vmInjectConstructor(module: ModuleDescriptor): KtConstructor<*>? {
  return annotatedConstructorOrNull(FqNames.vmInject, module)
}

fun KtClassOrObject.fragmentInjectConstructor(
  module: ModuleDescriptor
): KtConstructor<*>? {
  return annotatedConstructorOrNull(FqNames.fragmentInject, module)
}

fun KtClassOrObject.injectConstructor(
  module: ModuleDescriptor
): KtConstructor<*>? {
  return annotatedConstructorOrNull(FqNames.inject, module)
}

internal fun KtClassOrObject.annotatedConstructorOrNull(
  annotationFqName: FqName,
  module: ModuleDescriptor
): KtConstructor<*>? {
  val constructors = allConstructors.filter {
    it.hasAnnotation(annotationFqName, module)
  }

  return when (constructors.size) {
    0 -> null
    1 -> if (constructors[0].hasAnnotation(annotationFqName, module)) constructors[0] else null
    else -> throw TangleCompilationException(
      "Types may only contain one injected constructor.",
      element = this
    )
  }
}

internal fun KtAnnotationEntry.tangleParamName(): String? {
  return findAnnotationArgument<KtStringTemplateExpression>(name = "name", index = 0)
    ?.entries
    ?.firstOrNull()
    ?.text
}

fun List<KtCallableDeclaration>.mapToParameters(
  module: ModuleDescriptor
): List<ConstructorInjectParameter> =
  mapIndexed { index, parameter ->
    val typeElement = parameter.typeReference?.typeElement
    val typeFqName = typeElement?.fqNameOrNull(module)

    val isWrappedInProvider = typeFqName == FqNames.provider
    val isWrappedInLazy = typeFqName == FqNames.daggerLazy

    val annotations = parameter.annotationEntries

    val typeName = when {
      parameter.requireTypeReference(module).isNullable() ->
        parameter.requireTypeReference(module).requireTypeName(module).copy(nullable = true)

      isWrappedInProvider || isWrappedInLazy ->
        typeElement!!.children
          .filterIsInstance<KtTypeArgumentList>()
          .single()
          .children
          .filterIsInstance<KtTypeProjection>()
          .single()
          .children
          .filterIsInstance<KtTypeReference>()
          .single()
          .requireTypeName(module)

      else -> parameter.requireTypeReference(module).requireTypeName(module)
    }.withJvmSuppressWildcardsIfNeeded(parameter, module)

    val tangleParamName = parameter
      .findAnnotation(FqNames.tangleParam, module)
      ?.tangleParamName()

    val qualifiers = annotations.qualifierAnnotationSpecs(module)

    val isAssisted = annotations.any { it.requireFqName(module) == FqNames.vmAssisted }

    require(!isAssisted || parameter.typeReference?.isFunctionType() != true) {
      "Functional arguments like `${parameter.text}` can't be used as assisted arguments " +
        "for ViewModels.  They would leak the initial caller's instance into the ViewModel."
    }

    val baseName = parameter.name ?: "param$index"

    val name = when {
      isWrappedInLazy -> "${baseName}Lazy"
      isWrappedInProvider -> "${baseName}Provider"
      else -> baseName
    }

    ConstructorInjectParameter(
      name = name,
      typeName = typeName,
      providerTypeName = typeName.wrapInProvider(),
      lazyTypeName = typeName.wrapInLazy(),
      isWrappedInProvider = isWrappedInProvider,
      isWrappedInLazy = isWrappedInLazy,
      tangleParamName = tangleParamName,
      qualifiers = qualifiers,
      isAssisted = isAssisted
    )
  }

fun KtAnnotationEntry.qualifierArgumentsOrNull(module: ModuleDescriptor) = typeReference
  ?.requireFqName(module)
  // Often entries are annotated with @Inject, in this case we know it's not a qualifier and we
  // can stop early.
  ?.takeIf { it != FqNames.inject }
  ?.requireClassDescriptor(module)
  ?.annotations
  ?.findAnnotation(FqNames.qualifier)
  ?.allValueArguments

fun TypeName.wrapInProvider(): ParameterizedTypeName {
  return Provider::class.asClassName().parameterizedBy(this)
}

fun TypeName.wrapInLazy(): ParameterizedTypeName {
  return Lazy::class.asClassName().parameterizedBy(this)
}

internal fun <T : KtCallableDeclaration> TypeName.withJvmSuppressWildcardsIfNeeded(
  callableDeclaration: T,
  module: ModuleDescriptor
): TypeName {
  // If the parameter is annotated with @JvmSuppressWildcards, then add the annotation
  // to our type so that this information is forwarded when our Factory is compiled.
  val hasJvmSuppressWildcards =
    callableDeclaration.typeReference?.hasAnnotation(FqNames.jvmSuppressWildcards, module) ?: false

  // Add the @JvmSuppressWildcards annotation even for simple generic return types like
  // Set<String>. This avoids some edge cases where Dagger chokes.
  val isGenericType = callableDeclaration.typeReference?.isGenericType() ?: false

  // Same for functions.
  val isFunctionType = callableDeclaration.typeReference?.isFunctionType() ?: false

  return when {
    hasJvmSuppressWildcards || isGenericType -> this.jvmSuppressWildcards()
    isFunctionType -> this.jvmSuppressWildcards()
    else -> this
  }
}

internal fun PsiElement.fqNameOrNull(
  module: ModuleDescriptor
): FqName? {
  // Usually it's the opposite way, the require*() method calls the nullable method. But in this
  // case we'd like to preserve the better error messages in case something goes wrong.
  return try {
    requireFqName(module)
  } catch (e: TangleCompilationException) {
    null
  }
}

internal fun PsiElement.requireFqName(
  module: ModuleDescriptor
): FqName = delegateToAnvilUnsafe {
  anvilRequireFqName(module)
}

fun KtCallableDeclaration.requireTypeReference(module: ModuleDescriptor): KtTypeReference {
  typeReference?.let { return it }

  if (this is KtFunction && findAnnotation(FqNames.daggerProvides, module) != null) {
    throw TangleCompilationException(
      message = "Dagger provider methods must specify the return type explicitly when using " +
        "Anvil. The return type cannot be inferred implicitly.",
      element = this
    )
  }

  throw TangleCompilationException("Couldn't obtain type reference.", element = this)
}

fun FqName.requireClassDescriptor(module: ModuleDescriptor): ClassDescriptor {
  return module.resolveClassByFqName(this, FROM_BACKEND)
    ?: throw TangleCompilationException("Couldn't resolve class for $this.")
}

internal fun KtTypeReference.requireTypeName(
  module: ModuleDescriptor
): TypeName = delegateToAnvilUnsafe {
  anvilRequireTypeName(module)
}

fun FqName.asClassName(module: ModuleDescriptor): ClassName = delegateToAnvilUnsafe {
  anvilAsClassName(module)
}

/**
 * Delegate everything to Anvil's logic, but if it throws an exception,
 * catch that Anvil exception and re-brand as a Tangle exception.
 */
internal inline fun <T, R> T.delegateToAnvilUnsafe(action: T.() -> R): R = try {
  action()
} catch (e: AnvilCompilationException) {
  throw TangleCompilationException(e.message!!, e.cause, e.element)
}

/**
 * Converts the parameter list to comma separated argument list that can be used to call other
 * functions, e.g.
 * ```
 * [param0: String, param1: Int] -> "param0, param1"
 * ```
 * [asProvider] allows you to decide if each parameter is wrapped in a `Provider` interface. If
 * true, then the `get()` function will be called for the provider parameter. If false, then
 * then always only the parameter name will used in the argument list:
 * ```
 * "param0.get()" vs "param0"
 * ```
 * Set [includeModule] to true if a Dagger module instance is part of the argument list.
 */
fun List<Parameter>.asArgumentList(
  asProvider: Boolean,
  includeModule: Boolean
): String {
  return this
    .let { list ->
      if (asProvider) {
        list.map { parameter ->
          when {
            parameter.isWrappedInProvider -> parameter.name
            // Normally Dagger changes Lazy<Type> parameters to a Provider<Type> (usually the
            // container is a joined type), therefore we use `.lazy(..)` to convert the Provider
            // to a Lazy. Assisted parameters behave differently and the Lazy type is not changed
            // to a Provider and we can simply use the parameter name in the argument list.
            parameter.isWrappedInLazy && parameter.isTangleParam -> parameter.name
            parameter.isWrappedInLazy -> "${FqNames.daggerDoubleCheck}.lazy(${parameter.name})"
            parameter.isTangleParam -> parameter.name
            parameter.isAssisted -> parameter.name
            else -> "${parameter.name}.get()"
          }
        }
      } else list.map { it.name }
    }
    .let {
      if (includeModule) {
        val result = it.toMutableList()
        result.add(0, "module")
        result.toList()
      } else {
        it
      }
    }
    .joinToString()
}
