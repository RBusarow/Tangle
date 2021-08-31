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

import android.content.Context
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.memory.MemoryCache
import coil.request.*
import com.squareup.anvil.annotations.ContributesBinding
import tangle.sample.app.R
import tangle.sample.core.AppScope
import tangle.sample.core.di.ApplicationContext
import tangle.sample.core.requireNotNull
import javax.inject.Inject

// https://coil-kt.github.io/coil/image_loaders/#testing
@ContributesBinding(AppScope::class)
class FakeImageLoader @Inject constructor(
  @ApplicationContext
  private val context: Context
) : ImageLoader {

  @OptIn(ExperimentalCoilApi::class)
  private val disposable = object : Disposable {
    override val isDisposed get() = true
    override fun dispose() = Unit
    override suspend fun await() = Unit
  }

  private val puppy = ContextCompat.getDrawable(context, R.drawable.puppy).requireNotNull()

  override val defaults = DefaultRequestOptions()

  // Optionally, you can add a custom fake memory cache implementation.
  override val memoryCache get() = throw UnsupportedOperationException()

  override val bitmapPool = BitmapPool(0)

  override fun enqueue(request: ImageRequest): Disposable {
    // Always call onStart before onSuccess.
    request.target?.onStart(placeholder = puppy)
    request.target?.onSuccess(result = puppy)
    return disposable
  }

  override suspend fun execute(request: ImageRequest): ImageResult {
    return SuccessResult(
      drawable = puppy,
      request = request,
      metadata = ImageResult.Metadata(
        memoryCacheKey = MemoryCache.Key(""),
        isSampled = false,
        dataSource = DataSource.MEMORY_CACHE,
        isPlaceholderMemoryCacheKeyPresent = false
      )
    )
  }

  override fun shutdown() = Unit

  override fun newBuilder() = ImageLoader.Builder(context)
}
