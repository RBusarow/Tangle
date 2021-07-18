package tangle.viewmodel

import androidx.lifecycle.ViewModel
import tangle.inject.InternalTangleApi

public interface TangleViewModelComponent {
  public val tangleViewModelSubcomponentFactory: TangleViewModelSubcomponent.Factory

  @OptIn(InternalTangleApi::class)
  @get:TangleViewModelProviderMap.KeySet
  public val viewModelKeys: Set<Class<out ViewModel>>
}
