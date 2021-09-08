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

package tangle.inject.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.jvm.jvmSuppressWildcards

internal val ClassNames.tangleInjector: ClassName
  get() = ClassName("tangle.inject.internal", "TangleInjector")

internal val ClassNames.tangleInjectorComponent: ClassName
  get() = ClassName("tangle.inject.internal", "TangleInjectorComponent")

internal val ClassNames.tangleScopeMapProviderComponent: ClassName
  get() = ClassName("tangle.inject.internal", "TangleScopeMapProviderComponent")

internal val ClassNames.tangleScopeMap: ClassName
  get() = ClassName("tangle.inject.internal", "TangleScopeMap")

internal val ClassNames.tangleScopeToComponentMap: ClassName
  get() = ClassName("tangle.inject.internal", "TangleScopeToComponentMap")

internal val ClassNames.tangleScopeMapProvider: ClassName
  get() = ClassName("tangle.inject.internal", "TangleScopeMapProvider")

internal val ClassNames.tangleInjectorMap
  get() = Map::class.asClassName()
    .parameterizedBy(
      javaClassWildcard,
      tangleInjector.parameterizedBy(TypeVariableName("*"))
        .jvmSuppressWildcards()
    )

internal val ClassNames.memberInjector: ClassName
  get() = ClassName("dagger", "MembersInjector")
