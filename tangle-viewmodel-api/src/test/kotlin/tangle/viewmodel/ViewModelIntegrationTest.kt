/*
 * Copyright (C) 2022 Rick Busarow
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
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import tangle.inject.InternalTangleApi
import tangle.inject.test.utils.BaseTest
import tangle.inject.test.utils.appComponentFactoryCreate
import tangle.inject.test.utils.createFunction
import tangle.inject.test.utils.daggerAppComponent
import tangle.inject.test.utils.myViewModelClass
import tangle.inject.test.utils.propertyValue
import tangle.inject.test.utils.targetClass

@OptIn(InternalTangleApi::class)
class ViewModelIntegrationTest : BaseTest() {

  @Test
  fun `viewmodel is multi-bound into TangleViewModelScope`() = compileWithDagger(
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
      .invoke(null) as TangleViewModelComponent

    val mapSubcomponent = component.tangleViewModelMapSubcomponentFactory
      .create(SavedStateHandle())

    val map = mapSubcomponent.viewModelProviderMap

    map.size shouldBe 1
    map[myViewModelClass]!!.get()::class.java shouldBe myViewModelClass
  }

  @Test
  fun `viewmodel key is multi-bound into TangleAppScope`() = compileWithDagger(
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
      .invoke(null)as TangleViewModelComponent

    val keysSubcomponent = component.tangleViewModelKeysSubcomponentFactory
      .create()

    keysSubcomponent.viewModelKeys shouldBe setOf(myViewModelClass)
  }

  @Test
  fun `two components in classpath with same scope should not get duplicate bindings`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import javax.inject.Singleton

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent
     """,
      //language=kotlin
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
      //language=kotlin
      """
      package tangle.viewmodel

      import com.squareup.anvil.annotations.ContributesTo
      import dagger.Binds
      import dagger.Module
      import kotlin.Suppress
      import kotlin.Unit
      import tangle.inject.tests.TangleAppScope_Tangle_ViewModel_Keys_Subcomponent
      import tangle.inject.tests.TangleViewModelScope_Tangle_ViewModel_Map_Subcomponent

      @ContributesTo(Unit::class)
      @Module(subcomponents = [TangleViewModelScope_Tangle_ViewModel_Map_Subcomponent::class, TangleAppScope_Tangle_ViewModel_Keys_Subcomponent::class])
      public interface Unit_Tangle_ViewModel_SubcomponentFactory_Module {
        @Binds
        public
            fun bindTangleViewModelScope_Tangle_ViewModel_Map_Subcomponent_FactoryIntoSet(factory: TangleViewModelScope_Tangle_ViewModel_Map_Subcomponent.Factory):
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

  @Test
  fun `viewModel arguments with typed qualifiers get qualified bindings`() = compileWithDagger(
    """
    package tangle.inject.tests

    import androidx.lifecycle.ViewModel
    import com.squareup.anvil.annotations.ContributesTo
    import com.squareup.anvil.annotations.MergeComponent
    import dagger.BindsInstance
    import dagger.Component
    import tangle.inject.test.utils.AppScope
    import javax.inject.Singleton
    import tangle.viewmodel.VMInject
    import javax.inject.Qualifier

    @Qualifier
    annotation class SomeQualifier

    @Qualifier
    annotation class SomeOtherQualifier

    @Singleton
    @MergeComponent(AppScope::class)
    interface AppComponent {
      @Component.Factory
      interface Factory {
        fun create(
          @BindsInstance
          @SomeQualifier
          someArg: String,

          @BindsInstance
          @SomeOtherQualifier
          someOtherArg: String,
        ): AppComponent
      }
    }

    class Target @VMInject constructor(
      @SomeQualifier
      val someArg: String
    ) : ViewModel() {
      fun get() = someArg
    }
    """
  ) {
    val expected = "Expected"
    val unexpected = "Unexpected"

    val viewModel = (appComponentFactoryCreate(expected, unexpected) as TangleViewModelComponent)
      .tangleViewModelMapSubcomponentFactory
      .create(SavedStateHandle())
      .viewModelProviderMap[targetClass]!!
      .get()!!

    viewModel::class.java shouldBe targetClass

    viewModel.propertyValue<String>("someArg") shouldBe expected
  }

  @Test
  fun `viewModel arguments with named qualifiers get qualified bindings`() = compileWithDagger(
    """
    package tangle.inject.tests

    import androidx.lifecycle.ViewModel
    import com.squareup.anvil.annotations.ContributesTo
    import com.squareup.anvil.annotations.MergeComponent
    import dagger.BindsInstance
    import dagger.Component
    import javax.inject.Named
    import javax.inject.Singleton
    import tangle.inject.test.utils.AppScope
    import tangle.viewmodel.VMInject

    @Singleton
    @MergeComponent(AppScope::class)
    interface AppComponent {
      @Component.Factory
      interface Factory {
        fun create(
          @BindsInstance
          @Named("A")
          someArg: String,

          @BindsInstance
          @Named("B")
          someOtherArg: String,
        ): AppComponent
      }
    }

    class Target @VMInject constructor(
      @Named("A")
      val someArg: String
    ) : ViewModel()
    """
  ) {
    val expected = "Expected"
    val unexpected = "Unexpected"

    val viewModel = (appComponentFactoryCreate(expected, unexpected) as TangleViewModelComponent)
      .tangleViewModelMapSubcomponentFactory
      .create(SavedStateHandle.createHandle(null, null))
      .viewModelProviderMap[targetClass]!!
      .get()!!

    viewModel::class.java shouldBe targetClass

    viewModel.propertyValue<String>("someArg") shouldBe expected
  }

  @Test
  fun `qualified argument without binding should fail`() = compileWithDagger(
    """
    package tangle.inject.test

    import androidx.lifecycle.ViewModel
    import com.squareup.anvil.annotations.MergeComponent
    import dagger.Component
    import dagger.BindsInstance
    import dagger.Provides
    import tangle.viewmodel.VMInject
    import javax.inject.Qualifier
    import javax.inject.Singleton
    import tangle.inject.test.utils.AppScope

    @Qualifier
    annotation class SomeQualifier

    @Singleton
    @MergeComponent(AppScope::class)
    interface AppComponent {
      @Component.Factory
      interface Factory {
        fun create(@BindsInstance unqualified: String): AppComponent
      }
    }

    class Target @VMInject constructor(
      @SomeQualifier
      val someArg: String
    ) : ViewModel()
    """,
    shouldFail = true
  ) {
    messages shouldContain "[Dagger/MissingBinding] @tangle.inject.test.SomeQualifier " +
      "java.lang.String cannot be provided without an @Provides-annotated method."
  }
}
