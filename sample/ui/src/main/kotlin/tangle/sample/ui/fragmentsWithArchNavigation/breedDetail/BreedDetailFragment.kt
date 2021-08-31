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

package tangle.sample.ui.fragmentsWithArchNavigation.breedDetail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import coil.load
import dispatch.android.lifecycle.withViewLifecycle
import dispatch.core.MainImmediateCoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import tangle.fragment.ContributesFragment
import tangle.sample.core.AppScope
import tangle.sample.core.isMetric
import tangle.sample.core.onEachLatest
import tangle.sample.core.viewBinding
import tangle.sample.data.breed.BreedDetail
import tangle.sample.ui.R
import tangle.sample.ui.databinding.FragmentBreedDetailBinding
import tangle.viewmodel.fragment.tangleViewModel
import java.util.Locale
import javax.inject.Inject

@ContributesFragment(AppScope::class)
class BreedDetailFragment @Inject constructor(
  private val coroutineScope: MainImmediateCoroutineScope
) : Fragment(R.layout.fragment_breed_detail) {

  val binding by viewBinding(FragmentBreedDetailBinding::bind)

  val viewModel: BreedDetailViewModel by tangleViewModel()

  init {
    coroutineScope.withViewLifecycle(this) {
      viewModel.itemDeferred
        .filterNotNull()
        .onEachLatest { binding.updateView(it) }
        .launchOnStart()
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    fun TextView.readOnClick() {
      setOnClickListener { text?.toString()?.let { viewModel.onTextSelected(it) } }
    }

    binding.name.readOnClick()
    binding.group.readOnClick()
    binding.bredFor.readOnClick()
    binding.lifeSpan.readOnClick()
    binding.temperament.readOnClick()
    binding.height.readOnClick()
    binding.weight.readOnClick()
  }

  @SuppressLint("SetTextI18n")
  fun FragmentBreedDetailBinding.updateView(detail: BreedDetail) {

    name.text = detail.name

    group.text = detail.breedGroup
    bredFor.text = detail.bredFor
    lifeSpan.text = detail.lifeSpan
    temperament.text = detail.temperament

    if (Locale.getDefault().isMetric()) {

      height.text = "${detail.heightMetric} cm"
      weight.text = "${detail.weightMetric} kg"
    } else {
      height.text = "${detail.heightImperial} inches"
      weight.text = "${detail.weightImperial} pounds"
    }

    image.load(detail.imageUrl)
  }
}
