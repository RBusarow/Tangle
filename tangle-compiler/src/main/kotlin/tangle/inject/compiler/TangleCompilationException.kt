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

package tangle.inject.compiler

import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.ClassReference.Descriptor
import com.squareup.anvil.compiler.internal.reference.ClassReference.Psi
import org.jetbrains.kotlin.codegen.CompilationException
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiNameIdentifierOwner
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement
import kotlin.contracts.contract

class TangleCompilationException(
  message: String,
  cause: Throwable? = null,
  element: PsiElement? = null
) : CompilationException(message, cause, element) {

  @OptIn(com.squareup.anvil.annotations.ExperimentalAnvilApi::class)
  constructor(
    annotationDescriptor: AnnotationDescriptor,
    message: String,
    cause: Throwable? = null
  ) : this(message, cause = cause, element = annotationDescriptor.identifier)

  @OptIn(com.squareup.anvil.annotations.ExperimentalAnvilApi::class)
  constructor(
    classReference: ClassReference,
    message: String,
    cause: Throwable? = null
  ) : this(message, cause = cause, element = classReference.findPsi())

  @OptIn(com.squareup.anvil.annotations.ExperimentalAnvilApi::class)
  constructor(
    declarationDescriptor: DeclarationDescriptor,
    message: String,
    cause: Throwable? = null
  ) : this(message, cause = cause, element = declarationDescriptor.findPsi())
}

val ClassDescriptor.identifier: PsiElement?
  get() = (findPsi() as? PsiNameIdentifierOwner)?.identifyingElement

val AnnotationDescriptor.identifier: PsiElement?
  get() = (source as? KotlinSourceElement)?.psi

inline fun require(
  value: Boolean,
  classReference: ClassReference,
  cause: Throwable? = null,
  lazyMessage: () -> String
) {
  contract {
    returns() implies value
  }
  if (!value) {
    throw TangleCompilationException(lazyMessage(), cause, classReference.findPsi())
  }
}

@PublishedApi
internal fun ClassReference.findPsi() = when (this) {
  is Descriptor -> clazz.findPsi() as PsiElement
  is Psi -> clazz
}

inline fun require(
  value: Boolean,
  declarationDescriptor: DeclarationDescriptor,
  cause: Throwable? = null,
  lazyMessage: () -> String
) {
  contract {
    returns() implies value
  }
  if (!value) {
    throw TangleCompilationException(lazyMessage(), cause, declarationDescriptor.findPsi())
  }
}

@JvmName("requirePsi")
inline fun require(
  value: Boolean,
  psi: () -> PsiElement,
  cause: Throwable? = null,
  lazyMessage: () -> String
) {
  contract {
    returns() implies value
  }
  if (!value) {
    throw TangleCompilationException(lazyMessage(), cause, psi())
  }
}

inline fun require(
  value: Boolean,
  declarationDescriptor: () -> DeclarationDescriptor,
  cause: Throwable? = null,
  lazyMessage: () -> String
) {
  contract {
    returns() implies value
  }
  if (!value) {
    throw TangleCompilationException(lazyMessage(), cause, declarationDescriptor().findPsi())
  }
}

inline fun require(
  value: Boolean,
  annotationDescriptor: AnnotationDescriptor,
  cause: Throwable? = null,
  lazyMessage: () -> String
) {
  contract {
    returns() implies value
  }
  if (!value) {
    throw TangleCompilationException(lazyMessage(), cause, annotationDescriptor.identifier)
  }
}

inline fun require(
  value: Boolean,
  element: PsiElement? = null,
  cause: Throwable? = null,
  lazyMessage: () -> String
) {
  contract {
    returns() implies value
  }
  if (!value) {
    throw TangleCompilationException(lazyMessage(), cause, element)
  }
}
