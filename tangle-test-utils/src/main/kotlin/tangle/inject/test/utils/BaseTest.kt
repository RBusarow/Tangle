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
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.COMPILATION_ERROR
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import hermit.test.junit.HermitJUnit5
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestInfo
import java.io.File
import kotlin.properties.Delegates
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

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
  protected fun compileWithDagger(
    @Language("kotlin")
    vararg sources: String,
    shouldFail: Boolean = false,
    block: KotlinCompilation.Result.() -> Unit = { }
  ) {
    fun String.clean() = replace("[^a-zA-Z0-9]".toRegex(), "_")

    val className = testInfo.testClass.get().simpleName

    val testName = testInfo.displayName
      .clean()
      .replace("_{2,}".toRegex(), "_")
      .removeSuffix("_")

    val workingDir = File("build/test-builds/$className/$testName")

    compileTangle(
      sources = sources,
      enableDaggerAnnotationProcessor = true,
      generateDaggerFactories = false,
      // Many constructor parameters are unused.
      allWarningsAsErrors = false,
      block = { checkExitCode(shouldFail).block() },
      workingDir = workingDir
    )
  }

  @Suppress("NewApi")
  protected fun TestScope.compile(
    @Language("kotlin")
    vararg sources: String,
    shouldFail: Boolean = false,
    block: KotlinCompilation.Result.() -> Unit = { }
  ): KotlinCompilation.Result {
    fun String.clean() = replace("[^a-zA-Z0-9]".toRegex(), "_")

    val className = testInfo.testClass.get().simpleName

    val testName = testInfo.displayName
      .clean()
      .replace("_{2,}".toRegex(), "_")
      .removeSuffix("_")

    val compilerType = if (useAnvilFactories) "anvil" else "dagger"

    val workingDir = File("build/test-builds/$className/$compilerType/$testName")

    return compileTangle(
      sources = sources,
      enableDaggerAnnotationProcessor = !useAnvilFactories,
      generateDaggerFactories = useAnvilFactories,
      // Many constructor parameters are unused.
      allWarningsAsErrors = false,
      block = { checkExitCode(shouldFail).block() },
      workingDir = workingDir
    )
  }

  fun KotlinCompilation.Result.checkExitCode(shouldFail: Boolean) = apply {
    val expectedCode = if (shouldFail) {
      COMPILATION_ERROR
    } else {
      OK
    }

    messages.asClue {
      exitCode shouldBe expectedCode
    }
  }

  infix fun String.shouldContainIgnoringWhitespaces(expected: String) {
    val actual = this.replace("\\s{2,}".toRegex(), " ").trim()
    val expectedClean = expected.replace("\\s{2,}".toRegex(), " ").trim()
    actual shouldContain expectedClean
  }

  data class TestScope(val useAnvilFactories: Boolean)

  fun clearTangleGraph() {

    val graphClass = javaClass.classLoader
      .loadClass("tangle.inject.TangleGraph")
      .kotlin

    val graphInstance = graphClass.objectInstance ?: return

    val components = graphClass.memberProperties
      .find { it.name == "components" }
      ?: return

    components.isAccessible = true

    (components.call(graphInstance) as MutableSet<Any>).clear()
  }
}
