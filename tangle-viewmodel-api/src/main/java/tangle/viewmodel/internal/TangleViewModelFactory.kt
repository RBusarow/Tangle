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

package tangle.viewmodel.internal

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import dagger.multibindings.ClassKey
import tangle.inject.InternalTangleApi
import tangle.viewmodel.TangleGraph
import kotlin.reflect.KClass

public inline fun <reified VM : ViewModel, reified F : Any> tangleViewModel(
  noinline factory: F.() -> VM
): VM {

  return tangleViewModel(VM::class, F::class, factory)
}

@PublishedApi
internal fun <VM : ViewModel, F : Any> tangleViewModel(
  vmClass: KClass<VM>, fClass: KClass<F>,
  factory: F.() -> VM
): VM {

  val providerFactory = object : AbstractSavedStateViewModelFactory(this, arguments) {
    override fun <T : ViewModel> create(
      key: String, modelClass: Class<T>, handle: SavedStateHandle
    ): T {

      val subcomponent = TangleGraph
        .tangleViewModelSubcomponentFactory()
        .create(handle)
      val factoryImpl =
    }
  }
}

/** @suppress */
@InternalTangleApi
public class TangleViewModelFactory(
  owner: SavedStateRegistryOwner,
  defaultArgs: Bundle?,
  private val tangleViewModelKeys: Set<Class<*>>,
  private val delegateFactory: ViewModelProvider.Factory
) : ViewModelProvider.Factory {

  /** @suppress */
  private val tangleFactory: AbstractSavedStateViewModelFactory =
    object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
      override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
      ): T {
        val subcomponent = TangleGraph
          .tangleViewModelSubcomponentFactory()
          .create(handle)

        val provider = subcomponent.viewModelProviderMap[modelClass]
          ?: throw IllegalStateException(
            "A ${ClassKey::class.java.simpleName} exists for ${modelClass.name}, " +
              "but it can't be found in the map.\n\n" +
              "Bound viewModels:\n" +
              subcomponent.viewModelProviderMap.keys.joinToString {
                it.canonicalName?.toString() ?: "null"
              }
          )
        @Suppress("UNCHECKED_CAST")
        return provider.get() as T
      }
    }

  /** @suppress */
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return if (tangleViewModelKeys.contains(modelClass)) {
      tangleFactory.create(modelClass)
    } else {
      delegateFactory.create(modelClass)
    }
  }

  /** @suppress */
  public companion object {

    /** @suppress */
    public operator fun invoke(
      owner: SavedStateRegistryOwner,
      defaultArgs: Bundle?,
      defaultFactory: ViewModelProvider.Factory
    ): ViewModelProvider.Factory {

      return TangleViewModelFactory(
        owner,
        defaultArgs,
        TangleGraph.tangleViewModelKeys,
        defaultFactory
      )
    }
  }
}
