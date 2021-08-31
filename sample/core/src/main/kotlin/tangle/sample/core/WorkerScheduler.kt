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

import androidx.work.WorkManager

/**
 * Used to schedule a single long-running [Worker] in a [WorkManager] via Dagger multi-binding.
 *
 * Workers can be feature-toggled at runtime by the implementation of [isApplicable].
 *
 * ```
 * @ContributesMultibinding(AppScope::class)
 * class MyCustomWorkerScheduler @Inject constructor(
 *   val aFeatureToggle: AFeatureToggle
 * ) : WorkerScheduler {
 *
 *   private val request: PeriodicWorkRequest
 *     get() = PeriodicWorkRequestBuilder<MyCustomWorker>( ... )
 *       .setConstraints(... )
 *       .build()
 *
 *     override fun isApplicable(): Boolean = aFeatureToggle.enabled
 *
 *     override fun enqueue(workManager: WorkManager) {
 *       workManager.enqueueUniquePeriodicWork(
 *         "my-custom-worker",
 *         ExistingPeriodicWorkPolicy.KEEP,
 *         request
 *     )
 *   }
 * }
 * ```
 */
interface WorkerScheduler {

  /**
   * @return true if the [Worker] should be scheduled, or false if it shouldn't
   */
  fun isApplicable(): Boolean

  /**
   * Enqueues an associated [Worker] in the [workManager] parameter
   *
   * ```
   * override fun enqueue(workManager: WorkManager) {
   *   workManager.enqueueUniquePeriodicWork(
   *     "my-custom-worker",
   *     ExistingPeriodicWorkPolicy.KEEP,
   *     request
   * )
   * ```
   */
  fun enqueue(workManager: WorkManager)
}
