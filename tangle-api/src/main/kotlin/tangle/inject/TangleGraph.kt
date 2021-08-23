package tangle.inject

import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP

/**
 * Holds a reference to the application's Dagger graph,
 * so that it can be accessed by internal tooling.
 *
 * This should be initialized as soon as possible after initializing the AppComponent.
 *
 * @sample samples.TangleGraphSample.initializeTangleGraph
 * @since 0.10.0
 */
public object TangleGraph {

  @PublishedApi
  internal val components: MutableSet<Any> = mutableSetOf()

  /**
   * Sets a reference to the application's Dagger graph,
   * so that it can be accessed by internal tooling.
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

  /**
   * Used to retrieve a Component of a given type.
   *
   * This is an internal Tangle API and may change at any time.
   *
   * @since 0.10.0
   */
  @InternalTangleApi
  @RestrictTo(LIBRARY_GROUP)
  public inline fun <reified T> get(): T = components
    .filterIsInstance<T>()
    .single()
}
