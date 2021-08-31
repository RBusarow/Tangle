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

import android.content.Context
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.NONE
import tangle.sample.core.AppScope
import tangle.sample.core.di.ApplicationContext
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Suppress("MagicNumber")
@Module
@ContributesTo(AppScope::class)
object OkHttpClientModule {

  @Singleton
  @Provides
  fun provideOkHttpClient(
    @DogApiKey
    apiKey: String,
    @ApplicationContext
    context: Context
  ): OkHttpClient {
    return OkHttpClient.Builder()
      .cache(Cache(File(context.cacheDir, "api_cache"), 50L * 1024 * 1024))
      .connectionPool(ConnectionPool(10, 2, TimeUnit.MINUTES))
      .dispatcher(
        Dispatcher().apply { maxRequestsPerHost = 20 }
      )
      .addInterceptor(
        HttpLoggingInterceptor { message ->
          Timber.d(message)
        }.also { it.level = NONE }
      )
      .addInterceptor { chain ->
        val request = chain.request()
          .newBuilder()
          .addHeader("x-api-key", apiKey)
          .build()
        chain.proceed(request)
      }
      .build()
  }
}
