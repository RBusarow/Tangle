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
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("java-gradle-plugin")
  kotlin("jvm")
  id("com.gradle.plugin-publish") version "0.20.0"
  `maven-publish`
}

repositories {
  google()
  mavenCentral()
  maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {

  compileOnly(libs.android.gradle)
  compileOnly(libs.kotlin.reflect)

  implementation(libs.kotlin.annotation.processing)
  implementation(libs.kotlin.compiler)
  implementation(libs.kotlin.gradle.plug)
  implementation(libs.kotlin.gradle.pluginApi)
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

tasks.withType<KotlinCompile>()
  .configureEach {

    doFirst {
      val tangleVersion = project.extra.properties["VERSION_NAME"] as String

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
      version = project.extra.properties["VERSION_NAME"] as String
    }
  }
}

pluginBundle {
  website = "https://github.com/RBusarow/Tangle"
  vcsUrl = "https://github.com/RBusarow/Tangle"
  description = "Create Android component bindings for Dagger with Anvil"
  tags = listOf("android", "dagger2", "kotlin", "kotlin-compiler-plugin")

  plugins {
    getByName("tangle") {
      displayName = "Create Android component bindings for Dagger with Anvil"
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

/*
Adapted from Anvil:
https://github.com/square/anvil/blob/main/gradle-plugin/generate_build_properties.gradle

This pipes the current version and group from gradle.properties into generated source,
so that the plugin always applies the correct artifacts.
 */
val generatedDirPath = "$buildDir/generated/sources/build-properties/kotlin/main"
sourceSets {
  main.configure {
    java.srcDir(project.file(generatedDirPath))
  }
}

val generateBuildProperties by tasks.registering {

  val version = project.extra.properties["VERSION_NAME"] as String
  val group = project.extra.properties["GROUP"] as String

  val buildPropertiesDir = File(generatedDirPath)
  val buildPropertiesFile = File(buildPropertiesDir, "BuildProperties.kt")

  inputs.properties(mapOf("version" to version, "group" to group))
  outputs.file(buildPropertiesFile)

  doLast {

    buildPropertiesDir.mkdirs()

    buildPropertiesFile.writeText(
      """package tangle.inject.gradle
      |
      |internal object BuildProperties {
      |  const val VERSION = "$version"
      |  const val GROUP = "$group"
      |}
      |
    """.trimMargin()
    )
  }
}

tasks.withType<KotlinCompile>()
  .configureEach {

    dependsOn(generateBuildProperties)
  }
