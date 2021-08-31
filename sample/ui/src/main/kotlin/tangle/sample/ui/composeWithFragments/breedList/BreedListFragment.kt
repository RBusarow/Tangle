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

import androidx.compose.runtime.Composable
import tangle.fragment.ContributesFragment
import tangle.sample.core.AppScope
import tangle.sample.ui.composeWithActivities.breedList.BreedList
import tangle.sample.ui.composeWithFragments.BaseComposeFragment
import javax.inject.Inject

@ContributesFragment(AppScope::class)
class BreedListFragment @Inject constructor(
  private val breedListNavigation: BreedListNavigation
) : BaseComposeFragment() {
  override val ui = @Composable {

    BreedList {
      breedListNavigation.breedDetail(it.id, this)
    }
  }
}
