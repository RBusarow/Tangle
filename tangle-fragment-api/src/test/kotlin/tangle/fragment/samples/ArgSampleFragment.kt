/*
 * Copyright (C) 2022 Rick Busarow
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

package tangle.fragment.samples

import androidx.fragment.app.Fragment
import tangle.fragment.ContributesFragment
import tangle.fragment.FragmentInject
import tangle.fragment.FragmentInjectFactory
import tangle.fragment.arg
import tangle.fragment.argOrNull
import tangle.inject.TangleParam
import tangle.inject.test.utils.AppScope

@ContributesFragment(AppScope::class)
class ArgSampleFragment @FragmentInject constructor() : Fragment() {

  val nameArg: String by arg("name")

  @FragmentInjectFactory
  interface Factory {
    fun create(
      @TangleParam("name")
      name: String
    ): Fragment
  }
}

@ContributesFragment(AppScope::class)
class ArgOrNullSampleFragment @FragmentInject constructor() : Fragment() {

  val nameArg: String? by argOrNull("name")

  @FragmentInjectFactory
  interface Factory {
    fun create(
      @TangleParam("name")
      name: String
    ): Fragment
  }
}
