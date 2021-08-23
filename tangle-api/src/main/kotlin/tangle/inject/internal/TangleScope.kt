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

package tangle.inject.internal

/**
 * Used for precise scoping of Tangle dependencies,
 * such as a `ViewModel` scoped to a single `Fragment`.
 */
public abstract class TangleScope private constructor()

/**
 * Used for singleton scoping of Tangle dependencies,
 * running parallel to the App-scoped component.
 * This scope eliminates some need for additional scoping annotations.
 */
public abstract class TangleAppScope private constructor()
