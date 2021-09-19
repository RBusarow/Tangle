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

import org.junit.jupiter.api.Test

public class TanglePluginTest : PluginTest() {

  @Test
  fun `default application should apply Anvil`() {

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

    // The configuration only exists if Anvil is applied, so this would fail without the plugin
    build("anvil").shouldSucceed()
  }

  @Test
  fun `default application should add Tangle dependencies`() {

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

    build("anvil").deps() shouldBe listOf(
      "com.rickbusarow.tangle:tangle-fragment-compiler",
      "com.rickbusarow.tangle:tangle-viewmodel-compiler",
      "com.rickbusarow.tangle:tangle-work-compiler"
    )
    build("implementation").deps() shouldBe listOf(
      "com.rickbusarow.tangle:tangle-api",
      "com.rickbusarow.tangle:tangle-fragment-api",
      "com.rickbusarow.tangle:tangle-viewmodel-activity",
      "com.rickbusarow.tangle:tangle-viewmodel-api",
      "com.rickbusarow.tangle:tangle-viewmodel-fragment",
      "com.rickbusarow.tangle:tangle-work-api"
    )
    build("api").deps() shouldBe listOf()
  }

  @Test
  fun `disabling fragments in config should disable their dependencies`() {

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

    build("anvil").deps() shouldBe listOf(
      "com.rickbusarow.tangle:tangle-viewmodel-compiler",
      "com.rickbusarow.tangle:tangle-work-compiler"
    )
    build("implementation").deps() shouldBe listOf(
      "com.rickbusarow.tangle:tangle-api",
      "com.rickbusarow.tangle:tangle-viewmodel-activity",
      "com.rickbusarow.tangle:tangle-viewmodel-api",
      "com.rickbusarow.tangle:tangle-viewmodel-fragment",
      "com.rickbusarow.tangle:tangle-work-api"
    )
    build("api").deps() shouldBe listOf()
  }

  @Test
  fun `disabling work in config should disable its dependencies`() {

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

    build("anvil").deps() shouldBe listOf(
      "com.rickbusarow.tangle:tangle-fragment-compiler",
      "com.rickbusarow.tangle:tangle-viewmodel-compiler"
    )
    build("implementation").deps() shouldBe listOf(
      "com.rickbusarow.tangle:tangle-api",
      "com.rickbusarow.tangle:tangle-fragment-api",
      "com.rickbusarow.tangle:tangle-viewmodel-activity",
      "com.rickbusarow.tangle:tangle-viewmodel-api",
      "com.rickbusarow.tangle:tangle-viewmodel-fragment"
    )
    build("api").deps() shouldBe listOf()
  }

  @Test
  fun `disabling viewModels in config should disable its dependencies`() {

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

    build("anvil").deps() shouldBe listOf(
      "com.rickbusarow.tangle:tangle-fragment-compiler",
      "com.rickbusarow.tangle:tangle-work-compiler"
    )
    build("implementation").deps() shouldBe listOf(
      "com.rickbusarow.tangle:tangle-api",
      "com.rickbusarow.tangle:tangle-fragment-api",
      "com.rickbusarow.tangle:tangle-work-api"
    )
    build("api").deps() shouldBe listOf()
  }

  @Test
  fun `disabling viewModels fragments in config should disable the viewmodel fragment api dependency`() {

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

    build("anvil").deps() shouldBe listOf(
      "com.rickbusarow.tangle:tangle-fragment-compiler",
      "com.rickbusarow.tangle:tangle-viewmodel-compiler",
      "com.rickbusarow.tangle:tangle-work-compiler"
    )
    build("implementation").deps() shouldBe listOf(
      "com.rickbusarow.tangle:tangle-api",
      "com.rickbusarow.tangle:tangle-fragment-api",
      "com.rickbusarow.tangle:tangle-viewmodel-activity",
      "com.rickbusarow.tangle:tangle-viewmodel-api",
      "com.rickbusarow.tangle:tangle-work-api"
    )
    build("api").deps() shouldBe listOf()
  }

  @Test
  fun `disabling viewModels activities in config should disable the viewmodel activity api dependency`() {

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

    build("anvil").deps() shouldBe listOf(
      "com.rickbusarow.tangle:tangle-fragment-compiler",
      "com.rickbusarow.tangle:tangle-viewmodel-compiler",
      "com.rickbusarow.tangle:tangle-work-compiler"
    )
    build("implementation").deps() shouldBe listOf(
      "com.rickbusarow.tangle:tangle-api",
      "com.rickbusarow.tangle:tangle-fragment-api",
      "com.rickbusarow.tangle:tangle-viewmodel-api",
      "com.rickbusarow.tangle:tangle-viewmodel-fragment",
      "com.rickbusarow.tangle:tangle-work-api"
    )
    build("api").deps() shouldBe listOf()
  }

  @Test
  fun `enabling viewModels compose in config should disable the viewmodel compose api dependency`() {

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

    build("anvil").deps() shouldBe listOf(
      "com.rickbusarow.tangle:tangle-fragment-compiler",
      "com.rickbusarow.tangle:tangle-viewmodel-compiler",
      "com.rickbusarow.tangle:tangle-work-compiler"
    )
    build("implementation").deps() shouldBe listOf(
      "com.rickbusarow.tangle:tangle-api",
      "com.rickbusarow.tangle:tangle-fragment-api",
      "com.rickbusarow.tangle:tangle-viewmodel-activity",
      "com.rickbusarow.tangle:tangle-viewmodel-api",
      "com.rickbusarow.tangle:tangle-viewmodel-compose",
      "com.rickbusarow.tangle:tangle-viewmodel-fragment",
      "com.rickbusarow.tangle:tangle-work-api"
    )
    build("api").deps() shouldBe listOf()
  }

  public fun listDepsTasks() = """
    listOf("anvil", "api", "implementation")
  .forEach { config ->
    tasks.register(config) {
      doLast {
        project.configurations
          .named(config)
          .get()
          .dependencies
          .forEach { println("${'$'}{it.group}:${'$'}{it.name}") }
      }
    }
  }
  """.trimIndent()
}
