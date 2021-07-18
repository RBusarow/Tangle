package tangle.inject.api

import androidx.fragment.app.Fragment

fun <A : Any> Fragment.arg(bundleKey: String) = lazy {
  @Suppress("UNCHECKED_CAST")
  (arguments?.get(bundleKey) as? A)
    ?: throw IllegalStateException("Bundle does not contain key: $bundleKey")
}
