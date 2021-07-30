package tangle.viewmodel

import androidx.lifecycle.ViewModel
import tangle.viewmodel.internal.TangleViewModelFactory
import javax.inject.Qualifier

/**
 * Qualifier for the internal `Map<KClass<ViewModel>, ViewModel>>`
 * used in Tangle's [ViewModel][androidx.lifecycle.ViewModel] multi-binding.
 *
 * This is an internal Tangle API and should not be referenced directly.
 *
 * @since 0.10.0
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.FIELD,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.VALUE_PARAMETER
)
public annotation class TangleViewModelProviderMap {

  /**
   * Qualifier for all the keys contained in [TangleViewModelProviderMap].
   * [TangleViewModelProviderMap] is only provided by [TangleViewModelMapSubcomponent],
   * and a new subcomponent needs to be created for each viewModel injection, so it's inefficient
   * to create the object before we know if the map holds the [ViewModel].
   * The [TangleViewModelFactory] checks this Set in order to determine whether the map
   * holds a particular ViewModel type.
   *
   * This is an internal Tangle API and should not be referenced directly.
   *
   * @since 0.10.0
   */
  @Qualifier
  @Retention(AnnotationRetention.RUNTIME)
  @Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
  public annotation class KeySet
}
