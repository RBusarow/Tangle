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

package tangle.inject

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import tangle.inject.test.utils.BaseTest
import tangle.inject.test.utils.appComponent
import tangle.inject.test.utils.appComponentFactoryCreate
import tangle.inject.test.utils.createFunction
import tangle.inject.test.utils.createInstance
import tangle.inject.test.utils.daggerAppComponent
import tangle.inject.test.utils.fieldsValues
import tangle.inject.test.utils.getPrivateFieldByName
import tangle.inject.test.utils.targetClass

@Execution(ExecutionMode.SAME_THREAD)
class TangleScopeIntegrationTest : BaseTest() {

  @BeforeEach
  fun beforeEach() {
    clearTangleGraph()
  }

  @Test
  fun `target may be injected without any injected members`() = compileWithDagger(
    """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import tangle.inject.TangleGraph
      import tangle.inject.test.utils.AppScope
      import tangle.inject.TangleScope
      import javax.inject.Singleton
      import javax.inject.Inject

      @TangleScope(AppScope::class)
      class Target

      @Singleton
      @MergeComponent(AppScope::class)
      interface AppComponent
     """
  ) {

    val component = daggerAppComponent.createFunction()
      .invoke(null)

    TangleGraph.add(component)

    val target = targetClass.createInstance()

    TangleGraph.inject(target)

    target.fieldsValues() shouldBe mapOf()
  }

  @Test
  fun `target may be injected with declared injected members`() = compileWithDagger(
    """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import dagger.BindsInstance
      import dagger.Component
      import tangle.inject.TangleGraph
      import tangle.inject.test.utils.AppScope
      import tangle.inject.TangleScope
      import javax.inject.Singleton
      import javax.inject.Inject

      @TangleScope(AppScope::class)
      class Target {
        @Inject lateinit var str: String
      }

      @Singleton
      @MergeComponent(AppScope::class)
      interface AppComponent {
        @Component.Factory
        interface Factory {
          fun create(
            @BindsInstance str: String
          ): AppComponent
        }
      }
     """
  ) {

    val component = appComponentFactoryCreate("name")

    TangleGraph.add(component)

    val target = targetClass.createInstance()

    TangleGraph.inject(target)

    target.fieldsValues() shouldBe mapOf("str" to "name")
  }

  @Test
  fun `target may be injected with superclass injected members`() = compileWithDagger(
    """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import dagger.BindsInstance
      import dagger.Component
      import tangle.inject.TangleGraph
      import tangle.inject.test.utils.AppScope
      import tangle.inject.TangleScope
      import javax.inject.Singleton
      import javax.inject.Inject

      @TangleScope(AppScope::class)
      class Target  : Base()

      abstract class Base {
        @Inject lateinit var baseStr: String
      }

      @Singleton
      @MergeComponent(AppScope::class)
      interface AppComponent {
        @Component.Factory
        interface Factory {
          fun create(
            @BindsInstance str: String
          ): AppComponent
        }
      }
     """
  ) {

    val component = appComponentFactoryCreate("baseName")

    TangleGraph.add(component)

    val target = targetClass.createInstance()

    TangleGraph.inject(target)

    target.fieldsValues() shouldBe mapOf("baseStr" to "baseName")
  }

  @Test
  fun `target may be injected with declared injected members and superclass injected members`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import dagger.BindsInstance
      import dagger.Component
      import tangle.inject.TangleGraph
      import tangle.inject.test.utils.AppScope
      import tangle.inject.TangleScope
      import javax.inject.Singleton
      import javax.inject.Inject

      @TangleScope(AppScope::class)
      class Target : Base() {
        @Inject lateinit var str : String
      }

      abstract class Base {
        @Inject lateinit var strs: List<String>
      }

