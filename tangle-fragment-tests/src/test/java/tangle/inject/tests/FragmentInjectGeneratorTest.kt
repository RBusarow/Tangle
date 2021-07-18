package tangle.inject.tests

import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.COMPILATION_ERROR
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.TestFactory
import tangle.inject.test.utils.*
import javax.inject.Provider

class FragmentInjectGeneratorTest : BaseTest() {

  @TestFactory
  fun `FragmentInject constructors must have a corresponding factory interface`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import javax.inject.Inject

      @ContributesFragment(Unit::class)
      class MyFragment @FragmentInject constructor() : Fragment()
      """
    ) {
      exitCode shouldBe COMPILATION_ERROR

      messages shouldContain "@FragmentInject must only be applied to the constructor " +
        "of a Fragment, and that fragment must have a corresponding " +
        "FragmentInjectFactory-annotated factory interface."
    }
  }

  @TestFactory
  fun `factory interface must have a corresponding FragmentInject constructor`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import javax.inject.Inject

      @ContributesFragment(Unit::class)
      class MyFragment @Inject constructor() : Fragment() {

        @FragmentInjectFactory
        interface Factory {
          fun create(@TangleParam("name") name: String): MyFragment
        }
      }
      """
    ) {
      exitCode shouldBe COMPILATION_ERROR

      messages shouldContain "The @FragmentInjectFactory-annotated interface " +
        "`tangle.inject.tests.MyFragment.Factory` must be defined inside a Fragment " +
        "which is annotated with `@FragmentInject`."
    }
  }

  @TestFactory
  fun `factory interface must have a function`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import javax.inject.Inject

      @ContributesFragment(Unit::class)
      class MyFragment @FragmentInject constructor() : Fragment() {

        @FragmentInjectFactory
        interface Factory
      }
      """
    ) {
      exitCode shouldBe COMPILATION_ERROR

      messages shouldContain "@FragmentInjectFactory-annotated types must have exactly one " +
        "abstract function -- without a default implementation -- " +
        "which returns the FragmentInject Fragment type."
    }
  }

  @TestFactory
  fun `factory interface must have only one function`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.TangleParam
      import javax.inject.Inject

      @ContributesFragment(Unit::class)
      class MyFragment @FragmentInject constructor() : Fragment() {

        @FragmentInjectFactory
        interface Factory{
          fun create(@TangleParam("name") name: String): MyFragment
          fun create2(@TangleParam("name") name: String): MyFragment
        }
      }
      """
    ) {
      exitCode shouldBe COMPILATION_ERROR

      messages shouldContain "@FragmentInjectFactory-annotated types must have exactly one " +
        "abstract function -- without a default implementation -- " +
        "which returns the FragmentInject Fragment type."
    }
  }

  @TestFactory
  fun `factory interface must have a return type`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.TangleParam
      import javax.inject.Inject

      @ContributesFragment(Unit::class)
      class MyFragment @FragmentInject constructor() : Fragment() {

        @FragmentInjectFactory
        interface Factory{
          fun create(@TangleParam("name") name: String)
        }
      }
      """
    ) {
      exitCode shouldBe COMPILATION_ERROR

      messages shouldContain "Return type of 'create' is not a subtype of the return type " +
        "of the overridden member 'public abstract fun create(name: String): Unit " +
        "defined in tangle.inject.tests.MyFragment.Factory'"
    }
  }

  @TestFactory
  fun `factory interface return type may be supertype of the fragment`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.TangleParam
      import javax.inject.Inject

      @ContributesFragment(Unit::class)
      class MyFragment @FragmentInject constructor() : Fragment() {

        @FragmentInjectFactory
        interface Factory{
          fun create(@TangleParam("name") name: String): Fragment
        }
      }
      """
    ) {
      val factoryClass = myFragmentClass.factoryClass()
      val factoryInstance = factoryClass.createStatic()

      val factoryImplClass = myFragmentFactoryImplClass

      val factoryImplInstance = factoryImplClass.createInstance(factoryInstance)

      factoryImplClass.declaredMethods
        .filterNot { it.isStatic }
        .filter { it.name == "create" }
        .forEach {
          it.invoke(factoryImplInstance, "name")::class.java shouldBe myFragmentClass
        }
    }
  }

  @TestFactory
  fun `factory impl function name must match interface function name`() = test {
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
          fun pizza(
            @TangleParam("name") name: String
          ): MyFragment
        }
      }
     """
    ) {
      val factoryClass = myFragmentClass.factoryClass()
      val factoryInstance = factoryClass.createStatic()

      val factoryImplClass = myFragmentFactoryImplClass

      val factoryImplInstance = factoryImplClass.createInstance(factoryInstance)

      factoryImplClass.declaredMethods
        .filterNot { it.isStatic }
        .single { it.name == "pizza" }
        .invoke(factoryImplInstance, "name")::class.java shouldBe myFragmentClass
    }
  }

  @TestFactory
  fun `FragmentInject fragments must have ContributesFragment annotation`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.TangleParam

      class MyFragment @FragmentInject constructor() : Fragment() {

        @FragmentInjectFactory
        interface Factory {
          fun create(@TangleParam("name") name: String): MyFragment
        }
      }
      """
    ) {
      exitCode shouldBe COMPILATION_ERROR

      messages shouldContain "@FragmentInject-annotated Fragments must also have " +
        "a `tangle.fragment.ContributesFragment` class annotation."
    }
  }

  @TestFactory
  fun `factory arguments must be annotated with TangleParam`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.fragment.app.Fragment
      import tangle.fragment.*

      @ContributesFragment(Unit::class)
      class MyFragment @FragmentInject constructor() : Fragment() {

        @FragmentInjectFactory
        interface Factory {
          fun create(name: String): MyFragment
        }
      }
      """
    ) {
      exitCode shouldBe COMPILATION_ERROR

      messages shouldContain "could not find a @TangleParam annotation for parameter `name`"
    }
  }

  @TestFactory
  fun `factory is generated with only TangleParam arguments`() = test {
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
          fun create(
            @TangleParam("name") name: String
          ): MyFragment
        }
      }
     """
    ) {
      val factoryClass = myFragmentClass.factoryClass()

      val fragmentInstance = factoryClass.newInstanceStatic()
      val factoryInstance = factoryClass.createStatic()

      factoryInstance::class.java shouldBe factoryClass

      factoryInstance.factoryGet()::class.java shouldBe myFragmentClass

      fragmentInstance::class.java shouldBe myFragmentClass
    }
  }

  @TestFactory
  fun `factory is generated with only constructor arguments`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.fragment.app.Fragment
      import tangle.fragment.*

      @ContributesFragment(Unit::class)
      class MyFragment @FragmentInject constructor(
        val int: Int
      ) : Fragment(){

        @FragmentInjectFactory
        interface Factory {
          fun create(): MyFragment
        }
      }
     """
    ) {
      val factoryClass = myFragmentClass.factoryClass()

      val constructor = factoryClass.declaredConstructors.single()

      val factoryInstance = constructor.newInstance(Provider { 1 })

      factoryClass.getterFunction().invoke(factoryInstance)::class.java shouldBe myFragmentClass

      factoryClass.createStatic(Provider { 1 })::class.java shouldBe factoryClass
      factoryClass.newInstanceStatic(1)::class.java shouldBe myFragmentClass
    }
  }

  @TestFactory
  fun `factory is generated with TangleParam and constructor arguments of the same name`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.TangleParam

      @ContributesFragment(Unit::class)
      class MyFragment @FragmentInject constructor(
        val name: String
      ) : Fragment() {

        @FragmentInjectFactory
        interface Factory {
          fun create(
            @TangleParam("name") name: String
          ): MyFragment
        }
      }
     """
    ) {
      val factoryClass = myFragmentClass.factoryClass()

      val constructor = factoryClass.declaredConstructors.single()

      val factoryInstance = constructor.newInstance(Provider { "name" })

      factoryClass.getterFunction().invoke(factoryInstance)::class.java shouldBe myFragmentClass

      factoryClass.createStatic(Provider { "name" })::class.java shouldBe factoryClass
      factoryClass.newInstanceStatic("name")::class.java shouldBe myFragmentClass
    }
  }

  @TestFactory
  fun `injected arguments must be supported by Bundle`() = test {
    compile(
      """
      package tangle.inject.tests

      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.TangleParam

      class Illegal

      @ContributesFragment(Unit::class)
      class MyFragment @FragmentInject constructor() : Fragment() {

        @FragmentInjectFactory
        interface Factory {
          fun create(
            @TangleParam("name") name: Illegal
          ): MyFragment
        }
      }
     """
    ) {
      exitCode shouldBe COMPILATION_ERROR

      messages shouldContain "Tangle found Fragment runtime arguments which cannot " +
        "be inserted into a Bundle: [name: tangle.inject.tests.Illegal]"
    }
  }
}
