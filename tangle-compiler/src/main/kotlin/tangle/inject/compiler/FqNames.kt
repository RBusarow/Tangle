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
import com.squareup.anvil.annotations.MergeSubcomponent
import com.squareup.anvil.compiler.internal.fqName
import dagger.Provides
import dagger.internal.DoubleCheck
import dagger.internal.Factory
import org.jetbrains.kotlin.name.FqName
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Qualifier

object FqNames {

  val context = FqName("android.content.Context")
  val workerParameters = FqName("androidx.work.WorkerParameters")

  val assistedInject = FqName("dagger.assisted.AssistedInject")
  val daggerAssisted = FqName("dagger.assisted.Assisted")
  val vmInject = FqName("tangle.viewmodel.VMInject")
  val vmInjectFactory = FqName("tangle.viewmodel.VMInjectFactory")
  val vmAssisted = FqName("tangle.viewmodel.VMAssisted")
  val contributesViewModel = FqName("tangle.viewmodel.ContributesViewModel")

  val fragmentInject = FqName("tangle.fragment.FragmentInject")
  val fragmentInjectFactory = FqName("tangle.fragment.FragmentInjectFactory")
  val contributesFragment = FqName("tangle.fragment.ContributesFragment")

  val tangleAppScope = FqName("tangle.inject.internal.TangleAppScope")
  val tangleParam = FqName("tangle.inject.TangleParam")
  val tangleScope = FqName("tangle.inject.TangleScope")
  val tangleViewModelScope = FqName("tangle.viewmodel.internal.TangleViewModelScope")

  val tangleWorker = FqName("tangle.work.TangleWorker")

  val mergeComponent = MergeComponent::class.fqName
  val mergeSubcomponent = MergeSubcomponent::class.fqName
  val jvmSuppressWildcards = JvmSuppressWildcards::class.fqName
  val provider = Provider::class.fqName
  val daggerLazy = dagger.Lazy::class.fqName
  val daggerProvides = Provides::class.fqName
  val inject = Inject::class.fqName
  val qualifier = Qualifier::class.fqName
  val daggerDoubleCheck = DoubleCheck::class.java.canonicalName
  val daggerFactory = Factory::class.java.canonicalName
  val bundle = FqName("android.os.Bundle")
  val iBinder = FqName("android.os.IBinder")
  val parcelable = FqName("android.os.Parcelable")
  val size = FqName("android.util.Size")
  val sizeF = FqName("android.util.SizeF")

  val androidxFragment = FqName("androidx.fragment.app.Fragment")
  val androidxViewModel = FqName("androidx.lifecycle.ViewModel")
}
