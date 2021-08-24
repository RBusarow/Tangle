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

import androidx.lifecycle.SavedStateHandle
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.junit.jupiter.api.Test
import tangle.inject.InternalTangleApi
import tangle.inject.test.utils.*

@OptIn(InternalTangleApi::class)
class ViewModelIntegrationTest : BaseTest() {

  @Test
  fun `viewmodel is multi-bound into TangleScope`() = compileWithDagger(
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
      .invoke(null)
      .cast<TangleViewModelComponent>()

    val keysSubcomponent = component.tangleViewModelKeysSubcomponentFactory
      .create()

    keysSubcomponent.viewModelKeys shouldBe setOf(myViewModelClass)
  }

  @Test
  fun `assisted-only graph does not try to bind providers`() = compileWithDagger(
    //language=kotlin
    """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.lifecycle.ViewModel
      import tangle.viewmodel.VMAssisted
      import tangle.viewmodel.VMInject
      import tangle.viewmodel.VMInjectFactory
      import tangle.viewmodel.TangleGraph
      import javax.inject.Singleton
      import javax.inject.Inject

      class Repository @Inject constructor()

      class MyViewModel @VMInject constructor(
        @VMAssisted repository: Repository
      ) : ViewModel() {
        @VMInjectFactory
        interface Factory {
          fun create(repository: Repository): MyViewModel
        }
      }

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

    val mapSubcomponent = component.tangleViewModelMapSubcomponentFactory
      .create(SavedStateHandle())

    keysSubcomponent.viewModelKeys shouldBe setOf()
    mapSubcomponent.viewModelProviderMap shouldBe mapOf()
    mapSubcomponent.viewModelFactoryMap.keys shouldBe setOf(myViewModelFactoryClass)
  }

  @Test
  fun `two components in classpath with same scope should not get duplicate bindings`() =
    //language=kotlin
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import javax.inject.Singleton

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent
     """,
      """
      package tangle.inject.tests.other

      import com.squareup.anvil.annotations.MergeComponent
      import javax.inject.Singleton

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent2
     """
    )

  @Test
  fun `pre-existing Subcomponent factory Module in classpath should not get duplicate bindings`() =
    //language=kotlin
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import androidx.fragment.app.Fragment
      import tangle.fragment.*
      import tangle.inject.*
      import javax.inject.*

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent

      @ContributesFragment(Unit::class)
      class MyFragment @Inject constructor(
        val factory: TangleFragmentFactory
      ) : Fragment()
     """,
      """
      package tangle.viewmodel

      import com.squareup.anvil.annotations.ContributesTo
      import dagger.Binds
      import dagger.Module
      import kotlin.Suppress
      import kotlin.Unit
      import tangle.inject.tests.TangleAppScope_Tangle_ViewModel_Keys_Subcomponent
      import tangle.inject.tests.TangleScope_Tangle_ViewModel_Map_Subcomponent

      @ContributesTo(Unit::class)
      @Module(subcomponents = [TangleScope_Tangle_ViewModel_Map_Subcomponent::class, TangleAppScope_Tangle_ViewModel_Keys_Subcomponent::class])
      public interface Unit_Tangle_ViewModel_SubcomponentFactory_Module {
        @Binds
        public
            fun bindTangleScope_Tangle_ViewModel_Map_Subcomponent_FactoryIntoSet(factory: TangleScope_Tangle_ViewModel_Map_Subcomponent.Factory):
            TangleViewModelMapSubcomponent.Factory

        @Binds
        public
            fun bindTangleAppScope_Tangle_ViewModel_Keys_Subcomponent_FactoryIntoSet(factory: TangleAppScope_Tangle_ViewModel_Keys_Subcomponent.Factory):
            TangleViewModelKeysSubcomponent.Factory
      }
     """
    )

  @Test
  fun `two components in same package with same scope should not get duplicate bindings`() =
    //language=kotlin
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import javax.inject.Singleton

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent2
     """
    )
}
