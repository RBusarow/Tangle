package tangle.inject.compiler

import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeComponent
import com.squareup.anvil.annotations.MergeSubcomponent
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import dagger.*
import dagger.internal.Factory
import dagger.internal.InstanceFactory
import dagger.multibindings.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

object ClassNames {

  val androidContext = ClassName("android.content", "Context")
  val string = String::class.asClassName()

  val internalTangleApi = ClassName("tangle.inject", "InternalTangleApi")

  val tangleScope = ClassName("tangle.inject.internal", "TangleScope")
  val tangleAppScope = ClassName("tangle.inject.internal", "TangleAppScope")

  val provider = Provider::class.asClassName()

  val binds = Binds::class.asClassName()
  val bindsInstance = BindsInstance::class.asClassName()
  val classKey = ClassKey::class.asClassName()
  val contributesTo = ContributesTo::class.asClassName()
  val daggerFactory = Factory::class.asClassName()
  val intoMap = IntoMap::class.asClassName()
  val intoSet = IntoSet::class.asClassName()
  val mergeComponent = MergeComponent::class.asClassName()
  val mergeSubcomponent = MergeSubcomponent::class.asClassName()
  val module = Module::class.asClassName()
  val multibinds = Multibinds::class.asClassName()
  val named = Named::class.asClassName()
  val provides = Provides::class.asClassName()
  val stringKey = StringKey::class.asClassName()
  val subcomponentFactory = Subcomponent.Factory::class.asClassName()

  val instanceFactory = InstanceFactory::class.asClassName()

  val inject = Inject::class.asClassName()
  val jvmStatic = JvmStatic::class.asClassName()

  val bundle = ClassName("android.os", "Bundle")
  val iBinder = ClassName("android.os", "IBinder")
  val parcelable = ClassName("android.os", "Parcelable")
  val size = ClassName("android.util", "Size")
  val sizeF = ClassName("android.util", "SizeF")
}
