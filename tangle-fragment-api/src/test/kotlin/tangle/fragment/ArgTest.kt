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

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ArgTest {

  val fragmentName = TestFragment::class.java.canonicalName!!

  @Test
  fun `primitive argument of the correct type is returned`() {

    val fragment = TestFragment()
    fragment.arguments = bundleOf("int" to 1)

    val arg: Int by fragment.arg("int")

    arg shouldBe 1
  }

  @Test
  fun `typed array argument of the correct type is returned`() {

    val fragment = TestFragment()
    fragment.arguments = bundleOf("ints" to arrayOf(1))

    @Suppress("ArrayPrimitive")
    val arg: Array<Int> by fragment.arg("ints")

    arg shouldBe arrayOf(1)
  }

  @Test
  fun `IntArray argument of the correct type is returned`() {

    val fragment = TestFragment()
    fragment.arguments = bundleOf("ints" to intArrayOf(1))

    val arg: IntArray by fragment.arg("ints")

    arg shouldBe intArrayOf(1)
  }

  @Test
  fun `Parcelable argument of the correct type is returned`() {

    val fragment = TestFragment()
    fragment.arguments = bundleOf("parcelable" to ParcelablePacket())

    val arg: ParcelablePacket by fragment.arg("parcelable")

    arg shouldBe ParcelablePacket()
  }

  @Test
  fun `expected type may be a supertype of the actual argument type`() {

    val fragment = TestFragment()
    fragment.arguments = bundleOf("string" to "expected")

    val arg: CharSequence by fragment.arg("string")

    arg shouldBe "expected"
  }

  @Test
  fun `missing bundle throws IllegalStateException`() {

    val fragment = TestFragment()

    val arg: String by fragment.arg("missing")

    shouldThrow<IllegalStateException> { arg }
      .message shouldBe "Fragment $fragment does not have any arguments."
  }

  @Test
  fun `missing argument throws IllegalArgumentException`() {

    val fragment = TestFragment()

    fragment.arguments = bundleOf()

    val arg: String by fragment.arg("missing")

    shouldThrow<IllegalArgumentException> { arg }
      .message shouldBe "Fragment $fragmentName's arguments do not contain key: missing"
  }

  @Test
  fun `null argument with non-nullable expected type throws IllegalArgumentException`() {

    val fragment = TestFragment()
    fragment.arguments = bundleOf("nullable" to null)

    val arg: String by fragment.arg("nullable")

    shouldThrow<IllegalArgumentException> { arg }
      .message shouldBe "expected the argument for key 'nullable' to be of type 'kotlin.String', " +
      "but it is of type 'null'."
  }

  @Test
  fun `argument of the wrong non-nullable type throws IllegalArgumentException`() {

    val fragment = TestFragment()
    fragment.arguments = bundleOf("wrong" to 1)

    val arg: String by fragment.arg("wrong")

    shouldThrow<IllegalArgumentException> { arg }
      .message shouldBe "expected the argument for key 'wrong' to be of type 'kotlin.String', " +
      "but it is of type 'kotlin.Int'."
  }

  @Test
  fun `argument of the wrong nullable type throws IllegalArgumentException`() {

    val fragment = TestFragment()
    fragment.arguments = bundleOf("wrong" to 1)

    val arg: String? by fragment.arg("wrong")

    shouldThrow<IllegalArgumentException> { arg }
      .message shouldBe "expected the argument for key 'wrong' to be of type 'kotlin.String', " +
      "but it is of type 'kotlin.Int'."
  }

  @Test
  fun `null argument with nullable expected type just returns null`() {

    val fragment = TestFragment()
    fragment.arguments = bundleOf("nullable" to null)

    val arg: String? by fragment.arg("nullable")
    val arg2: Int? by fragment.arg("nullable")

    arg shouldBe null
    arg2 shouldBe null
  }
}

class TestFragment : Fragment()

class ParcelablePacket() : Parcelable {
  constructor(parcel: Parcel) : this()

  override fun writeToParcel(parcel: Parcel, flags: Int) = Unit
  override fun describeContents(): Int = 0
  override fun equals(other: Any?): Boolean = other is ParcelablePacket
  override fun hashCode(): Int = javaClass.hashCode()

  companion object CREATOR : Creator<ParcelablePacket> {
    override fun createFromParcel(parcel: Parcel): ParcelablePacket {
      return ParcelablePacket(parcel)
    }

    override fun newArray(size: Int): Array<ParcelablePacket?> {
      return arrayOfNulls(size)
    }
  }
}
