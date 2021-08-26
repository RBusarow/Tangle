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

package tangle.inject.test.utils

import com.squareup.anvil.annotations.MergeComponent
import com.squareup.anvil.annotations.MergeSubcomponent
import kotlin.reflect.KClass

interface AnyDaggerComponent {
  val modules: List<KClass<*>>
  val dependencies: List<KClass<*>>
}

fun Class<*>.anyDaggerComponent(annotationClass: KClass<*>): AnyDaggerComponent {
  return when (annotationClass) {
    MergeComponent::class -> object : AnyDaggerComponent {
      override val modules: List<KClass<*>> = daggerComponent.modules.toList()
      override val dependencies: List<KClass<*>> = daggerComponent.dependencies.toList()
    }
    MergeSubcomponent::class -> object : AnyDaggerComponent {
      override val modules: List<KClass<*>> = daggerSubcomponent.modules.toList()
      override val dependencies: List<KClass<*>> get() = throw IllegalAccessException()
    }
    else -> throw IllegalArgumentException("Cannot handle $annotationClass")
  }
}
