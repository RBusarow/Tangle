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

plugins {
  androidLibrary
  id("com.squareup.anvil")
  scabbard
}

anvil {
  generateDaggerFactories.set(true)
}

android {

  buildFeatures {
    viewBinding = true
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
  }
}

dependencies {

  anvil(projects.tangleCompiler)
  anvil(projects.tangleFragmentCompiler)
  anvil(projects.tangleViewmodelCompiler)
  anvil(projects.tangleWorkCompiler)

  api(projects.sample.core)
  api(projects.sample.data)
  api(projects.tangleApi)
  api(projects.tangleFragmentApi)
  api(projects.tangleViewmodelApi)
  api(projects.tangleViewmodelCompose)
  api(projects.tangleViewmodelFragment)

  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.activity.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.compose.material.core)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.ui.core)
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.constraintLayout)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.lifecycle.viewModel.compose)
  implementation(libs.androidx.lifecycle.viewModel.ktx)
  implementation(libs.androidx.navigation.fragment.ktx)
  implementation(libs.androidx.navigation.ui.ktx)
  implementation(libs.androidx.paging.compose)
  implementation(libs.androidx.startup.runtime)
  implementation(libs.androidx.work.ktx)
  implementation(libs.coil.compose)
  implementation(libs.google.dagger.api)
  implementation(libs.google.material.android)

  implementation(projects.tangleViewmodelActivity)
  implementation(projects.tangleWorkApi)

  testImplementation(projects.tangleTestUtils)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()
  .configureEach {

    kotlinOptions {

      freeCompilerArgs = freeCompilerArgs + listOf(
        "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
      )
    }
  }
