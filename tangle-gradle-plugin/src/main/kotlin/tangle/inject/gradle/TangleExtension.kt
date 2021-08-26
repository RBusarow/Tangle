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
   * default value is true
   */
  public var fragmentsEnabled: Boolean by objectFactory.property(FRAGMENTS_ENABLED)

  /**
   * Worker/WorkManager code generation and API's enabled
   *
   * default value is true
   */
  public var workEnabled: Boolean by objectFactory.property(WORK_ENABLED)

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

  internal companion object {
    const val FRAGMENTS_ENABLED = true
    const val WORK_ENABLED = true
  }
}

public open class ViewModelOptions @Inject constructor(
  objectFactory: ObjectFactory
) {

  /**
   * ViewModel code generation enabled
   *
   * default value is true
   */
  public var enabled: Boolean by objectFactory.property(true)

  /**
   * Activity ViewModel API's enabled
   *
   * default value is true
   */
  public var activitiesEnabled: Boolean by objectFactory.property(true)

  /**
   * Compose ViewModel API's enabled
   *
   * default value is false
   */
  public var composeEnabled: Boolean by objectFactory.property(false)

  /**
   * Fragment ViewModel API's enabled
   *
   * default value is true
   */
  public var fragmentsEnabled: Boolean by objectFactory.property(true)
}

internal inline fun <reified T> ObjectFactory.property(initialValue: T): ReadWriteProperty<Any, T> =
  object : ReadWriteProperty<Any, T> {

    val delegate = property(T::class.java).convention(initialValue)

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
      return delegate.get()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
      delegate.set(value)
    }
  }
