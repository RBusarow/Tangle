package tangle.viewmodel

import tangle.inject.InternalTangleApi
import tangle.viewmodel.internal.TangleViewModelFactory

/**
 * Holds a reference to the application's Dagger graph,
 * so that it can be accessed by [TangleViewModelFactory].
 *
 * This should be initialized as soon as possible after initializing the AppComponent.
 *
 * @sample samples.TangleGraphSample.initializeTangleGraph
 * @since 0.10.0
 */
@OptIn(InternalTangleApi::class)
public object TangleGraph {

  private val components: MutableSet<Any> = mutableSetOf()

  internal val tangleViewModelKeys by lazy {
    get<TangleViewModelComponent>()
      .tangleViewModelKeysSubcomponentFactory
      .create()
      .viewModelKeys
  }

  /**
   * Sets a reference to the application's Dagger graph,
   * so that it can be accessed by [TangleViewModelFactory].
   *
   * This should be initialized as soon as possible after initializing the AppComponent.
   *
   * @sample samples.TangleGraphSample.initializeTangleGraph
   * @param component the application-scoped Dagger component
   * @since 0.10.0
   */
  public fun init(component: Any) {
    components.add(component)
  }

  internal fun tangleViewModelSubcomponentFactory() = get<TangleViewModelComponent>()
    .tangleViewModelMapSubcomponentFactory

  /**
   * Used to retrieve a Component of a given type.
   *
   * This is an internal Tangle API and may change at any time.
   *
   * @since 0.10.0
   */
  private inline fun <reified T> get(): T = components
    .filterIsInstance<T>()
    .single()
}
