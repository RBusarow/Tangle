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
import tangle.inject.internal.TangleInjector
import tangle.inject.internal.TangleInjectorComponent
import tangle.inject.internal.TangleScopeMapProvider
import tangle.inject.internal.TangleScopeMapProviderComponent

/**
 * Holds a reference to the application's Dagger graph,
 * so that it can be accessed by internal tooling.
 *
 * This should be initialized as soon as possible after initializing the AppComponent.
 *
 * @sample tangle.inject.samples.TangleGraphSample.tangleGraphSample
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
   * This function has been renamed to `add`.  This "init" version will be removed soon.
   *
   * @sample tangle.inject.samples.TangleGraphSample.tangleGraphSample
   * @param component the application-scoped Dagger component
   * @since 0.10.0
   */
  @Deprecated(
    "use TangleGraph.add(...) instead",
    ReplaceWith("TangleGraph.add(component)", "tangle.inject.TangleGraph")
  )
  public fun init(component: Any) {
    add(component)
  }

  /**
   * Sets a reference to the application's Dagger graph,
   * so that it can be accessed by internal tooling.
   *
   * This should be initialized as soon as possible after initializing the AppComponent.
   *
   * @sample tangle.inject.samples.TangleGraphSample.tangleGraphSample
   * @param component the application-scoped Dagger component
   * @since 0.13.0
   */
  public fun add(component: Any) {
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
    .singleOrNull()
    ?: throw IllegalStateException(
      "Requested component of type `${T::class.java.canonicalName}` is missing from " +
        "the currently registered components.\n\nCurrently registered components:\n" +
        "${components.joinToString("\n") { it::class.java.canonicalName }}\n\n---"
    )

  /**
   * Used to retrieve a Component of a given type.
   *
   * This is an internal Tangle API and may change at any time.
   *
   * @since 0.13.0
   */
  @Suppress("UNCHECKED_CAST")
  @PublishedApi
  internal fun <T : Any> get(tClass: Class<T>): T = components
    .firstOrNull { tClass.isAssignableFrom(it::class.java) } as? T
    ?: throw IllegalStateException(
      "Requested component of type `${tClass.canonicalName}` is missing from " +
        "the currently registered components.\n\nCurrently registered components:\n" +
        "${components.joinToString("\n") { it::class.java.canonicalName }}\n\n---"
    )

  /**
   * Performs member/field injection upon [target] using its bound scope.
   *
   * Be sure to call [TangleGraph.add] with your scope's Component or Subcomponent
   * before calling this function.
   *
   * @sample tangle.inject.samples.MemberInjectSample.memberInjectSample
   * @since 0.13.0
   */
  @OptIn(InternalTangleApi::class)
  public inline fun <reified T : Any> inject(target: T) {

    val provider = get<TangleScopeMapProviderComponent>()
      .scopeMapProvider

    val scopeClass = scopeClassForInjectedClass(target, provider)

    val componentClass = componentClassForScope(scopeClass, provider)

    @Suppress("UNCHECKED_CAST")
    val injectorProvider = (get(componentClass) as TangleInjectorComponent)
      .injectors[target::class.java]

    requireNotNull(injectorProvider) {
      "unable to find a TangleInjector bound for ${target::class.java.canonicalName} " +
        "in ${componentClass.canonicalName}."
    }

    @Suppress("UNCHECKED_CAST")
    val injector = injectorProvider.get() as TangleInjector<T>

    injector.inject(target)
  }

  @InternalTangleApi
  @PublishedApi
  internal inline fun <reified T : Any> scopeClassForInjectedClass(
    target: T,
    provider: TangleScopeMapProvider
  ): Class<*> {

    val scopeClass = provider
      .injectedClassToScopeClass[target::class.java]

    requireNotNull(scopeClass) {
      "No scope is defined for ${target::class.qualifiedName}. " +
        "Did you forget to annotate it with @MemberInject(YourScope::class)?"
    }

    return scopeClass
  }

  @InternalTangleApi
  @PublishedApi
  internal fun componentClassForScope(
    scopeClass: Class<*>,
    provider: TangleScopeMapProvider
  ): Class<*> {

    val componentClass = provider
      .scopeClassToComponentClass[scopeClass]

    requireNotNull(componentClass) {
      """
    ${provider.scopeClassToComponentClass.entries.joinToString("\n")}

      No Component or Subcomponent scope is defined for the scope ${scopeClass.canonicalName}.
      """.trimIndent()
    }

    return componentClass
  }
}
