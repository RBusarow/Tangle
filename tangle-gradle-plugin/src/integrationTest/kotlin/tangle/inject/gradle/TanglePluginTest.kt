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

import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.TestFactory
import java.io.File

class TanglePluginTest : BasePluginTest() {

  @TestFactory
  fun `default application should apply Anvil`() = test {

    module(
      """
      plugins {
        id("tangle.library.android")
        kotlin("android")
        id("com.rickbusarow.tangle")
      }

      android {
        compileSdk = 30

        defaultConfig {
          minSdk = 23
          targetSdk = 30
        }
      }

      ${listDepsTasks()}
      """.trimIndent()
    )

    File(testProjectDir, "module/src/main/java/tangle/inject/tests/Component.kt")
      .also { it.parentFile.mkdirs() }
      .writeText(
        """
        package tangle.inject.tests

        import com.squareup.anvil.annotations.ContributesTo

        @ContributesTo(Unit::class)
        interface Component
        """.trimIndent()
      )

    tasks("assembleDebug")
      .build()
      .task(":module:assembleDebug")!!
      .outcome shouldBe SUCCESS
  }

  @TestFactory
  fun `default application should add Tangle dependencies`() = test {

    module(
      """
      plugins {
        id("tangle.library.android")
        kotlin("android")
        id("com.rickbusarow.tangle")
      }

      android {
        compileSdk = 30

        defaultConfig {
          minSdk = 23
          targetSdk = 30
        }
      }

      dependencies {
        $activities
        $fragments
        $viewModels
        $compose
        $workManager
      }

      ${listDepsTasks()}
      """.trimIndent()
    )

    build("deps").tangleDeps() shouldBe listOf(
      "anvil com.rickbusarow.tangle:tangle-compiler",
      "anvil com.rickbusarow.tangle:tangle-fragment-compiler",
      "anvil com.rickbusarow.tangle:tangle-viewmodel-compiler",
      "anvil com.rickbusarow.tangle:tangle-work-compiler",
      "implementation com.rickbusarow.tangle:tangle-api",
      "implementation com.rickbusarow.tangle:tangle-fragment-api",
      "implementation com.rickbusarow.tangle:tangle-viewmodel-activity",
      "implementation com.rickbusarow.tangle:tangle-viewmodel-api",
      "implementation com.rickbusarow.tangle:tangle-viewmodel-compose",
      "implementation com.rickbusarow.tangle:tangle-viewmodel-fragment",
      "implementation com.rickbusarow.tangle:tangle-work-api"
    )
  }

  @TestFactory
  fun `only base compiler and api should be enabled without corresponding androidx dependencies`() =
    test {

      module(
        """
      plugins {
        id("tangle.library.android")
        kotlin("android")
        id("com.rickbusarow.tangle")
      }

      android {
        compileSdk = 30

        defaultConfig {
          minSdk = 23
          targetSdk = 30
        }
      }

      dependencies {
      }

      ${listDepsTasks()}
        """.trimIndent()
      )

      build("deps").tangleDeps() shouldBe listOf(
        "anvil com.rickbusarow.tangle:tangle-compiler",
        "implementation com.rickbusarow.tangle:tangle-api"
      )
    }

  @TestFactory
  fun `build will fail if applied to a module without AGP`() = test {

    module(
      """
      plugins {
        id("org.jetbrains.kotlin.jvm")
        id("com.rickbusarow.tangle")
      }

      tangle {
        viewModelOptions {
          composeEnabled = true // default is null
        }
      }

      ${listDepsTasks()}
      """.trimIndent()
    )

    tasks("test").shouldFailWithMessage(
      "A problem occurred configuring project ':module'.\n" +
        "> Tangle is applied to project ':module', but no Android plugin has been applied.  " +
        "Tangle serves no purpose unless the project is Android and the Kotlin plugin is applied."
    )
  }

  fun listDepsTasks() = """
  tasks.register("deps") {
    doLast {
      listOf("anvil", "api", "implementation")
        .forEach { config ->
          project.configurations
            .named(config)
            .get()
            .dependencies
            .forEach { println("${'$'}config ${'$'}{it.group}:${'$'}{it.name}") }
        }
    }
  }
  """.trimIndent()
}
