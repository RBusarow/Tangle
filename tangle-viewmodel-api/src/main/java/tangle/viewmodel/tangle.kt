package tangle.viewmodel

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import tangle.inject.InternalTangleApi
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Equivalent to the Androidx ktx `by viewModels()` delegate.
 *
 * ```
 * class MyFragment: Fragment() {
 *    val viewModel: MyViewModel by tangle()
 * }
 * ```
 *
 * @return lazy [ViewModel] instance of the specified type, injected by Tangle/Anvil/Dagger
 */
@OptIn(InternalTangleApi::class)
public inline fun <reified VM : ViewModel> Fragment.tangle(): Lazy<VM> = lazy(mode = NONE) {
  val viewModelFactory = TangleViewModelFactory(
    owner = this,
    defaultArgs = arguments,
    defaultFactory = defaultViewModelProviderFactory
  )

  viewModels<VM>(factoryProducer = { viewModelFactory }).value
}
