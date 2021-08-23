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

package tangle.viewmodel

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.TestFactory
import tangle.inject.test.utils.BaseTest
import tangle.inject.test.utils.factoryClass
import tangle.inject.test.utils.myViewModelClass
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaMethod

class AssistedViewModelGeneratedTest : BaseTest() {

  @TestFactory
  fun `factory impl is generated for a VMInjectFactory factory interface`() =
    test {
      compile(
        //language=kotlin
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.*
      import javax.inject.Inject

      class MyViewModel @VMInject constructor(
        @VMAssisted val name: String
      ) : ViewModel() {

        @VMInjectFactory
        interface Factory {
          fun create(name: String): MyViewModel
        }
      }
     """
      ) {
        val factoryClass = myViewModelClass.factoryClass()

        val constructor = factoryClass.declaredConstructors.single()
        val factoryInstance = constructor.newInstance()

        val createFunction = factoryClass.kotlin
          .memberFunctions
          .single { it.name == "create" }
          .javaMethod!!

        createFunction.invoke(factoryInstance, "name")::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `factory impl function name should match interface's`() =
    test {
      compile(
        //language=kotlin
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.*
      import javax.inject.Inject

      class MyViewModel @VMInject constructor(
        @VMAssisted val name: String
      ) : ViewModel() {

        @VMInjectFactory
        interface Factory {
          fun makeIt(name: String): MyViewModel
        }
      }
     """
      ) {
        val factoryClass = myViewModelClass.factoryClass()

        val constructor = factoryClass.declaredConstructors.single()
        val factoryInstance = constructor.newInstance()

        val createFunction = factoryClass.kotlin
          .memberFunctions
          .single { it.name == "makeIt" }
          .javaMethod!!

        createFunction.invoke(factoryInstance, "name")::class.java shouldBe myViewModelClass
      }
    }

  @TestFactory
  fun `assisted arguments can't be function types`() =
    test {
      compile(
        //language=kotlin
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.*
      import javax.inject.Inject

      class MyViewModel @VMInject constructor(
        @VMAssisted val function: () -> Unit
      ) : ViewModel() {

        @VMInjectFactory
        interface Factory {
          fun create(function: () -> Unit): MyViewModel
        }
      }
     """,
        shouldFail = true
      ) {

        messages shouldContain "Functional arguments like `@VMAssisted val function: () -> Unit` " +
          "can't be used as assisted arguments for ViewModels.  " +
          "They would leak the initial caller's instance into the ViewModel."
      }
    }

  @TestFactory
  fun `arguments cannot be both VMAssisted and TangleParam`() =
    test {
      compile(
        //language=kotlin
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.*
      import javax.inject.Inject

      class MyViewModel @VMInject constructor(
        @VMAssisted
        @TangleParam("name")
        val name: String
      ) : ViewModel()
     """,
        shouldFail = true
      ) {

        messages shouldContain "MyViewModel's constructor parameter `name` is annotated " +
          "with both `tangle.viewmodel.VMAssisted` (meaning it's passed directly from a Factory) " +
          "and `tangle.inject.TangleParam` (meaning it's passed via SavedStateHandle).  " +
          "Only one of these annotations can be applied to a single property."
      }
    }

  @TestFactory
  fun `assisted arguments require an annotated factory interface`() =
    test {
      compile(
        //language=kotlin
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.*
      import javax.inject.Inject

      class MyViewModel @VMInject constructor(
        @VMAssisted val name: String
      ) : ViewModel()
     """,
        shouldFail = true
      ) {

        messages shouldContain "MyViewModel's constructor has @VMAssisted-annotated parameters, " +
          "but there is no corresponding factory interface.  In order to provide assisted " +
          "parameters, create a Factory interface and annotated it with @VMInjectFactory."
      }
    }

  @TestFactory
  fun `assisted arguments must be provided by factory function`() =
    test {
      compile(
        //language=kotlin
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.*
      import javax.inject.Inject

      class MyViewModel @VMInject constructor(
        @VMAssisted val name: String,
        @VMAssisted val age: Int
      ) : ViewModel() {

        @VMInjectFactory
        interface Factory {
          fun create(name: String): MyViewModel
        }
      }
     """,
        shouldFail = true
      ) {

        messages shouldContain """@VMAssisted-annotated constructor parameters and factory interface function parameters don't match.
        |
        |assisted constructor parameters
        |	name: kotlin.String
        |	age: kotlin.Int
        |
        |factory function parameters
        |	name: kotlin.String""".trimMargin()
      }
    }

  @TestFactory
  fun `factory function arguments must also be in constructor`() =
    test {
      compile(
        //language=kotlin
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.*
      import javax.inject.Inject

      class MyViewModel @VMInject constructor(
      ) : ViewModel() {

        @VMInjectFactory
        interface Factory {
          fun create(name: String): MyViewModel
        }
      }
     """,
        shouldFail = true
      ) {

        messages shouldContainIgnoringWhitespaces """@VMAssisted-annotated constructor parameters and factory interface function parameters don't match.
          |
          |assisted constructor parameters
          |
          |
          |factory function parameters
          |  name: kotlin.String""".trimMargin()
      }
    }

  @TestFactory
  fun `factory function arguments must also be in constructor with VMAssisted annotation`() =
    test {
      compile(
        //language=kotlin
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.inject.TangleParam
      import tangle.viewmodel.*
      import javax.inject.Inject

      class MyViewModel @VMInject constructor(
        val name: String
      ) : ViewModel() {

        @VMInjectFactory
        interface Factory {
          fun create(name: String): MyViewModel
        }
      }
     """,
        shouldFail = true
      ) {

        messages shouldContainIgnoringWhitespaces """@VMAssisted-annotated constructor parameters and factory interface function parameters don't match.
        |
        |assisted constructor parameters
        |
        |
        |factory function parameters
        |	name: kotlin.String""".trimMargin()
      }
    }
}
