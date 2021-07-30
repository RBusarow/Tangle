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
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

buildscript {
  repositories {
    mavenCentral()
    google()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:7.0.0")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
    classpath("org.jetbrains.kotlinx:kotlinx-knit:0.3.0")
    classpath("org.jmailen.gradle:kotlinter-gradle:3.4.5")
  }
}

plugins {
  id("java-gradle-plugin")
  kotlin("jvm")
  id("com.gradle.plugin-publish") version "0.15.0"
  `kotlin-dsl`
  `maven-publish`
}

repositories {
  google()
  mavenCentral()
  maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {

  compileOnly(libs.androidGradlePlugin)
  compileOnly(libs.kotlin.reflect)

  implementation(libs.kotlin.annotation.processing)
  implementation(libs.kotlin.gradle.pluginApi)
  implementation(libs.kotlin.compiler)
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlin.stdlib.jdk8)

  testImplementation(libs.bundles.hermit)
  testImplementation(libs.bundles.jUnit)
  testImplementation(libs.bundles.kotest)
}

kotlin {
  explicitApi()
}

val ci = !System.getenv("CI").isNullOrBlank()

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()
  .configureEach {

    doFirst {
      val tangleVersion = libs.versions.versionName.get()

      System.setProperty("tangle.version", tangleVersion)
    }

    kotlinOptions {

      allWarningsAsErrors = false

      jvmTarget = "1.8"
    }
  }

tasks.withType<Test> {
  useJUnitPlatform()

  testLogging {
    events = setOf(
      TestLogEvent.PASSED,
      TestLogEvent.FAILED
    )
    exceptionFormat = TestExceptionFormat.FULL
    showExceptions = true
    showCauses = true
    showStackTraces = true
  }

  project
    .properties
    .asSequence()
    .filter { (key, value) ->
      key.startsWith("tangle") && value != null
    }
    .forEach { (key, value) ->
      systemProperty(key, value!!)
    }
}

java {
  // force Java 8 source when building java-only artifacts.
  // This is different than the Kotlin jvm target.
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

val testJvm by tasks.registering {
  dependsOn("test")
}

val buildTests by tasks.registering {
  dependsOn("testClasses")
}

gradlePlugin {
  plugins {
    create("tangle") {
      id = "com.rickbusarow.tangle"
      group = "com.rickbusarow.tangle"
      implementationClass = "tangle.inject.gradle.TanglePlugin"
      version = libs.versions.versionName.get()
    }
  }
}

pluginBundle {
  website = "https://github.com/RBusarow/Tangle"
  vcsUrl = "https://github.com/RBusarow/Tangle"
  description = "Fast dependency graph validation for gradle"
  tags = listOf("android", "dagger2", "kotlin", "kotlin-compiler-plugin")

  plugins {
    getByName("tangle") {
      displayName = "Hilt-like ViewModel injection using the Anvil compiler plugin"
    }
  }
}

tasks.create("setupPluginUploadFromEnvironment") {
  doLast {
    val key = System.getenv("GRADLE_PUBLISH_KEY")
    val secret = System.getenv("GRADLE_PUBLISH_SECRET")

    if (key == null || secret == null) {
      throw GradleException(
        "gradlePublishKey and/or gradlePublishSecret are not defined environment variables"
      )
    }

    System.setProperty("gradle.publish.key", key)
    System.setProperty("gradle.publish.secret", secret)
  }
}
