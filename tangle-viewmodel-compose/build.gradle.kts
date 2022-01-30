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
  id("com.vanniktech.maven.publish")
}
android {

  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
  }
}

dependencies {

  api(libs.androidx.compose.compiler)
  api(libs.androidx.compose.runtime)
  api(libs.androidx.lifecycle.viewModel.core)
  api(libs.androidx.navigation.common)

  compileOnly(libs.androidx.annotations)

  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.fragment.core)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.lifecycle.viewModel.compose)
  implementation(libs.androidx.savedstate)

  implementation(projects.tangleApi)
  implementation(projects.tangleViewmodelApi)
}
