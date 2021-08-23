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

package tangle.fragment.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.jvm.jvmSuppressWildcards
import tangle.inject.compiler.ClassNames

val ClassNames.tangleFragmentFactory
  get() = ClassName("tangle.fragment", "TangleFragmentFactory")

val ClassNames.tangleFragmentKey
  get() = ClassName("tangle.fragment", "FragmentKey")
val ClassNames.tangleFragmentProviderMap
  get() = ClassName("tangle.fragment", "TangleFragmentProviderMap")

val ClassNames.androidxFragment
  get() = ClassName("androidx.fragment.app", "Fragment")

private val ClassNames.javaClassOutFragment: ParameterizedTypeName
  get() = Class::class.asClassName()
    .parameterizedBy(TypeVariableName("out·${androidxFragment.canonicalName}"))

val ClassNames.fragmentMap: ParameterizedTypeName
  get() = Map::class.asClassName()
    .parameterizedBy(
      javaClassOutFragment,
      androidxFragment.jvmSuppressWildcards()
    )

val ClassNames.fragmentProviderMap: ParameterizedTypeName
  get() = Map::class.asClassName()
    .parameterizedBy(
      javaClassOutFragment,
      ClassNames.provider
        .parameterizedBy(androidxFragment.jvmSuppressWildcards())
        .jvmSuppressWildcards()
    )
