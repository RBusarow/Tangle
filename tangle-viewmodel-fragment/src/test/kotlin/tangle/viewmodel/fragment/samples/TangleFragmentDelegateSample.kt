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

package tangle.viewmodel.fragment.samples

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import tangle.inject.test.utils.Sample
import tangle.viewmodel.AssistedViewModel
import tangle.viewmodel.VMAssisted
import tangle.viewmodel.VMInject
import tangle.viewmodel.VMInjectFactory
import tangle.viewmodel.fragment.MyViewModel
import tangle.viewmodel.fragment.samples.MyAssistedViewModel2.MyFactory2
import tangle.viewmodel.fragment.tangleAssisted
import tangle.viewmodel.fragment.tangleViewModel

class TangleFragmentDelegateSample {

  @Sample
  fun byTangleViewModelSample() {
    class MyFragment : Fragment() {

      val viewModel: MyViewModel by tangleViewModel<MyViewModel>()
    }
  }

  @Sample
  fun byTangleAssistedSample() {
    class MyFragment : Fragment() {

      val viewModel: MyAssistedViewModel2 by tangleAssisted {
        create("name")
      }
    }
  }
}

class MyViewModel @VMInject constructor() : ViewModel()

class MyAssistedViewModel2 @VMInject constructor(
  @VMAssisted val name: String
) : ViewModel(),
    AssistedViewModel<MyFactory2> {

  @VMInjectFactory
  interface MyFactory2 {
    fun create(name: String): MyAssistedViewModel2
  }
}
