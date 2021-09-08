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

@file:Suppress("EXPERIMENTAL_API_USAGE")

package tangle.inject.test.utils

import dagger.BindsInstance
import dagger.Component

@Component
interface MyAppComponent {
  @Component.Factory
  fun interface Factory {

    fun create(
      @BindsInstance
      application: Application
    ): MyAppComponent
  }
}

open class Application {
  open fun onCreate() = Unit
}

object DaggerAppComponent : MyAppComponent {

  fun factory() = MyAppComponent.Factory {
    DaggerAppComponent
  }
}

interface MyApplicationComponent {
  fun inject(application: Application)
}

object Timber {
  @Suppress("UnusedPrivateMember", "UNUSED_PARAMETER")
  fun plant(any: Any) = Unit
}

object MyLogger
