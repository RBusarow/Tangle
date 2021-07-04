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
import org.gradle.api.provider.Property
import javax.inject.Inject

@Suppress("UnnecessaryAbstractClass") // Gradle optimization
public abstract class TangleExtension @Inject constructor(
  objectFactory: ObjectFactory
) {

  public val composeEnabled: Property<Boolean> =
    objectFactory.property(Boolean::class.java)
      .convention(COMPOSE_ENABLED)

  internal companion object {
    const val COMPOSE_ENABLED = false
  }
}
