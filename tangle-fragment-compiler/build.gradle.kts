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
  kotlin("kapt")
  id("tangle.library.java")
  `maven-publish`
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = "com.rickbusarow.tangle"
      artifactId = "tangle-fragment-compiler"

      from(components["java"])
    }
  }
}

//experimentalAnvil()

dependencies {

  implementation(libs.google.auto.service.processor)

  api(libs.kotlin.reflect)
  api(libs.square.anvil.compiler.api)
  api(libs.square.kotlinPoet)

  api(projects.tangleCompiler)

  compileOnly(libs.google.auto.service.processor)

  implementation(libs.google.dagger.api)
  implementation(libs.square.anvil.annotations)
  implementation(libs.square.anvil.compiler.utils)

  testImplementation(libs.bundles.hermit)
  testImplementation(libs.bundles.kotest)
  testImplementation(libs.google.dagger.compiler)
  testImplementation(libs.kotlin.compile.testing)
  testImplementation(libs.square.anvil.compiler.core)
}
