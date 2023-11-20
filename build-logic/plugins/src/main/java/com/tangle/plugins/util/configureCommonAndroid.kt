/*
 * Copyright (C) 2023 Rick Busarow
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

package com.tangle.plugins.util

import com.android.build.api.dsl.ApplicationBaseFlavor
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryBaseFlavor
import com.android.build.gradle.TestedExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("UnstableApiUsage")
fun Project.configureCommonAndroid() {
  val publishedAsArtifact = extensions.findByName("com.vanniktech.maven.publish") != null
  configure<TestedExtension> {
    compileSdkVersion(AndroidVersions.compileSdk)

    defaultConfig {
      minSdk = AndroidVersions.minsSdk

      // `targetSdk` doesn't have a single base interface, as of AGP 7.1.0
      when (this@defaultConfig) {
        is LibraryBaseFlavor -> targetSdk = 33
        is ApplicationBaseFlavor -> targetSdk = 33 // TODO review code gen target
      }

      vectorDrawables {
        useSupportLibrary = true
      }
    }

    buildTypes.configureEach {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
      )
    }

    compileOptions {
      sourceCompatibility = AndroidVersions.javaVersion
      targetCompatibility = AndroidVersions.javaVersion
    }

    sourceSets {
      findByName("androidTest")?.java?.srcDirs("src/androidTest/kotlin")
      findByName("main")?.java?.srcDirs("src/main/kotlin")
      findByName("test")?.java?.srcDirs("src/test/kotlin")
    }

    testOptions {
      unitTests.isIncludeAndroidResources = true
      unitTests.isReturnDefaultValues = true
      animationsDisabled = true
    }
  }

  extensions.configure(CommonExtension::class.java) {
    if (publishedAsArtifact) {
      lint {
        disable.addAll(setOf("ObsoleteLintCustomCheck", "MissingTranslation"))
        enable.addAll(setOf("InvalidPackage", "Interoperability"))
        abortOnError = true
        checkDependencies = true
        checkAllWarnings = true
      }
    }
  }

  tasks.create("lintMain") {
    doFirst {
      tasks.withType<KotlinCompile>()
        .configureEach {
          kotlinOptions {
            allWarningsAsErrors = true

            if (publishedAsArtifact) {
              freeCompilerArgs = freeCompilerArgs + "-Xexplicit-api=strict"
            }
          }
        }
    }
    finalizedBy("lintDebug")
  }

  tasks.create("testJvm") {
    dependsOn("testDebugUnitTest")
  }

  tasks.create("buildTests") {
    dependsOn("assembleDebugUnitTest")
  }
}
