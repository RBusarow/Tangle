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

import androidx.work.ListenableWorker
import tangle.inject.InternalTangleApi

/**
 * Internal-use [Subcomponent][dagger.Subcomponent] which provides a map
 * of [ListenableWorker]s to the [TangleWorkerFactory].
 *
 * A new Subcomponent is created each time a `Worker` is injected.
 *
 * @since 0.12.0
 */
public interface TangleWorkerFactoryMapSubcomponent {

  /** @suppress */
  @OptIn(InternalTangleApi::class)
  @get:TangleAssistedWorkerFactoryMap
  public val workerFactoryMap: Map<String, @JvmSuppressWildcards AssistedWorkerFactory<out ListenableWorker>>

  /** Internal use only. */
  public interface Factory {
    /** Internal use only. */
    public fun create(): TangleWorkerFactoryMapSubcomponent
  }
}
