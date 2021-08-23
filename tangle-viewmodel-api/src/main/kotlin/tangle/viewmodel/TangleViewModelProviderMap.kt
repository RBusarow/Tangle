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

package tangle.viewmodel

import androidx.lifecycle.ViewModel
import tangle.viewmodel.internal.TangleViewModelFactory
import javax.inject.Qualifier

/**
 * Qualifier for the internal `Map<KClass<ViewModel>, ViewModel>>`
 * used in Tangle's [ViewModel][androidx.lifecycle.ViewModel] multi-binding.
 *
 * This is an internal Tangle API and should not be referenced directly.
 *
 * @since 0.10.0
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.FIELD,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.VALUE_PARAMETER
)
public annotation class TangleViewModelProviderMap {

  /**
   * Qualifier for all the keys contained in [TangleViewModelProviderMap].
   * [TangleViewModelProviderMap] is only provided by [TangleViewModelMapSubcomponent],
   * and a new subcomponent needs to be created for each viewModel injection, so it's inefficient
   * to create the object before we know if the map holds the [ViewModel].
   * The [TangleViewModelFactory] checks this Set in order to determine whether the map
   * holds a particular ViewModel type.
   *
   * This is an internal Tangle API and should not be referenced directly.
   *
   * @since 0.10.0
   */
  @Qualifier
  @Retention(AnnotationRetention.RUNTIME)
  @Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
  public annotation class KeySet
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.FIELD,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.VALUE_PARAMETER
)
public annotation class TangleViewModelFactoryMap
