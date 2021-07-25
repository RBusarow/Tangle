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
   * The [TangleViewModelFactory][tangle.viewmodel.internal.TangleViewModelFactory] checks this Set in order to determine whether the map
   * holds a particular ViewModel type.
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
