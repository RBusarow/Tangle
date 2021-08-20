package tangle.fragment

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.TestFactory
import tangle.inject.test.utils.BaseTest
import tangle.inject.test.utils.annotationClasses
import tangle.inject.test.utils.myFragmentFactoryImplClass
import tangle.inject.test.utils.tangleUnitFragmentInjectModuleClass
import kotlin.reflect.full.functions

class FragmentInjectModuleGenerationTest : BaseTest() {

  @TestFactory
  fun `module scope should match contributed scope`() = test {
    compile(
      //language=kotlin
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
      tangleUnitFragmentInjectModuleClass
        .getAnnotation(ContributesTo::class.java)!!.scope shouldBe Unit::class
    }
  }

  @TestFactory
  fun `FragmentInject annotation gets qualified map binding`() = test {
    compile(
      //language=kotlin
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
      tangleUnitFragmentInjectModuleClass.annotationClasses() shouldContainExactly listOf(
        Module::class,
        ContributesTo::class,
        Metadata::class
      )

      tangleUnitFragmentInjectModuleClass
        .kotlin
        .functions
        .first { it.name == "provide_MyFragment_Factory" }
        .call(tangleUnitFragmentInjectModuleClass.kotlin.objectInstance)!!::class.java shouldBe myFragmentFactoryImplClass
    }
  }
}
