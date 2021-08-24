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
 * Internal use only.
 *
 * Referenced by [TangleWorkerFactory] in order to create injected [ListenableWorker]s.
 *
 * @since 0.12.0
 */
public interface TangleWorkerComponent {
  /**
   * Referenced by [TangleWorkerFactory] in order to create
   * a scoped [TangleWorkerFactoryMapSubcomponent], in order to access the map.
   *
   * @since 0.12.0
   */
  @InternalTangleApi
  public val tangleWorkerMapSubcomponentFactory: TangleWorkerFactoryMapSubcomponent.Factory
}
