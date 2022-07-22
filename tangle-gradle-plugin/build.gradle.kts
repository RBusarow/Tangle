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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import tangle.builds.GROUP
import tangle.builds.VERSION_NAME

plugins {
  javaLibrary
  published
  id("com.gradle.plugin-publish") version "0.21.0"
  id("java-gradle-plugin")
  idea
}

tanglePublishing {
  artifactId.set("tangle-gradle-plugin")
}

val main by sourceSets.getting
val test by sourceSets.getting

val integrationTest by java.sourceSets.registering {
  kotlin.apply {
    compileClasspath += main.output
      .plus(test.output)
      .plus(configurations.testRuntimeClasspath.get())
    runtimeClasspath += output + compileClasspath
  }
}

// mark the integrationTest directory as a test directory in the IDE
idea {
  module {
    integrationTest.configure {
      allSource.srcDirs
        .forEach { srcDir ->
          module.testSourceDirs.add(srcDir)
        }
    }
  }
}

val integrationTestCompile by configurations.registering {
  extendsFrom(configurations["testCompileOnly"])
}
val integrationTestRuntime by configurations.registering {
  extendsFrom(configurations["testRuntimeOnly"])
}

dependencies {

  compileOnly(libs.android.gradle)
  compileOnly(libs.kotlin.reflect)

  implementation(libs.kotlin.annotation.processing)
  implementation(libs.kotlin.compiler)
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlin.stdlib.jdk8)

  testImplementation(libs.bundles.hermit)
  testImplementation(libs.bundles.jUnit)
  testImplementation(libs.bundles.kotest)

  "integrationTestImplementation"(libs.bundles.hermit)
  "integrationTestImplementation"(libs.bundles.jUnit)
  "integrationTestImplementation"(libs.bundles.kotest)
}

gradlePlugin {
  plugins {
    create("tangle") {
      id = "com.rickbusarow.tangle"
      group = "com.rickbusarow.tangle"
      displayName = "Tangle"
      implementationClass = "tangle.inject.gradle.TanglePlugin"
      version = VERSION_NAME
      description = "Create Android component bindings for Dagger with Anvil"
    }
  }
}

pluginBundle {
  website = "https://github.com/RBusarow/Tangle"
  vcsUrl = "https://github.com/RBusarow/Tangle"
  description = "Create Android component bindings for Dagger with Anvil"

  (plugins) {
    "tangle" {
      displayName = "Tangle"
      tags = listOf("android", "dagger2", "kotlin", "kotlin-compiler-plugin")
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

val generatedDirPath = "$buildDir/generated/sources/build-properties/kotlin/main"
sourceSets {
  main.configure {
    java.srcDir(project.file(generatedDirPath))
  }
}

val generateBuildProperties by tasks.registering {

  val version = VERSION_NAME
  val group = GROUP

  val buildPropertiesDir = File(generatedDirPath)
  val buildPropertiesFile = File(
    buildPropertiesDir,
    "tangle/inject/gradle/BuildProperties.kt"
  )

  inputs.file(rootProject.file("buildSrc/src/main/kotlin/tangle/builds/Versions.kt"))
  inputs.properties(mapOf("version" to version, "group" to group))
  outputs.file(buildPropertiesFile)

  doLast {

    buildPropertiesDir.deleteRecursively()
    buildPropertiesFile.parentFile.mkdirs()

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

val generatedTestDirPath = "$buildDir/generated/sources/build-properties/kotlin/test"
sourceSets {
  test.configure {
    java.srcDir(project.file(generatedTestDirPath))
  }
  integrationTest.configure {
    java.srcDir(project.file(generatedTestDirPath))
  }
}
val generateTestVersions by tasks.registering {

  val testVersionsDir = File(generatedTestDirPath)
  val testVersionsFile = File(testVersionsDir, "tangle/inject/gradle/TestVersions.kt")

  inputs.file(rootProject.file("buildSrc/src/main/kotlin/tangle/builds/Versions.kt"))
  inputs.file(rootProject.file("gradle/libs.versions.toml"))
  outputs.file(testVersionsFile)

  doLast {

    testVersionsDir.deleteRecursively()
    testVersionsFile.parentFile.mkdirs()

    testVersionsFile.writeText(
      """package tangle.inject.gradle
      |
      |object TestVersions {
      |  const val AGP = "${libs.versions.androidTools.get()}"
      |  const val ANVIL = "${libs.versions.square.anvil.get()}"
      |  const val GRADLE = "${gradle.gradleVersion}"
      |  const val KOTLIN = "${libs.versions.kotlin.get()}"
      |
      |  const val ACTIVITY = "${libs.versions.androidx.activity.get()}"
      |  const val FRAGMENT = "${libs.versions.androidx.fragment.version.get()}"
      |  const val LIFECYCLE = "${libs.versions.androidx.lifecycle.get()}"
      |  const val COMPOSE = "${libs.versions.androidx.compose.runtime.get()}"
      |  const val WORK = "${libs.versions.androidx.work.version.get()}"
      |}
      |
      """.trimMargin()
    )
  }
}

tasks.withType<KotlinCompile>().configureEach {
  dependsOn(generateBuildProperties)
}

tasks.matching {
  it.name in setOf(
    "javaSourcesJar",
    "runKtlintCheckOverMainSourceSet",
    "runKtlintFormatOverMainSourceSet"
  )
}
  .configureEach {
    dependsOn(generateBuildProperties)
  }

tasks.matching {
  it.name in setOf(
    "compileIntegrationTestKotlin",
    "compileTestKotlin",
    "runKtlintCheckOverIntegrationTestSourceSet",
    "runKtlintCheckOverTestSourceSet",
    "runKtlintFormatOverIntegrationTestSourceSet",
    "runKtlintFormatOverTestSourceSet"
  )
}
  .configureEach {
    dependsOn(generateTestVersions)
  }

val integrationTestTask = tasks.register("integrationTest", Test::class) {
  val integrationTestSourceSet = java.sourceSets["integrationTest"]
  testClassesDirs = integrationTestSourceSet.output.classesDirs
  classpath = integrationTestSourceSet.runtimeClasspath
  dependsOn(rootProject.tasks.matching { it.name == "publishToMavenLocalNoDokka" })
}

tasks.matching { it.name == "check" }.all { dependsOn(integrationTestTask) }

kotlin {
  val compilations = target.compilations

  compilations.getByName("integrationTest") {
    associateWith(compilations.getByName("main"))
  }
}
