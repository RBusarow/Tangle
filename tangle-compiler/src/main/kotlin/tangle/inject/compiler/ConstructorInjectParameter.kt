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

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName

sealed class Parameter {
  abstract val name: String
  abstract val typeName: TypeName
  abstract val providerTypeName: ParameterizedTypeName
  abstract val lazyTypeName: ParameterizedTypeName
  abstract val isWrappedInProvider: Boolean
  abstract val isWrappedInLazy: Boolean
  abstract val tangleParamName: String?
  abstract val qualifiers: List<AnnotationSpec>

  val isTangleParam: Boolean
    get() = tangleParamName != null
}

data class ConstructorInjectParameter(
  override val name: String,
  override val typeName: TypeName,
  override val providerTypeName: ParameterizedTypeName,
  override val lazyTypeName: ParameterizedTypeName,
  override val isWrappedInProvider: Boolean,
  override val isWrappedInLazy: Boolean,
  override val tangleParamName: String?,
  override val qualifiers: List<AnnotationSpec>
) : Parameter()

data class MemberInjectParameter(

  override val name: String,
  override val typeName: TypeName,
  override val providerTypeName: ParameterizedTypeName,
  override val lazyTypeName: ParameterizedTypeName,
  override val isWrappedInProvider: Boolean,
  override val isWrappedInLazy: Boolean,
  override val tangleParamName: String?,
  override val qualifiers: List<AnnotationSpec>,
  val memberInjectorClass: ClassName
) : Parameter()

fun List<ConstructorInjectParameter>.uniqueName(base: String, attempt: Int = 0): String {
  return map { it.name }.uniqueName(base, attempt)
}

@JvmName("uniqueNameStrings")
fun List<String>.uniqueName(base: String, attempt: Int = 0): String {
  val maybeName = if (attempt == 0) base else "$base$attempt"
  val unique = none { it == maybeName }
  return if (unique) maybeName else uniqueName(base, attempt + 1)
}
