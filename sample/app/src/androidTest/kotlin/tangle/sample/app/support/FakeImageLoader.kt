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

package tangle.sample.app.support

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import coil.ComponentRegistry
import coil.ImageLoader
import coil.decode.DataSource.MEMORY_CACHE
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.DefaultRequestOptions
import coil.request.Disposable
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.request.SuccessResult
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CompletableDeferred
import tangle.sample.core.AppScope
import javax.inject.Inject

// https://coil-kt.github.io/coil/image_loaders/#testing
@ContributesBinding(AppScope::class)
class FakeImageLoader @Inject constructor() : ImageLoader {

  override val defaults = DefaultRequestOptions()
  override val components = ComponentRegistry()
  override val memoryCache: MemoryCache? get() = null
  override val diskCache: DiskCache? get() = null

  override fun enqueue(request: ImageRequest): Disposable {
    // Always call onStart before onSuccess.
    request.target?.onStart(request.placeholder)
    val result = ColorDrawable(Color.BLACK)
    request.target?.onSuccess(result)
    return object : Disposable {
      override val job = CompletableDeferred(newResult(request, result))
      override val isDisposed get() = true
      override fun dispose() = Unit
    }
  }

  override suspend fun execute(request: ImageRequest): ImageResult {
    return newResult(request, ColorDrawable(Color.BLACK))
  }

  private fun newResult(request: ImageRequest, drawable: Drawable): SuccessResult {
    return SuccessResult(
      drawable = drawable,
      request = request,
      dataSource = MEMORY_CACHE
    )
  }

  override fun newBuilder() = throw UnsupportedOperationException()

  override fun shutdown() = Unit
}
