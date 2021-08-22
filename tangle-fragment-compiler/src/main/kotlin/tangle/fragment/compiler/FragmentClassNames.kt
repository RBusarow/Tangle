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
