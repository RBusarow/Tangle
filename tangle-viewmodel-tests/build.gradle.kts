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
}

dependencies {

  compileOnly(libs.google.auto.service.processor)

  implementation(libs.androidx.annotations)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.ui.core)
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.lifecycle.viewModel.compose)
  implementation(libs.androidx.lifecycle.viewModel.core)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.navigation.runtime.ktx)
  implementation(libs.anvil.annotations)
  implementation(libs.anvil.compiler.api)
  implementation(libs.anvil.compiler.core)
  implementation(libs.anvil.compiler.utils)
  implementation(libs.bundles.hermit)
  implementation(libs.bundles.jUnit)
  implementation(libs.bundles.kotest)
  implementation(libs.google.dagger.api)
  implementation(libs.google.dagger.compiler)
  implementation(libs.javax.annotation.jsr250.api)
  implementation(libs.kotlin.annotation.processing)
  implementation(libs.kotlin.compile.testing)
  implementation(libs.kotlin.compiler)
  implementation(libs.kotlin.reflect)
  implementation(libs.square.kotlinPoet)

  implementation(projects.tangleApi)
  implementation(projects.tangleCompiler)

  implementation(projects.tangleFragmentApi)
  implementation(projects.tangleFragmentCompiler)

  implementation(projects.tangleTestUtils)

  implementation(projects.tangleViewmodelApi)
  implementation(projects.tangleViewmodelCompiler)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()
  .configureEach {

    kotlinOptions {

      freeCompilerArgs = freeCompilerArgs + listOf(
        "-Xopt-in=com.squareup.anvil.annotations.ExperimentalAnvilApi",
        "-Xopt-in=tangle.inject.InternalTangleApi"
      )
    }
  }
