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

package tangle.inject.test.utils

import com.tschuchort.compiletesting.KotlinCompilation
import hermit.test.junit.HermitJUnit5
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestInfo
import java.io.File
import kotlin.properties.Delegates

abstract class BaseTest : HermitJUnit5() {

  private var testInfo: TestInfo by Delegates.notNull()

  // This is automatically injected by JUnit5
  @BeforeEach
  internal fun injectTestInfo(testInfo: TestInfo) {
    this.testInfo = testInfo
  }

  inline fun test(crossinline action: TestScope.() -> Unit) = listOf(true, false)
    .map { useAnvilFactoryGen ->
      val name = if (useAnvilFactoryGen) "anvil" else "dagger"
      DynamicTest.dynamicTest(name) {
        action.invoke(TestScope(useAnvilFactoryGen))
      }
    }

  @Suppress("NewApi")
  protected fun TestScope.compile(
    vararg sources: String,
    block: KotlinCompilation.Result.() -> Unit = { }
  ): KotlinCompilation.Result {
    fun String.clean() = replace("[^a-zA-Z0-9]".toRegex(), "_")

    val testName = testInfo.testClass.get().simpleName
      .plus("-")
      .plus(testInfo.displayName.clean())
      .plus("_use-anvil-$useAnvilFactories")
      .replace("_{2,}".toRegex(), "_")

    return compileAnvil(
      sources = sources,
      enableDaggerAnnotationProcessor = !useAnvilFactories,
      generateDaggerFactories = useAnvilFactories,
      // Many constructor parameters are unused.
      allWarningsAsErrors = false,
      block = block,
      workingDir = File("build/test-builds/$testName")
    )
  }

  data class TestScope(val useAnvilFactories: Boolean)
}
