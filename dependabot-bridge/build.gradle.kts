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

plugins{
  id("com.rickbusarow.gradle-dependency-sync") version "0.11.1"
}

dependencies {

  dependencySync("com.android.tools.build:gradle:4.2.1")

  dependencySync("androidx.activity:activity-ktx:1.2.3")
  dependencySync("androidx.activity:activity-compose:1.3.0-rc01")
  dependencySync("androidx.annotation:annotation:1.2.0")
  dependencySync("androidx.appcompat:appcompat:1.3.0")
  dependencySync("androidx.arch.core:core-testing:2.1.0")
  dependencySync("androidx.constraintlayout:constraintlayout:2.0.4")
  dependencySync("androidx.core:core-ktx:1.6.0")

  dependencySync("androidx.fragment:fragment:1.3.5")
  dependencySync("androidx.fragment:fragment-ktx:1.3.5")
  dependencySync("androidx.fragment:fragment-testing:1.3.5")

  dependencySync("androidx.lifecycle:lifecycle-common:2.3.1")
  dependencySync("androidx.lifecycle:lifecycle-livedata-core:2.3.1")
  dependencySync("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
  dependencySync("androidx.lifecycle:lifecycle-runtime:2.3.1")
  dependencySync("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")

  dependencySync("androidx.lifecycle:lifecycle-viewmodel:2.3.1")
  dependencySync("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07")
  dependencySync("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
  dependencySync("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.3.1")

  dependencySync("androidx.navigation:navigation-common:2.4.0-alpha04")
  dependencySync("androidx.navigation:navigation-runtime-ktx:2.3.5")
  dependencySync("androidx.navigation:navigation-compose:2.4.0-alpha04")

  dependencySync("androidx.compose.ui:ui:1.0.0-rc01")
  dependencySync("androidx.compose.ui:ui-tooling:1.0.0-rc01")
  dependencySync("androidx.compose.runtime:runtime:1.0.0-rc01")
  dependencySync("androidx.compose.foundation:foundation:1.0.0-rc01")
  dependencySync("androidx.compose.material:material:1.0.0-rc01")
  dependencySync("androidx.compose.material:material-icons-core:1.0.0-rc01")
  dependencySync("androidx.compose.material:material-icons-extended:1.0.0-rc01")
  dependencySync("androidx.compose.ui:ui-test-junit4:1.0.0-rc01")

  dependencySync("androidx.savedstate:savedstate:1.1.0")
  dependencySync("androidx.test:core:1.4.0")

  dependencySync("androidx.test.espresso:espresso-contrib:3.4.0")
  dependencySync("androidx.test.espresso:espresso-core:3.4.0")
  dependencySync("androidx.test.espresso:espresso-idling-resource:3.4.0")
  dependencySync("androidx.test.espresso:espresso-intents:3.4.0")
  dependencySync("androidx.test.espresso:espresso-web:3.4.0")

  dependencySync("androidx.test.ext:junit:1.1.3")
  dependencySync("androidx.test:orchestrator:1.4.0")
  dependencySync("androidx.test:rules:1.4.0")
  dependencySync("androidx.test:runner:1.4.0")
  dependencySync("androidx.test.ext:truth:1.4.0")

  dependencySync("com.squareup.anvil:annotations:2.3.3")
  dependencySync("com.squareup.anvil:compiler-api:2.3.3")
  dependencySync("com.squareup.anvil:compiler:2.3.3")
  dependencySync("com.squareup.anvil:compiler-utils:2.3.3")
  dependencySync("com.squareup.anvil:gradle-plugin:2.3.3")

  dependencySync("com.google.auto:auto-common:1.1")
  dependencySync("com.google.auto.service:auto-service-annotations:1.0")
  dependencySync("com.google.auto.service:auto-service:1.0")

  dependencySync("com.google.dagger:dagger:2.37")
  dependencySync("com.google.dagger:dagger-compiler:2.37")

  dependencySync("com.google.accompanist:accompanist-coil:0.13.0")

  dependencySync("org.codehaus.groovy:groovy:3.0.8")
  dependencySync("org.codehaus.groovy:groovy-xml:3.0.8")

  dependencySync("com.github.tschuchortdev:kotlin-compile-testing:1.4.2")

  dependencySync("com.rickbusarow.hermit:hermit-core:0.9.5")
  dependencySync("com.rickbusarow.hermit:hermit-coroutines:0.9.5")
  dependencySync("com.rickbusarow.hermit:hermit-junit4:0.9.5")
  dependencySync("com.rickbusarow.hermit:hermit-junit5:0.9.5")
  dependencySync("com.rickbusarow.hermit:hermit-mockk:0.9.5")

  dependencySync("com.github.javaparser:javaparser-symbol-solver-core:3.22.1")

  dependencySync("javax.annotation:jsr250-api:1.0")
  dependencySync("javax.inject:javax.inject:1")

  dependencySync("junit:junit:4.13.2")

  dependencySync("org.junit.jupiter:junit-jupiter-api:5.7.2")
  dependencySync("org.junit.jupiter:junit-jupiter-engine:5.7.2")
  dependencySync("org.junit.jupiter:junit-jupiter:5.7.2")
  dependencySync("org.junit.jupiter:junit-jupiter-params:5.7.2")
  dependencySync("org.junit.vintage:junit-vintage-engine:5.7.2")

  dependencySync("io.kotest:kotest-assertions-core-jvm:4.6.1")
  dependencySync("io.kotest:kotest-assertions-shared-jvm:4.6.1")
  dependencySync("io.kotest:kotest-common-jvm:4.6.1")
  dependencySync("io.kotest:kotest-property-jvm:4.6.1")
  dependencySync("io.kotest:kotest-runner-junit5-jvm:4.6.1")

  dependencySync("org.jetbrains.kotlin:kotlin-annotation-processing-embeddable:1.5.21")
  dependencySync("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.5.10")
  dependencySync("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.5.21")
  dependencySync("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
  dependencySync("org.jetbrains.kotlin:kotlin-reflect:1.5.10")
  dependencySync("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.10")

  dependencySync("com.squareup:kotlinpoet:1.9.0")

  dependencySync("org.jmailen.gradle:kotlinter-gradle:3.4.5")

  dependencySync("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.16.2")
  dependencySync("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.1-native-mt")
  dependencySync("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
  dependencySync("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.1")
  dependencySync("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.5.1")
  dependencySync("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.1")
  dependencySync("org.jetbrains.kotlinx:kotlinx-knit-test:0.3.0")

  dependencySync("io.mockk:mockk:1.12.0")

  dependencySync("org.robolectric:robolectric:4.6.1")

  dependencySync("net.swiftzer.semver:semver:1.1.1")

  dependencySync("com.jakewharton.timber:timber:4.7.1")

}
