package tangle.inject.api

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import tangle.inject.annotations.internal.InternalTangleApi
import tangle.inject.annotations.internal.TangleViewModelProviderMap
import javax.inject.Provider

public interface TangleViewModelSubcomponent {
  @OptIn(InternalTangleApi::class)
  @get:TangleViewModelProviderMap
  public val viewModelProviderMap:
    Map<Class<out ViewModel>, Provider<@JvmSuppressWildcards ViewModel>>

  public interface Factory {
    public fun create(savedStateHandle: SavedStateHandle):
      TangleViewModelSubcomponent
  }
}
