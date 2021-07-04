package tangle.inject.compiler

import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeComponent
import com.squareup.anvil.annotations.MergeSubcomponent
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import dagger.*
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import tangle.inject.annotations.TangleScope
import javax.inject.Inject
import javax.inject.Provider

internal object ClassNames {

  val tangleComponent = ClassName("tangle.inject.api", "TangleComponent")
  val tangleSubcomponent = ClassName("tangle.inject.api", "TangleSubcomponent")
  val tangleSubcomponentFactory =
    ClassName("tangle.inject.api", "TangleSubcomponent", "Factory")
  val tangleViewModelKey = ClassName("tangle.inject.api", "ViewModelKey")
  val tangleViewModelProviderMap =
    ClassName("tangle.inject.annotations", "TangleViewModelProviderMap")
  val tangleViewModelProviderMapKeySet =
    ClassName("tangle.inject.annotations", "TangleViewModelProviderMap", "KeySet")
  val tangleScope = TangleScope::class.asClassName()

  val androidxViewModel = ClassName("androidx.lifecycle", "ViewModel")
  val androidxSavedStateHandle = ClassName("androidx.lifecycle", "SavedStateHandle")

  val contributesTo = ContributesTo::class.asClassName()
  val mergeComponent = MergeComponent::class.asClassName()
  val mergeSubomponent = MergeSubcomponent::class.asClassName()
  val javaClassOutVM = Class::class.asClassName()
    .parameterizedBy(TypeVariableName("out ${androidxViewModel.canonicalName}"))
  val binds = Binds::class.asClassName()
  val bindsInstance = BindsInstance::class.asClassName()
  val subcomponentFactory = Subcomponent.Factory::class.asClassName()
  val intoMap = IntoMap::class.asClassName()
  val intoSet = IntoSet::class.asClassName()
  val provides = Provides::class.asClassName()
  val module = Module::class.asClassName()

  val inject = Inject::class.asClassName()
  val jvmStatic = JvmStatic::class.asClassName()
  val providerSavedStateHandle = Provider::class.asClassName()
    .parameterizedBy(androidxSavedStateHandle)
}
