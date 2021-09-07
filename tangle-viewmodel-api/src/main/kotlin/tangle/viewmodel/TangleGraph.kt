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
public object TangleGraph {
  @Deprecated(
    message = "TangleGraph has been moved to `tangle.inject.TangleGraph`.  " +
      "This breadcrumb object will be removed in a future version of Tangle.",
    replaceWith = ReplaceWith("TangleGraph.add(any)", "tangle.inject.TangleGraph")
  )
  public fun init(any: Any) {
    NewTangleGraph.add(any)
  }
}
