package tangle.inject.tests

import androidx.lifecycle.SavedStateHandle
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.junit.jupiter.api.Test
import tangle.inject.test.utils.BaseTest
import tangle.inject.test.utils.createFunction
import tangle.inject.test.utils.daggerAppComponent
import tangle.inject.test.utils.myViewModelClass
import tangle.viewmodel.TangleViewModelComponent

class ViewModelIntegrationTest : BaseTest() {

  @Test
  fun `viewmodel is multi-bound into TangleScope`() = compileWithDagger(
    """
      package tangleViewModel.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.lifecycle.ViewModel
      import tangleViewModel.viewmodel.VMInject
      import tangleViewModel.viewmodel.TangleGraph
      import javax.inject.Singleton

      class MyViewModel @VMInject constructor() : ViewModel()

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent
     """
  ) {

    val component = daggerAppComponent.createFunction()
      .invoke(null)
      .cast<TangleViewModelComponent>()

    val mapSubcomponent = component.tangleViewModelMapSubcomponentFactory
      .create(SavedStateHandle())

    val map = mapSubcomponent.viewModelProviderMap

    map.size shouldBe 1
    map[myViewModelClass]!!.get()::class.java shouldBe myViewModelClass
  }

  @Test
  fun `viewmodel key is multi-bound into TangleAppScope`() = compileWithDagger(
    """
      package tangleViewModel.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.lifecycle.ViewModel
      import tangleViewModel.viewmodel.VMInject
      import tangleViewModel.viewmodel.TangleGraph
      import javax.inject.Singleton

      class MyViewModel @VMInject constructor() : ViewModel()

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent
     """
  ) {

    val component = daggerAppComponent.createFunction()
      .invoke(null)
      .cast<TangleViewModelComponent>()

    val keysSubcomponent = component.tangleViewModelKeysSubcomponentFactory
      .create()

    keysSubcomponent.viewModelKeys shouldBe setOf(myViewModelClass)
  }
}
