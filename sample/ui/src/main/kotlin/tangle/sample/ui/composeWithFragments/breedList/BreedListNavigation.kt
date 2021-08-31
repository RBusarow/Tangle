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

package tangle.sample.ui.composeWithFragments.breedList

import tangle.sample.ui.R
import tangle.sample.ui.composeWithFragments.breedDetail.BreedDetailFragment
import javax.inject.Inject

/**
 * In a real application, this would be an interface with an implementation which lives in a higher
 * level module.
 */
class BreedListNavigation @Inject constructor(
  internal val breedDetailFragmentFactory: BreedDetailFragment.Factory
) {

  fun breedDetail(breedId: Int, breedListFragment: BreedListFragment) {

    val fragment = breedDetailFragmentFactory.create(breedId)
    val name = fragment::class.qualifiedName!!

    breedListFragment.parentFragmentManager
      .beginTransaction()
      .replace(R.id.fragment_content_main, fragment, name)
      .addToBackStack(name)
      .commitAllowingStateLoss()
  }
}
