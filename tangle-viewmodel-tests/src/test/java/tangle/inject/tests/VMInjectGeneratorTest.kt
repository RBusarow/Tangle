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

package tangle.inject.tests

import androidx.lifecycle.SavedStateHandle
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.TestFactory
import tangle.inject.test.utils.BaseTest
import tangle.inject.test.utils.getterFunction
import tangle.inject.test.utils.myViewModelClass
import tangle.inject.test.utils.providerClass
import javax.inject.Provider

class VMInjectGeneratorTest : BaseTest() {

  @TestFactory
  fun `provider is generated without arguments`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.ContributesViewModel
      import tangle.viewmodel.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor() : ViewModel()
     """
    ) {
      val providerClass = myViewModelClass.providerClass()

      val constructor = providerClass.declaredConstructors.single()
      val providerInstance = constructor.newInstance()
      val getter = providerClass.getterFunction()

      getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
    }
  }

  @TestFactory
  fun `provider is generated for an argument`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.ContributesViewModel
      import tangle.viewmodel.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        name: String
      ) : ViewModel()
     """
    ) {
      val providerClass = myViewModelClass.providerClass()

      val constructor = providerClass.declaredConstructors.single()
      val providerInstance = constructor.newInstance(Provider { "name" })
      val getter = providerClass.getterFunction()

      getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
    }
  }

  @TestFactory
  fun `provider is generated for a generic argument`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.ContributesViewModel
      import tangle.viewmodel.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        names: List<String>
      ) : ViewModel()
     """
    ) {
      val providerClass = myViewModelClass.providerClass()

      val constructor = providerClass.declaredConstructors.single()
      val providerInstance = constructor.newInstance(Provider { listOf("name") })
      val getter = providerClass.getterFunction()

      getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
    }
  }

  @TestFactory
  fun `provider is generated for a dagger Lazy argument`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.ContributesViewModel
      import tangle.viewmodel.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        names: dagger.Lazy<String>
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.providerClass()

        val constructor = providerClass.declaredConstructors.single()
        val providerInstance = constructor.newInstance(Provider { "name" })
        val getter = providerClass.getterFunction()

        getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `provider is generated for a dagger Lazy argument which is generic`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.ContributesViewModel
      import tangle.viewmodel.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        names: dagger.Lazy<List<String>>
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.providerClass()

        val constructor = providerClass.declaredConstructors.single()
        val providerInstance = constructor.newInstance(Provider { listOf("name") })
        val getter = providerClass.getterFunction()

        getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `provider is generated for a SavedStateHandle argument`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.ContributesViewModel
      import tangle.viewmodel.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        savedStateHandle: SavedStateHandle
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.providerClass()

        val constructor = providerClass.declaredConstructors.single()
        val providerInstance = constructor.newInstance(Provider { SavedStateHandle() })
        val getter = providerClass.getterFunction()

        getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `provider is generated for a SavedStateHandle argument wrapped in Provider and a TangleParam argument`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.ContributesViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.VMInject
      import javax.inject.Provider

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        savedStateHandle: Provider<SavedStateHandle>,
        @TangleParam("name") val name: String
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.providerClass()

        val constructor = providerClass.declaredConstructors.single()
        val providerInstance = constructor.newInstance(
          Provider { SavedStateHandle(mapOf("name" to "Leeroy")) }
        )
        val getter = providerClass.getterFunction()

        getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `provider is generated for an argument named savedStateHandleProvider and a TangleParam argument`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.ContributesViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.VMInject
      import javax.inject.Provider

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        @TangleParam("name") val name: String,
        savedStateHandleProvider: String
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.providerClass()

        val constructor = providerClass.declaredConstructors.single()

        val providerInstance = constructor.newInstance(
          Provider { "a string" },
          Provider { SavedStateHandle(mapOf("name" to "Leeroy")) }
        )
        val getter = providerClass.getterFunction()

        getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
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
      import tangle.viewmodel.ContributesViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        @TangleParam("name") val name: String?
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.providerClass()

        val constructor = providerClass.declaredConstructors.single()
        val providerInstance = constructor.newInstance(Provider { SavedStateHandle() })
        val getter = providerClass.getterFunction()

        getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `provider is generated for a TangleParam argument`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.ContributesViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        @TangleParam("name") val name: String
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.providerClass()

        val constructor = providerClass.declaredConstructors.single()
        val providerInstance = constructor.newInstance(
          Provider { SavedStateHandle(mapOf("name" to "Leeroy")) }
        )
        val getter = providerClass.getterFunction()

        getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `provider is generated with SavedStateHandle and TangleParam arguments`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.ContributesViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        savedStateHandle: SavedStateHandle,
        @TangleParam("name") val name: String
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.providerClass()

        val constructor = providerClass.declaredConstructors.single()
        val providerInstance = constructor.newInstance(
          Provider { SavedStateHandle(mapOf("name" to "Leeroy")) }
        )
        val getter = providerClass.getterFunction()

        getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `provider is generated with two TangleParam arguments`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.ContributesViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        @TangleParam("firstName") val firstName: String,
        @TangleParam("lastName") val lastName: String
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.providerClass()

        val constructor = providerClass.declaredConstructors.single()
        val providerInstance = constructor.newInstance(
          Provider {
            SavedStateHandle(
              mapOf(
                "firstName" to "Leeroy",
                "lastName" to "Jenkins"
              )
            )
          }
        )
        val getter = providerClass.getterFunction()

        getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `provider is generated with two TangleParam arguments of different types`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.ContributesViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        @TangleParam("name") val name: String,
        @TangleParam("age") val age: Int
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.providerClass()

        val constructor = providerClass.declaredConstructors.single()
        val providerInstance = constructor.newInstance(
          Provider {
            SavedStateHandle(
              mapOf(
                "name" to "Leeroy",
                "age" to 85
              )
            )
          }
        )
        val getter = providerClass.getterFunction()

        getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
      }
    }
}
