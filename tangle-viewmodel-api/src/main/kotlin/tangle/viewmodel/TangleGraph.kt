package tangle.viewmodel

import tangle.inject.TangleGraph as NewTangleGraph

/**
 * This is the old location for [TangleGraph][NewTangleGraph].
 *
 * IntelliJ/Android Studio currently gets confused if using @Deprecated/ReplaceWith on a typealias,
 * so this object exists purely for IDE support.
 */
@Deprecated(
  message = "TangleGraph has been moved to `tangle.inject.TangleGraph`.  " +
    "This breadcrumb object will be removed in a future version of Tangle.",
  replaceWith = ReplaceWith("TangleGraph", "tangle.inject.TangleGraph")
)
object TangleGraph {
  @Deprecated(
    message = "TangleGraph has been moved to `tangle.inject.TangleGraph`.  " +
      "This breadcrumb object will be removed in a future version of Tangle.",
    replaceWith = ReplaceWith("TangleGraph.init(any)", "tangle.inject.TangleGraph")
  )
  fun init(any: Any) = NewTangleGraph.init(any)
}
