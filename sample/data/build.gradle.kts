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
  androidLibrary
  id("com.squareup.anvil")
  id("com.google.devtools.ksp")
  scabbard
}

anvil {
  generateDaggerFactories.set(true)
}

dependencies {
  anvil(projects.tangleCompiler)
  anvil(projects.tangleFragmentCompiler)
  anvil(projects.tangleViewmodelCompiler)
  anvil(projects.tangleWorkCompiler)

  api(libs.androidx.paging.android)
  api(libs.androidx.room.ktx)
  api(libs.androidx.room.paging)
  api(libs.square.moshi.adapters)
  api(libs.square.moshi.core)
  api(libs.square.okhttp.core)
  api(libs.square.okhttp.loggingInterceptor)
  api(libs.square.retrofit.core)
  api(libs.timber)

  api(projects.sample.core)
  api(projects.tangleApi)

  implementation(libs.androidx.activity.ktx)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.lifecycle.viewModel.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.work.ktx)
  implementation(libs.coil.core)
  implementation(libs.google.dagger.api)
  implementation(libs.square.retrofit.moshi)

  implementation(projects.tangleApi)
  implementation(projects.tangleViewmodelActivity)
  implementation(projects.tangleViewmodelApi)
  implementation(projects.tangleViewmodelCompose)
  implementation(projects.tangleViewmodelFragment)
  implementation(projects.tangleWorkApi)

  ksp(libs.androidx.room.compiler)
  ksp(libs.square.moshi.kotlinCodegen)

  testImplementation(projects.tangleTestUtils)
}
