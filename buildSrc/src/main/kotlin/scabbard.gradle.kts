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

import org.gradle.api.internal.artifacts.dsl.ModuleVersionSelectorParsers

plugins {
  id("scabbard.gradle")
}

scabbard {
  enabled = true
  fullBindingGraphValidation = true
  outputFormat = "svg"
}
configurations.all {
  resolutionStrategy {

    eachDependency {
      if (requested == ModuleVersionSelectorParsers.parser()
          .parseNotation("com.github.kittinunf.result:result:3.0.0")
      ) {
        useVersion("3.0.1")
        because("Transitive dependency of Scabbard, currently not available on mavenCentral()")
      }
    }
  }
}
