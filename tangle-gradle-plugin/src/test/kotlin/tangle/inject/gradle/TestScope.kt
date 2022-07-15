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

package tangle.inject.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.TestInfo
import java.io.File

public data class TestScope(
  val testInfo: TestInfo,
  val gradleVersion: String,
  val kotlinVersion: String,
  val agpVersion: String,
  val anvilVersion: String
) {

  public val classNameDir: File by lazy {
    val className = testInfo.testClass
      .get()
      .simpleName
      .replace("[^a-zA-Z0-9]".toRegex(), "_")

    File("build/tests/$className").also {
      it.mkdir()
    }
  }

  public val testProjectDir: File by lazy {
    val testName = testInfo.displayName
      .replace("[^a-zA-Z0-9]".toRegex(), "_")
      .replace("_{2,}".toRegex(), "_")
      .removeSuffix("_")

    val scopeName = toString()
      .replace("[^a-zA-Z0-9]".toRegex(), "_")
      .replace("_{2,}".toRegex(), "_")
      .removeSuffix("_")

    File(classNameDir, "$testName/$scopeName").also {
      it.mkdir()
    }
  }

  public fun gradleRunner(): GradleRunner = GradleRunner.create()
    .forwardOutput()
    .withGradleVersion(gradleVersion)
    .withPluginClasspath()
    // .withDebug(true)
    .withProjectDir(testProjectDir)

  public fun projectBuildFile(): File {
    testProjectDir.mkdirs()
    return File(testProjectDir, "build.gradle.kts").also {
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
        |  configurations.all {
        |    resolutionStrategy {
        |
        |      eachDependency {
        |        // use dynamic version because Gradle-test add the **plugin** classpath,
        |        // but not the other artifacts
        |        if (requested.group == "com.rickbusarow.tangle") {
        |          useTarget("${'$'}{requested.group}:${'$'}{requested.module.name}:0.1+")
        |        }
        |      }
        |    }
        |  }
        |}
        |
        """.trimMargin()
      )
    }
  }

  public fun settingsFile(): File {
    testProjectDir.mkdirs()
    return File(testProjectDir, "settings.gradle.kts").also {
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
        |
        """.trimMargin()
      )
    }
  }

  private fun propertiesFile() {
    File(testProjectDir, "gradle.properties")
      .writeText(
        """org.gradle.jvmargs=-Xmx3g -XX:MaxMetaspaceSize=1g
          |android.useAndroidX=true
          |org.gradle.daemon=false
          |org.gradle.caching=false
        """.trimMargin()
      )
  }

  public fun module(
    @Language("kotlin")
    buildFile: String
  ) {
    File(testProjectDir, "module/src/main").mkdirs()
    File(testProjectDir, "module/src/main/AndroidManifest.xml")
      .writeText("<manifest package=\"module.foo\" />")
    File(testProjectDir, "module/build.gradle.kts")
      .writeText(buildFile)
  }

  public fun build(vararg tasks: String): BuildResult {
    propertiesFile()
    settingsFile()
    projectBuildFile()
    return gradleRunner().withArguments(*tasks).build()
  }

  public fun tasks(vararg tasks: String): GradleRunner {
    propertiesFile()
    settingsFile()
    projectBuildFile()
    return gradleRunner().withArguments(*tasks)
  }

  override fun toString(): String {
    return "[gradle='$gradleVersion', kotlin='$kotlinVersion', agp='$agpVersion', anvil='$anvilVersion']"
  }
}
