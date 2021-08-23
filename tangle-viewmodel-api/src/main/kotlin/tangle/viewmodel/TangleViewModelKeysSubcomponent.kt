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

import androidx.lifecycle.ViewModel
import tangle.inject.InternalTangleApi

/**
 * Used to provide [viewModelKeys] for use in the
 * [TangleViewModelFactory][tangle.viewmodel.internal.TangleViewModelFactory]
 *
 * @since 0.10.0
 */
public interface TangleViewModelKeysSubcomponent {

  /**
   * Copy of all the keys contained in [TangleViewModelProviderMap].
   * [TangleViewModelProviderMap] is only provided by [TangleViewModelMapSubcomponent],
   * and a new subcomponent needs to be created for each viewModel injection, so it's inefficient
   * to create the object before we know if the map holds the [ViewModel].
   * The [TangleViewModelFactory][tangle.viewmodel.internal.TangleViewModelFactory] checks this
   * Set in order to determine whether the map holds a particular ViewModel type.
   *
   * @since 0.10.0
   */
  @OptIn(InternalTangleApi::class)
  @get:TangleViewModelProviderMap.KeySet
  public val viewModelKeys: Set<Class<out ViewModel>>

  /** @suppress */
  public interface Factory {
    /** @suppress */
    public fun create(): TangleViewModelKeysSubcomponent
  }
}
