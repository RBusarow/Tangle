package tangle.inject.tests

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
        """
      package tangle.inject.tests

      import androidx.lifecycle.SavedStateHandle
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.*
      import javax.inject.Inject

      class MyViewModel @VMInject constructor(
        @VMAssisted val factory: () -> Unit
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
  fun `assisted arguments can't be function types`() =
    test {
      compile(
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
          "They would leak the initial caller's instance into the ViewModelStore."
      }
    }
}
