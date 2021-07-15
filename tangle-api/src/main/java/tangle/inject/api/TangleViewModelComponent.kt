package tangle.inject.api

import androidx.lifecycle.ViewModel
import tangle.inject.annotations.internal.InternalTangleApi
import tangle.inject.annotations.internal.TangleViewModelProviderMap

public interface TangleViewModelComponent {
  public val tangleViewModelSubcomponentFactory: TangleViewModelSubcomponent.Factory

  @OptIn(InternalTangleApi::class)
  @get:TangleViewModelProviderMap.KeySet
  public val viewModelKeys: Set<Class<out ViewModel>>
}
