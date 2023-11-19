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

package tangle.inject

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.TestFactory
import tangle.inject.internal.TangleInjector
import tangle.inject.test.utils.BaseTest
import tangle.inject.test.utils.baseClass
import tangle.inject.test.utils.createInstance
import tangle.inject.test.utils.fieldsValues
import tangle.inject.test.utils.membersInjector
import tangle.inject.test.utils.provider
import tangle.inject.test.utils.tangleInjector
import tangle.inject.test.utils.targetClass

@OptIn(InternalTangleApi::class)
class TangleScopeTest : BaseTest() {

  @TestFactory
  fun `injector is generated without injected members`() = test {
    compile(
      """
      package tangle.inject.tests

      import tangle.inject.TangleScope
      import javax.inject.Inject

      @TangleScope(Unit::class)
      class Target
     """
    ) {
      val tangleInjector = targetClass.tangleInjector().createInstance() as TangleInjector<Any>

      val target = targetClass.createInstance()

      tangleInjector.inject(target)

      target.fieldsValues() shouldBe mapOf()
    }
  }

  @TestFactory
  fun `injector is generated with declared injected members`() = test {
    compile(
      """
      package tangle.inject.tests

      import tangle.inject.TangleScope
      import javax.inject.Inject

      @TangleScope(Unit::class)
      class Target {
        @Inject lateinit var str: String
      }
     """
    ) {
      val membersInjector = targetClass.membersInjector().createInstance("name".provider())
      val tangleInjector = targetClass.tangleInjector().createInstance(membersInjector) as TangleInjector<Any>

      val target = targetClass.createInstance()

      tangleInjector.inject(target)

      target.fieldsValues() shouldBe mapOf("str" to "name")
    }
  }

  @TestFactory
  fun `injector is generated with injected members only in superclass`() = test {
    compile(
      """
      package tangle.inject.tests

      import tangle.inject.TangleScope
      import javax.inject.Inject

      @TangleScope(Unit::class)
      class Target : Base()

      abstract class Base {
        @Inject lateinit var baseStr: String
      }
     """
    ) {
      val membersInjector = baseClass.membersInjector().createInstance("baseName".provider())
      val tangleInjector = targetClass.tangleInjector()
        .createInstance(membersInjector) as TangleInjector<Any>

      val target = targetClass.createInstance()

      tangleInjector.inject(target)

      target.fieldsValues() shouldBe mapOf("baseStr" to "baseName")
    }
  }

  @TestFactory
  fun `injector is generated with declared injected members and injected members in superclass`() =
    test {
      compile(
        """
      package tangle.inject.tests

      import tangle.inject.TangleScope
      import javax.inject.Inject

      @TangleScope(Unit::class)
      class Target : Base() {
        @Inject lateinit var str : String
      }

      abstract class Base {
        @Inject lateinit var baseStr : String
      }
     """
      ) {
        val membersInjector = targetClass.membersInjector()
          .createInstance("baseName".provider(), "name".provider())

        val tangleInjector = targetClass.tangleInjector()
          .createInstance(membersInjector) as TangleInjector<Any>

        val target = targetClass.createInstance()

        tangleInjector.inject(target)

        target.fieldsValues() shouldBe mapOf("str" to "name", "baseStr" to "baseName")
      }
    }
}
