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
    classpath("com.android.tools.build:gradle:7.0.1")
    classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:1.5.21-1.0.0-beta07")
    classpath("com.squareup.anvil:gradle-plugin:2.3.3")
    classpath("com.vanniktech:gradle-maven-publish-plugin:0.17.0")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
    classpath("org.jetbrains.kotlinx:kotlinx-knit:0.3.0")
    classpath("org.jlleitschuh.gradle:ktlint-gradle:10.1.0")
  }
}

plugins {
  kotlin("jvm")
  id("com.github.ben-manes.versions") version "0.39.0"
  id("io.gitlab.arturbosch.detekt") version "1.18.0"
  id("com.rickbusarow.module-check") version "0.10.0"
  id("org.jetbrains.dokka") version "1.5.0"
  id("com.osacky.doctor") version "0.7.1"
  id("com.dorongold.task-tree") version "2.1.0"
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.7.0"
  base
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
      "tangle-work-compiler"
    )
  )

  /**
   * Set of annotations that exclude API from being public.
   * Typically, it is all kinds of `@InternalApi` annotations that mark
   * effectively private API that cannot be actually private for technical reasons.
   */
  nonPublicMarkers.add("tangle.api.internal.InternalTangleApi")
}

subprojects {

  val includeSubproject = when {
    path == ":tangle-test-utils" -> false
    path.endsWith("samples") -> false
    path.endsWith("tests") -> false
    path.endsWith("compiler") -> false
    else -> File("$projectDir/src").exists()
  }

  if (includeSubproject) {
    apply(plugin = "org.jetbrains.dokka")

    val proj = this

    proj.tasks.withType<org.jetbrains.dokka.gradle.AbstractDokkaLeafTask>().configureEach {

      dependsOn(allprojects.mapNotNull { it.tasks.findByName("compileKotlin") })

      dokkaSourceSets {

        getByName("main") {

          if (File("${proj.projectDir}/samples").exists()) {
            samples.setFrom("${proj.projectDir}/samples")
          }

          if (File("${proj.projectDir}/README.md").exists()) {
            includes.from(files("${proj.projectDir}/README.md"))
          }

          sourceLink {
            localDirectory.set(file("src/main"))

            val modulePath = proj.path.replace(":", "/").replaceFirst("/", "")

            // URL showing where the source code can be accessed through the web browser
            remoteUrl.set(uri("https://github.com/RBusarow/Tangle/blob/main/$modulePath/src/main").toURL())
            // Suffix which is used to append the line number to the URL. Use #L for GitHub
            remoteLineSuffix.set("#L")
          }
        }
      }
    }
  }
}

apply(plugin = "kotlinx-knit")

extensions.configure<kotlinx.knit.KnitPluginExtension> {

  rootDir = rootProject.rootDir

  files = fileTree(project.rootDir) {
    include(
      "**/*.md",
      "**/*.kt",
      "**/*.kts"
    )
    exclude(
      "**/node_modules/**",
      "**/build/**",
      "**/.gradle/**"
    )
  }

  moduleRoots = listOf(".")

  moduleDocs = "build/dokka"
  moduleMarkers = listOf("build.gradle", "build.gradle.kts")
  siteRoot = "https://rbusarow.github.io/Tangle/api/"
}

// Build API docs for all modules with dokka before running Knit
tasks.withType<kotlinx.knit.KnitTask>().configureEach {
  dependsOn(tasks.findByName("dokkaHtmlMultiModule"))
}

val startSite by tasks.registering(Exec::class) {
  workingDir("./website")
  commandLine("npm", "run", "start")
}

val versionDocs by tasks.registering(Exec::class) {
  workingDir("./website")
  val version = project.extra.properties["VERSION_NAME"] as String
  commandLine("npm", "run", "docusaurus", "docs:version", version)
}

val updateWebsiteApiDocs by tasks.registering(Copy::class) {

  doFirst {
    delete(
      fileTree("./website/static/api") {
        exclude("**/styles/*")
      }
    )
  }

  dependsOn(tasks.findByName("knit"))

  from(
    fileTree("$buildDir/dokka/htmlMultiModule") {
      exclude("**/styles/*")
    }
  )

  into("./website/static/api")
}
