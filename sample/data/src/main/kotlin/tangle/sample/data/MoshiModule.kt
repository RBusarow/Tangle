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

package tangle.sample.data

import com.squareup.anvil.annotations.ContributesTo
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import retrofit2.Converter
import retrofit2.converter.moshi.MoshiConverterFactory
import tangle.sample.core.AppScope
import javax.inject.Singleton

@Module
@ContributesTo(AppScope::class)
object MoshiModule {
  @Provides
  fun provideMoshi(): Moshi = Moshi.Builder().build()

  @IntoSet
  @Provides
  @Singleton
  fun provideMoshiConverterFactory(moshi: Moshi): Converter.Factory =
    MoshiConverterFactory.create(moshi)
}
