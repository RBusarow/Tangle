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
  `kotlin-dsl`
}

repositories {
  mavenCentral()
  google()
  maven("https://plugins.gradle.org/m2/")
}

val kotlinVersion = libs.versions.kotlin.get()

dependencies {
  implementation(platform(libs.kotlin.bom))

  compileOnly(gradleApi())

  implementation(libs.kotlin.reflect)
  implementation(libs.kotlin.stdlib.jdk8)
  implementation(libs.kotlin.annotation.processing)
  implementation(libs.kotlin.compiler)
  implementation(libs.kotlin.gradle.pluginApi)
  implementation(libs.ktlint.gradle)

  implementation(kotlin("gradle-plugin", version = kotlinVersion))
  implementation(kotlin("stdlib", version = kotlinVersion))
  implementation(kotlin("stdlib-common", version = kotlinVersion))
  implementation(kotlin("stdlib-jdk7", version = kotlinVersion))
  implementation(kotlin("stdlib-jdk8", version = kotlinVersion))
  implementation(kotlin("reflect", version = kotlinVersion))

  implementation(libs.vanniktech.publish)
  implementation(libs.android.gradle)
  implementation(libs.dokka.gradle)
  implementation(libs.dropbox.dependencyGuard)
  implementation(libs.kotlinx.knit.gradle)
}
