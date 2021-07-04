package tangle.inject.api

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import tangle.inject.annotations.InternalTangleApi
import tangle.inject.api.internal.TangleViewModelFactory
import kotlin.LazyThreadSafetyMode.NONE

@OptIn(InternalTangleApi::class)
public inline fun <reified VM : ViewModel> Fragment.tangle(): Lazy<VM> = lazy(mode = NONE) {
  val viewModelFactory = TangleViewModelFactory(
    owner = this,
    defaultArgs = arguments,
    defaultFactory = defaultViewModelProviderFactory
  )

  viewModels<VM>(factoryProducer = { viewModelFactory }).value
}
