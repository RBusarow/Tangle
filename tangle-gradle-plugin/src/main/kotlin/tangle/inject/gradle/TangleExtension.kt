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

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UnnecessaryAbstractClass") // Gradle optimization
public abstract class TangleExtension @Inject constructor(
  objectFactory: ObjectFactory
) {

  /**
   * Fragment code generation and API's enabled
   *
   * If this property is set, then Tangle will use that setting regardless of what Androidx
   * dependencies are in the classpath.
   *
   * If this property is not set, Tangle will automatically enable its Fragment dependencies if the
   * module declares any `androidx.fragment` group dependencies.
   */
  public var fragmentsEnabled: Boolean? by objectFactory.property()

  /**
   * Worker/WorkManager code generation and API's enabled
   *
   * If this property is set, then Tangle will use that setting regardless of what Androidx
   * dependencies are in the classpath.
   *
   * If this property is not set, Tangle will automatically enable its Worker/WorkManager
   * dependencies if the module declares any `androidx.work` group dependencies.
   */
  public var workEnabled: Boolean? by objectFactory.property()

  /**
   * ViewModel configuration options
   */
  public val viewModelOptions: ViewModelOptions = ViewModelOptions(objectFactory)

  /**
   * ViewModel configuration options
   */
  public fun viewModelOptions(action: Action<ViewModelOptions>) {
    action.execute(viewModelOptions)
  }
}

internal fun ObjectFactory.property(): ReadWriteProperty<Any, Boolean?> =
  object : ReadWriteProperty<Any, Boolean?> {

    val delegate = property(Boolean::class.java)

    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean? {
      return delegate.orNull
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean?) {
      delegate.set(value)
    }
  }
