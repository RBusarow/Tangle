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
import tangle.inject.test.utils.*
import javax.inject.Provider

class VMInjectGeneratorTest : BaseTest() {

  @TestFactory
  fun `factory is generated without arguments`() = test {
    compile(
      """
      package tangleViewModel.inject.tests

      import androidx.lifecycle.ViewModel
      import tangleViewModel.viewmodel.VMInject

      class MyViewModel @VMInject constructor() : ViewModel()
     """
    ) {
      val factoryClass = myViewModelClass.factoryClass()
      val factoryInstance = factoryClass.newInstance()

      val getter = factoryClass.createFunction()

      getter.invoke(factoryInstance)::class.java shouldBe myViewModelClass
    }
  }

  @TestFactory
  fun `factory is generated for an argument`() = test {
    compile(
      """
      package tangleViewModel.inject.tests

      import androidx.lifecycle.ViewModel
      import tangleViewModel.viewmodel.VMInject

      class MyViewModel @VMInject constructor(
        name: String
      ) : ViewModel()
     """
    ) {
      val factoryClass = myViewModelClass.factoryClass()

      val constructor = factoryClass.declaredConstructors.single()
      val factoryInstance = constructor.newInstance(Provider { "name" })
      val getter = factoryClass.createFunction()

      getter.invoke(factoryInstance)::class.java shouldBe myViewModelClass
    }
  }

  @TestFactory
  fun `factory is generated for a generic argument`() = test {
    compile(
      """
      package tangleViewModel.inject.tests

      import androidx.lifecycle.ViewModel
      import tangleViewModel.viewmodel.VMInject

      class MyViewModel @VMInject constructor(
        names: List<String>
      ) : ViewModel()
     """
    ) {
      val factoryClass = myViewModelClass.factoryClass()

      val constructor = factoryClass.declaredConstructors.single()
      val factoryInstance = constructor.newInstance(Provider { listOf("name") })
      val getter = factoryClass.createFunction()

      getter.invoke(factoryInstance)::class.java shouldBe myViewModelClass
    }
  }

