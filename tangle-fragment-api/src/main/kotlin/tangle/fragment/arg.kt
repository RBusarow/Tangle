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

/**
 * Lazily retrieve an argument passed to this fragment. You should generally prefer to use the
 * inline version.
 *
 * @param bundleKey key used to pass this argument
 * @param clazz type class expected for this argument
 * @see FragmentInjectFactory for instantiating fragments with arguments
 */
public fun <A : Any?> Fragment.arg(bundleKey: String, clazz: Class<A>): Lazy<A> = lazy {
  val args = arguments

  if (args?.containsKey(bundleKey) != true)
    throw IllegalStateException("Bundle does not contain key: $bundleKey")

  val arg = args.get(bundleKey)

  if (!clazz.isInstance(arg))
    throw IllegalStateException("Bundle did not contain value of type ${clazz.simpleName} for key $bundleKey, had ${arg?.javaClass?.simpleName} instead")

  // SAFETY: Just checked using isInstance
  @Suppress("UNCHECKED_CAST")
  arg as A
}

/**
 * Lazily retrieve an argument passed to this fragment.
 *
 * @param bundleKey key used to pass this argument
 * @see FragmentInjectFactory for instantiating fragments with arguments
 */
public inline fun <reified A : Any?> Fragment.arg(bundleKey: String): Lazy<A> =
  arg(bundleKey, A::class.java)
