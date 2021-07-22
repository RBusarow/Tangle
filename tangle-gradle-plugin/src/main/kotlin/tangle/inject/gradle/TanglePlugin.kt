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
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.androidJvm
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import java.util.concurrent.atomic.AtomicBoolean

public open class TanglePlugin_Plain : BasePlugin() {

  @Suppress("UnstableApiUsage")
  override fun apply(target: Project) {
    val extension = target.extensions
      .create(EXTENSION_NAME, TangleExtension::class.java)

    val once = AtomicBoolean()

    fun PluginManager.withPluginOnce(
      id: String,
      action: (AppliedPlugin) -> Unit
    ) {
      withPlugin(id) {
        if (once.compareAndSet(false, true)) {
          action(this)
        }
      }
    }

    var hasAndroid = false

    target.pluginManager.withPluginOnce(ANVIL_ID) {
      hasAndroid = target.extensions.findByType<BaseExtension>() != null

      target.dependencies.add(
        "implementation",
        "$TANGLE_GROUP:tangle-annotations:$TANGLE_VERSION"
      )
      target.dependencies.add(
        "implementation",
        "$TANGLE_GROUP:tangle-api:$TANGLE_VERSION"
      )
      target.dependencies.add(
        "anvil",
        "$TANGLE_GROUP:tangle-compiler:$TANGLE_VERSION"
      )
    }

    target.afterEvaluate {
      if (!once.get()) {
        throw GradleException(
          "Tangle is applied to project '${target.path}', but $ANVIL_ID has not been applied.  " +
            "Tangle requires Anvil in order to generate code."
        )
      }
      if (!hasAndroid) {
        throw GradleException(
          "Tangle is applied to project '${target.path}', " +
            "but $KOTLIN_ANDROID_ID has not been applied.  " +
            "Tangle serves no purpose unless the project is Android " +
            "and the Kotlin plugin is applied."
        )
      }

      val composeEnabled = target.extensions.findByType<BaseExtension>()
        ?.buildFeatures
        ?.compose
        ?: false

      if (extension.composeEnabled.get()) {
        if (!composeEnabled) {
          throw GradleException(
            "Tangle's compose support is enabled, but AGP's `buildFeatures.compose` is disabled. " +
              "Compose must be enabled in the Android Gradle Plugin."
          )
        }

        target.dependencies.add(
          "implementation",
          "$TANGLE_GROUP:tangle-compose:$TANGLE_VERSION"
        )
      }
    }
  }

  internal companion object {
    const val TANGLE_GROUP = "com.rickbusarow.tangle"
    const val TANGLE_VERSION = "0.11.0"
    const val EXTENSION_NAME = "tangle"
    const val KOTLIN_ANDROID_ID = "org.jetbrains.kotlin.android"
    const val ANVIL_ID = "com.squareup.anvil"
  }
}

@Suppress("UnstableApiUsage")
internal open class TanglePlugin : KotlinCompilerPluginSupportPlugin {
  override fun applyToCompilation(
    kotlinCompilation: KotlinCompilation<*>
  ): Provider<List<SubpluginOption>> {

    val target = kotlinCompilation.target.project
    val extension = target.extensions
      .create(TanglePlugin.EXTENSION_NAME, TangleExtension::class.java)

    val once = AtomicBoolean()

    fun PluginManager.withPluginOnce(
      id: String,
      action: (AppliedPlugin) -> Unit
    ) {
      withPlugin(id) {
        if (once.compareAndSet(false, true)) {
          action(this)
        }
      }
    }

    var hasAndroid = false

    target.pluginManager.withPluginOnce(TanglePlugin.ANVIL_ID) {
      hasAndroid = target.extensions.findByType<BaseExtension>() != null

      target.dependencies.add(
        "implementation",
        "${TanglePlugin.TANGLE_GROUP}:tangle-annotations:${TanglePlugin.TANGLE_VERSION}"
      )
      target.dependencies.add(
        "implementation",
        "${TanglePlugin.TANGLE_GROUP}:tangle-api:${TanglePlugin.TANGLE_VERSION}"
      )
      target.dependencies.add(
        "anvil",
        "${TanglePlugin.TANGLE_GROUP}:tangle-compiler:${TanglePlugin.TANGLE_VERSION}"
      )
    }

    target.afterEvaluate {
      if (!once.get()) {
        throw GradleException(
          "Tangle is applied to project '${target.path}', but ${TanglePlugin.ANVIL_ID} has not been applied.  " +
            "Tangle requires Anvil in order to generate code."
        )
      }
      if (!hasAndroid) {
        throw GradleException(
          "Tangle is applied to project '${target.path}', " +
            "but ${TanglePlugin.KOTLIN_ANDROID_ID} has not been applied.  " +
            "Tangle serves no purpose unless the project is Android " +
            "and the Kotlin plugin is applied."
        )
      }

      val composeEnabled = target.extensions.findByType<BaseExtension>()
        ?.buildFeatures
        ?.compose
        ?: false

      if (extension.composeEnabled.get()) {
        if (!composeEnabled) {
          throw GradleException(
            "Tangle's compose support is enabled, but AGP's `buildFeatures.compose` is disabled. " +
              "Compose must be enabled in the Android Gradle Plugin."
          )
        }

        target.dependencies.add(
          "implementation",
          "${TanglePlugin.TANGLE_GROUP}:tangle-compose:${TanglePlugin.TANGLE_VERSION}"
        )
      }
    }

    return target.provider {
      listOf(
        SubpluginOption(
          key = "fooo",
          lazy { "true" }
        )
      )
    }
  }

  override fun getCompilerPluginId(): String = "com.squareup.anvil.compiler"

  override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
    groupId = GROUP,
    artifactId = "compiler",
    version = VERSION
  )

  override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
    return when (kotlinCompilation.platformType) {
      // If the variant is ignored, then don't apply the compiler plugin.
      androidJvm -> true
      else -> false
    }
  }

  internal companion object {
    const val TANGLE_GROUP = "com.rickbusarow.tangle"
    const val TANGLE_VERSION = "0.11.0"
    const val EXTENSION_NAME = "tangle"
    const val KOTLIN_ANDROID_ID = "org.jetbrains.kotlin.android"
    const val ANVIL_ID = "com.squareup.anvil"
  }
}

internal const val VERSION = "2.3.3"
internal const val GROUP = "com.squareup.anvil"
