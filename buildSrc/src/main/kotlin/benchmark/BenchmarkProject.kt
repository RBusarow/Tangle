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
package benchmark

import java.io.File

interface GenFile {
  val directory: String
  val name: String
  val content: String
  val extension: String

  fun write() {

    val dir = File(directory).also { it.mkdirs() }
    val f = File(dir, "$name.$extension")
    f.writeText(content)
  }
}

data class BuildFile(
  override val directory: String, override val content: String
) : GenFile {
  override val name = "build"
  override val extension: String = "gradle.kts"
}

data class SettingsFile(
  override val directory: String, override val content: String
) : GenFile {
  override val name = "settings"
  override val extension: String = "gradle.kts"
}

data class PropertiesFile(
  override val directory: String, override val content: String
) : GenFile {
  override val name = "gradle"
  override val extension: String = "properties"
}

data class AndroidManifest(
  override val directory: String, override val content: String
) : GenFile {
  override val name = "AndroidManifest"
  override val extension: String = "xml"
}

data class KotlinFile(
  override val directory: String, override val name: String, override val content: String
) : GenFile {
  override val extension = "kt"
}

/**
 * Creates a project with two "applications" - one for Hilt, and one for Tangle.  Both applications
 * have their own set of library modules with corresponding dependencies.  These two applications
 * share a single root project.
 *
 * @param numberOfModules The number of *library* module dependencies for *each* application
 * @param rootDir Where the project's generated
 * @param tangleVersion The current version of Tangle. Necessary so that we can put the version in
 *   the classpath.
 */
fun createBenchmarkProject(numberOfModules: Int, rootDir: File, tangleVersion: String) {

  projectBuildFile(rootDir, tangleVersion).write()

  projectPropertiesFile(rootDir).write()

  val padding = numberOfModules.toString().length

  val moduleNames = List(numberOfModules) {
    "Lib" + it.plus(1).toString().padStart(padding, '0')
  }

  val hiltModuleNames = moduleNames.map { "hilt$it" }
  val tangleModuleNames = moduleNames.map { "tangle$it" }

  val includes = hiltModuleNames
    .plus(tangleModuleNames)
    .joinToString("\n") { "include(\":libs:$it\")" }

  projectSettingsFile(rootDir, includes).write()

  createTangleProject(rootDir, tangleModuleNames)
  createHiltProject(rootDir, hiltModuleNames)
}

private fun projectSettingsFile(rootDir: File, includes: String) = SettingsFile(
  rootDir.path,
  """
        |$includes
        |
        |include(":hilt-app")
        |include(":tangle-app")
        |
        |enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
        |""".trimMargin()
)

private fun projectPropertiesFile(rootDir: File) = PropertiesFile(
  rootDir.path, """
      |org.gradle.jvmargs=-Xmx8192m -XX:MaxPermSize=1024m -XX:+HeapDumpOnOutOfMemoryError -XX:+UseParallelGC
      |org.gradle.daemon=true
      |org.gradle.caching=true
      |org.gradle.parallel=true
      |org.gradle.vfs.watch=true
      |
      |android.useAndroidX=true
      |
      |kotlin.caching.enabled=true
      |kotlin.incremental=true
      """.trimMargin()
)

private fun projectBuildFile(rootDir: File, tangleVersion: String) = BuildFile(
  rootDir.path, """
      |import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
      |
      |buildscript {
      | repositories {
      |   mavenLocal()
      |   mavenCentral()
      |   google()
      |   maven("https://plugins.gradle.org/m2/")
      | }
      |}
      |
      |allprojects {
      |
      | repositories {
      |   mavenLocal()
      |   mavenCentral()
      |   google()
      |   maven("https://plugins.gradle.org/m2/")
      | }
      |  tasks.withType<KotlinCompile>()
      |    .configureEach {
      |
      |      kotlinOptions {
      |        freeCompilerArgs = freeCompilerArgs + listOf(
      |          "-opt-in=kotlin.RequiresOptIn"
      |        )
      |      }
      |    }
      |
      |}""".trimMargin()
)

internal fun manifestFile(
  moduleRoot: String,
  packageName: String
) = AndroidManifest(
  directory = "$moduleRoot/src/main",
  content = """<?xml version="1.0" encoding="utf-8"?>
        <manifest package="$packageName"/>
        """
)
