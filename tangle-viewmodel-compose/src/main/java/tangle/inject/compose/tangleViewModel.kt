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

package tangle.inject.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

/**
 * Returns an existing [VMInject][tangle.viewmodel.VMInject]-annotated [ViewModel]
 * or creates a new one scoped to the current navigation graph present on
 * the NavController back stack.
 *
 * @since 0.10.0
 */
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated(
  """this is the wrong package name.  Use `tangle.viewmodel.compose.tangleViewModel` instead.
    |
    |Please forgive the lack of an auto-fix but IntelliJ can't handle replacing the inline function properly.
  """
)
@Composable
public inline fun <reified VM : ViewModel> tangleViewModel(
  viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
  }
): VM = tangle.viewmodel.compose.tangleViewModel(viewModelStoreOwner)
