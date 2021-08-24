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

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import tangle.inject.InternalTangleApi
import tangle.viewmodel.internal.AssistedTangleViewModelFactory
import tangle.viewmodel.internal.TangleViewModelFactory
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Equivalent to the Androidx ktx `by viewModels()` delegate.
 *
 * @sample samples.TangleFragmentDelegateSample.byTangleViewModelSample
 * @return lazy [ViewModel] instance of the specified type, injected by Tangle/Anvil/Dagger
 * @since 0.11.0
 */
@OptIn(InternalTangleApi::class)
public inline fun <reified VM : ViewModel> Fragment.tangleViewModel(
  savedStateRegistryOwner: SavedStateRegistryOwner = this,
  defaultFactory: ViewModelProvider.Factory = defaultViewModelProviderFactory
): Lazy<VM> =
  lazy(mode = NONE) {
    val viewModelFactory = TangleViewModelFactory(
      owner = savedStateRegistryOwner,
      defaultArgs = arguments,
      defaultFactory = defaultFactory
    )

    viewModels<VM>(factoryProducer = { viewModelFactory }).value
  }

/**
 * Equivalent to the Androidx ktx `by viewModels()` delegate.
 *
 * @sample samples.TangleFragmentDelegateSample.byTangleViewModelSample
 * @return lazy [ViewModel] instance of the specified type, injected by Tangle/Anvil/Dagger
 * @since 0.11.0
 */
@OptIn(InternalTangleApi::class)
public inline fun <reified VM : ViewModel> ComponentActivity.tangleViewModel(
  savedStateRegistryOwner: SavedStateRegistryOwner = this,
  defaultFactory: ViewModelProvider.Factory = defaultViewModelProviderFactory
): Lazy<VM> =
  lazy(mode = NONE) {
    val viewModelFactory = TangleViewModelFactory(
      owner = savedStateRegistryOwner,
      defaultArgs = intent.extras,
      defaultFactory = defaultFactory
    )

    viewModels<VM>(factoryProducer = { viewModelFactory }).value
  }
