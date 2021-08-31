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

package tangle.sample.ui.composeWithFragments

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.squareup.anvil.annotations.ContributesTo
import tangle.fragment.TangleFragmentFactory
import tangle.sample.core.AppScope
import tangle.sample.core.Components
import tangle.sample.ui.R
import tangle.sample.ui.composeWithFragments.breedList.BreedListFragment
import javax.inject.Inject
import javax.inject.Provider

@ContributesTo(AppScope::class)
interface ComposeWithFragmentsMainActivityComponent {
  fun inject(activity: ComposeWithFragmentsMainActivity)
}

class ComposeWithFragmentsMainActivity : FragmentActivity() {

  @Inject lateinit var tangleFragmentFactory: TangleFragmentFactory
  @Inject lateinit var navigation: ComposeWithFragmentsMainActivityNavigation

  override fun onCreate(savedInstanceState: Bundle?) {
    Components.get<ComposeWithFragmentsMainActivityComponent>().inject(this)

    supportFragmentManager.fragmentFactory = tangleFragmentFactory
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_compose_fragments_main)

    navigation.breedList(this)
  }
}

/**
 * In a real application, this would be an interface with an implementation which lives in a higher
 * level module.
 */
class ComposeWithFragmentsMainActivityNavigation @Inject constructor(
  internal val breedListFragmentProvider: Provider<BreedListFragment>
) {
  fun breedList(activity: ComposeWithFragmentsMainActivity) {
    val fragment = breedListFragmentProvider.get()
    val name = fragment::class.qualifiedName!!

    activity.supportFragmentManager.beginTransaction()
      .replace(R.id.fragment_content_main, fragment, name)
      .commitAllowingStateLoss()
  }
}
