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
import tangle.inject.InternalTangleApi
import tangle.viewmodel.internal.TangleViewModelFactory
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Equivalent to the Androidx ktx `by viewModels()` delegate.
 *
 * @sample tangle.viewmodel.samples.TangleFragmentDelegateSample.byTangleViewModelSample
 * @return lazy [ViewModel] instance of the specified type, injected by Tangle/Anvil/Dagger
 * @since 0.11.0
 */
@OptIn(InternalTangleApi::class)
public inline fun <reified VM : ViewModel> Fragment.tangleViewModel(): Lazy<VM> =
  lazy(mode = NONE) {
    val viewModelFactory = TangleViewModelFactory(
      owner = this,
      defaultArgs = arguments,
      defaultFactory = defaultViewModelProviderFactory
    )

    viewModels<VM>(factoryProducer = { viewModelFactory }).value
  }

/**
 * Equivalent to the Androidx ktx `by viewModels()` delegate.
 *
 * @sample tangle.viewmodel.samples.TangleFragmentDelegateSample.byTangleViewModelSample
 * @return lazy [ViewModel] instance of the specified type, injected by Tangle/Anvil/Dagger
 * @since 0.11.0
 */
@OptIn(InternalTangleApi::class)
public inline fun <reified VM : ViewModel> ComponentActivity.tangleViewModel(): Lazy<VM> =
  lazy(mode = NONE) {
    val viewModelFactory = TangleViewModelFactory(
      owner = this,
      defaultArgs = intent.extras,
      defaultFactory = defaultViewModelProviderFactory
    )

    viewModels<VM>(factoryProducer = { viewModelFactory }).value
  }
