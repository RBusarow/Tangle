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

import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.TestFactory
import java.io.File

public class TanglePluginTest : BasePluginTest() {

  @TestFactory
  fun `default application should apply Anvil`() = test {

    //language=kotlin
    module(
      """
      plugins {
        id("com.android.library")
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

    //language=kotlin
    module(
      """
      plugins {
        id("com.android.library")
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

    build("deps").deps() shouldBe listOf(
      "anvil com.rickbusarow.tangle:tangle-fragment-compiler",
      "anvil com.rickbusarow.tangle:tangle-viewmodel-compiler",
      "anvil com.rickbusarow.tangle:tangle-work-compiler",
      "implementation com.rickbusarow.tangle:tangle-api",
      "implementation com.rickbusarow.tangle:tangle-fragment-api",
      "implementation com.rickbusarow.tangle:tangle-viewmodel-activity",
      "implementation com.rickbusarow.tangle:tangle-viewmodel-api",
      "implementation com.rickbusarow.tangle:tangle-viewmodel-fragment",
      "implementation com.rickbusarow.tangle:tangle-work-api"
    )
  }

  @TestFactory
  fun `disabling fragments in config should disable their dependencies`() = test {

    //language=kotlin
    module(
      """
      plugins {
        id("com.android.library")
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

      tangle {
        fragmentsEnabled = false // default is true
      }

      ${listDepsTasks()}
      """.trimIndent()
    )

    build("deps").deps() shouldBe listOf(
      "anvil com.rickbusarow.tangle:tangle-viewmodel-compiler",
      "anvil com.rickbusarow.tangle:tangle-work-compiler",
      "implementation com.rickbusarow.tangle:tangle-api",
      "implementation com.rickbusarow.tangle:tangle-viewmodel-activity",
      "implementation com.rickbusarow.tangle:tangle-viewmodel-api",
      "implementation com.rickbusarow.tangle:tangle-viewmodel-fragment",
      "implementation com.rickbusarow.tangle:tangle-work-api"
    )
  }

  @TestFactory
  fun `disabling work in config should disable its dependencies`() = test {

    //language=kotlin
    module(
      """
      plugins {
        id("com.android.library")
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

      tangle {
        workEnabled = false // default is true
      }

      ${listDepsTasks()}
      """.trimIndent()
    )

    build("deps").deps() shouldBe listOf(
      "anvil com.rickbusarow.tangle:tangle-fragment-compiler",
      "anvil com.rickbusarow.tangle:tangle-viewmodel-compiler",
      "implementation com.rickbusarow.tangle:tangle-api",
      "implementation com.rickbusarow.tangle:tangle-fragment-api",
      "implementation com.rickbusarow.tangle:tangle-viewmodel-activity",
      "implementation com.rickbusarow.tangle:tangle-viewmodel-api",
      "implementation com.rickbusarow.tangle:tangle-viewmodel-fragment"
    )
  }

  @TestFactory
  fun `disabling viewModels in config should disable its dependencies`() = test {

    //language=kotlin
    module(
      """
      plugins {
        id("com.android.library")
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

      tangle {
        viewModelOptions {
          enabled = false // default is true
        }
      }

      ${listDepsTasks()}
      """.trimIndent()
    )

    build("deps").deps() shouldBe listOf(
      "anvil com.rickbusarow.tangle:tangle-fragment-compiler",
      "anvil com.rickbusarow.tangle:tangle-work-compiler",
      "implementation com.rickbusarow.tangle:tangle-api",
      "implementation com.rickbusarow.tangle:tangle-fragment-api",
      "implementation com.rickbusarow.tangle:tangle-work-api"
    )
  }

  @TestFactory
  fun `disabling viewModels fragments in config should disable the viewmodel fragment api dependency`() =
    test {

      //language=kotlin
      module(
        """
      plugins {
        id("com.android.library")
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

      tangle {
        viewModelOptions {
          fragmentsEnabled = false // default is true
        }
      }

      ${listDepsTasks()}
        """.trimIndent()
      )

      build("deps").deps() shouldBe listOf(
        "anvil com.rickbusarow.tangle:tangle-fragment-compiler",
        "anvil com.rickbusarow.tangle:tangle-viewmodel-compiler",
        "anvil com.rickbusarow.tangle:tangle-work-compiler",
        "implementation com.rickbusarow.tangle:tangle-api",
        "implementation com.rickbusarow.tangle:tangle-fragment-api",
        "implementation com.rickbusarow.tangle:tangle-viewmodel-activity",
        "implementation com.rickbusarow.tangle:tangle-viewmodel-api",
        "implementation com.rickbusarow.tangle:tangle-work-api"
      )
    }

  @TestFactory
  fun `disabling viewModels activities in config should disable the viewmodel activity api dependency`() =
    test {

      //language=kotlin
      module(
        """
      plugins {
        id("com.android.library")
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

      tangle {
        viewModelOptions {
          activitiesEnabled = false // default is true
        }
      }

      ${listDepsTasks()}
        """.trimIndent()
      )

      build("deps").deps() shouldBe listOf(
        "anvil com.rickbusarow.tangle:tangle-fragment-compiler",
        "anvil com.rickbusarow.tangle:tangle-viewmodel-compiler",
        "anvil com.rickbusarow.tangle:tangle-work-compiler",
        "implementation com.rickbusarow.tangle:tangle-api",
        "implementation com.rickbusarow.tangle:tangle-fragment-api",
        "implementation com.rickbusarow.tangle:tangle-viewmodel-api",
        "implementation com.rickbusarow.tangle:tangle-viewmodel-fragment",
        "implementation com.rickbusarow.tangle:tangle-work-api"
      )
    }

  @TestFactory
  fun `enabling viewModels compose in config should disable the viewmodel compose api dependency`() =
    test {

      //language=kotlin
      module(
        """
      plugins {
        id("com.android.library")
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

      tangle {
        viewModelOptions {
          composeEnabled = true // default is false
        }
      }

      ${listDepsTasks()}
        """.trimIndent()
      )

      build("deps").deps() shouldBe listOf(
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
  fun `build will fail if applied to a module without AGP`() = test {

    //language=kotlin
    module(
      """
      plugins {
        kotlin("jvm")
        id("com.rickbusarow.tangle")
      }

      tangle {
        viewModelOptions {
          composeEnabled = true // default is false
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

  public fun listDepsTasks() = """
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
