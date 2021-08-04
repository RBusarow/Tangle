package tangle.viewmodel

import tangle.inject.InternalTangleApi
import tangle.viewmodel.internal.TangleViewModelFactory

/**
 * Used to provide a way of retrieving [TangleViewModelMapSubcomponent.Factory]
 * from a main app [Component][dagger.Component].
 *
 * @since 0.10.0
 */
public interface TangleViewModelComponent {
  /**
   * Referenced by [TangleViewModelFactory] in order to create
   * a scoped [TangleViewModelMapSubcomponent], in order to access the [TangleViewModelProviderMap].
   *
   * @since 0.10.0
   */
  @InternalTangleApi
  public val tangleViewModelMapSubcomponentFactories: Set<TangleViewModelMapSubcomponent.Factory>

  /**
   * Referenced by [TangleViewModelFactory] in order to create
   * a scoped [TangleViewModelMapSubcomponent], in order to access the [TangleViewModelProviderMap].
   *
   * @since 0.10.0
   */
  @InternalTangleApi
  public val tangleViewModelKeysSubcomponentFactories: Set<TangleViewModelKeysSubcomponent.Factory>
}
