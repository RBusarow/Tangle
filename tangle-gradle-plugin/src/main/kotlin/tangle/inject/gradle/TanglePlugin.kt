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

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

public open class TanglePlugin : BasePlugin() {

  override fun apply(target: Project) {
    val extension = target.extensions
      .create(EXTENSION_NAME, TangleExtension::class.java)

    if (!target.pluginManager.hasPlugin(ANVIL_ID)) {
      target.pluginManager.apply(ANVIL_ID)
    }

    target.afterEvaluate {

      val hasAndroid = target.extensions.findByName("android") != null

      if (!hasAndroid) {
        throw GradleException(
          "Tangle is applied to project '${target.path}', " +
            "but no Android plugin has been applied.  " +
            "Tangle serves no purpose unless the project is Android " +
            "and the Kotlin plugin is applied."
        )
      }

      target.pluginManager.withPlugin(ANVIL_ID) {

        target.addImplementation("tangle-api")

        if (extension.fragmentsEnabled) {
          target.addImplementation("tangle-fragment-api")
          target.addAnvil("tangle-fragment-compiler")
        }

        if (extension.workEnabled) {
          target.addImplementation("tangle-work-api")
          target.addAnvil("tangle-work-compiler")
        }

        val viewModelOptions = extension.viewModelOptions

        if (viewModelOptions.enabled) {
          target.addImplementation("tangle-viewmodel-api")
          target.addAnvil("tangle-viewmodel-compiler")

          if (viewModelOptions.activitiesEnabled) {
            target.addImplementation("tangle-viewmodel-activity")
          }
          if (viewModelOptions.composeEnabled) {
            target.addImplementation("tangle-viewmodel-compose")
          }
          if (viewModelOptions.fragmentsEnabled) {
            target.addImplementation("tangle-viewmodel-fragment")
          }
        }
      }
    }
  }

  private fun Project.addAnvil(name: String) {
    dependencies.add(
      "anvil",
      "${BuildProperties.GROUP}:$name:${BuildProperties.VERSION}"
    )
  }

  private fun Project.addImplementation(name: String) {
    dependencies.add(
      "implementation",
      "${BuildProperties.GROUP}:$name:${BuildProperties.VERSION}"
    )
  }

  internal companion object {

    const val EXTENSION_NAME = "tangle"
    const val ANVIL_ID = "com.squareup.anvil"
  }
}
