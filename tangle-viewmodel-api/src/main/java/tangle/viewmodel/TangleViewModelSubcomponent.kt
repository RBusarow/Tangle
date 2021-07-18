package tangle.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import tangle.inject.InternalTangleApi
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
