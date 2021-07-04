package tangle.inject.api

import androidx.lifecycle.ViewModel
import tangle.inject.annotations.TangleViewModelProviderMap

public interface TangleComponent {
  public val tangleSubcomponentFactory: TangleSubcomponent.Factory

  @get:TangleViewModelProviderMap.KeySet
  public val viewModelKeys: Set<Class<out ViewModel>>
}
