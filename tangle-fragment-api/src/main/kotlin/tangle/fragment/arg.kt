package tangle.fragment

import androidx.fragment.app.Fragment

public fun <A : Any> Fragment.arg(bundleKey: String): Lazy<A> = lazy {
  @Suppress("UNCHECKED_CAST")
  (arguments?.get(bundleKey) as? A)
    ?: throw IllegalStateException("Bundle does not contain key: $bundleKey")
}
