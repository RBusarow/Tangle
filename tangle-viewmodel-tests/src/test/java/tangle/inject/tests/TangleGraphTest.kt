package tangle.inject.tests

import androidx.lifecycle.ViewModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import tangle.inject.test.utils.BaseTest
import tangle.inject.test.utils.createFunction
import tangle.inject.test.utils.daggerAppComponent
import tangle.inject.test.utils.getPrivateFieldByName
import tangle.inject.test.utils.getPrivateFunctionByName
import tangle.inject.test.utils.myViewModelClass
import tangle.viewmodel.TangleGraph
import tangle.viewmodel.TangleViewModelMapSubcomponent
import kotlin.reflect.KClass

@Execution(ExecutionMode.SAME_THREAD)
class TangleGraphTest : BaseTest() {

  @BeforeEach
  fun beforeEach() {
    TangleGraph.clear()
  }

  @Test
  fun `should hold viewModel keys`() = compileWithDagger(
    """
      package tangleViewModel.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.VMInject
      import tangle.viewmodel.TangleGraph
      import javax.inject.Singleton

      class MyViewModel @VMInject constructor() : ViewModel()

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent
     """
  ) {

    val component = daggerAppComponent.createFunction()
      .invoke(null)!!

    TangleGraph.init(component)

    val keys: Set<KClass<ViewModel>> = TangleGraph.getPrivateFieldByName("tangleViewModelKeys")

    keys shouldBe setOf(myViewModelClass)
  }

  @Test
  fun `viewModel keys should persist`() = compileWithDagger(
    """
      package tangleViewModel.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.VMInject
      import tangle.viewmodel.TangleGraph
      import javax.inject.Singleton

      class MyViewModel @VMInject constructor() : ViewModel()

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent
     """
  ) {

    val component = daggerAppComponent.createFunction()
      .invoke(null)!!

    TangleGraph.init(component)

    val first: Set<KClass<ViewModel>> = TangleGraph.getPrivateFieldByName("tangleViewModelKeys")
    val second: Set<KClass<ViewModel>> = TangleGraph.getPrivateFieldByName("tangleViewModelKeys")

    first shouldBeSameInstanceAs second
  }

  @Test
  fun `should provide ViewModel map subcomponent factory`() = compileWithDagger(
    """
      package tangleViewModel.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.VMInject
      import tangle.viewmodel.TangleGraph
      import javax.inject.Singleton

      class MyViewModel @VMInject constructor() : ViewModel()

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent
     """
  ) {

    val component = daggerAppComponent.createFunction()
      .invoke(null)!!

    TangleGraph.init(component)

    val factory = TangleGraph.getPrivateFunctionByName("tangleViewModelSubcomponentFactory")

    factory.shouldBeInstanceOf<TangleViewModelMapSubcomponent.Factory>()
  }
}
