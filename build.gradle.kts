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

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.detekt
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask

buildscript {
  repositories {
    mavenLocal()
    mavenCentral()
    google()
    maven("https://plugins.gradle.org/m2/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
  }
  dependencies {
    classpath("com.android.tools.build:gradle:7.0.2")
    classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:1.5.21-1.0.0-beta07")
    classpath("com.squareup.anvil:gradle-plugin:2.3.4")
    classpath("com.vanniktech:gradle-maven-publish-plugin:0.17.0")

    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
    classpath("org.jlleitschuh.gradle:ktlint-gradle:10.2.0")
  }
}

plugins {
  kotlin("jvm")
  id("com.github.ben-manes.versions") version "0.39.0"
  id("io.gitlab.arturbosch.detekt") version "1.18.1"
  id("com.rickbusarow.module-check") version "0.10.0"
  id("com.osacky.doctor") version "0.7.1"
  id("com.dorongold.task-tree") version "2.1.0"
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.7.1"
  base
  dokka
  knit
  website
}

allprojects {

  repositories {
    google()
    mavenLocal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
  }
  configurations.all {
    resolutionStrategy {

      eachDependency {
        when {
          requested.group == "org.jetbrains.kotlin" -> useVersion(libs.versions.kotlin.get())
        }
      }
    }
  }
}

@Suppress("DEPRECATION")
detekt {

  parallel = true
  config = files("$rootDir/detekt/detekt-config.yml")

  reports {
    xml.enabled = false
    html.enabled = true
    txt.enabled = false
  }
}

tasks.withType<DetektCreateBaselineTask> {

  setSource(files(rootDir))

  include("**/*.kt", "**/*.kts")
  exclude("**/resources/**", "**/build/**", "**/src/test/java**")

  // Target version of the generated JVM bytecode. It is used for type resolution.
  this.jvmTarget = "1.8"
}

tasks.withType<Detekt> {

  setSource(files(rootDir))

  include("**/*.kt", "**/*.kts")
  exclude("**/resources/**", "**/build/**", "**/src/test/java**", "**/src/test/kotlin**")

  // Target version of the generated JVM bytecode. It is used for type resolution.
  this.jvmTarget = "1.8"
}

fun isNonStable(version: String): Boolean {
  val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
  val regex = "^[0-9,.v-]+(-r)?$".toRegex()
  val isStable = stableKeyword || regex.matches(version)
  return isStable.not()
}

tasks.named(
  "dependencyUpdates",
  com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class.java
).configure {
  rejectVersionIf {
    isNonStable(candidate.version) && !isNonStable(currentVersion)
  }
}

allprojects {
  apply(plugin = "org.jlleitschuh.gradle.ktlint")

  configure<KtlintExtension> {
    debug.set(false)

    disabledRules.set(
      setOf(
        "no-wildcard-imports",
        "max-line-length", // manually formatting still does this, and KTLint will still wrap long chains when possible
        "filename", // same as Detekt's MatchingDeclarationName, but Detekt's version can be suppressed and this can't
        "experimental:argument-list-wrapping" // doesn't work half the time
      )
    )
  }
  tasks.withType<BaseKtLintCheckTask> {
    workerMaxHeapSize.set("512m")
  }
}

apiValidation {
  /**
   * Packages that are excluded from public API dumps even if they
   * contain public API.
   */
  ignoredPackages.add("tangle.inject.api.internal")

  /**
   * Sub-projects that are excluded from API validation
   */
  ignoredProjects.addAll(
    listOf(
      "tangle-test-utils",
      "tangle-compiler",
      "tangle-fragment-compiler",
      "tangle-viewmodel-compiler",
      "tangle-work-compiler",
      "app",
      "core",
      "data",
      "ui"
    )
  )

  /**
   * Set of annotations that exclude API from being public.
   * Typically, it is all kinds of `@InternalApi` annotations that mark
   * effectively private API that cannot be actually private for technical reasons.
   */
  nonPublicMarkers.add("tangle.api.internal.InternalTangleApi")
}

// Delete any empty directories while cleaning.
// This is mostly just because IntelliJ/AS likes to randomly create both `/java` and `/kotlin`
// source directories and that annoys me.
allprojects {
  val proj = this@allprojects

  proj.tasks
    .withType<Delete>()
    .configureEach {
      doLast {

        val subprojectDirs = proj.subprojects
          .map { it.projectDir.path }

        proj.projectDir.walkBottomUp()
          .filter { it.isDirectory }
          .filterNot { dir -> subprojectDirs.any { dir.path.startsWith(it) } }
          .filterNot { it.path.contains(".gradle") }
          .filter { it.listFiles()?.isEmpty() != false }
          .forEach { it.deleteRecursively() }
      }
    }
}
