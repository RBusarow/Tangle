/*
 * Copyright (C) 2023 Rick Busarow
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
  `java-gradle-plugin`
  `maven-publish`
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

gradlePlugin {
  plugins {

    create("plugins.tangle.library.androidapp") {
      id = "tangle.library.androidapp"
      implementationClass = "com.tangle.plugins.AndroidApplication"
      description = "Set up a module as a Kotlin library module access to Android App"
    }

    create("plugins.tangle.library.android") {
      id = "tangle.library.android"
      implementationClass = "com.tangle.plugins.AndroidLibrary"
      description = "Set up a module as a Kotlin library module access to Android Libraries"
    }

    create("plugins.tangle.library.java") {
      id = "tangle.library.java"
      implementationClass = "com.tangle.plugins.JavaLibrary"
      description = "Set up a module as a Kotlin library module access to JavaLibrary Libraries"
    }
  }
}
