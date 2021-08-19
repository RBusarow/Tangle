package tangle.viewmodel

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
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
public inline fun <reified VM : ViewModel> Fragment.tangleViewModel(): Lazy<VM> =
  lazy(mode = NONE) {
    val viewModelFactory = TangleViewModelFactory(
      owner = this,
      defaultArgs = arguments,
      defaultFactory = defaultViewModelProviderFactory
    )

    viewModels<VM>(factoryProducer = { viewModelFactory }).value
  }

@OptIn(InternalTangleApi::class)
public inline fun <reified VM : ViewModel, reified F : Any> Fragment.tangleViewModel(
  noinline factory: F.() -> VM
): VM {

  return AssistedTangleViewModelFactory(
    owner = this,
    defaultArgs = arguments,
    vmClass = VM::class,
    fClass = F::class
  ).create(factory)
}

/**
 * Equivalent to the Androidx ktx `by viewModels()` delegate.
 *
 * @sample samples.TangleFragmentDelegateSample.byTangleViewModelSample
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

@OptIn(InternalTangleApi::class)
public inline fun <reified VM : ViewModel, reified F : Any> ComponentActivity.tangleViewModel(
  noinline factory: F.() -> VM
): VM {

  return AssistedTangleViewModelFactory(
    owner = this,
    defaultArgs = intent.extras,
    vmClass = VM::class,
    fClass = F::class
  ).create(factory)
}
