package tangle.viewmodel

import tangle.inject.InternalTangleApi

/**
 * Holds a reference to the application's Dagger graph,
 * so that it can be accessed by [TangleViewModelFactory].
 *
 * This should be initialized as soon as possible after initializing the AppComponent.
 *
 * @sample samples.TangleGraphSample.initializeTangleGraph
 * @since 0.10.0
 */
public object TangleGraph {

  @PublishedApi
  @Suppress("ObjectPropertyNaming")
  internal val _components: MutableSet<Any> = mutableSetOf()

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
    _components.add(component)
  }

  /**
   * Used to retrieve a Component of a given type.
   *
   * This is an internal Tangle API and may change at any time.
   *
   * @since 0.10.0
   */
  @InternalTangleApi
  public inline fun <reified T> get(): T = _components
    .filterIsInstance<T>()
    .single()
}
