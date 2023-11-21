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

package com.tangle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import com.android.build.gradle.LibraryExtension
import com.tangle.plugins.util.common
import com.tangle.plugins.util.configureCommonAndroid
import namedPlugin
import namedLib

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

class AndroidLibrary : Plugin<Project> {
  override fun apply(target: Project) = with(target)  {
    apply(plugin = namedPlugin("android-library"))
    apply(plugin = namedPlugin("kotlin-android"))


    configure<LibraryExtension> {
      @Suppress("UnstableApiUsage")
      buildFeatures.buildConfig = false
    }

    configureCommonAndroid()
    common()
  }
}
