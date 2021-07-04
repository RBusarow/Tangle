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
import tangle.inject.test.utils.factoryClass
import tangle.inject.test.utils.getterFunction
import tangle.inject.test.utils.myViewModelClass
import javax.inject.Provider

class VMInjectGeneratorTest : BaseTest() {

  @TestFactory
  fun `factory is generated for a constructor without arguments`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.lifecycle.ViewModel
      import tangle.inject.annotations.ContributesViewModel
      import tangle.inject.annotations.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor() : ViewModel()
     """
    ) {
      val providerClass = myViewModelClass.factoryClass()

      val constructor = providerClass.declaredConstructors.single()
      val providerInstance = constructor.newInstance()
      val getter = providerClass.getterFunction()

      getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
    }
  }

  @TestFactory
  fun `factory is generated for a constructor with an argument`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.lifecycle.ViewModel
      import tangle.inject.annotations.ContributesViewModel
      import tangle.inject.annotations.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        name: String
      ) : ViewModel()
     """
    ) {
      val providerClass = myViewModelClass.factoryClass()

      val constructor = providerClass.declaredConstructors.single()
      val providerInstance = constructor.newInstance(Provider { "name" })
      val getter = providerClass.getterFunction()

      getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
    }
  }

  @TestFactory
  fun `factory is generated for a constructor with a generic argument`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.lifecycle.ViewModel
      import tangle.inject.annotations.ContributesViewModel
      import tangle.inject.annotations.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        names: List<String>
      ) : ViewModel()
     """
    ) {
      val providerClass = myViewModelClass.factoryClass()

      val constructor = providerClass.declaredConstructors.single()
      val providerInstance = constructor.newInstance(Provider { listOf("name") })
      val getter = providerClass.getterFunction()

      getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
    }
  }

  @TestFactory
  fun `factory is generated for a constructor with a dagger Lazy argument`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.ViewModel
      import tangle.inject.annotations.ContributesViewModel
      import tangle.inject.annotations.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        names: dagger.Lazy<String>
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.factoryClass()

        val constructor = providerClass.declaredConstructors.single()
        val providerInstance = constructor.newInstance(Provider { "name" })
        val getter = providerClass.getterFunction()

        getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `factory is generated for a constructor with a dagger Lazy argument which is generic`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.ViewModel
      import tangle.inject.annotations.ContributesViewModel
      import tangle.inject.annotations.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        names: dagger.Lazy<List<String>>
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.factoryClass()

        val constructor = providerClass.declaredConstructors.single()
        val providerInstance = constructor.newInstance(Provider { listOf("name") })
        val getter = providerClass.getterFunction()

        getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `factory is generated for a constructor with a SavedStateHandle argument`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.annotations.ContributesViewModel
      import tangle.inject.annotations.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        savedStateHandle: SavedStateHandle
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.factoryClass()

        val constructor = providerClass.declaredConstructors.single()
        val providerInstance = constructor.newInstance(Provider { SavedStateHandle() })
        val getter = providerClass.getterFunction()

        getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `nullable FromSavedState-annotated arguments are just null if missing`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.annotations.ContributesViewModel
      import tangle.inject.annotations.FromSavedState
      import tangle.inject.annotations.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        @FromSavedState("name") val name: String?
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.factoryClass()

        val constructor = providerClass.declaredConstructors.single()
        val providerInstance = constructor.newInstance(Provider { SavedStateHandle() })
        val getter = providerClass.getterFunction()

        getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `factory is generated for a constructor with a FromSavedState argument`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.annotations.ContributesViewModel
      import tangle.inject.annotations.FromSavedState
      import tangle.inject.annotations.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        @FromSavedState("name") val name: String
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.factoryClass()

        val constructor = providerClass.declaredConstructors.single()
        val providerInstance = constructor.newInstance(
          Provider { SavedStateHandle(mapOf("name" to "Leeroy")) }
        )
        val getter = providerClass.getterFunction()

        getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `factory is generated for a constructor with SavedStateHandle and FromSavedState arguments`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.annotations.ContributesViewModel
      import tangle.inject.annotations.FromSavedState
      import tangle.inject.annotations.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        savedStateHandle: SavedStateHandle,
        @FromSavedState("name") val name: String
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.factoryClass()

        val constructor = providerClass.declaredConstructors.single()
        val providerInstance = constructor.newInstance(
          Provider { SavedStateHandle(mapOf("name" to "Leeroy")) }
        )
        val getter = providerClass.getterFunction()

        getter.invoke(providerInstance)::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `factory is generated for a constructor with two FromSavedState arguments`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.annotations.ContributesViewModel
      import tangle.inject.annotations.FromSavedState
      import tangle.inject.annotations.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        @FromSavedState("firstName") val firstName: String,
        @FromSavedState("lastName") val lastName: String
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.factoryClass()

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
  fun `factory is generated for a constructor with two FromSavedState arguments of different types`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.annotations.ContributesViewModel
      import tangle.inject.annotations.FromSavedState
      import tangle.inject.annotations.VMInject

      @ContributesViewModel(Unit::class)
      class MyViewModel @VMInject constructor(
        @FromSavedState("name") val name: String,
        @FromSavedState("age") val age: Int
      ) : ViewModel()
     """
      ) {
        val providerClass = myViewModelClass.factoryClass()

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
