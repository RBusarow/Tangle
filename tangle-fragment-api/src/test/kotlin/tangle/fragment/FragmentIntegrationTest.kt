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

package tangle.fragment

import androidx.fragment.app.Fragment
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import tangle.inject.test.utils.BaseTest
import tangle.inject.test.utils.appComponent
import tangle.inject.test.utils.createFunction
import tangle.inject.test.utils.daggerAppComponent
import tangle.inject.test.utils.getPrivateFieldByName
import tangle.inject.test.utils.myFragmentClass
import javax.inject.Provider

interface FragmentComponent {
  @get:TangleFragmentProviderMap
  val tangleProviderMap: Map<Class<out Fragment>, @JvmSuppressWildcards Fragment>
  val providerMap: Map<Class<out Fragment>, @JvmSuppressWildcards Fragment>
  val fragmentFactory: TangleFragmentFactory
}

class FragmentIntegrationTest : BaseTest() {

  @Test
  fun `fragment with FragmentInjectFactory is multi-bound into TangleFragmentProviderMap`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.*
      import javax.inject.*

      @ContributesFragment(Unit::class)
      class MyFragment @FragmentInject constructor() : Fragment() {

        @FragmentInjectFactory
        interface Factory {
          fun create(@TangleParam("name") name: String): MyFragment
        }
      }

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent : FragmentComponent
     """
    ) {

      val component = daggerAppComponent.createFunction()
        .invoke(null) as FragmentComponent

      val fragment = component.tangleProviderMap[myFragmentClass]!!

      fragment::class.java shouldBe myFragmentClass
    }

  @Test
  fun `fragment with FragmentInjectFactory can be created by TangleFragmentFactory`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.*
      import javax.inject.*

      @ContributesFragment(Unit::class)
      class MyFragment @FragmentInject constructor() : Fragment() {

        @FragmentInjectFactory
        interface Factory {
          fun create(@TangleParam("name") name: String): MyFragment
        }
      }

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent : FragmentComponent
     """
    ) {

      val component = daggerAppComponent.createFunction()
        .invoke(null) as FragmentComponent

      val factory = component.fragmentFactory

      factory.instantiate(
        classLoader,
        myFragmentClass.canonicalName!!
      )::class.java shouldBe myFragmentClass
    }

  @Test
  fun `fragment with FragmentInjectFactory cannot be injected as Provider`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.*
      import javax.inject.*

      @ContributesFragment(Unit::class)
      class MyFragment @FragmentInject constructor() : Fragment() {

        @FragmentInjectFactory
        interface Factory {
          fun create(@TangleParam("name") name: String): MyFragment
        }
      }

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent : FragmentComponent {
        val fragmentProvider: Provider<MyFragment>
      }
     """,
      shouldFail = true
    ) {

      messages shouldContain "[Dagger/MissingBinding] tangle.inject.tests.MyFragment " +
        "cannot be provided without an @Inject constructor or an @Provides-annotated method."
    }

  @Test
  fun `fragment with normal Inject constructor is multi-bound into unqualified map`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.*
      import javax.inject.*

      @ContributesFragment(Unit::class)
      class MyFragment @Inject constructor() : Fragment()

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent : FragmentComponent
     """
    ) {

      val component = daggerAppComponent.createFunction()
        .invoke(null) as FragmentComponent

      val fragment = component.providerMap[myFragmentClass]!!

      fragment::class.java shouldBe myFragmentClass
    }

  @Test
  fun `fragment with normal Inject constructor can be created by TangleFragmentFactory`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.*
      import javax.inject.*

      @ContributesFragment(Unit::class)
      class MyFragment @Inject constructor(): Fragment()

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent : FragmentComponent
     """
    ) {

      val component = daggerAppComponent.createFunction()
        .invoke(null) as FragmentComponent

      val factory = component.fragmentFactory

      factory.instantiate(
        classLoader,
        myFragmentClass.canonicalName!!
      )::class.java shouldBe myFragmentClass
    }

  @Test
  fun `fragment with normal Inject constructor can be injected as Provider`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.*
      import javax.inject.*

      @ContributesFragment(Unit::class)
      class MyFragment @Inject constructor() : Fragment()

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent : FragmentComponent {
        val myFragmentProvider: Provider<MyFragment>
      }
     """
    ) {

      val component = daggerAppComponent.createFunction()
        .invoke(null)

      requireNotNull(component)

      val provider: Provider<Fragment> = appComponent.getPrivateFieldByName(
        "myFragmentProvider",
        component
      )

      val fragment = provider.get()

      fragment::class.java shouldBe myFragmentClass
    }

  @Test
  fun `empty multibindings are created if no Fragments are bound`() = compileWithDagger(
    """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import javax.inject.Singleton

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent
     """
  )

  @Test
  fun `two components in classpath with same scope should not get duplicate bindings`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.*
      import javax.inject.*

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent

      @ContributesFragment(Unit::class)
      class MyFragment @Inject constructor(
        val factory: TangleFragmentFactory
      ) : Fragment()
     """,
      """
      package tangle.inject.tests.other

      import com.squareup.anvil.annotations.MergeComponent
      import javax.inject.Singleton

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent2
     """
    )

  @Test
  fun `pre-existing FragmentFactory Module in classpath should not get duplicate bindings`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.*
      import javax.inject.*

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent

      @ContributesFragment(Unit::class)
      class MyFragment @Inject constructor(
        val factory: TangleFragmentFactory
      ) : Fragment()
     """,
      //language=kotlin
      """
      package tangle.fragment

      import androidx.fragment.app.Fragment
      import com.squareup.anvil.annotations.ContributesTo
      import dagger.Module
      import dagger.Provides
      import dagger.multibindings.Multibinds
      import java.lang.Class
      import javax.inject.Provider
      import kotlin.Suppress
      import kotlin.Unit
      import kotlin.collections.Map
      import kotlin.jvm.JvmSuppressWildcards

      @Module
      @ContributesTo(Unit::class)
      public interface Unit_Tangle_FragmentFactory_Module {
        @Multibinds
        public fun bindProviderMap(): Map<Class<out androidx.fragment.app.Fragment>, @JvmSuppressWildcards
            Fragment>

        @Multibinds
        @TangleFragmentProviderMap
        public fun bindTangleProviderMap(): Map<Class<out androidx.fragment.app.Fragment>,
            @JvmSuppressWildcards Fragment>

        public companion object {
          @Provides
          public
              fun provideTangleFragmentFactory(providerMap: Map<Class<out androidx.fragment.app.Fragment>,
              @JvmSuppressWildcards Provider<@JvmSuppressWildcards Fragment>>, @TangleFragmentProviderMap
              tangleProviderMap: Map<Class<out androidx.fragment.app.Fragment>, @JvmSuppressWildcards
              Provider<@JvmSuppressWildcards Fragment>>): TangleFragmentFactory =
              TangleFragmentFactory(providerMap, tangleProviderMap)
        }
      }
     """
    )

  @Test
  fun `two components in same package with same scope should not get duplicate bindings`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.*
      import javax.inject.*

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent2

      @ContributesFragment(Unit::class)
      class MyFragment @Inject constructor(
        val factory: TangleFragmentFactory
      ) : Fragment()
     """
    )
}
