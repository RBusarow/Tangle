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

package tangle.sample.app

import android.app.Application
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import tangle.inject.TangleGraph
import tangle.sample.core.AppPlugin
import tangle.sample.core.AppScope
import tangle.sample.core.Components
import tangle.sample.data.DogApiKey

class TangleApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    val component = DaggerAppComponent.factory()
      .create(this)

    Components.add(component)
    TangleGraph.init(component)

    Components.get<TangleApplicationComponent>()
      .appPlugins
      .forEach {
        it.apply(this)
      }
  }
}

@ContributesTo(AppScope::class)
interface TangleApplicationComponent {

  val appPlugins: Set<@JvmSuppressWildcards AppPlugin>
}
