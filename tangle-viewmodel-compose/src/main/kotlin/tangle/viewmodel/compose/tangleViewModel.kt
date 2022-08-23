/*
 * Copyright (C) 2022 Rick Busarow
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

package tangle.viewmodel.compose

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.savedstate.SavedStateRegistryOwner
import tangle.inject.InternalTangleApi
import tangle.viewmodel.internal.TangleViewModelFactory

/**
 * Returns an existing [VMInject][tangle.viewmodel.VMInject]-annotated [ViewModel]
 * or creates a new one scoped to the current navigation graph present on
 * the NavController back stack.
 *
 * @since 0.10.0
 */
@Composable
@OptIn(InternalTangleApi::class)
public inline fun <reified VM : ViewModel> tangleViewModel(
  viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
  }
): VM {
  return when {
    viewModelStoreOwner is NavBackStackEntry -> {
      val factory = TangleViewModelFactory(viewModelStoreOwner)
      viewModel(viewModelStoreOwner, factory = factory)
    }
    viewModelStoreOwner is ComponentActivity -> {

      val args = viewModelStoreOwner.intent.extras
      val defaultFactory = viewModelStoreOwner.defaultViewModelProviderFactory

      val factory = TangleViewModelFactory(viewModelStoreOwner, args, defaultFactory)
      viewModel(viewModelStoreOwner, factory = factory)
    }
    viewModelStoreOwner is SavedStateRegistryOwner &&
      viewModelStoreOwner is HasDefaultViewModelProviderFactory -> {

      val args = currentFragmentOrNull(viewModelStoreOwner)?.arguments
      val defaultFactory = viewModelStoreOwner.defaultViewModelProviderFactory

      val factory = TangleViewModelFactory(viewModelStoreOwner, args, defaultFactory)
      viewModel(viewModelStoreOwner, factory = factory)
    }
    else -> {
      viewModel()
    }
  }
}

@Composable
@PublishedApi
internal fun currentFragmentOrNull(
  viewModelStoreOwner: ViewModelStoreOwner
): Fragment? {
  val view = LocalView.current
  return try {
    FragmentManager.findFragment<Fragment>(view).takeIf {
      // let's make sure the fragment for a view is the correct store owner
      it.viewLifecycleOwner == viewModelStoreOwner
    }
  } catch (_: IllegalStateException) {
    // current scope is not a fragment
    null
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
    navBackStackEntry.defaultViewModelProviderFactory
  )
}
