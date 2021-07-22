package tangle.inject.compiler

import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeComponent
import com.squareup.anvil.annotations.MergeSubcomponent
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.jvm.jvmSuppressWildcards
import dagger.*
import dagger.internal.Factory
import dagger.internal.InstanceFactory
import dagger.multibindings.*
import javax.inject.Inject
import javax.inject.Provider

public object ClassNames {

  public val tangleViewModelComponent: ClassName =
    ClassName("tangle.viewmodel", "TangleViewModelComponent")
  public val tangleViewModelSubcomponent: ClassName =
    ClassName("tangle.viewmodel", "TangleViewModelSubcomponent")
  public val tangleViewModelSubcomponentFactory: ClassName =
    tangleViewModelSubcomponent.nestedClass("Factory")

  public val tangleViewModelProviderMap: ClassName =
    ClassName("tangle.viewmodel", "TangleViewModelProviderMap")
  public val tangleViewModelProviderMapKeySet: ClassName =
    ClassName("tangle.viewmodel", "TangleViewModelProviderMap", "KeySet")
  public val tangleFragmentFactory: ClassName =
    ClassName("tangle.fragment", "TangleFragmentFactory")
  public val tangleFragmentSubcomponent: ClassName =
    ClassName("tangle.fragment", "TangleFragmentSubcomponent")
  public val tangleFragmentSubcomponentFactory: ClassName =
    tangleFragmentSubcomponent.nestedClass("Factory")
  public val tangleFragmentKey: ClassName = ClassName("tangle.fragment", "FragmentKey")
  public val tangleFragmentProviderMap: ClassName =
    ClassName("tangle.fragment", "TangleFragmentProviderMap")

  public val internalTangleApi: ClassName = ClassName("tangle.inject", "InternalTangleApi")
  public val optIn: ClassName = ClassName("kotlin", "OptIn")

  public val tangleScope: ClassName = ClassName("tangle.inject", "TangleScope")

  public val androidxViewModel: ClassName = ClassName("androidx.lifecycle", "ViewModel")
  public val androidxFragment: ClassName = ClassName("androidx.fragment.app", "Fragment")
  public val androidxFragmentFactory: ClassName =
    ClassName("androidx.fragment.app", "FragmentFactory")
  public val androidxSavedStateHandle: ClassName =
    ClassName("androidx.lifecycle", "SavedStateHandle")

  public val javaClassOutFragment: ParameterizedTypeName = Class::class.asClassName()
    .parameterizedBy(TypeVariableName("out·${androidxFragment.canonicalName}"))
  public val javaClassOutVM: ParameterizedTypeName = Class::class.asClassName()
    .parameterizedBy(TypeVariableName("out·${androidxViewModel.canonicalName}"))

  public val fragmentMap: ParameterizedTypeName = Map::class.asClassName().parameterizedBy(
    javaClassOutFragment,
    androidxFragment.jvmSuppressWildcards()
  )
  public val provider: ClassName = Provider::class.asClassName()
  public val fragmentProviderMap: ParameterizedTypeName = Map::class.asClassName().parameterizedBy(
    javaClassOutFragment,
    provider.parameterizedBy(androidxFragment.jvmSuppressWildcards()).jvmSuppressWildcards()
  )

  public val binds: ClassName = Binds::class.asClassName()
  public val bindsInstance: ClassName = BindsInstance::class.asClassName()
  public val classKey: ClassName = ClassKey::class.asClassName()
  public val contributesTo: ClassName = ContributesTo::class.asClassName()
  public val daggerFactory: ClassName = Factory::class.asClassName()
  public val intoMap: ClassName = IntoMap::class.asClassName()
  public val intoSet: ClassName = IntoSet::class.asClassName()
  public val mergeComponent: ClassName = MergeComponent::class.asClassName()
  public val mergeSubomponent: ClassName = MergeSubcomponent::class.asClassName()
  public val module: ClassName = Module::class.asClassName()
  public val multibinds: ClassName = Multibinds::class.asClassName()
  public val provides: ClassName = Provides::class.asClassName()
  public val stringKey: ClassName = StringKey::class.asClassName()
  public val subcomponentFactory: ClassName = Subcomponent.Factory::class.asClassName()

  public val instanceFactory: ClassName = InstanceFactory::class.asClassName()

  public val inject: ClassName = Inject::class.asClassName()
  public val jvmStatic: ClassName = JvmStatic::class.asClassName()
  public val providerSavedStateHandle: ParameterizedTypeName = Provider::class.asClassName()
    .parameterizedBy(androidxSavedStateHandle)

  public val bundle: ClassName = ClassName("android.os", "Bundle")
  public val iBinder: ClassName = ClassName("android.os", "IBinder")
  public val parcelable: ClassName = ClassName("android.os", "Parcelable")
  public val size: ClassName = ClassName("android.util", "Size")
  public val sizeF: ClassName = ClassName("android.util", "SizeF")
}
