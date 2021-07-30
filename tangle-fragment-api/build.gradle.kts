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

plugins {
  androidLibrary
  id("com.vanniktech.maven.publish")
}

dependencies {

  api(libs.google.dagger.api)
  api(projects.tangleApi)

  api(libs.androidx.savedstate)
  api(libs.androidx.fragment.ktx)
  api(libs.androidx.lifecycle.viewModel.savedstate)
  implementation(libs.androidx.lifecycle.viewModel.core)
}

kotlin {
  explicitApi()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()
  .configureEach {

    kotlinOptions {

      freeCompilerArgs = freeCompilerArgs + listOf(
        "-Xopt-in=com.squareup.anvil.annotations.ExperimentalAnvilApi"
      )
    }
  }

// https://youtrack.jetbrains.com/issue/KT-37652
tasks
  .matching { it is org.jetbrains.kotlin.gradle.tasks.KotlinCompile }
  .configureEach {
    val task = this
    val shouldEnable = !task.name.contains("test", ignoreCase = true)
    val kotlinCompile = task as org.jetbrains.kotlin.gradle.tasks.KotlinCompile

    if (shouldEnable && !project.hasProperty("kotlin.optOutExplicitApi")) {
      if ("-Xexplicit-api=strict" !in kotlinCompile.kotlinOptions.freeCompilerArgs) {
        kotlinCompile.kotlinOptions.freeCompilerArgs += "-Xexplicit-api=strict"
      }
    } else {
      kotlinCompile.kotlinOptions.freeCompilerArgs = kotlinCompile.kotlinOptions
        .freeCompilerArgs
        .filterNot { it == "-Xexplicit-api=strict" }
    }
  }
