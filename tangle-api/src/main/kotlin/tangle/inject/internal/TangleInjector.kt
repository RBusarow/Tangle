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

package tangle.inject.internal

import tangle.inject.InternalTangleApi

/**
 * This is an internal Tangle API and may be changed at any time.
 *
 * This interface is just a facade around Dagger's [MembersInjector][dagger.MembersInjector].
 *
 * It's necessary because we multi-bind the injectors into a Map, and need to provide a default
 * empty map in case there are no real injectors:
 * ```
 * @Multibinds
 * public fun bindTangleInjectorMap(): Map<Class<*>, @JvmSuppressWildcards TangleInjector<*>>
 * ```
 * If this was a Map<Class<*>, MembersInjector<*>>, Dagger would throw an exception because it sees
 * `MemberInjector` as an intrinsic like Lazy or Provider,
 * and wants us to bind the type parameter instead.
 *
 * @since 0.13.0
 */
@InternalTangleApi
public interface TangleInjector<T> {

  /**
   * Injects all dependencies into [target].
   *
   * @since 0.13.0
   */
  public fun inject(target: T)
}
