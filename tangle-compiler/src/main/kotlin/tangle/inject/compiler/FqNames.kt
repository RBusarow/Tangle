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

package tangle.inject.compiler

import com.squareup.anvil.annotations.MergeComponent
import com.squareup.anvil.compiler.internal.fqName
import dagger.Provides
import dagger.internal.DoubleCheck
import tangle.inject.annotations.ContributesViewModel
import tangle.inject.annotations.FromSavedState
import tangle.inject.annotations.TangleParam
import tangle.inject.annotations.VMInject
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Qualifier

internal object FqNames {

  val vmInject = VMInject::class.fqName
  val contributesViewModel = ContributesViewModel::class.fqName
  val tangleParam = TangleParam::class.fqName
  val fromSavedState = FromSavedState::class.fqName
  val mergeComponent = MergeComponent::class.fqName
  val jvmSuppressWildcards = JvmSuppressWildcards::class.fqName
  val provider = Provider::class.fqName
  val daggerLazy = dagger.Lazy::class.fqName
  val daggerProvides = Provides::class.fqName
  val inject = Inject::class.fqName
  val qualifier = Qualifier::class.fqName
  val daggerDoubleCheckString = DoubleCheck::class.java.canonicalName
}