      @Singleton
      @MergeComponent(AppScope::class)
      interface AppComponent {
        @Component.Factory
        interface Factory {
          fun create(
            @BindsInstance str: String,
            @BindsInstance strs: List<String>,
          ): AppComponent
        }
      }
     """
    ) {

      val component = appComponentFactoryCreate("name", listOf("strs"))

      TangleGraph.add(component)

      val target = targetClass.createInstance()

      TangleGraph.inject(target)

      target.fieldsValues() shouldBe mapOf("str" to "name", "strs" to listOf("strs"))
    }

  @Test
  fun `target may be injected from superclass`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import dagger.BindsInstance
      import dagger.Component
      import tangle.inject.TangleGraph
      import tangle.inject.test.utils.AppScope
      import tangle.inject.TangleScope
      import javax.inject.Singleton
      import javax.inject.Inject

      @TangleScope(AppScope::class)
      class Target : Base() {
        @Inject lateinit var str : String
      }

      abstract class Base {
        @Inject lateinit var strs: List<String>

        fun onCreate() {
          TangleGraph.inject(this)
        }
      }

      @Singleton
      @MergeComponent(AppScope::class)
      interface AppComponent {
        @Component.Factory
        interface Factory {
          fun create(
            @BindsInstance str: String,
            @BindsInstance strs: List<String>,
          ): AppComponent
        }
      }
     """
    ) {

      val component = appComponentFactoryCreate("name", listOf("strs"))

      TangleGraph.add(component)

      val target = targetClass.createInstance()

      targetClass.methods
        .first { it.name == "onCreate" }
        .invoke(target)

      target.fieldsValues() shouldBe mapOf("str" to "name", "strs" to listOf("strs"))
    }

  @Test
  fun `target may be injected with declared injected members from subcomponent`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.ContributesTo
      import com.squareup.anvil.annotations.MergeComponent
      import com.squareup.anvil.annotations.MergeSubcomponent
      import dagger.BindsInstance
      import dagger.Component
      import dagger.Module
      import dagger.Provides
      import tangle.inject.TangleGraph
      import tangle.inject.test.utils.AppScope
      import tangle.inject.test.utils.UserScope
      import tangle.inject.TangleScope
      import javax.inject.Inject
      import javax.inject.Singleton

      @TangleScope(UserScope::class)
      class Target {
        @Inject lateinit var str: String
      }

      @Singleton
      @MergeComponent(AppScope::class)
      interface AppComponent {

        val userComponent: UserComponent
      }

      @MergeSubcomponent(UserScope::class)
      interface UserComponent

      @Module
      @ContributesTo(UserScope::class)
      object StrModule {
        @Provides
        fun provideStr(): String = "name"
      }
     """
    ) {

      val component = daggerAppComponent.createFunction()
        .invoke(null)

      val userComponent: Any = appComponent.getPrivateFieldByName("userComponent", component)

      TangleGraph.add(component)
      TangleGraph.add(userComponent)

      val target = targetClass.createInstance()

      TangleGraph.inject(target)

      target.fieldsValues() shouldBe mapOf("str" to "name")
    }

  @Test
  fun `build will fail if target requires dependency from different scope`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.ContributesTo
      import com.squareup.anvil.annotations.MergeComponent
      import com.squareup.anvil.annotations.MergeSubcomponent
      import dagger.BindsInstance
      import dagger.Component
      import dagger.Module
      import dagger.Provides
      import tangle.inject.TangleGraph
      import tangle.inject.test.utils.AppScope
      import tangle.inject.test.utils.UserScope
      import tangle.inject.TangleScope
      import javax.inject.Inject
      import javax.inject.Singleton

      @TangleScope(AppScope::class)
      class Target {
        @Inject lateinit var str: String
      }

      @Singleton
      @MergeComponent(AppScope::class)
      interface AppComponent {

        val userComponent: UserComponent
      }

      @MergeSubcomponent(UserScope::class)
      interface UserComponent

      @Module
      @ContributesTo(UserScope::class)
      object StrModule {
        @Provides
        fun provideStr(): String = "name"
      }
     """,
      shouldFail = true
    ) {

      messages shouldContain """[Dagger/MissingBinding] java.lang.String cannot be provided without an @Inject constructor or an @Provides-annotated method"""
    }

  @Test
  fun `build will fail if target has TangleScope and injected constructor`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.ContributesTo
      import com.squareup.anvil.annotations.MergeComponent
      import com.squareup.anvil.annotations.MergeSubcomponent
      import dagger.BindsInstance
      import dagger.Component
      import dagger.Module
      import dagger.Provides
      import tangle.inject.TangleGraph
      import tangle.inject.test.utils.AppScope
      import tangle.inject.test.utils.UserScope
      import tangle.inject.TangleScope
      import javax.inject.Inject
      import javax.inject.Singleton

      @TangleScope(AppScope::class)
      class Target @Inject constructor() {
        @Inject lateinit var str: String
      }
     """,
      shouldFail = true
    ) {

      messages shouldContain """@TangleScope cannot be applied to classes which use injected constructors"""
    }
}
