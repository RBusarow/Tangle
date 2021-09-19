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

package tangle.inject.gradle

import hermit.test.junit.HermitJUnit5
import io.kotest.matchers.collections.shouldContain
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import java.io.File
import kotlin.properties.Delegates
import io.kotest.matchers.shouldBe as kotestShouldBe

public val DEFAULT_GRADLE_VERSION: String = System
  .getProperty("tangle.gradleVersion", "7.2")
  /*
  * The GitHub Actions test matrix parses "7.0" into an Int and passes in a command line argument of "7".
  * That version doesn't resolve.  So if the String doesn't contain a period, just append ".0"
  */
  .let { prop ->
    if (prop.contains('.')) prop else "$prop.0"
  }
public val DEFAULT_KOTLIN_VERSION: String =
  System.getProperty("tangle.kotlinVersion", "1.5.30")
public val DEFAULT_AGP_VERSION: String =
  System.getProperty("tangle.agpVersion", "7.0.2")
public val DEFAULT_ANVIL_VERSION: String =
  System.getProperty("tangle.anvilVersion", "2.3.4")

public abstract class PluginTest : HermitJUnit5() {

  public val testProjectDir: File by resets {
    val className = testInfo.testClass.get().simpleName
      .replace("[^a-zA-Z0-9]".toRegex(), "_")

    val testName = testInfo.displayName
      .replace("[^a-zA-Z0-9]".toRegex(), "_")
      .replace("_{2,}".toRegex(), "_")
      .removeSuffix("_")

    File("build/tests/$className/$testName").also {
      it.mkdir()
    }
  }

  public val buildFile: File by resets {
    testProjectDir.mkdirs()
    File(testProjectDir, "build.gradle.kts").also {
      it.writeText(
        """buildscript {
            |  repositories {
            |    mavenCentral()
            |    google()
            |    maven("https://plugins.gradle.org/m2/")
            |  }
            |  dependencies {
            |    classpath("com.squareup.anvil:gradle-plugin:$anvilVersion")
            |    classpath("com.android.tools.build:gradle:$agpVersion")
            |    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
            |  }
            |}
            |allprojects {
            |  repositories {
            |    mavenCentral()
            |    google()
            |    maven("https://plugins.gradle.org/m2/")
            |  }
            |}
            |""".trimMargin()
      )
    }
  }

  @Suppress("BlockingMethodInNonBlockingContext")
  public val settingsFile: File by resets {
    testProjectDir.mkdirs()
    File(testProjectDir, "settings.gradle.kts").also {
      it.writeText(
        """pluginManagement {
          |  repositories {
          |    gradlePluginPortal()
          |    google()
          |  }
          |  resolutionStrategy {
          |    eachPlugin {
          |      if (requested.id.id.startsWith("com.android")) {
          |        useVersion("$agpVersion")
          |      }
          |      if (requested.id.id.startsWith("org.jetbrains.kotlin")) {
          |        useVersion("$kotlinVersion")
          |      }
          |    }
          |  }
          |}
          |include(":module")
          |""".trimMargin()
      )
    }
  }

  public fun File.relativePath(): String = path.removePrefix(testProjectDir.path)

  public fun String.fixPath(): String = replace(File.separator, "/")

  private val gradleVersion = DEFAULT_GRADLE_VERSION

  private val kotlinVersion = DEFAULT_KOTLIN_VERSION
  private val agpVersion = DEFAULT_AGP_VERSION
  private val anvilVersion = DEFAULT_ANVIL_VERSION

  public val gradleRunner: GradleRunner by resets {
    GradleRunner
      .create()
      .forwardOutput()
      .withGradleVersion(gradleVersion)
      .withPluginClasspath()
      // .withDebug(true)
      .withProjectDir(testProjectDir)
  }

  private var testInfo: TestInfo by Delegates.notNull()

  // This is automatically injected by JUnit5
  @BeforeEach
  internal fun injectTestInfo(testInfo: TestInfo) {
    this.testInfo = testInfo
    testProjectDir.delete()
  }

  public fun module(buildFile: String) {
    File(testProjectDir, "module").mkdirs()
    File(testProjectDir, "module/src/main").mkdirs()
    File(testProjectDir, "module/src/main/AndroidManifest.xml")
      .writeText("<manifest package=\"module.foo\" />")
    File(testProjectDir, "module/build.gradle.kts")
      .writeText(buildFile)
  }

  public fun build(vararg tasks: String): BuildResult {
    settingsFile
    buildFile
    return gradleRunner.withArguments(*tasks).build()
  }

  public fun BuildResult.shouldSucceed() {
    tasks.forEach { it.outcome shouldBe TaskOutcome.SUCCESS }
  }

  @Language("RegExp")
  public fun BuildResult.deps(): List<String> = output
    .replace("[\\s\\S]*> Task :module:\\S*\\s*".toRegex(), "")
    .replace(
      "\\s*BUILD SUCCESSFUL in \\d*[m]*s\\s*\\d* actionable task: \\d* executed\\s*".toRegex(), ""
    )
    .lines()
    .filterNot { it.isBlank() }
    .sorted()

  public fun shouldFailWithMessage(vararg tasks: String, messageBlock: (String) -> Unit) {
    val result = gradleRunner.withArguments(*tasks).buildAndFail()

    result.tasks.map { it.outcome } shouldContain TaskOutcome.FAILED
    messageBlock(result.output.fixPath())
  }

  @Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
  public infix fun <T, U : T> T.shouldBe(expected: U?) {
    /*
    Any AssertionError generated by this function will have this function at the top of its stacktrace.

    The actual call site for the assertion is always the _second_ line.

    So, we can catch the assertion error, remove this function from the stacktrace, and rethrow.
     */
    try {
      kotestShouldBe(expected)
    } catch (assertionError: AssertionError) {
      // remove this function from the stacktrace and rethrow
      assertionError.stackTrace = assertionError
        .stackTrace
        .drop(1)
        .toTypedArray()
      throw assertionError
    }
  }
}
