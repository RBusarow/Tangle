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

package tangle.sample.ui.composeWithFragments.breedDetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import tangle.fragment.ContributesFragment
import tangle.fragment.FragmentInject
import tangle.fragment.FragmentInjectFactory
import tangle.inject.TangleParam
import tangle.sample.core.AppScope
import tangle.sample.core.isMetric
import tangle.sample.ui.composeWithFragments.BaseComposeFragment
import tangle.viewmodel.compose.tangleViewModel
import java.util.Locale

@ContributesFragment(AppScope::class)
class BreedDetailFragment @FragmentInject constructor() : BaseComposeFragment() {
  override val ui = @Composable {

    val viewModel = tangleViewModel<BreedDetailViewModel>()

    val breedDetail = viewModel.detailFlow
      .collectAsState()
      .value

    if (breedDetail != null) {
      BreedDetail(
        Locale.getDefault().isMetric(),
        breedDetail
      ) { viewModel.onTextSelected(it) }
    }
  }

  @FragmentInjectFactory
  interface Factory {
    fun create(
      @TangleParam("breedId")
      breedId: Int
    ): BreedDetailFragment
  }
}
