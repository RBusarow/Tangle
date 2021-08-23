package tangle.viewmodel.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.jvm.jvmSuppressWildcards
import tangle.inject.compiler.ClassNames
import javax.inject.Provider

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

private val ClassNames.javaClassWildcard
  get() = Class::class.asClassName()
    .parameterizedBy(TypeVariableName("*"))

val ClassNames.tangleViewModelFactoryMap
  get() = ClassName("tangle.viewmodel", "TangleViewModelFactoryMap")

val ClassNames.viewModelClassSet
  get() = Set::class.asClassName()
    .parameterizedBy(
      javaClassOutVM
    )
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
