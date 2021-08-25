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

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.junit.jupiter.api.Assertions.*

import tangle.inject.test.utils.BaseTest
import tangle.viewmodel.Assisted.AF

internal class DogFoodTest : BaseTest()

class SimpleViewModel : ViewModel()

class Assisted : ViewModel(), AssistedViewModel<Assisted, AF> {
  interface AF {
    fun create(): Assisted
  }
}

class MyFragment : Fragment() {

  val assisted: Assisted by tangleViewModel { create() }

  val assisted2: Assisted by tangleViewModel() // should fail

  val viewModel: SimpleViewModel by tangleViewModel() // should resolve

  val viewModel2 by tangleViewModel<SimpleViewModel>()
}
