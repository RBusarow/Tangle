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

package tangle.inject.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import tangle.inject.InternalTangleApi
import tangle.viewmodel.TangleViewModelFactory
import tangle.viewmodel.tangle

/**
 * Returns an existing [VMInject][tangle.viewmodel.VMInject]-annotated [ViewModel]
 * or creates a new one scoped to the current navigation graph present on
 * the NavController back stack.
 */
@Composable
inline fun <reified VM : ViewModel> tangle(
  viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
  }
): VM {
  return if (viewModelStoreOwner is NavBackStackEntry) {
    val factory = TangleViewModelFactory(viewModelStoreOwner)
    viewModel(viewModelStoreOwner, factory = factory)
  } else {
    viewModel()
  }
}

@PublishedApi
@OptIn(InternalTangleApi::class)
internal fun TangleViewModelFactory(
  navBackStackEntry: NavBackStackEntry
): ViewModelProvider.Factory {
  return TangleViewModelFactory(
    navBackStackEntry,
    navBackStackEntry.arguments,
    navBackStackEntry.defaultViewModelProviderFactory,
  )
}
