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

package tangle.fragment

import androidx.fragment.app.Fragment

/**
 * Lazily retrieve an argument passed to this fragment.
 *
 * Throws an exception if the fragment's [arguments][Fragment.getArguments] do not contain a
 * compatible value for the given [bundleKey]. Use [argOrNull][tangle.fragment.argOrNull] for a
 * "safe" alternative if the `bundleKey` may not be present.
 *
 * @param A the expected return type, which may be nullable only if a `null` value is being
 *   explicitly set in the arguments bundle. See [bundleOf][androidx.core.os.bundleOf]
 *   for all supported types and how they're treated.
 * @param bundleKey key used to pass this argument
 * @return a [Lazy<A>][A] from [arguments][Fragment.getArguments], stored with this [bundleKey]
 * @throws IllegalStateException if this fragment's [arguments][Fragment.getArguments] is null
 * @throws IllegalArgumentException if either:
 *   - the `arguments` [bundle][android.os.Bundle] exists, but does not contain an entry for [bundleKey]
 *   - an entry for [bundleKey] exists, but its value is not of type [A].
 * @sample tangle.fragment.samples.ArgSampleFragment
 * @see tangle.fragment.argOrNull for the non-throwing version
 * @see androidx.core.os.bundleOf for all supported types and how they're treated
 */
public inline fun <reified A : Any?> Fragment.arg(bundleKey: String): Lazy<A> = lazy {

  val args = requireArguments()

  require(args.containsKey(bundleKey)) {
    "Fragment ${javaClass.canonicalName}'s arguments do not contain key: $bundleKey"
  }

  val arg = args.get(bundleKey)

  require(arg is A) {
    val actualType = arg?.let { it::class.qualifiedName }

    "expected the argument for key '$bundleKey' to be of type '${A::class.qualifiedName}', " +
      "but it is of type '$actualType'."
  }

  arg
}

/**
 * Lazily retrieve an argument passed to this fragment. Returns `null` if:
 * - the fragment's [arguments][Fragment.getArguments] property is null
 * - the fragment's arguments do not contain an entry for this [bundleKey]
 * - the fragment's arguments contain an explicit `null` value for this key
 * - the fragment's arguments contain a value for this key, but it cannot be assigned to type [A]
 *
 * @param A the expected return type. See [bundleOf][androidx.core.os.bundleOf] for all supported
 *   types and how they're treated.
 * @param bundleKey key used to pass this argument
 * @return a [Lazy<A>][A] from [arguments][Fragment.getArguments], stored with this [bundleKey], if
 *   it exists. Otherwise `null`. Use [arg][tangle.fragment.arg]
 *   if the argument is guaranteed to be present.
 * @sample tangle.fragment.samples.ArgOrNullSampleFragment
 * @see tangle.fragment.arg for a non-nullable return when the argument is guaranteed
 * @see androidx.core.os.bundleOf for all supported types and how they're treated
 */
public inline fun <reified A : Any?> Fragment.argOrNull(bundleKey: String): Lazy<A?> = lazy {

  val args = arguments ?: return@lazy null

  args.get(bundleKey) as? A
}
