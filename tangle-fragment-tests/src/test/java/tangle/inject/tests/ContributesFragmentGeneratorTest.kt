package tangle.inject.tests

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.TestFactory
import tangle.fragment.FragmentKey
import tangle.fragment.TangleFragmentProviderMap
import tangle.inject.test.utils.*
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

class ContributesFragmentGeneratorTest : BaseTest() {

  @TestFactory
  fun `regular inject annotation gets unqualified map binding`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import javax.inject.Inject

      @ContributesFragment(Unit::class)
      class MyFragment @Inject constructor() : Fragment()
      """
    ) {
      bindMyFragment.annotationClasses() shouldContainExactly listOf(
        Binds::class,
        IntoMap::class,
        FragmentKey::class
      )

      bindMyFragment.getAnnotation(FragmentKey::class.java)!!.value shouldBe myFragmentClass.kotlin
    }
  }

  @TestFactory
  fun `FragmentInject annotation gets qualified map binding`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.TangleParam

      @ContributesFragment(Unit::class)
      class MyFragment @FragmentInject constructor() : Fragment() {

        @FragmentInjectFactory
        interface Factory {
          fun create(@TangleParam("name") name: String): MyFragment
        }
      }
      """
    ) {
      bindMyFragment.annotationClasses() shouldContainExactly listOf(
        Binds::class,
        IntoMap::class,
        FragmentKey::class,
        TangleFragmentProviderMap::class
      )

      bindMyFragment.getAnnotation(FragmentKey::class.java)!!.value shouldBe myFragmentClass.kotlin
    }
  }

  @TestFactory
  fun `FragmentInject annotation gets provider function`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.TangleParam

      @ContributesFragment(Unit::class)
      class MyFragment @FragmentInject constructor() : Fragment() {

        @FragmentInjectFactory
        interface Factory {
          fun create(@TangleParam("name") name: String): MyFragment
        }
      }
      """
    ) {
      provideMyFragment.annotationClasses() shouldContainExactly listOf(
        Provides::class,
        TangleFragmentProviderMap::class
      )

      val moduleClass = tangleUnitFragmentModuleClass.kotlin

      moduleClass.companionObject!!.functions
        .first { it.name == "provide_MyFragment" }
        .call(moduleClass.companionObjectInstance)!!::class.java shouldBe myFragmentClass
    }
  }

  @TestFactory
  fun `module scope should match contributed scope`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import javax.inject.Inject

      @ContributesFragment(Unit::class)
      class MyFragment @Inject constructor() : Fragment()
      """
    ) {
      tangleUnitFragmentModuleClass.annotationClasses() shouldContainExactly listOf(
        Module::class,
        ContributesTo::class,
        Metadata::class
      )

      tangleUnitFragmentModuleClass
        .getAnnotation(ContributesTo::class.java)!!.scope shouldBe Unit::class
    }
  }
}
