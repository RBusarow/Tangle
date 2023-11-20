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
  id("tangle.library.android")
  `maven-publish`
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = "com.rickbusarow.tangle"
      artifactId = "tangle-work-api"
    }
  }
}
android {
  namespace = "tangle.work"
}

dependencies {

  api(libs.androidx.work.core)
  api(libs.google.dagger.api)

  implementation(libs.javax.inject)

  implementation(projects.tangleApi)

  testCompileOnly(libs.google.auto.service.processor)

  testImplementation(libs.hermit.mockk)
  testImplementation(libs.mockk)

  testImplementation(projects.tangleApi)
  testImplementation(projects.tangleCompiler)
  testImplementation(projects.tangleFragmentApi)
  testImplementation(projects.tangleFragmentCompiler)
  testImplementation(projects.tangleTestUtils)
  testImplementation(projects.tangleViewmodelApi)
  testImplementation(projects.tangleViewmodelCompiler)
  testImplementation(projects.tangleWorkCompiler)
}
