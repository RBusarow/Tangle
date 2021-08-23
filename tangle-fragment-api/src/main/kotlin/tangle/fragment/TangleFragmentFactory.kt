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

package tangle.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import javax.inject.Provider

public class TangleFragmentFactory(
  private val providerMap: Map<Class<out Fragment>, Provider<@JvmSuppressWildcards Fragment>>,
  @TangleFragmentProviderMap
  private val assistedProviderMap: Map<Class<out Fragment>, Provider<@JvmSuppressWildcards Fragment>>
) : FragmentFactory() {

  override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
    val fragmentClass = loadFragmentClass(classLoader, className)

    return providerMap[fragmentClass]?.get()
      ?: assistedProviderMap[fragmentClass]?.get()
      ?: return super.instantiate(classLoader, className)
  }
}
