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

import com.squareup.anvil.compiler.api.AnvilCompilationException
import org.jetbrains.kotlin.codegen.CompilationException
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiNameIdentifierOwner
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement
import kotlin.contracts.contract

class TangleCompilationException(
  message: String,
  cause: Throwable? = null,
  element: PsiElement? = null
) : CompilationException(message, cause, element) {
  constructor(
    classDescriptor: ClassDescriptor,
    message: String,
    cause: Throwable? = null
  ) : this(message, cause = cause, element = classDescriptor.identifier)

  constructor(
    annotationDescriptor: AnnotationDescriptor,
    message: String,
    cause: Throwable? = null
  ) : this(message, cause = cause, element = annotationDescriptor.identifier)
}

internal val ClassDescriptor.identifier: PsiElement?
  get() = (findPsi() as? PsiNameIdentifierOwner)?.identifyingElement

internal val AnnotationDescriptor.identifier: PsiElement?
  get() = (source as? KotlinSourceElement)?.psi

internal inline fun require(
  value: Boolean,
  classDescriptor: ClassDescriptor,
  cause: Throwable? = null,
  lazyMessage: () -> String
) {
  contract {
    returns() implies value
  }
  if (!value) {
    throw TangleCompilationException(lazyMessage(), cause, classDescriptor.identifier)
  }
}

internal inline fun require(
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

internal inline fun require(
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

internal fun <T> rebrandAnvilException(action: () -> T): T {
  return try {
    action()
  } catch (anvil: AnvilCompilationException) {
    throw TangleCompilationException(anvil.message ?: "", anvil)
  }
}
