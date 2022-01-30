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

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
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
    classpath(libs.android.gradle)
    classpath(libs.square.anvil.gradle)
    classpath(libs.google.ksp)
    classpath(libs.vanniktech.maven.publish)
    classpath(libs.kotlin.gradle.plug)
    classpath(libs.ktlint.gradle)
  }
}

@Suppress("UnstableApiUsage")
plugins {
  kotlin("jvm")
  alias(libs.plugins.detekt)
  alias(libs.plugins.gradleDoctor)
  alias(libs.plugins.taskTree)
  alias(libs.plugins.moduleCheck)
  alias(libs.plugins.benManes)
  alias(libs.plugins.kotlinx.binaryCompatibility)
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

detekt {

  parallel = true
  config = files("$rootDir/detekt/detekt-config.yml")
}

tasks.withType<DetektCreateBaselineTask> {

  setSource(files(rootDir))

  include("**/*.kt", "**/*.kts")
  exclude("**/resources/**", "**/build/**", "**/src/test/java**")

  // Target version of the generated JVM bytecode. It is used for type resolution.
  this.jvmTarget = "1.8"
}

tasks.withType<Detekt> {

  reports {
    xml.required.set(true)
    html.required.set(true)
    txt.required.set(false)
  }

  setSource(files(projectDir))

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
    version.set("0.43.2")
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

val createBenchmarkProject by tasks.registering(Copy::class) {

  description = "Generates a full benchmarking project in the root build folder"
  group = "profiling"

  val benchmarkRoot = File(rootDir, "build/benchmark-project")

  doFirst {
    benchmarkRoot.deleteRecursively()
  }

  // This copies the current Gradle distribution to the new project.
  // It also copies the libs.versions.toml file,
  // so that the project automatically gets up-to-date dependencies.
  from(rootDir) {
    include("/gradle/**")
    include("gradlew")
    include("gradlew.bat")
  }
  into(benchmarkRoot)

  doLast {
    val tangleVersion = project.extra.properties["VERSION_NAME"] as String

    benchmark.createBenchmarkProject(
      numberOfModules = 100,
      rootDir = benchmarkRoot,
      tangleVersion = tangleVersion
    )
  }
}

val profile by tasks.registering(Exec::class) {

  description = "Generates a benchmarking project, then runs Gradle-Profiler against it"
  group = "profiling"

  // The generated project uses mavenLocal() to get its Tangle dependencies.
  // This was necessary to ensure that it's always using the current Tangle code.
  // Originally it just used an included build,
  // but that meant building the Tangle project as part of the profiled job.
  getTasksByName("publishToMavenLocal", true)
    .forEach { dependsOn(it) }

  dependsOn(createBenchmarkProject)

  // installs the Gradle-Profiler tool using HomeBrew (meaning it assumes it's running on a Mac)
  commandLine("brew", "install", "gradle-profiler")

  workingDir("./benchmarks")
  commandLine("sh", "profile.sh")
}
