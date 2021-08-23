package tangle.viewmodel

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import tangle.inject.InternalTangleApi
import tangle.inject.TangleGraph
import tangle.inject.test.utils.BaseTest
import tangle.inject.test.utils.createFunction
import tangle.inject.test.utils.daggerAppComponent
import tangle.inject.test.utils.myViewModelClass

@OptIn(InternalTangleApi::class)
@Execution(ExecutionMode.SAME_THREAD)
class TangleGraphTest : BaseTest() {

  @BeforeEach
  fun beforeEach() {
    clearTangleGraph()
  }

  @Test
  fun `should hold viewModel keys`() = compileWithDagger(
    //language=kotlin
    """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.VMInject
      import tangle.inject.TangleGraph
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

    val keys = TangleGraph.get<TangleViewModelComponent>()
      .tangleViewModelKeysSubcomponentFactory
      .create()
      .viewModelKeys

    keys shouldBe setOf(myViewModelClass)
  }

  @Test
  fun `should provide ViewModel map subcomponent factory`() = compileWithDagger(
    //language=kotlin
    """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.VMInject
      import tangle.inject.TangleGraph
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

    val factory = TangleGraph.get<TangleViewModelComponent>()
      .tangleViewModelMapSubcomponentFactory

    factory.shouldBeInstanceOf<TangleViewModelMapSubcomponent.Factory>()
  }
}
