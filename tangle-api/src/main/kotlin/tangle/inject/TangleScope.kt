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

import kotlin.reflect.KClass

/**
 * Annotates a member-injected class to indicate the scope which will provide its dependencies.
 *
 * @sample tangle.inject.samples.MemberInjectSample.memberInjectSample
 * @since 0.13.0
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class TangleScope(
  /**
   * The scope from which to pull the annotated class's dependencies.
   */
  val scope: KClass<*>
)
