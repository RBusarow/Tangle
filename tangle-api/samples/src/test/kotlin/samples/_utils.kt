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

package samples

import android.app.Application
import androidx.lifecycle.ViewModel
import dagger.BindsInstance
import dagger.Component
import org.junit.Test
import tangle.viewmodel.VMInject
import javax.inject.Inject

typealias Sample = Test

abstract class AppScope private constructor()

class MyViewModel @VMInject constructor() : ViewModel()

class MyRepository @Inject constructor()

@Component
interface MyAppComponent {
  @Component.Factory
  interface Factory {

    fun create(@BindsInstance application: Application): MyAppComponent
  }
}
