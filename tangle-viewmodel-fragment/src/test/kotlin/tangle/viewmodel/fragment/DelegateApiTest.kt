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

package tangle.viewmodel.fragment

import org.junit.jupiter.api.Test
import tangle.inject.test.utils.BaseTest

class DelegateApiTest : BaseTest() {

  @Test
  fun `assisted viewModel using factory and inferred type should build`() {

    compileWithDagger(
      //language=kotlin
      """
      package tangle.viewmodel.fragment

      import androidx.fragment.app.Fragment
      import tangle.viewmodel.fragment.MyAssistedViewModel
      import tangle.viewmodel.fragment.MyAssistedViewModel.MyFactory
      import tangle.viewmodel.fragment.tangleAssisted

      class MyFragment : Fragment() {

        val viewModel: MyAssistedViewModel by tangleAssisted { create("name") }
      }
      """
    )
  }

  @Test
  fun `assisted viewModel using factory and explicit type should build`() {

    compileWithDagger(
      //language=kotlin
      """
      package tangle.viewmodel.fragment

      import androidx.fragment.app.Fragment
      import tangle.viewmodel.fragment.MyAssistedViewModel
      import tangle.viewmodel.fragment.MyAssistedViewModel.MyFactory
      import tangle.viewmodel.fragment.tangleAssisted

      class MyFragment : Fragment() {

        val viewModel by tangleAssisted<MyAssistedViewModel, MyAssistedViewModel.MyFactory> { create("name") }
      }
      """
    )
  }

  @Test
  fun `assisted viewModel using no-arg delegate and inferred type should NOT build`() {

    compileWithDagger(
      //language=kotlin
      """
      package tangle.viewmodel.fragment

      import androidx.fragment.app.Fragment
      import tangle.viewmodel.fragment.MyAssistedViewModel
      import tangle.viewmodel.fragment.tangleAssisted

      class MyFragment : Fragment() {

        val viewModel: MyAssistedViewModel by tangleViewModel()
      }
      """,
      shouldFail = true
    )
  }

  @Test
  fun `assisted viewModel using no-arg delegate and explicit type should NOT build`() {

    compileWithDagger(
      //language=kotlin
      """
      package tangle.viewmodel.fragment

      import androidx.fragment.app.Fragment
      import tangle.viewmodel.fragment.MyAssistedViewModel
      import tangle.viewmodel.fragment.tangleViewModel

      class MyFragment : Fragment() {

        val viewModel by tangleViewModel<MyAssistedViewModel>()
      }
      """,
      shouldFail = true
    )
  }
}
