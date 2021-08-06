package tangle.viewmodel.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
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
public inline fun <reified VM : ViewModel> tangleViewModel(
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
