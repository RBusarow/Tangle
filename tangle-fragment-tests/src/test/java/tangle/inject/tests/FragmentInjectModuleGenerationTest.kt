package tangle.inject.tests

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.TestFactory
import tangle.inject.test.utils.*
import kotlin.reflect.full.functions

class FragmentInjectModuleGenerationTest : BaseTest() {

  @TestFactory
  fun `module scope should match contributed scope`() = test {
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
      tangleUnitFragmentInjectModuleClass
        .getAnnotation(ContributesTo::class.java)!!.scope shouldBe Unit::class
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
