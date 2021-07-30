package tangle.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import tangle.inject.InternalTangleApi
import tangle.viewmodel.internal.TangleViewModelFactory
import javax.inject.Provider

/**
 * Internal-use [Subcomponent][dagger.Subcomponent] which provides a map
 * of [ViewModel]s to the [TangleViewModelFactory].
 *
 * A new Subcomponent is created each time a `ViewModel` is injected,
 * and the Subcomponent is scoped to the corresponding [LifecycleOwner][androidx.lifecycle.LifecycleOwner].
 *
 * @since 0.10.0
 */
public interface TangleViewModelMapSubcomponent {
  /** @suppress */
  @OptIn(InternalTangleApi::class)
  @get:TangleViewModelProviderMap
  public val viewModelProviderMap: Map<Class<*>, Provider<@JvmSuppressWildcards ViewModel>>

  /** @suppress */
  public interface Factory {
    /** @suppress */
    public fun create(savedStateHandle: SavedStateHandle): TangleViewModelMapSubcomponent
  }
}
