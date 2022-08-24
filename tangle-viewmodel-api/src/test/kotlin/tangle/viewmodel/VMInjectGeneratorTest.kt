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

package tangle.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.tschuchort.compiletesting.KotlinCompilation.Result
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.TestFactory
import tangle.inject.test.utils.BaseTest
import tangle.inject.test.utils.targetClass
import javax.inject.Provider
import kotlin.reflect.KFunction
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

class VMInjectGeneratorTest : BaseTest() {

  @TestFactory
  fun `provider function is generated without arguments`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.VMInject

      class Target @VMInject constructor() : ViewModel()
     """
    ) {

      provideTarget()::class.java shouldBe targetClass
    }
  }

  @TestFactory
  fun `provider function is generated for an argument`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.VMInject

      class Target @VMInject constructor(
        name: String
      ) : ViewModel()
     """
    ) {
      provideTarget(Provider { "name" })::class.java shouldBe targetClass
    }
  }

  @TestFactory
  fun `provider function is generated for a generic argument`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.VMInject

      class Target @VMInject constructor(
        names: List<String>
      ) : ViewModel()
     """
    ) {
      provideTarget(Provider { listOf("name") })::class.java shouldBe targetClass
    }
  }

  @TestFactory
  fun `provider function is generated for a dagger Lazy argument`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.VMInject

      class Target @VMInject constructor(
        names: dagger.Lazy<String>
      ) : ViewModel()
     """
      ) {
        provideTarget(Provider { "name" })::class.java shouldBe targetClass
      }
    }

  @TestFactory
  fun `provider function is generated for a dagger Lazy argument which is generic`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.VMInject

      class Target @VMInject constructor(
        names: dagger.Lazy<List<String>>
      ) : ViewModel()
     """
      ) {
        provideTarget(Provider { listOf("name") })::class.java shouldBe targetClass
      }
    }

  @TestFactory
  fun `provider function is generated for a SavedStateHandle argument`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.VMInject

      class Target @VMInject constructor(
        savedStateHandle: SavedStateHandle
      ) : ViewModel()
     """
      ) {
        provideTarget(Provider { SavedStateHandle() })::class.java shouldBe targetClass
      }
    }

  @TestFactory
  fun `provider function is generated for a SavedStateHandle argument wrapped in Provider and a TangleParam argument`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.VMInject
      import javax.inject.Provider
      import javax.inject.Inject

      class Target @VMInject constructor(
        savedStateHandle: Provider<SavedStateHandle>,
        @TangleParam("name") val name: String
      ) : ViewModel()
     """
      ) {
        provideTarget(
          Provider { SavedStateHandle(mapOf("name" to "Leeroy")) }
        )::class.java shouldBe targetClass
      }
    }

  @TestFactory
  fun `generated checkNotNull error message in factory should handle very long names`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.VMInject
      import javax.inject.Provider
      import javax.inject.Inject

      class Target @VMInject constructor(
        savedStateHandle: Provider<SavedStateHandle>,
        @TangleParam("aNameWhichIsVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLong") val name: String
      ) : ViewModel()
     """
      ) {
        provideTarget(
          Provider { SavedStateHandle(mapOf("aNameWhichIsVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLong" to "Leeroy")) }
        )::class.java shouldBe targetClass
      }
    }

  @TestFactory
  fun `provider function is generated for an argument named savedStateHandleProvider and a TangleParam argument`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.VMInject
      import javax.inject.Provider

      class Target @VMInject constructor(
        @TangleParam("savedName") val name: String,
        savedStateHandleProvider: String
      ) : ViewModel()
     """
      ) {
        provideTarget(
          Provider { "a string" },
          Provider { SavedStateHandle(mapOf("savedName" to "Leeroy")) }
        )::class.java shouldBe targetClass
      }
    }

  @TestFactory
  fun `nullable TangleParam-annotated arguments are just null if missing`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.VMInject

      class Target @VMInject constructor(
        @TangleParam("name") val name: String?
      ) : ViewModel()
     """
      ) {
        provideTarget(Provider { SavedStateHandle() })::class.java shouldBe targetClass
      }
    }

  @TestFactory
  fun `provider function is generated for a TangleParam argument`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.VMInject

      class Target @VMInject constructor(
        @TangleParam("name") val name: String
      ) : ViewModel()
     """
      ) {
        provideTarget(
          Provider { SavedStateHandle(mapOf("name" to "Leeroy")) }
        )::class.java shouldBe targetClass
      }
    }

  @TestFactory
  fun `provider function is generated with SavedStateHandle and TangleParam arguments`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.VMInject

      class Target @VMInject constructor(
        savedStateHandle: SavedStateHandle,
        @TangleParam("name") val name: String
      ) : ViewModel()
     """
      ) {
        provideTarget(
          Provider { SavedStateHandle(mapOf("name" to "Leeroy")) }
        )::class.java shouldBe targetClass
      }
    }

  @TestFactory
  fun `provider function is generated with two TangleParam arguments`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.VMInject

      class Target @VMInject constructor(
        @TangleParam("firstName") val firstName: String,
        @TangleParam("lastName") val lastName: String
      ) : ViewModel()
     """
      ) {
        provideTarget(
          Provider {
            SavedStateHandle(
              mapOf(
                "firstName" to "Leeroy",
                "lastName" to "Jenkins"
              )
            )
          }
        )::class.java shouldBe targetClass
      }
    }

  @TestFactory
  fun `provider function is generated with two TangleParam arguments of different types`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.VMInject

      class Target @VMInject constructor(
        @TangleParam("name") val name: String,
        @TangleParam("age") val age: Int
      ) : ViewModel()
     """
      ) {
        provideTarget(
          Provider {
            SavedStateHandle(
              mapOf(
                "name" to "Leeroy",
                "age" to 85
              )
            )
          }
        )::class.java shouldBe targetClass
      }
    }

  @TestFactory
  fun `qualified inject parameter propagates qualifiers`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.lifecycle.ViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.VMInject
      import javax.inject.Qualifier
      import javax.inject.Inject

      @Qualifier
      annotation class SomeQualifier

      class Target @VMInject constructor(
        @SomeQualifier
        val someArg: String
      ) : ViewModel()
      """
    ) {
      val someQualifier = classLoader.loadClass("tangle.inject.tests.SomeQualifier").kotlin

      val factoryProviderAnnotations = provideTargetFunction.parameters
        .single { it.name == "someArg" }
        .annotations
        .map { it.annotationClass }

      factoryProviderAnnotations shouldContain someQualifier
    }
  }

  val Result.provideTargetFunction: KFunction<ViewModel>
    get() {
      val moduleClass = classLoader
        .loadClass("tangle.inject.tests.TangleViewModelScope_VMInject_Module").kotlin

      val companionObject = moduleClass.companionObject!!

      val funName = "provide_Target"

      val providerFunction = companionObject.functions
        .find { it.name == funName }

      requireNotNull(providerFunction) { "could not find a function named `$funName`" }

      @Suppress("UNCHECKED_CAST")
      return providerFunction as KFunction<ViewModel>
    }

  fun Result.provideTarget(vararg args: Any?): Any {
    val moduleClass = classLoader
      .loadClass("tangle.inject.tests.TangleViewModelScope_VMInject_Module").kotlin

    return provideTargetFunction.call(moduleClass.companionObjectInstance, *args)!!
  }
}
