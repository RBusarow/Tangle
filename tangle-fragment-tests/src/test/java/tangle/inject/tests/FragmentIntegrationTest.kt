package tangle.inject.tests

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.junit.jupiter.api.Test
import tangle.fragment.TangleFragmentProviderMap
import tangle.inject.test.utils.BaseTest
import tangle.inject.test.utils.createFunction
import tangle.inject.test.utils.daggerAppComponent
import tangle.inject.test.utils.invokeGet
import tangle.inject.test.utils.myFragmentClass
import tangle.inject.test.utils.property
import javax.inject.Provider
import kotlin.reflect.KProperty1

interface FragmentComponent {
  @get:TangleFragmentProviderMap
  val tangleProviderMap: Map<Class<out Fragment>, @JvmSuppressWildcards Fragment>
  val providerMap: Map<Class<out Fragment>, @JvmSuppressWildcards Fragment>
  val fragmentFactory: FragmentFactory
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
        .invoke(null)
        .cast<FragmentComponent>()

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
        .invoke(null)
        .cast<FragmentComponent>()

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
        .invoke(null)
        .cast<FragmentComponent>()

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
        .invoke(null)
        .cast<FragmentComponent>()

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
        .cast<FragmentComponent>()
      val fragment = component::class.property("myFragmentProvider")
        .cast<KProperty1<FragmentComponent, Provider<Fragment>>>()
        .get(component)
        .invokeGet()

      fragment::class.java shouldBe myFragmentClass
    }

  @Test
  fun `empty multibindings are created if no ViewModels are bound`() = compileWithDagger(
    """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import javax.inject.Singleton

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent
     """
  )
}
