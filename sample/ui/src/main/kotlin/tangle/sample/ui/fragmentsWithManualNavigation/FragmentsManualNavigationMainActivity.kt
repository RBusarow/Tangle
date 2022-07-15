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

package tangle.sample.ui.fragmentsWithManualNavigation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tangle.fragment.TangleFragmentFactory
import tangle.inject.TangleGraph
import tangle.inject.TangleScope
import tangle.sample.core.AppScope
import tangle.sample.ui.R
import tangle.sample.ui.fragmentsWithManualNavigation.breedList.BreedListFragment
import javax.inject.Inject
import javax.inject.Provider

@TangleScope(AppScope::class)
class FragmentsManualNavigationMainActivity : AppCompatActivity() {

  @Inject lateinit var tangleFragmentFactory: TangleFragmentFactory

  @Inject lateinit var navigation: FragmentsManualNavigationMainActivityNavigation

  override fun onCreate(savedInstanceState: Bundle?) {
    TangleGraph.inject(this)

    supportFragmentManager.fragmentFactory = tangleFragmentFactory
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_fragments_manual_navigation_main)

    navigation.breedList(this)
  }
}

/**
 * In a real application, this would be an interface with an implementation which lives in a higher
 * level module.
 */
class FragmentsManualNavigationMainActivityNavigation @Inject constructor(
  internal val breedListFragmentProvider: Provider<BreedListFragment>
) {
  fun breedList(activity: FragmentsManualNavigationMainActivity) {
    val fragment = breedListFragmentProvider.get()
    val name = fragment::class.qualifiedName!!

    activity.supportFragmentManager.beginTransaction()
      .replace(R.id.fragment_content_main, fragment, name)
      .commitAllowingStateLoss()
  }
}
