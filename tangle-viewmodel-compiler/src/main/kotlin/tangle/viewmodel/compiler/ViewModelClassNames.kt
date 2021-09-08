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

package tangle.viewmodel.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.jvm.jvmSuppressWildcards
import tangle.inject.compiler.ClassNames
import javax.inject.Provider

val ClassNames.tangleViewModelScope: ClassName
  get() = ClassName("tangle.viewmodel.internal", "TangleViewModelScope")

val ClassNames.tangleViewModelComponent
  get() =
    ClassName("tangle.viewmodel", "TangleViewModelComponent")
val ClassNames.tangleViewModelMapSubcomponent
  get() =
    ClassName("tangle.viewmodel", "TangleViewModelMapSubcomponent")
val ClassNames.tangleViewModelMapSubcomponentFactory
  get() =
    tangleViewModelMapSubcomponent.nestedClass("Factory")
val ClassNames.tangleViewModelKeysSubcomponent
  get() =
    ClassName("tangle.viewmodel", "TangleViewModelKeysSubcomponent")
val ClassNames.tangleViewModelKeysSubcomponentFactory
  get() =
    tangleViewModelKeysSubcomponent.nestedClass("Factory")

val ClassNames.tangleViewModelProviderMap
  get() =
    ClassName("tangle.viewmodel", "TangleViewModelProviderMap")

val ClassNames.tangleViewModelProviderMapKeySet
  get() =
    ClassName("tangle.viewmodel", "TangleViewModelProviderMap", "KeySet")

val ClassNames.androidxViewModel
  get() = ClassName("androidx.lifecycle", "ViewModel")
val ClassNames.androidxSavedStateHandle
  get() =
    ClassName("androidx.lifecycle", "SavedStateHandle")

val ClassNames.providerSavedStateHandle: ParameterizedTypeName
  get() = Provider::class.asClassName()
    .parameterizedBy(androidxSavedStateHandle)
val ClassNames.javaClassOutVM
  get() = Class::class.asClassName()
    .parameterizedBy(TypeVariableName("out·${androidxViewModel.canonicalName}"))

val ClassNames.tangleViewModelFactoryMap
  get() = ClassName("tangle.viewmodel", "TangleViewModelFactoryMap")

val ClassNames.viewModelClassSet
  get() = Set::class.asClassName()
    .parameterizedBy(javaClassOutVM)

val ClassNames.viewModelMap
  get() = Map::class.asClassName()
    .parameterizedBy(
      javaClassWildcard,
      androidxViewModel.jvmSuppressWildcards()
    )

val ClassNames.any
  get() = Any::class.asClassName()
val ClassNames.anyMap: ParameterizedTypeName
  get() = Map::class.asClassName().parameterizedBy(
    ClassNames.javaClassWildcard,
    Any::class.asClassName().jvmSuppressWildcards()
  )
