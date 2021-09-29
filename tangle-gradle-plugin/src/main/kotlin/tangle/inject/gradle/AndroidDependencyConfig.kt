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

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency

internal data class AndroidDependencyConfig(
  val configName: String,
  val activities: Boolean,
  val compose: Boolean,
  val fragments: Boolean,
  val viewModels: Boolean,
  val workManager: Boolean
)

private const val ACTIVITY_GROUP = "androidx.activity"
private const val COMPOSE_GROUP = "androidx.compose.ui"
private const val FRAGMENT_GROUP = "androidx.fragment"
private const val WORKMANAGER_GROUP = "androidx.work"
private const val LIFECYCLE_GROUP = "androidx.lifecycle"
private const val VIEWMODEL_MODULE_PREFIX = "lifecycle-viewmodel"

internal fun Project.projectAndroidDependencyConfigs() = configurations
  .filterNot { it.name == "ktlintRuleset" }
  .mapNotNull { config ->

    config.androidDependencyConfigOrNull()

  }

private fun Configuration.androidDependencyConfigOrNull(): AndroidDependencyConfig? {
  var activities = false
  var compose = false
  var fragments = false
  var viewModels = false
  var workManager = false

  val deps = dependencies
    .withType(org.gradle.api.artifacts.ExternalModuleDependency::class.java)
    .takeIf { it.isNotEmpty() }
    ?: return null

  deps.forEach { dependency ->

    if (dependency.isActivities()) {
      activities = true
      return@forEach
    }
    if (dependency.isCompose()) {
      compose = true
      return@forEach
    }
    if (dependency.isFragments()) {
      fragments = true
      return@forEach
    }
    if (dependency.isViewModels()) {
      viewModels = true
      return@forEach
    }
    if (dependency.isWorkManager()) {
      workManager = true
      return@forEach
    }
  }

  return AndroidDependencyConfig(
    configName = name,
    activities = activities,
    compose = compose,
    fragments = fragments,
    viewModels = viewModels,
    workManager = workManager
  )
}

private fun ExternalModuleDependency.isActivities(): Boolean = group == ACTIVITY_GROUP
private fun ExternalModuleDependency.isCompose(): Boolean = group == COMPOSE_GROUP
private fun ExternalModuleDependency.isFragments(): Boolean = group == FRAGMENT_GROUP
private fun ExternalModuleDependency.isWorkManager(): Boolean = group == WORKMANAGER_GROUP
private fun ExternalModuleDependency.isViewModels(): Boolean =
  group == LIFECYCLE_GROUP && module.name.startsWith(VIEWMODEL_MODULE_PREFIX)
