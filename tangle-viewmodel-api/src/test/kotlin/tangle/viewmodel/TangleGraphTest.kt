/*
 * Copyright (C) 2021 Rick Busarow
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

    TangleGraph.add(component)

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

    TangleGraph.add(component)

    val factory = TangleGraph.get<TangleViewModelComponent>()
      .tangleViewModelMapSubcomponentFactory

    factory.shouldBeInstanceOf<TangleViewModelMapSubcomponent.Factory>()
  }
}
