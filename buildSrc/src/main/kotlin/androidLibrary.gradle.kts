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

import com.android.build.gradle.TestedExtension
import org.gradle.api.JavaVersion.VERSION_1_8
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(plugin = "com.android.library")
apply(plugin = "org.jetbrains.kotlin.android")

@Suppress("MagicNumber")
configure<TestedExtension> {
  compileSdkVersion(31)

  defaultConfig {
    minSdkVersion(21)
    targetSdkVersion(31)

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  compileOptions {
    sourceCompatibility = VERSION_1_8
    targetCompatibility = VERSION_1_8
  }

  lintOptions {
    disable("ObsoleteLintCustomCheck")
    disable("MissingTranslation")
    enable("InvalidPackage")
    enable("Interoperability")
    isAbortOnError = true
  }

  testOptions {
    unitTests.isIncludeAndroidResources = true
    unitTests.isReturnDefaultValues = true
    animationsDisabled = true
  }
}

val ci = !System.getenv("CI").isNullOrBlank()

tasks.withType<KotlinCompile>()
  .configureEach {

    kotlinOptions {
      allWarningsAsErrors = false

      jvmTarget = "1.8"

      freeCompilerArgs = freeCompilerArgs + listOf(
        "-Xjvm-default=enable",
        "-Xallow-result-return-type",
        "-Xopt-in=kotlin.contracts.ExperimentalContracts",
        "-Xopt-in=kotlin.Experimental",
        "-Xopt-in=kotlin.time.ExperimentalTime",
        "-Xopt-in=kotlin.RequiresOptIn",
        "-Xinline-classes",
        "-Xopt-in=com.squareup.anvil.annotations.ExperimentalAnvilApi"
      )
    }
  }

// https://youtrack.jetbrains.com/issue/KT-37652
tasks
  .matching { it is KotlinCompile && !it.name.contains("test", ignoreCase = true) }
  .configureEach {
    val task = this
    if (!project.hasProperty("kotlin.optOutExplicitApi")) {
      val kotlinCompile = task as KotlinCompile
      if ("-Xexplicit-api=strict" !in kotlinCompile.kotlinOptions.freeCompilerArgs) {
        kotlinCompile.kotlinOptions.freeCompilerArgs += "-Xexplicit-api=strict"
      }
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
      key.startsWith("vminject") && value != null
    }
    .forEach { (key, value) ->
      systemProperty(key, value!!)
    }
}

val lintMain by tasks.registering {

  doFirst {
    tasks.withType<KotlinCompile>()
      .configureEach {
        kotlinOptions {
          allWarningsAsErrors = true
        }
      }
  }

  dependsOn("lintDebug")
}

val testJvm by tasks.registering {
  dependsOn("testDebugUnitTest")
}

val buildTests by tasks.registering {
  dependsOn("assembleDebugUnitTest")
}

extensions.configure<KotlinAndroidProjectExtension> {
  explicitApi = ExplicitApiMode.Strict
}
