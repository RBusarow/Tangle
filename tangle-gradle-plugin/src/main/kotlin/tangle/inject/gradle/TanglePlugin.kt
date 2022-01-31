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

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

public open class TanglePlugin : BasePlugin() {

  override fun apply(target: Project) {
    val extension = target.extensions
      .create(EXTENSION_NAME, TangleExtension::class.java)

    if (!target.pluginManager.hasPlugin(ANVIL_ID)) {
      target.pluginManager.apply(ANVIL_ID)
    }

    target.afterEvaluate { project ->

      project.tasks.withType(KotlinCompile::class.java)
        .configureEach { kotlinCompile ->

          kotlinCompile.kotlinOptions {

            freeCompilerArgs = freeCompilerArgs + listOf(
              "-Xopt-in=kotlin.RequiresOptIn"
            )
          }
        }

      val hasAndroid = target.extensions.findByName("android") != null

      if (!hasAndroid) {
        throw GradleException(
          "Tangle is applied to project '${target.path}', " +
            "but no Android plugin has been applied.  " +
            "Tangle serves no purpose unless the project is Android " +
            "and the Kotlin plugin is applied."
        )
      }

      target.addImplementation("tangle-api")
      target.addAnvil("tangle-compiler")

      target.addFeatureDependencies(extension)
    }
  }

  private fun Project.addFeatureDependencies(
    extension: TangleExtension
  ) {

    val viewModelOptions = extension.viewModelOptions

    projectAndroidDependencyConfigs()
      .forEach { config ->

        if (extension.fragmentsEnabled ?: config.fragments) {
          addImplementation("tangle-fragment-api")
          addAnvil("tangle-fragment-compiler")
        }

        if (extension.workEnabled ?: config.workManager) {
          addImplementation("tangle-work-api")
          addAnvil("tangle-work-compiler")
        }

        if (viewModelOptions.enabled ?: config.viewModels) {
          addImplementation("tangle-viewmodel-api")
          addAnvil("tangle-viewmodel-compiler")

          if (viewModelOptions.activitiesEnabled ?: config.activities) {
            addImplementation("tangle-viewmodel-activity")
          }

          if (viewModelOptions.composeEnabled ?: config.compose) {
            addImplementation("tangle-viewmodel-compose")
          }
          if (viewModelOptions.fragmentsEnabled ?: config.fragments) {
            addImplementation("tangle-viewmodel-fragment")
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
