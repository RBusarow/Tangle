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

plugins {
  androidApplication
  id("com.squareup.anvil")
  kotlin("kapt")
  scabbard
}

android {
  /*
  This sample app requires an api key.  You can get a free one here: https://thedogapi.com/signup

  After getting your own key, it needs to be set as a Gradle property,
  such as in any gradle.properties file. Not that you'll probably ever use it again,
  but the best place to put it would be in your global Gradle home.

  Add this line somewhere, such as ~/.gradle/gradle.properties:
    tangle.dog.api.key=<your own key>
  */
  val dogApiKey = project.extra.properties["tangle.dog.api.key"] as? String ?: ""

  defaultConfig {

    buildConfigField("String", "DOG_API_KEY", "\"$dogApiKey\"")

    testInstrumentationRunner = "tangle.sample.app.support.TangleTestRunner"
  }

  buildFeatures {
    viewBinding = true
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
  }
  packagingOptions {
    resources.excludes.add("META-INF/*.kotlin_module")
    resources.excludes.add("META-INF/AL2.0")
    resources.excludes.add("META-INF/DEPENDENCIES")
    resources.excludes.add("META-INF/DEPENDENCIES.txt")
    resources.excludes.add("META-INF/dependencies.txt")
    resources.excludes.add("META-INF/LGPL2.1")
    resources.excludes.add("META-INF/LICENSE")
    resources.excludes.add("META-INF/LICENSE-notice.md")
    resources.excludes.add("META-INF/LICENSE.md")
    resources.excludes.add("META-INF/LICENSE.txt")
    resources.excludes.add("META-INF/license.txt")
    resources.excludes.add("META-INF/licenses/ASM")
    resources.excludes.add("META-INF/NOTICE")
    resources.excludes.add("META-INF/NOTICE.txt")
    resources.excludes.add("META-INF/notice.txt")
    resources.excludes.add("META-INF/versions/**/*.class")
    resources.excludes.add("win32-x86-64/attach_hotspot_windows.dll")
    resources.excludes.add("win32-x86/attach_hotspot_windows.dll")
  }
}

dependencies {

  kapt(libs.google.dagger.compiler)

  kaptAndroidTest(libs.google.dagger.compiler)

  androidTestImplementation(libs.androidx.arch.test.core)
  androidTestImplementation(libs.androidx.compose.test.junit)
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.espresso.core)
  androidTestImplementation(libs.androidx.test.espresso.idlingResource)
  androidTestImplementation(libs.androidx.test.jUnit)
  androidTestImplementation(libs.androidx.test.rules)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.rickBusarow.dispatch.espresso)

  anvil(projects.tangleCompiler)
  anvil(projects.tangleFragmentCompiler)
  anvil(projects.tangleViewmodelCompiler)
  anvil(projects.tangleWorkCompiler)

  anvilAndroidTest(projects.tangleCompiler)
  anvilAndroidTest(projects.tangleFragmentCompiler)
  anvilAndroidTest(projects.tangleViewmodelCompiler)
  anvilAndroidTest(projects.tangleWorkCompiler)

  api(projects.sample.core)
  api(projects.sample.data)
  api(projects.tangleApi)

  debugImplementation(libs.androidx.compose.test.manifest)

  implementation(libs.androidx.activity.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.compose.material.core)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.ui.core)
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.constraintLayout)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.lifecycle.viewModel.compose)
  implementation(libs.androidx.lifecycle.viewModel.ktx)
  implementation(libs.androidx.navigation.fragment.ktx)
  implementation(libs.androidx.navigation.ui.ktx)
  implementation(libs.androidx.paging.compose)
  implementation(libs.androidx.startup.runtime)
  implementation(libs.androidx.work.ktx)
  implementation(libs.coil.compose)
  implementation(libs.google.dagger.api)
  implementation(libs.google.material.android)

  implementation(projects.sample.ui)
  implementation(projects.tangleFragmentApi)
  implementation(projects.tangleViewmodelActivity)
  implementation(projects.tangleViewmodelApi)
  implementation(projects.tangleViewmodelCompose)
  implementation(projects.tangleViewmodelFragment)
  implementation(projects.tangleWorkApi)

  testImplementation(projects.tangleTestUtils)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()
  .configureEach {

    kotlinOptions {

      freeCompilerArgs = freeCompilerArgs + listOf(
        "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
      )
    }
  }

// adapted from Tivi
// https://github.com/chrisbanes/tivi/blob/main/app/build.gradle#L213-L223
androidComponents.onVariants { variant ->
  val caps = variant.name.capitalize()
  tasks.register("open$caps", Exec::class.java) {
    dependsOn("install$caps")

    val args =
      "adb shell monkey -p ${variant.applicationId.get()} -c android.intent.category.LAUNCHER 1"
        .split(" ")

    commandLine(args)
  }
}
