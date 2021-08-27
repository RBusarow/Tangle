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

package tangle.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import tangle.inject.InternalTangleApi
import tangle.inject.TangleGraph

/**
 * A [WorkerFactory] for Tangle.  This factory references the Dagger dependency graph
 * via the [TangleGraph] singleton, and uses it to generate injected [ListenableWorker] instances.
 *
 * @sample tangle.work.samples.TangleWorkerFactorySample.tangleWorkerFactorySample
 * @since 0.12.0
 */
public class TangleWorkerFactory : WorkerFactory() {

  @OptIn(InternalTangleApi::class)
  internal val workerFactories = TangleGraph.get<TangleWorkerComponent>()
    .tangleWorkerMapSubcomponentFactory
    .create()
    .workerFactoryMap

  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters
  ): ListenableWorker? {
    val workerFactory = workerFactories[workerClassName]
      ?: return null

    return workerFactory.create(appContext, workerParameters)
  }
}
