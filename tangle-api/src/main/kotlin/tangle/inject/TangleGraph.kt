/*
 * Copyright (C) 2021 Rick Busarow
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
