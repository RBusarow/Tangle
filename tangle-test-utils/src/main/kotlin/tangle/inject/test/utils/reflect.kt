/*
 * Copyright (C) 2022 Rick Busarow
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

import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

inline fun <reified T : Any, reified R : Any> T.getStaticPrivateFieldByName(name: String): R {
  val kClass = T::class

  val property = kClass.memberProperties.find { it.name == name }

  requireNotNull(property) {
    """Cannot find a property named `$name` in ${kClass.qualifiedName}.
    |
    | -- existing member properties
    |${kClass.memberProperties.joinToString("\n")}
    |
    """.trimMargin()
  }

  property.isAccessible = true

  return property.getter.call(this) as R
}

inline fun <T : Any, reified R : Any> KClass<T>.getPrivateFieldByName(
  name: String,
  receiverInstance: Any
): R {
  val kClass = this

  val property = kClass.memberProperties.find { it.name == name }

  requireNotNull(property) {
    """Cannot find a property named `$name` in ${kClass.qualifiedName}.
    |
    | -- existing member properties
    |${kClass.memberProperties.joinToString("\n")}
    |
    """.trimMargin()
  }

  property.isAccessible = true

  @Suppress("UNCHECKED_CAST")
  return property.get(receiverInstance as T) as R
}

inline fun <reified T : Any, reified R : Any> T.getPrivateFieldByName(name: String): R {
  val kClass = T::class

  val property = kClass.memberProperties.find { it.name == name }

  requireNotNull(property) {
    """Cannot find a property named `$name` in ${kClass.qualifiedName}.
    |
    | -- existing member properties
    |${kClass.memberProperties.joinToString("\n")}
    |
    """.trimMargin()
  }

  property.isAccessible = true

  return property.get(this) as R
}

inline fun <reified T : Any> T.getPrivateFunctionByName(name: String, vararg args: Any?): Any? {
  val kClass = T::class

  val function = kClass.memberFunctions.find { it.name == name }

  requireNotNull(function) {
    """Cannot find a function named `$name` in ${kClass.qualifiedName}.
    |
    | -- existing member functions
    |${kClass.memberFunctions.joinToString("\n")}
    |
    """.trimMargin()
  }

  function.isAccessible = true

  return function.call(this, *args)
}
