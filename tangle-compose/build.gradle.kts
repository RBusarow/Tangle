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
  `maven-publish`
}
android {

  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.0.0-rc01"
  }
}

dependencies {

  api(libs.androidx.compose.runtime)
  api(libs.androidx.lifecycle.viewModel.core)
  api(libs.androidx.navigation.common)

  compileOnly(libs.androidx.annotations)

  implementation(libs.androidx.lifecycle.viewModel.compose)
  implementation(libs.androidx.savedstate)
  implementation(projects.tangleApi)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()
  .configureEach {

    kotlinOptions {

      freeCompilerArgs = freeCompilerArgs + listOf(
        "-Xopt-in=com.squareup.anvil.annotations.ExperimentalAnvilApi"
      )
    }
  }


afterEvaluate {
  publishing {
    publications {
      create<MavenPublication>("maven") {

        groupId = "com.rickbusarow.tangle"
        artifactId = "tangle-compose"

        version = libs.versions.versionName.get()

        from(components["release"])
      }
    }
  }
}
