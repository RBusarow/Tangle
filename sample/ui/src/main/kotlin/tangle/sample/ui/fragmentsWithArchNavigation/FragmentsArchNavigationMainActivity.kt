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

package tangle.sample.ui.fragmentsWithArchNavigation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.squareup.anvil.annotations.ContributesTo
import tangle.fragment.TangleFragmentFactory
import tangle.sample.core.AppScope
import tangle.sample.core.Components
import tangle.sample.ui.R
import javax.inject.Inject

@ContributesTo(AppScope::class)
interface FragmentsArchNavigationMainActivityComponent {
  fun inject(activity: FragmentsArchNavigationMainActivity)
}

class FragmentsArchNavigationMainActivity : AppCompatActivity() {

  @Inject lateinit var tangleFragmentFactory: TangleFragmentFactory

  override fun onCreate(savedInstanceState: Bundle?) {
    Components.get<FragmentsArchNavigationMainActivityComponent>()
      .inject(this)

    supportFragmentManager.fragmentFactory = tangleFragmentFactory
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_fragments_arch_navigation_main)

    findNavController(R.id.nav_host_fragment_content_main)
  }
}
