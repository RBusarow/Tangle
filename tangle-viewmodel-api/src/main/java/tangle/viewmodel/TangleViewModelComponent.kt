package tangle.viewmodel

import androidx.lifecycle.ViewModel
import tangle.inject.InternalTangleApi

/**
 * Used to provide a way of retrieving [TangleViewModelSubcomponent.Factory]
 * from a main app [Component][dagger.Component].
 *
 * Also provides [viewModelKeys] for use in the [TangleViewModelFactory]
 *
 * @since 0.10.0
 */
public interface TangleViewModelComponent {
  /**
   * Referenced by [TangleViewModelFactory] in order to create
   * a scoped [TangleViewModelSubcomponent], in order to access the [TangleViewModelProviderMap].
   *
   * @since 0.10.0
   */
  public val tangleViewModelSubcomponentFactory: TangleViewModelSubcomponent.Factory

  /**
   * Copy of all the keys contained in [TangleViewModelProviderMap].
   * [TangleViewModelProviderMap] is only provided by [TangleViewModelSubcomponent],
   * and a new subcomponent needs to be created for each viewModel injection, so it's inefficient
   * to create the object before we know if the map holds the [ViewModel].
   * The [TangleViewModelFactory] checks this Set in order to determine whether the map
   * holds a particular ViewModel type.
   *
   * @since 0.10.0
   */
  @OptIn(InternalTangleApi::class)
  @get:TangleViewModelProviderMap.KeySet
  public val viewModelKeys: Set<Class<out ViewModel>>
}
