package tangle.viewmodel

import tangle.inject.test.utils.getStaticPrivateFieldByName

fun TangleGraph.clear() {
  val set: MutableSet<Any> = getStaticPrivateFieldByName("components")

  set.clear()
}
