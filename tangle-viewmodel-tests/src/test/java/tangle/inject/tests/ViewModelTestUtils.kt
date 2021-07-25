package tangle.inject.tests

import tangle.inject.test.utils.getStaticPrivateFieldByName
import tangle.viewmodel.TangleGraph

fun TangleGraph.clear() {
  val set: MutableSet<Any> = getStaticPrivateFieldByName("components")

  set.clear()
}
