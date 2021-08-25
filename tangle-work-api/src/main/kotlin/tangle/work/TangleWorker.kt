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

import javax.inject.Qualifier

/**
 * Adds the annotated [ListenableWorker][androidx.work.ListenableWorker]
 * to Dagger's graph via Tangle.  The corresponding Worker can then be created
 * using [TangleWorkerFactory].
 *
 * @sample tangle.work.samples.TangleWorkerSample.tangleWorkerSample
 * @since 0.12.0
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class TangleWorker

/**
 * Qualifier for the internal `Map<String, AssistedWorkerFactory<out ListenableWorker>>`
 * used in Tangle's [Worker][androidx.work.Worker] multi-binding.
 *
 * This is an internal Tangle API and should not be referenced directly.
 *
 * @since 0.12.0
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.FIELD,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.VALUE_PARAMETER
)
public annotation class TangleAssistedWorkerFactoryMap
