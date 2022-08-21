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

import com.android.builder.model.v2.models.Versions
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask
import tangle.builds.VERSION_NAME
import java.io.File

require(project == rootProject) {
  "Only apply the 'benchmarks' plugin to the root project."
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
    val tangleVersion = tangle.builds.VERSION_NAME

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
  getTasksByName("publishToMavenLocalNoDokka", true)
    .forEach { dependsOn(it) }

  dependsOn(createBenchmarkProject)

  // installs the Gradle-Profiler tool using HomeBrew (meaning it assumes it's running on a Mac)
  commandLine("brew", "install", "gradle-profiler")

  workingDir("./benchmarks")
  commandLine("sh", "profile.sh")
}
