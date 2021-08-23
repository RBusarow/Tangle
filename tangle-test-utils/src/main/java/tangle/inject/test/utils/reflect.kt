package tangle.inject.test.utils

import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

inline fun <reified T : Any, reified R : Any> T.getStaticPrivateFieldByName(name: String): R {
  val kClass = T::class

  val property = kClass.memberProperties.find { it.name == name }

  requireNotNull(property) {
    """Cannot find a property named `$name` in ${kClass::qualifiedName}.
    |
    | -- existing member properties
    |${kClass.memberProperties.joinToString("\n")}
    |
  """.trimMargin()
  }

  property.isAccessible = true

  return property.getter.call(this) as R
}

inline fun <reified T : Any, reified R : Any> T.getPrivateFieldByName(name: String): R {
  val kClass = T::class

  val property = kClass.memberProperties.find { it.name == name }

  requireNotNull(property) {
    """Cannot find a property named `$name` in ${kClass::qualifiedName}.
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
    """Cannot find a function named `$name` in ${kClass::qualifiedName}.
    |
    | -- existing member functions
    |${kClass.memberFunctions.joinToString("\n")}
    |
  """.trimMargin()
  }

  function.isAccessible = true

  return function.call(this, *args)
}
