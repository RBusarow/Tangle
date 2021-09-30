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

package tangle.inject.gradle

import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

public open class ViewModelOptions @Inject constructor(
  objectFactory: ObjectFactory
) {

  /**
   * ViewModel code generation and API's enabled
   *
   * If this property is set, then Tangle will use that setting regardless of what Androidx
   * dependencies are in the classpath.
   *
   * If this property is not set, Tangle will automatically enable its ViewModel dependencies if the
   * module declares any `androidx.lifecycle:lifecycle-viewmodel*` group dependencies.
   */
  public var enabled: Boolean? by objectFactory.property()

  /**
   * Activity ViewModel API's enabled
   *
   * If this property is set, then Tangle will use that setting regardless of what Androidx
   * dependencies are in the classpath.
   *
   * If this property is not set, Tangle will automatically enable its ViewModel-Activity
   * dependencies if ViewModel code generation is enabled via [enabled] and the module declares any
   * `androidx.activity` group dependencies.
   */
  public var activitiesEnabled: Boolean? by objectFactory.property()

  /**
   * Compose ViewModel API's enabled
   *
   * If this property is set, then Tangle will use that setting regardless of what Androidx
   * dependencies are in the classpath.
   *
   * If this property is not set, Tangle will automatically enable its ViewModel-Compose
   * dependencies if ViewModel code generation is enabled via [enabled] and the module declares any
   * `androidx.compose.ui` group dependencies.
   */
  public var composeEnabled: Boolean? by objectFactory.property()

  /**
   * Fragment ViewModel API's enabled
   *
   * If this property is set, then Tangle will use that setting regardless of what Androidx
   * dependencies are in the classpath.
   *
   * If this property is not set, Tangle will automatically enable its ViewModel-Fragment
   * dependencies if ViewModel code generation is enabled via [enabled] and the module declares any
   * `androidx.fragment` group dependencies.
   */
  public var fragmentsEnabled: Boolean? by objectFactory.property()
}
