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

package tangle.inject.samples

import tangle.inject.TangleGraph
import tangle.inject.TangleScope
import tangle.inject.test.utils.*
import javax.inject.Inject

class MemberInjectSample {

  @Sample
  fun memberInjectSample() {

    @TangleScope(AppScope::class) // dependencies will come from the AppScope
    class MyApplication : Application() {

      @Inject lateinit var logger: MyLogger

      override fun onCreate() {
        super.onCreate()

        val appComponent = DaggerAppComponent.factory()
          .create(this)

        // connect the app's Dagger graph to Tangle
        TangleGraph.add(appComponent)

        // inject MyLogger using Dagger/Tangle
        TangleGraph.inject(this)

        // logger is not initialized and safe to use
        Timber.plant(logger)
      }
    }
  }
}
