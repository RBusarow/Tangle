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

fun createTangleProject(rootDir: File, moduleNames: List<String>) {

  moduleNames.forEach { moduleName ->

    val moduleRoot = File(File(rootDir, "libs"), moduleName).path

    tangleLibraryBuildFile(moduleRoot).write()

    val packageName = "tangle.benchmark.$moduleName"

    manifestFile(moduleRoot, packageName).write()

    val packageRoot = "$moduleRoot/src/main/java/tangle/benchmark/$moduleName"

    val classPrefix = moduleName.capitalize(Locale.US)

    tangleViewModelFile(packageRoot, classPrefix, packageName).write()
    tangleFragmentFile(packageRoot, classPrefix, packageName).write()
  }

  val appModuleRoot = File(rootDir, "tangle-app").path
  val appPackageName = "tangle.benchmark.app"

  tangleAppBuildFile(moduleNames, appModuleRoot).write()
  manifestFile(appModuleRoot, appPackageName).write()
  tangleAppComponentFile(appModuleRoot, appPackageName).write()
}

internal fun tangleViewModelFile(
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
          |import tangle.viewmodel.VMInject
          |
          |class $viewModelName @VMInject constructor() : ViewModel()
        |""".trimMargin()
  )
}

internal fun tangleFragmentFile(
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
        |import androidx.fragment.app.Fragment
        |import tangle.fragment.ContributesFragment
        |import tangle.fragment.FragmentInject
        |import tangle.fragment.FragmentInjectFactory
        |import tangle.inject.TangleParam
        |import javax.inject.Inject
        |
        |@ContributesFragment(Unit::class)
        |class $fragmentName @Inject constructor() : Fragment()
        |""".trimMargin()
  )
}

internal fun tangleLibraryBuildFile(
  moduleRoot: String
): BuildFile {

  return BuildFile(
    directory = moduleRoot,
    content = """plugins{
        |  id("com.android.library")
        |  kotlin("android")
        |  id("com.rickbusarow.tangle")
        |}
        |
        |anvil {
        |  generateDaggerFactories.set(true)
        |}
        |
        |tangle {
        |  fragmentsEnabled = true
        |  workEnabled = true
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
        |  api(libs.androidx.fragment.ktx)
        |  api(libs.androidx.lifecycle.viewModel.ktx)
        |}
        |""".trimMargin()
  )
}


internal fun tangleAppComponentFile(moduleRoot: String, packageName: String): KotlinFile {
  val packageRoot = "$moduleRoot/src/main/java/tangle/benchmark/app"

  return KotlinFile(
    directory = packageRoot,
    name = "TangleAppComponent",
    content = """package $packageName
        |
        |import com.squareup.anvil.annotations.MergeComponent
        |import javax.inject.Singleton
        |
        |@Singleton
        |@MergeComponent(Unit::class)
        |interface TangleAppComponent
        |""".trimMargin()
  )
}

internal fun tangleAppBuildFile(
  moduleNames: List<String>,
  moduleRoot: String
): BuildFile {

  val projectDeps = moduleNames.joinToString("\n  ") { name ->
    "api(project(path = \":libs:$name\"))"
  }

  return BuildFile(
    directory = moduleRoot,
    content = """plugins{
        |  id("com.android.library")
        |  kotlin("android")
        |  kotlin("kapt")
        |  id("com.rickbusarow.tangle")
        |}
        |
        |tangle {
        |  fragmentsEnabled = true
        |  workEnabled = true
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
        |  kapt(libs.google.dagger.compiler)
        |
        |  api(libs.androidx.fragment.ktx)
        |  api(libs.androidx.lifecycle.viewModel.ktx)
        |
        |  $projectDeps
        |}
        |""".trimMargin()
  )
}
