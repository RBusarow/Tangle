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

package tangle.sample.ui.fragmentsWithArchNavigation.breedList

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dispatch.android.lifecycle.withViewLifecycle
import dispatch.core.MainImmediateCoroutineScope
import kotlinx.coroutines.flow.onEach
import tangle.fragment.ContributesFragment
import tangle.sample.core.AppScope
import tangle.sample.core.viewBinding
import tangle.sample.ui.R
import tangle.sample.ui.databinding.FragmentBreedListBinding
import tangle.viewmodel.fragment.tangleViewModel
import javax.inject.Inject

@ContributesFragment(AppScope::class)
class BreedListFragment @Inject constructor(
  private val coroutineScope: MainImmediateCoroutineScope
) : Fragment(R.layout.fragment_breed_list) {

  val binding by viewBinding(FragmentBreedListBinding::bind)

  val viewModel by tangleViewModel<BreedListViewModel>()

  val pagingAdapter = BreedListAdapter {

    findNavController().navigate(
      R.id.action_BreedListFragment_to_BreedDetailFragment,
      bundleOf("breedId" to it.id)
    )
  }

  init {
    coroutineScope.withViewLifecycle(this) {

      viewModel.pagingDataFlow
        .onEach { pagingData ->
          pagingAdapter.submitData(pagingData)
        }.launchOnStart()
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val recyclerView = binding.breedList
    recyclerView.adapter = pagingAdapter
  }
}
