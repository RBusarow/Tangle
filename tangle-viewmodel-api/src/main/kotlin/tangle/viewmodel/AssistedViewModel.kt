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

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import tangle.inject.InternalTangleApi
import tangle.viewmodel.internal.AssistedTangleViewModelFactory
import tangle.viewmodel.internal.TangleViewModelFactory
import kotlin.DeprecationLevel.ERROR
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface AssistedViewModel<T : ViewModel, F : Any>

@OptIn(InternalTangleApi::class)
public inline fun <reified VM, reified F : Any> Fragment.tangleViewModel(
  noinline factory: F.() -> VM
): Lazy<VM> where VM : AssistedViewModel<VM, F>, VM : ViewModel = viewModels {
  AssistedTangleViewModelFactory(
    owner = this,
    defaultArgs = arguments,
    vmClass = VM::class,
    fClass = F::class,
    factory = factory
  )
}

@Deprecated("no"/*, level = ERROR*/)
public fun <VM : AssistedViewModel<*, *> > Fragment.tangleViewModel(): Lazy<VM>  = TODO()

@OptIn(InternalTangleApi::class)
public inline fun <reified VM, reified F : Any> ComponentActivity.tangleViewModel(
  noinline factory: F.() -> VM
): Lazy<VM> where VM : AssistedViewModel<VM, F>, VM : ViewModel = viewModels {
  AssistedTangleViewModelFactory(
    owner = this,
    defaultArgs = intent.extras,
    vmClass = VM::class,
    fClass = F::class,
    factory = factory
  )
}

@Deprecated("no", level = ERROR)
@OptIn(InternalTangleApi::class)
public inline fun <reified VM> ComponentActivity.tangleViewModel(): VM
  where VM : AssistedViewModel<VM, *>, VM : ViewModel {
  throw UnsupportedOperationException("")
}
