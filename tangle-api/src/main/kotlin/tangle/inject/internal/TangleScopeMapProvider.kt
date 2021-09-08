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
import javax.inject.Qualifier

/**
 *
 * @since 0.13.0
 */
@InternalTangleApi
public interface TangleScopeMapProvider {

  /**
   * Given:
   * ```
   * @MemberInject(UserScope::class)
   * class UserToken { /* ... */ }
   * ```
   *
   * The map would be
   * ```
   * mapOf( UserToken::class to UserScope::class )
   * ```
   *
   * @since 0.13.0
   */
  @get:TangleScopeMap
  public val injectedClassToScopeClass: Map<Class<*>, Class<*>>

  /**
   * Used to look
   *
   * Given:
   * ```
   * @MergeSubcomponent(UserScope::class)
   * interface UserComponent { /* ... */ }
   * ```
   *
   * The map would be
   * ```
   * mapOf( UserScope::class to UserComponent::class )
   * ```
   *
   * @since 0.13.0
   */
  @get:TangleScopeToComponentMap
  public val scopeClassToComponentClass: Map<Class<*>, Class<*>>
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.FIELD,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.VALUE_PARAMETER
)
public annotation class TangleScopeMap

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.FIELD,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.VALUE_PARAMETER
)
public annotation class TangleScopeToComponentMap
