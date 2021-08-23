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

import tangle.inject.InternalTangleApi
import tangle.viewmodel.internal.TangleViewModelFactory

/**
 * Used to provide a way of retrieving [TangleViewModelMapSubcomponent.Factory]
 * from a main app [Component][dagger.Component].
 *
 * @since 0.10.0
 */
public interface TangleViewModelComponent {
  /**
   * Referenced by [TangleViewModelFactory] in order to create
   * a scoped [TangleViewModelMapSubcomponent], in order to access the [TangleViewModelProviderMap].
   *
   * @since 0.10.0
   */
  @InternalTangleApi
  public val tangleViewModelMapSubcomponentFactory: TangleViewModelMapSubcomponent.Factory

  /**
   * Referenced by [TangleViewModelFactory] in order to create
   * a scoped [TangleViewModelMapSubcomponent], in order to access the [TangleViewModelProviderMap].
   *
   * @since 0.10.0
   */
  @InternalTangleApi
  public val tangleViewModelKeysSubcomponentFactory: TangleViewModelKeysSubcomponent.Factory
}
