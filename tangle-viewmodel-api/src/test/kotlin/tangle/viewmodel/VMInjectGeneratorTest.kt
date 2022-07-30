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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.tschuchort.compiletesting.KotlinCompilation.Result
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import tangle.inject.InternalTangleApi
import tangle.inject.TangleGraph
import tangle.inject.test.utils.BaseTest
import tangle.inject.test.utils.appComponentFactoryCreate
import tangle.inject.test.utils.property
import tangle.inject.test.utils.targetClass
import javax.inject.Provider
import kotlin.reflect.KProperty1
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
          Provider {
            SavedStateHandle(
              mapOf(
                "aNameWhichIsVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLong" to "Leeroy"
              )
            )
          }
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

  @Test
  fun `provided argument should keep qualifier annotations`() = compileWithDagger(
    """
    package tangle.inject.tests

    import androidx.lifecycle.ViewModel
    import com.squareup.anvil.annotations.ContributesTo
    import com.squareup.anvil.annotations.MergeComponent
    import dagger.BindsInstance
    import dagger.Component
    import tangle.inject.test.utils.AppScope
    import javax.inject.Singleton
    import tangle.viewmodel.VMInject
    import javax.inject.Qualifier

    @Qualifier
    annotation class SomeQualifier

    @Qualifier
    annotation class SomeOtherQualifier

    @Singleton
    @MergeComponent(AppScope::class)
    interface AppComponent {
      @Component.Factory
      interface Factory {
        fun create(
          @BindsInstance
          @SomeQualifier
          someArg: String,

          @BindsInstance
          @SomeOtherQualifier
          someOtherArg: String,
        ): AppComponent
      }
    }

    class Target @VMInject constructor(
      @SomeQualifier
      val someArg: String
    ) : ViewModel() {
      fun get() = someArg
    }
    """
  ) {
    val expected = "Expected"
    val unexpected = "Unexpected"

    @OptIn(InternalTangleApi::class)
    val instance: ViewModel =
      appComponentFactoryCreate(expected, unexpected)
        .cast<TangleViewModelComponent>()
        .tangleViewModelMapSubcomponentFactory
        .create(SavedStateHandle())
        .viewModelProviderMap[targetClass]!!
        .get()!!

    instance::class.java shouldBe targetClass
    val property: KProperty1<ViewModel, *> = instance::class.property("someArg").cast()

    property.get(instance) shouldBe expected
  }

  @Test
  fun `qualifier arguments are propagated`() = compileWithDagger(
    """
    package tangle.inject.tests

    import androidx.lifecycle.ViewModel
    import com.squareup.anvil.annotations.ContributesTo
    import com.squareup.anvil.annotations.MergeComponent
    import dagger.BindsInstance
    import dagger.Component
    import tangle.inject.test.utils.AppScope
    import javax.inject.Singleton
    import tangle.viewmodel.VMInject
    import javax.inject.Qualifier

    @Qualifier
    annotation class SomeQualifier(val value: String)

    @Singleton
    @MergeComponent(AppScope::class)
    interface AppComponent {
      @Component.Factory
      interface Factory {
        fun create(
          @BindsInstance
          @SomeQualifier("A")
          someArg: String,

          @BindsInstance
          @SomeQualifier("B")
          someOtherArg: String,
        ): AppComponent
      }
    }

    class Target @VMInject constructor(
      @SomeQualifier("A")
      val someArg: String
    ) : ViewModel()
    """
  ) {
    val expected = "Expected"
    val unexpected = "Unexpected"

    @OptIn(InternalTangleApi::class)
    val instance: ViewModel =
        appComponentFactoryCreate(expected, unexpected)
        .cast<TangleViewModelComponent>()
        .tangleViewModelMapSubcomponentFactory
        .create(SavedStateHandle.createHandle(null, null))
        .viewModelProviderMap[targetClass]!!
        .get()!!

    instance::class.java shouldBe targetClass
    val property: KProperty1<ViewModel, *> = instance::class.property("someArg").cast()

    property.get(instance) shouldBe expected
  }

  @TestFactory
  fun `string interpolation error message`() = test {
    compile(
      """
      package tangle.inject.test

      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.VMInject
      import javax.inject.Qualifier

      @Qualifier
      annotation class SomeQualifier(val value: String)

      const val WORLD: String = "world!"

      class Target @VMInject constructor(
        @SomeQualifier("Hello, ${'$'}WORLD")
        val qualified: String
      ) : ViewModel()
      """,
      shouldFail = true
    ) {
      messages shouldContain """
        String Interpolation in Qualifier Arguments is not currently supported
        Here: "Hello, ${'$'}WORLD"
        In: "@SomeQualifier("Hello, ${'$'}WORLD")"
      """.trimIndent()
    }
  }

  @Test
  fun `qualified argument without binding should fail`() = compileWithDagger(
    """
    package tangle.inject.test

    import androidx.lifecycle.ViewModel
    import com.squareup.anvil.annotations.MergeComponent
    import dagger.Component
    import dagger.BindsInstance
    import dagger.Provides
    import tangle.viewmodel.VMInject
    import javax.inject.Qualifier
    import javax.inject.Singleton
    import tangle.inject.test.utils.AppScope

    @Qualifier
    annotation class SomeQualifier

    @Singleton
    @MergeComponent(AppScope::class)
    interface AppComponent {
      @Component.Factory
      interface Factory {
        fun create(@BindsInstance unqualified: String): AppComponent
      }
    }

    class Target @VMInject constructor(
      @SomeQualifier
      val someArg: String
    ) : ViewModel()
    """,
    shouldFail = true
  ) {
    messages shouldContain "[Dagger/MissingBinding] @tangle.inject.test.SomeQualifier java.lang.String cannot be provided without an @Provides-annotated method."
  }

  fun Result.provideTarget(vararg args: Any?): Any {
    val moduleClass =
      classLoader.loadClass("tangle.inject.tests.TangleViewModelScope_VMInject_Module").kotlin

    val companionObject = moduleClass.companionObject!!

    val funName = "provide_Target"

    val providerFunction = companionObject.functions
      .find { it.name == funName }

    requireNotNull(providerFunction) { "could not find a function named `$funName`" }

    return providerFunction.call(moduleClass.companionObjectInstance, *args)!!
  }
}
