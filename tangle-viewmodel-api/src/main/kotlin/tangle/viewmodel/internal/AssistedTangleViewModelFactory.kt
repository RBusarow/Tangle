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
import androidx.savedstate.SavedStateRegistryOwner
import tangle.inject.InternalTangleApi
import tangle.inject.TangleGraph
import tangle.viewmodel.TangleViewModelComponent
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
@InternalTangleApi
public class AssistedTangleViewModelFactory<VM : ViewModel, F : Any>(
  private val owner: SavedStateRegistryOwner,
  private val defaultArgs: Bundle?,
  private val vmClass: KClass<VM>,
  private val factoryClass: KClass<F>,
  private val factory: F.() -> VM
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
  override fun <T : ViewModel> create(
    key: String,
    modelClass: Class<T>,
    handle: SavedStateHandle
  ): T {

    val subcomponent = TangleGraph.get<TangleViewModelComponent>()
      .tangleViewModelMapSubcomponentFactory
      .create(handle)

    val factoryImpl = (subcomponent.viewModelFactoryMap[factoryClass.java]?.get() as? F)
      ?: throw IllegalStateException(
        "Tangle can't find a factory of type $factoryClass, " +
          "which is necessary in order to create an assisted $vmClass."
      )
    return factory(factoryImpl) as T
  }
}
