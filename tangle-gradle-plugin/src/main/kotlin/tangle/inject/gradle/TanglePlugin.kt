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

import com.android.build.gradle.BaseExtension
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.kotlin.dsl.findByType

public open class TanglePlugin : BasePlugin() {

  @Suppress("UnstableApiUsage")
  override fun apply(target: Project) {
    val extension = target.extensions
      .create(EXTENSION_NAME, TangleExtension::class.java)
    var hasAndroid = false

    target.pluginManager.withPlugin(ANVIL_ID) {
      hasAndroid = target.extensions.findByType<BaseExtension>() != null

      target.addImplementation("tangle-api")
    }

    target.afterEvaluate {
      if (!hasAndroid) {
        throw GradleException(
          "Tangle is applied to project '${target.path}', " +
            "but $KOTLIN_ANDROID_ID has not been applied.  " +
            "Tangle serves no purpose unless the project is Android " +
            "and the Kotlin plugin is applied."
        )
      }

      if (extension.fragmentsEnabled.get()) {
        target.addImplementation("tangle-fragment-api")
        target.addAnvil("tangle-fragment-compiler")
      }

      if (extension.viewModelsEnabled.get()) {
        target.addImplementation("tangle-viewmodel-api")
        target.addAnvil("tangle-viewmodel-compiler")
      }

      if (extension.composeEnabled.get()) {

        val composeEnabled = target.extensions.findByType<BaseExtension>()
          ?.buildFeatures
          ?.compose
          ?: false

        if (!composeEnabled) {
          throw GradleException(
            "Tangle's compose support is enabled, but AGP's `buildFeatures.compose` is disabled. " +
              "Compose must be enabled in the Android Gradle Plugin."
          )
        }

        target.addImplementation("tangle-viewmodel-compose")
      }
    }
  }

  private fun Project.addAnvil(name: String) {
    dependencies.add(
      "anvil",
      "$TANGLE_GROUP:$name:$TANGLE_VERSION"
    )
  }

  private fun Project.addImplementation(name: String) {
    dependencies.add(
      "implementation",
      "$TANGLE_GROUP:$name:$TANGLE_VERSION"
    )
  }

  internal companion object {
    const val TANGLE_GROUP = "com.rickbusarow.tangle"
    const val TANGLE_VERSION = "0.11.5"
    const val EXTENSION_NAME = "tangle"
    const val KOTLIN_ANDROID_ID = "org.jetbrains.kotlin.android"
    const val ANVIL_ID = "com.squareup.anvil"
  }
}
