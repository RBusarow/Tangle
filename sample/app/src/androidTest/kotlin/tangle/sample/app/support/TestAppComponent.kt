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

package tangle.sample.app.support

import android.app.Application
import com.squareup.anvil.annotations.MergeComponent
import dagger.BindsInstance
import dagger.Component
import tangle.sample.core.AppScope
import tangle.sample.data.CoilImageLoaderModule
import tangle.sample.data.RetrofitModule
import javax.inject.Singleton

@Singleton
@MergeComponent(AppScope::class, exclude = [CoilImageLoaderModule::class, RetrofitModule::class])
interface TestAppComponent {

  @Component.Factory
  interface Factory {
    fun create(
      @BindsInstance
      application: Application
    ): TestAppComponent
  }
}
