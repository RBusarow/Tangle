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

import android.app.Application
import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.util.CoilUtils
import com.squareup.anvil.annotations.ContributesMultibinding
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import tangle.sample.core.AppPlugin
import tangle.sample.core.AppScope
import tangle.sample.core.di.ApplicationContext
import javax.inject.Inject
import javax.inject.Provider

@ContributesMultibinding(AppScope::class)
class CoilOkHttpAppPlugin @Inject constructor(
  private val imageLoaderProvider: Provider<ImageLoader>
) : AppPlugin {

  override fun apply(application: Application) {
    Coil.setImageLoader { imageLoaderProvider.get() }
  }
}

@Module
@ContributesTo(AppScope::class)
object CoilImageLoaderModule {

  @Provides
  fun provideImageLoader(
    @ApplicationContext
    context: Context,
    okHttpClient: OkHttpClient
  ): ImageLoader {

    val client = okHttpClient
      .newBuilder()
      .cache(CoilUtils.createDefaultCache(context))
      .build()

    return ImageLoader.Builder(context)
      .okHttpClient(client)
      .build()
  }
}

