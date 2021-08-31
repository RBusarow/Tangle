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

package tangle.sample.app.support

import android.app.Application
import com.squareup.anvil.annotations.ContributesTo
import tangle.inject.TangleGraph
import tangle.sample.core.AppPlugin
import tangle.sample.core.AppScope
import tangle.sample.core.Components
import javax.inject.Inject

class TestTangleApplication : Application() {

  @Inject
  lateinit var appPlugins: Set<@JvmSuppressWildcards AppPlugin>

  override fun onCreate() {
    super.onCreate()

    val component = DaggerTestAppComponent.factory()
      .create(this)

    Components.add(component)
    TangleGraph.init(component)

    Components.get<TestTangleApplicationComponent>().inject(this)

    appPlugins
      .forEach {
        it.apply(this)
      }
  }
}

@ContributesTo(AppScope::class)
interface TestTangleApplicationComponent {
  fun inject(application: TestTangleApplication)
}
