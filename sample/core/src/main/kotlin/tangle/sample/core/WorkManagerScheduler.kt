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

package tangle.sample.core

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.squareup.anvil.annotations.ContributesMultibinding
import tangle.work.TangleWorkerFactory
import javax.inject.Inject
import javax.inject.Provider

/**
 * Used to automatically schedule periodic [Workers][androidx.work.Worker] upon app launch.
 *
 * In order to add a Worker, create an implementation of [WorkerScheduler]
 * and add it to Dagger's `Set<WorkerScheduler>` via multi-binding.
 *
 * ```
 * @ContributesMultibinding(AppScope::class)
 * class MyCustomWorkerScheduler @Inject constructor(...) : WorkerScheduler {
 *
 *   override fun isApplicable() = ...
 *   override fun enqueue(workManager: WorkManager) { ... }
 *
 * }
 * ```
 *
 * @see WorkerScheduler
 */
@ContributesMultibinding(AppScope::class)
class WorkManagerScheduler @Inject constructor(
  val schedulersProvider: Provider<Set<WorkerScheduler>>,
  val workerFactory: TangleWorkerFactory
) : AppPlugin {

  override fun apply(application: Application) {
    WorkManager.initialize(
      application,
      Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()
    )
    val workManager = WorkManager.getInstance(application)

    schedulersProvider.get()
      .filter { it.isApplicable() }
      .forEach { it.enqueue(workManager) }
  }
}
