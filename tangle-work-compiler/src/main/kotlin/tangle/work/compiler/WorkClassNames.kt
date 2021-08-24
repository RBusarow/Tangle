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

package tangle.work.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import tangle.inject.compiler.ClassNames

val ClassNames.androidxWorkerParameters
  get() = ClassName("androidx.work", "WorkerParameters")

val ClassNames.tangleAssistedWorkerFactoryMap
  get() = ClassName("tangle.work", "TangleAssistedWorkerFactoryMap")

val ClassNames.tangleWorkerFactory
  get() = ClassName("tangle.work", "TangleWorkerFactory")

val ClassNames.assistedWorkerFactory
  get() = ClassName("tangle.work", "AssistedWorkerFactory")
val ClassNames.androidxListenableWorker
  get() = ClassName("androidx.work", "ListenableWorker")
val ClassNames.tangleWorkerComponent
  get() = ClassName("tangle.work", "TangleWorkerComponent")
val ClassNames.tangleWorkerFactoryMapSubcomponent
  get() = ClassName("tangle.work", "TangleWorkerFactoryMapSubcomponent")
val ClassNames.tangleWorkerFactoryMapSubcomponentFactory
  get() = tangleWorkerFactoryMapSubcomponent.nestedClass("Factory")

val ClassNames.assistedWorkerFactoryMap: ParameterizedTypeName
  get() = Map::class.asClassName()
    .parameterizedBy(
      string,
      assistedWorkerFactory
        .parameterizedBy(TypeVariableName("out·${androidxListenableWorker.canonicalName}"))
    )