  @TestFactory
  fun `factory is generated for a dagger Lazy argument`() =
    test {
      compile(
        """
      package tangleViewModel.inject.tests

      import androidx.lifecycle.ViewModel
      import tangleViewModel.viewmodel.VMInject

      class MyViewModel @VMInject constructor(
        names: dagger.Lazy<String>
      ) : ViewModel()
     """
      ) {
        val factoryClass = myViewModelClass.factoryClass()

        val constructor = factoryClass.declaredConstructors.single()
        val factoryInstance = constructor.newInstance(Provider { "name" })
        val getter = factoryClass.createFunction()

        getter.invoke(factoryInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `factory is generated for a dagger Lazy argument which is generic`() =
    test {
      compile(
        """
      package tangleViewModel.inject.tests

      import androidx.lifecycle.ViewModel
      import tangleViewModel.viewmodel.VMInject

      class MyViewModel @VMInject constructor(
        names: dagger.Lazy<List<String>>
      ) : ViewModel()
     """
      ) {
        val factoryClass = myViewModelClass.factoryClass()

        val constructor = factoryClass.declaredConstructors.single()
        val factoryInstance = constructor.newInstance(Provider { listOf("name") })
        val getter = factoryClass.createFunction()

        getter.invoke(factoryInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `factory is generated for a SavedStateHandle argument`() =
    test {
      compile(
        """
      package tangleViewModel.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangleViewModel.viewmodel.VMInject

      class MyViewModel @VMInject constructor(
        savedStateHandle: SavedStateHandle
      ) : ViewModel()
     """
      ) {
        val factoryClass = myViewModelClass.factoryClass()

        val constructor = factoryClass.declaredConstructors.single()
        val factoryInstance = constructor.newInstance(Provider { SavedStateHandle() })
        val getter = factoryClass.createFunction()

        getter.invoke(factoryInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `factory is generated for a SavedStateHandle argument wrapped in Provider and a TangleParam argument`() =
    test {
      compile(
        """
      package tangleViewModel.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangleViewModel.inject.TangleParam
      import tangleViewModel.viewmodel.VMInject
      import javax.inject.Provider
      import javax.inject.Inject

      class MyViewModel @VMInject constructor(
        savedStateHandle: Provider<SavedStateHandle>,
        @TangleParam("name") val name: String
      ) : ViewModel()
     """
      ) {
        val factoryClass = myViewModelClass.factoryClass()

        val constructor = factoryClass.declaredConstructors.single()
        val factoryInstance = constructor.newInstance(
          Provider { SavedStateHandle(mapOf("name" to "Leeroy")) }
        )
        val getter = factoryClass.createFunction()

        getter.invoke(factoryInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `factory is generated for an argument named savedStateHandleProvider and a TangleParam argument`() =
    test {
      compile(
        """
      package tangleViewModel.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangleViewModel.inject.TangleParam
      import tangleViewModel.viewmodel.VMInject
      import javax.inject.Provider

      class MyViewModel @VMInject constructor(
        @TangleParam("savedName") val name: String,
        savedStateHandleProvider: String
      ) : ViewModel()
     """
      ) {
        val factoryClass = myViewModelClass.factoryClass()

        val constructor = factoryClass.declaredConstructors.single()

        val factoryInstance = constructor.newInstance(
          Provider { "a string" },
          Provider { SavedStateHandle(mapOf("savedName" to "Leeroy")) }
        )
        val getter = factoryClass.createFunction()

        getter.invoke(factoryInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `nullable TangleParam-annotated arguments are just null if missing`() =
    test {
      compile(
        """
      package tangleViewModel.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangleViewModel.inject.TangleParam
      import tangleViewModel.viewmodel.VMInject

      class MyViewModel @VMInject constructor(
        @TangleParam("name") val name: String?
      ) : ViewModel()
     """
      ) {
        val factoryClass = myViewModelClass.factoryClass()

        val constructor = factoryClass.declaredConstructors.single()
        val factoryInstance = constructor.newInstance(Provider { SavedStateHandle() })
        val getter = factoryClass.createFunction()

        getter.invoke(factoryInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `factory is generated for a TangleParam argument`() =
    test {
      compile(
        """
      package tangleViewModel.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangleViewModel.inject.TangleParam
      import tangleViewModel.viewmodel.VMInject

      class MyViewModel @VMInject constructor(
        @TangleParam("name") val name: String
      ) : ViewModel()
     """
      ) {
        val factoryClass = myViewModelClass.factoryClass()

        val constructor = factoryClass.declaredConstructors.single()
        val factoryInstance = constructor.newInstance(
          Provider { SavedStateHandle(mapOf("name" to "Leeroy")) }
        )
        val getter = factoryClass.createFunction()

        getter.invoke(factoryInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `factory is generated with SavedStateHandle and TangleParam arguments`() =
    test {
      compile(
        """
      package tangleViewModel.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangleViewModel.inject.TangleParam
      import tangleViewModel.viewmodel.VMInject

      class MyViewModel @VMInject constructor(
        savedStateHandle: SavedStateHandle,
        @TangleParam("name") val name: String
      ) : ViewModel()
     """
      ) {
        val factoryClass = myViewModelClass.factoryClass()

        val constructor = factoryClass.declaredConstructors.single()
        val factoryInstance = constructor.newInstance(
          Provider { SavedStateHandle(mapOf("name" to "Leeroy")) }
        )
        val getter = factoryClass.createFunction()

        getter.invoke(factoryInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `factory is generated with two TangleParam arguments`() =
    test {
      compile(
        """
      package tangleViewModel.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangleViewModel.inject.TangleParam
      import tangleViewModel.viewmodel.VMInject

      class MyViewModel @VMInject constructor(
        @TangleParam("firstName") val firstName: String,
        @TangleParam("lastName") val lastName: String
      ) : ViewModel()
     """
      ) {
        val factoryClass = myViewModelClass.factoryClass()

        val constructor = factoryClass.declaredConstructors.single()
        val factoryInstance = constructor.newInstance(
          Provider {
            SavedStateHandle(
              mapOf(
                "firstName" to "Leeroy",
                "lastName" to "Jenkins"
              )
            )
          }
        )
        val getter = factoryClass.createFunction()

        getter.invoke(factoryInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `factory is generated with two TangleParam arguments of different types`() =
    test {
      compile(
        """
      package tangleViewModel.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangleViewModel.inject.TangleParam
      import tangleViewModel.viewmodel.VMInject

      class MyViewModel @VMInject constructor(
        @TangleParam("name") val name: String,
        @TangleParam("age") val age: Int
      ) : ViewModel()
     """
      ) {
        val factoryClass = myViewModelClass.factoryClass()

        val constructor = factoryClass.declaredConstructors.single()
        val factoryInstance = constructor.newInstance(
          Provider {
            SavedStateHandle(
              mapOf(
                "name" to "Leeroy",
                "age" to 85
              )
            )
          }
        )
        val getter = factoryClass.createFunction()

        getter.invoke(factoryInstance)::class.java shouldBe myViewModelClass
      }
    }
}
