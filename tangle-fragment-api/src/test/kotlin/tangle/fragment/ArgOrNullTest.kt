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

import androidx.core.os.bundleOf
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ArgOrNullTest {

  val fragmentName = TestFragment::class.java.canonicalName!!

  @Test
  fun `argument of the correct type is returned`() {

    val fragment = TestFragment()
    fragment.arguments = bundleOf("int" to 1)

    val arg: Int? by fragment.argOrNull("int")

    arg shouldBe 1
  }

  @Test
  fun `existing value of incompatible type returns null`() {

    val fragment = TestFragment()
    fragment.arguments = bundleOf("char" to 'c')

    val arg: String? by fragment.argOrNull("char")
    val arg2: Int? by fragment.argOrNull("char")

    arg shouldBe null
    arg2 shouldBe null
  }

  @Test
  fun `existing entry of null returns null`() {

    val fragment = TestFragment()
    fragment.arguments = bundleOf("nullable" to null)

    val arg: String? by fragment.argOrNull("nullable")

    arg shouldBe null
  }

  @Test
  fun `expected type may be a supertype of the actual argument type`() {

    val fragment = TestFragment()
    fragment.arguments = bundleOf("string" to "expected")

    val arg: CharSequence? by fragment.argOrNull("string")

    arg shouldBe "expected"
  }
}
