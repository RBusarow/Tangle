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

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

fun Project.common() {

  tasks.withType<KotlinCompile>()
    .configureEach {

      compilerOptions {
        allWarningsAsErrors.set(false)

        jvmTarget.set(JVM_1_8)

        freeCompilerArgs.set(
          freeCompilerArgs.get() + listOf(
            "-Xjvm-default=all",
            "-Xallow-result-return-type",
            "-opt-in=kotlin.contracts.ExperimentalContracts",
            "-opt-in=kotlin.Experimental",
            "-opt-in=kotlin.time.ExperimentalTime",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi",
            "-Xinline-classes"
          )
        )
      }
    }
  tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
      events = setOf(PASSED, FAILED)
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

    // Allow unit tests to run in parallel
    maxParallelForks = Runtime.getRuntime().availableProcessors()
  }

  tasks.register("moveJavaSrcToKotlin") {
    doLast {
      val reg = """.*/src/([^/]*)/java.*""".toRegex()

      projectDir.walkTopDown()
        .filter { it.path.matches(reg) }
        .forEach { file ->

          val oldPath = file.path
          val newPath = oldPath.replace("/java", "/kotlin")


          if (file.isFile) {
            val text = file.readText()

            File(newPath).also {
              it.createNewFile()
              it.writeText(text)
            }
          } else {

            File(newPath).mkdirs()
          }
        }

      projectDir.walkBottomUp()
        .filter { it.path.matches(reg) }
        .forEach { file ->

          file.deleteRecursively()
        }
    }
  }

  tasks.register("format") {
    group = "formatting"
    description = "ktlintFormat, moduleCheckSortDependenciesAuto, dependencySync"

    dependsOn("ktlintFormat")
    dependsOn(rootProject.tasks.getByPath("moduleCheckSortDependencies"))
    dependsOn("moveJavaSrcToKotlin")

    val dependencySyncTasks = rootProject
      .allprojects
      .mapNotNull { it.tasks.findByPath("dependencySync") }
    dependsOn(dependencySyncTasks)
  }
}

fun Project.experimentalAnvil() {

  tasks.withType<KotlinCompile>()
    .configureEach {

      kotlinOptions {

        freeCompilerArgs = freeCompilerArgs + listOf(
          "-opt-in=com.squareup.anvil.annotations.ExperimentalAnvilApi"
        )
      }
    }

}
