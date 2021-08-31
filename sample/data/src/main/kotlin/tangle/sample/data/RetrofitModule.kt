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
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.create
import tangle.sample.core.AppScope
import tangle.sample.data.breed.DogService

@Module
@ContributesTo(AppScope::class)
object RetrofitModule {

  @Provides
  fun provideRetrofit(
    lazyClient: dagger.Lazy<OkHttpClient>,
    converterFactories: Set<@JvmSuppressWildcards Converter.Factory>
  ): Retrofit {

    return Retrofit.Builder()
      .callFactory { request -> lazyClient.get().newCall(request) }
      .baseUrl("https://api.thedogapi.com/")
      .apply {
        converterFactories.forEach { converterFactory ->
          addConverterFactory(converterFactory)
        }
      }
      .build()
  }

  @Provides
  fun provideBreedService(retrofit: Retrofit): DogService = retrofit.create()
}
