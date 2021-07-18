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

package tangle.viewmodel

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.reflect.KClass

@Target(CONSTRUCTOR)
@Retention(BINARY)
@MustBeDocumented
public annotation class VMInject

@Target(CLASS)
@Retention(RUNTIME)
public annotation class ContributesViewModel(
  /**
   * The scope in which to include this module.
   */
  val scope: KClass<*>,
  /**
   * This contributed module will replace these contributed classes. The array is allowed to
   * include other contributed bindings, multibindings and Dagger modules. All replaced classes
   * must use the same scope.
   */
  val replaces: Array<KClass<*>> = []
)
