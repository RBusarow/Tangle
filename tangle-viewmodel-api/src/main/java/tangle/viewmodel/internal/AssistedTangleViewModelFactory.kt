package tangle.viewmodel.internal

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import tangle.inject.InternalTangleApi
import tangle.viewmodel.TangleGraph
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
@InternalTangleApi
public class AssistedTangleViewModelFactory<VM : ViewModel, F : Any>(
  private val owner: SavedStateRegistryOwner,
  private val defaultArgs: Bundle?,
  private val vmClass: KClass<VM>,
  private val fClass: KClass<F>
) {

  public fun create(factory: F.() -> VM): VM =
    object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
      override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
      ): T {

        val subcomponent = TangleGraph.tangleViewModelSubcomponentFactory()
          .create(handle)
        val factoryImpl = (subcomponent.viewModelFactoryMap[fClass.java]?.get() as? F)
          ?: throw IllegalStateException(
            "Tangle can't find a factory of type $fClass, " +
              "which is necessary in order to create an assisted $vmClass."
          )
        return factory(factoryImpl) as T
      }
    }.create(vmClass.java)
}
