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

  api(libs.androidx.fragment.ktx)
  api(libs.androidx.lifecycle.viewModel.savedstate)
  api(libs.androidx.savedstate)
  api(libs.google.dagger.api)

  api(projects.tangleApi)
  api(projects.tangleViewmodelApi)

  implementation(libs.androidx.lifecycle.viewModel.ktx)

  testCompileOnly(libs.google.auto.service.processor)

  testImplementation(projects.tangleApi)
  testImplementation(projects.tangleCompiler)
  testImplementation(projects.tangleFragmentApi)
  testImplementation(projects.tangleFragmentCompiler)
  testImplementation(projects.tangleTestUtils)
  testImplementation(projects.tangleTestUtilsAndroid)
  testImplementation(projects.tangleViewmodelCompiler)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()
  .configureEach {

    kotlinOptions {

      freeCompilerArgs = freeCompilerArgs + listOf(
        "-Xopt-in=com.squareup.anvil.annotations.ExperimentalAnvilApi"
      )
    }
  }
