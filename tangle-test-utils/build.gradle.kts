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
  javaLibrary
  kotlin("kapt")
}

experimentalAnvil()

dependencies {

  kapt(libs.google.dagger.compiler)

  api(libs.bundles.hermit)
  api(libs.bundles.jUnit)
  api(libs.bundles.kotest)
  api(libs.google.dagger.api)
  api(libs.google.dagger.compiler)
  api(libs.javax.annotation.jsr250.api)
  api(libs.junit.vintage)
  api(libs.kotlin.compile.testing)
  api(libs.kotlin.compiler)
  api(libs.kotlin.reflect)
  api(libs.square.anvil.annotations)
  api(libs.square.anvil.compiler.api)
  api(libs.square.anvil.compiler.core)
  api(libs.square.anvil.compiler.utils)
  api(libs.square.kotlinPoet)

  api(projects.tangleApi)
  api(projects.tangleCompiler)

  compileOnly(libs.google.auto.service.processor)
}
