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
@file:Suppress("ComplexMethod", "TooManyFunctions", "NestedBlockDepth")

package tangle.inject.compiler

import com.squareup.anvil.compiler.api.AnvilCompilationException
import com.squareup.anvil.compiler.internal.reference.AnnotationReference
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.FunctionReference
import com.squareup.anvil.compiler.internal.reference.ParameterReference
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import dagger.Lazy
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import javax.inject.Provider

fun ClassReference.vmInjectConstructor(): FunctionReference? {
  return annotatedConstructorOrNull(FqNames.vmInject)
}

fun ClassReference.assistedInjectConstructor(): FunctionReference? {
  return annotatedConstructorOrNull(FqNames.assistedInject)
}

fun ClassReference.fragmentInjectConstructor(): FunctionReference? {
  return annotatedConstructorOrNull(FqNames.fragmentInject)
}

fun ClassReference.injectConstructor(): FunctionReference? {
  return annotatedConstructorOrNull(FqNames.inject)
}

internal fun ClassReference.annotatedConstructorOrNull(
  annotationFqName: FqName
): FunctionReference? {
  val constructors = constructors.filter {
    it.hasAnnotation(annotationFqName)
  }

  return when (constructors.size) {
    0 -> null
    1 -> if (constructors[0].hasAnnotation(annotationFqName)) constructors[0] else null
    else -> throw TangleCompilationException(
      classReference = this,
      "Types may only contain one injected constructor."
    )
  }
}

fun <T : AnnotationReference> Iterable<T>.find(fqName: FqName): T? = find { it.fqName == fqName }

fun FunctionReference.hasAnnotation(fqName: FqName): Boolean =
  annotations.any { it.fqName == fqName }

fun List<ParameterReference>.mapToParameters(
  module: ModuleDescriptor
): List<ConstructorInjectParameter> =
  map { parameter ->

    val annotations = parameter.annotations
    val type = parameter.type()

    val typeFqName = type.asClassReference().fqName

    val isWrappedInProvider = typeFqName == FqNames.provider
    val isWrappedInLazy = typeFqName == FqNames.daggerLazy

    val unwrappedTypeOrSelf = if (isWrappedInLazy || isWrappedInProvider) {
      type.unwrappedTypes.first()
    } else type

    val typeName = unwrappedTypeOrSelf.asTypeName()
      .withJvmSuppressWildcardsIfNeeded(module, unwrappedTypeOrSelf)

    val tangleParamName = parameter.tangleParamNameOrNull()

    val qualifiers = annotations.qualifierAnnotationSpecs(module)

    val baseName = parameter.name

    val name = when {
      isWrappedInLazy -> "${baseName}Lazy"
      isWrappedInProvider -> "${baseName}Provider"
      else -> baseName
    }

    val isDaggerAssisted = annotations.any { it.fqName == FqNames.daggerAssisted }

    ConstructorInjectParameter(
      name = name,
      typeName = typeName,
      providerTypeName = typeName.wrapInProvider(),
      lazyTypeName = typeName.wrapInLazy(),
      isWrappedInProvider = isWrappedInProvider,
      isWrappedInLazy = isWrappedInLazy,
      tangleParamName = tangleParamName,
      qualifiers = qualifiers,
      isDaggerAssisted = isDaggerAssisted
    )
  }

fun TypeName.wrapInProvider(): ParameterizedTypeName {
  return Provider::class.asClassName().parameterizedBy(this)
}

fun TypeName.wrapInLazy(): ParameterizedTypeName {
  return Lazy::class.asClassName().parameterizedBy(this)
}

/**
 * Delegate everything to Anvil's logic, but if it throws an exception, catch that Anvil exception
 * and re-brand as a Tangle exception.
 *
 * This will hopefully prevent Tangle bugs getting reported to Anvil as Anvil bugs.
 */
internal inline fun <T, R> T.delegateToAnvilUnsafe(action: T.() -> R): R = try {
  action()
} catch (e: AnvilCompilationException) {
  throw TangleCompilationException(e.message!!, e.cause, e.element)
}

/**
 * Converts the parameter list to comma separated argument list that can be used to call other
 * functions, e.g.
 *
 * ```
 * [param0: String, param1: Int] -> "param0, param1"
 * ```
 *
 * [asProvider] allows you to decide if each parameter is wrapped in a `Provider` interface. If
 * true, then the `get()` function will be called for the provider parameter. If false, then then
 * always only the parameter name will used in the argument list:
 * ```
 * "param0.get()" vs "param0"
 * ```
 *
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
