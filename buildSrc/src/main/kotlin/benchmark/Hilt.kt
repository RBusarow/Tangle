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

package benchmark

import java.io.File
import java.util.Locale

fun createHiltProject(rootDir: File, moduleNames: List<String>) {

  moduleNames.forEach { moduleName ->

    val moduleRoot = File(File(rootDir, "libs"), moduleName).path

    hiltLibraryBuildFile(moduleRoot).write()

    val packageName = "hilt.benchmark.$moduleName"

    manifestFile(moduleRoot, packageName).write()

    val packageRoot = "$moduleRoot/src/main/java/hilt/benchmark/$moduleName"

    val classPrefix = moduleName.capitalize(Locale.US)

    hiltViewModelFile(packageRoot, classPrefix, packageName).write()
    hiltFragmentFile(packageRoot, classPrefix, packageName).write()
  }

  val appModuleRoot = File(rootDir, "hilt-app").path
  val appPackageName = "hilt.benchmark.app"

  hiltAppBuildFile(moduleNames, appModuleRoot).write()
  manifestFile(appModuleRoot, appPackageName).write()
  hiltAppFile(appModuleRoot, appPackageName).write()
}

internal fun hiltViewModelFile(
  packageRoot: String,
  classPrefix: String,
  packageName: String
): KotlinFile {

  val viewModelName = "${classPrefix}ViewModel"

  return KotlinFile(
    directory = packageRoot,
    name = viewModelName,
    content = """package $packageName
          |
          |import androidx.lifecycle.ViewModel
          |import dagger.hilt.android.lifecycle.HiltViewModel
          |import javax.inject.Inject
          |
          |@HiltViewModel
          |class $viewModelName @Inject constructor() : ViewModel()
        |""".trimMargin()
  )
}

internal fun hiltFragmentFile(
  packageRoot: String,
  classPrefix: String,
  packageName: String
): KotlinFile {

  val fragmentName = "${classPrefix}Fragment"

  return KotlinFile(
    directory = packageRoot,
    name = fragmentName,
    content = """package $packageName
        |
        |import android.app.Application
        |import androidx.fragment.app.Fragment
        |import dagger.hilt.android.AndroidEntryPoint
        |import javax.inject.Inject
        |
        |@AndroidEntryPoint
        |class $fragmentName : Fragment() {
        |  // just need something to inject,
        |  // so that Hilt doesn't get to skip generating MemberInjectors
        |  @Inject lateinit var app: Application
        |}
        |""".trimMargin()
  )
}

internal fun hiltLibraryBuildFile(
  moduleRoot: String
): BuildFile {

  return BuildFile(
    directory = moduleRoot,
    content = """plugins{
        |  id("tangle.library.android")
        |  kotlin("android")
        |  kotlin("kapt")
        |  id("dagger.hilt.android.plugin")
        |}
        |
        |kapt {
        |  correctErrorTypes = true
        |  useBuildCache = true
        |}
        |
        |android {
        |  compileSdk = 31
        |
        |  defaultConfig {
        |    minSdk = 21
        |    targetSdk = 31
        |  }
        |}
        |
        |dependencies {
        |  kapt(libs.google.hilt.compiler)
        |
        |  api(libs.google.hilt.library)
        |  api(libs.androidx.fragment.ktx)
        |  api(libs.androidx.lifecycle.viewModel.ktx)
        |}
        |""".trimMargin()
  )
}

internal fun hiltAppFile(moduleRoot: String, packageName: String): KotlinFile {
  val packageRoot = "$moduleRoot/src/main/java/hilt/benchmark/app"

  return KotlinFile(
    directory = packageRoot,
    name = "App",
    content = """package $packageName
        |
        |import android.app.Application
        |import dagger.hilt.android.HiltAndroidApp
        |
        |@HiltAndroidApp
        |class App: Application()
        |""".trimMargin()
  )
}

internal fun hiltAppBuildFile(
  moduleNames: List<String>,
  moduleRoot: String
): BuildFile {

  val projectDeps = moduleNames.joinToString("\n  ") { name ->
    "api(project(path = \":libs:$name\"))"
  }

  return BuildFile(
    directory = moduleRoot,
    content = """plugins{
        |  id("tangle.library.android")
        |  kotlin("android")
        |  kotlin("kapt")
        |  id("dagger.hilt.android.plugin")
        |}
        |
        |kapt {
        |  correctErrorTypes = true
        |  useBuildCache = true
        |}
        |
        |android {
        |  compileSdk = 31
        |
        |  defaultConfig {
        |    minSdk = 21
        |    targetSdk = 31
        |  }
        |}
        |
        |dependencies {
        |  kapt(libs.google.hilt.compiler)
        |  kapt(libs.androidx.hilt.compiler)
        |
        |  api(libs.google.hilt.library)
        |
        |  api(libs.androidx.fragment.ktx)
        |  api(libs.androidx.lifecycle.viewModel.ktx)
        |
        |  $projectDeps
        |}
        |""".trimMargin()
  )
}
