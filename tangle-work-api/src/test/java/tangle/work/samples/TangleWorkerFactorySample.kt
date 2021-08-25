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

package tangle.work.samples

import android.app.Application
import androidx.work.Configuration
import tangle.inject.TangleGraph
import tangle.inject.test.utils.DaggerAppComponent
import tangle.inject.test.utils.MyApplicationComponent
import tangle.inject.test.utils.Sample
import tangle.work.TangleWorkerFactory
import javax.inject.Inject

class TangleWorkerFactorySample {

  @Sample
  fun tangleWorkerFactorySample() {
    class MyApplication : Application(), Configuration.Provider {

      @Inject lateinit var workerFactory: TangleWorkerFactory

      override fun onCreate() {
        super.onCreate()

        val myAppComponent = DaggerAppComponent.factory()
          .create(this)

        TangleGraph.init(myAppComponent)

        // inject your application class after initializing TangleGraph
        (myAppComponent as MyApplicationComponent).inject(this)
      }

      // now the WorkManager instances will use TangleWorkerFactory
      override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
          .setWorkerFactory(workerFactory)
          .build()
      }
    }
  }
}
