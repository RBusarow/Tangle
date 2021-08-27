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
  kotlin("kapt")
}

dependencies {

  kapt(libs.google.auto.service.processor)
  kapt(libs.google.dagger.compiler)

  kaptTest(libs.google.dagger.compiler)

  api(libs.bundles.hermit)
  api(libs.bundles.jUnit)
  api(libs.bundles.kotest)
  api(libs.google.dagger.api)
  api(libs.google.dagger.compiler)
  api(libs.javax.annotation.jsr250.api)
  api(libs.junit.vintage)
  api(libs.kotest.assertions)
  api(libs.kotest.properties)
  api(libs.kotest.runner)
  api(libs.kotlin.annotation.processing)
  api(libs.kotlin.compile.testing)
  api(libs.kotlin.compiler)
  api(libs.kotlin.reflect)
  api(libs.kotlinx.coroutines.core)
  api(libs.kotlinx.coroutines.test)
  api(libs.robolectric) {
    exclude(group = "org.bouncycastle")
  }
  api(libs.square.anvil.annotations)
  api(libs.square.anvil.compiler.api)
  api(libs.square.anvil.compiler.core)
  api(libs.square.anvil.compiler.utils)
  api(libs.square.kotlinPoet)

  api(projects.tangleApi)
  api(projects.tangleCompiler)

  compileOnly(libs.google.auto.service.processor)
}
