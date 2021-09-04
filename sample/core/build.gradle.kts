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
  id("com.squareup.anvil")
  scabbard
}

anvil {
  generateDaggerFactories.set(true)
}

android {

  buildFeatures {
    viewBinding = true
  }
}

dependencies {

  anvil(projects.tangleCompiler)
  anvil(projects.tangleFragmentCompiler)
  anvil(projects.tangleViewmodelCompiler)
  anvil(projects.tangleWorkCompiler)

  api(libs.coil.core)
  api(libs.rickBusarow.dispatch.core)
  api(libs.rickBusarow.dispatch.lifecycle)
  api(libs.rickBusarow.dispatch.lifecycleExtensions)
  api(libs.timber)

  api(projects.tangleApi)
  api(projects.tangleWorkApi)

  implementation(libs.androidx.activity.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.constraintLayout)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.lifecycle.viewModel.ktx)
  implementation(libs.androidx.work.ktx)
  implementation(libs.google.dagger.api)
  implementation(libs.google.material.android)

  implementation(projects.tangleApi)
  implementation(projects.tangleViewmodelActivity)
  implementation(projects.tangleViewmodelApi)
  implementation(projects.tangleViewmodelCompose)
  implementation(projects.tangleViewmodelFragment)

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
