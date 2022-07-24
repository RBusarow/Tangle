/*
 * Copyright (C) 2022 Rick Busarow
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
import coil.disk.DiskCache.Builder
import coil.memory.MemoryCache
import com.squareup.anvil.annotations.ContributesMultibinding
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
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

  private const val MEM_CACHE_MAX_SIZE = 0.25
  private const val DISK_CACHE_MAX_SIZE = 0.02

  @Provides
  fun provideImageLoader(
    @ApplicationContext
    context: Context
  ): ImageLoader {

    return ImageLoader.Builder(context)
      .memoryCache {
        MemoryCache.Builder(context)
          .maxSizePercent(MEM_CACHE_MAX_SIZE)
          .build()
      }
      .diskCache {
        Builder()
          .directory(context.cacheDir.resolve("image_cache"))
          .maxSizePercent(DISK_CACHE_MAX_SIZE)
          .build()
      }
      .build()
  }
}
