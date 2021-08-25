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
  javaLibrary
  id("com.vanniktech.maven.publish")
  kotlin("kapt")
}

dependencies {

  kapt(libs.google.auto.service.processor)

  api(libs.anvil.compiler.api)
  api(libs.kotlin.compiler)
  api(libs.square.kotlinPoet)

  api(projects.tangleCompiler)

  compileOnly(libs.androidx.annotations)
  compileOnly(libs.google.auto.service.processor)

  implementation(libs.anvil.annotations)
  implementation(libs.anvil.compiler.utils)
  implementation(libs.google.dagger.api)

  testImplementation(libs.anvil.compiler.core)
  testImplementation(libs.bundles.hermit)
  testImplementation(libs.bundles.kotest)
  testImplementation(libs.google.dagger.compiler)
  testImplementation(libs.kotlin.compile.testing)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()
  .configureEach {

    kotlinOptions {

      freeCompilerArgs = freeCompilerArgs + listOf(
        "-Xopt-in=com.squareup.anvil.annotations.ExperimentalAnvilApi"
      )
    }
  }
