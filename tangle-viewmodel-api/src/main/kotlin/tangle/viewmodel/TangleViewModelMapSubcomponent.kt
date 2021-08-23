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

package tangle.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import tangle.inject.InternalTangleApi
import tangle.viewmodel.internal.TangleViewModelFactory
import javax.inject.Provider

/**
 * Internal-use [Subcomponent][dagger.Subcomponent] which provides a map
 * of [ViewModel]s to the [TangleViewModelFactory].
 *
 * A new Subcomponent is created each time a `ViewModel` is injected,
 * and the Subcomponent is scoped to the corresponding [LifecycleOwner][androidx.lifecycle.LifecycleOwner].
 *
 * @since 0.10.0
 */
public interface TangleViewModelMapSubcomponent {
  /** @suppress */
  @OptIn(InternalTangleApi::class)
  @get:TangleViewModelProviderMap
  public val viewModelProviderMap: Map<Class<*>, Provider<@JvmSuppressWildcards ViewModel>>

  /** @suppress */
  public interface Factory {
    /** @suppress */
    public fun create(savedStateHandle: SavedStateHandle): TangleViewModelMapSubcomponent
  }
}
