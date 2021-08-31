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

package tangle.sample.data.breed

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.squareup.anvil.annotations.ContributesMultibinding
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import tangle.sample.core.AppScope
import tangle.sample.core.WorkerScheduler
import tangle.work.TangleWorker
import java.io.IOException
import javax.inject.Inject

/**
 * Worker injection is extremely similar to Hilt's.
 * 1. Use @AssistedInject constructor
 * 2. Use @Assisted for [Context] and [WorkerParameters]
 * 3. Annotate the class with [@TangleWorker][TangleWorker]
 *
 * This API has a page size limit. This worker just eagerly downloads all the data and caches it in
 * Room after app launch.
 *
 * In a real application, this functionality might be handled by a RemoteMediator from the Paging
 * library -- but this is a sample app and I needed something to do in a Worker. This was the best
 * candidate.
 */
@TangleWorker
class BreedDownloaderWorker @AssistedInject constructor(
  /**
   * Context doesn't technically need to be @Assisted, assuming that the Dagger graph has a Context
   * binding for an **application** context.  This is common, however.
   */
  @Assisted
  context: Context,
  @Assisted
  params: WorkerParameters,
  private val service: DogService,
  private val dao: BreedDao
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    var pageIndex = 1

    while (pageIndex > 0) {
      try {
        val breeds = service.getAllBreedsPaged(
          page = pageIndex,
          limit = NETWORK_PAGE_SIZE
        )

        dao.insertAll(breeds.map { it.toBreedEntity() })

        if (breeds.size == NETWORK_PAGE_SIZE) {
          pageIndex++
        } else {
          pageIndex = 0
        }
      } catch (exception: IOException) {
        Result.failure()
      } catch (exception: HttpException) {
        Result.failure()
      }
    }

    return Result.success()
  }

  companion object {
    const val NETWORK_PAGE_SIZE = 20
  }
}

/**
 * Automatically enqueues [BreedDownloaderWorker]'s execution upon app launch.
 */
@ContributesMultibinding(AppScope::class)
class BreedDownloaderScheduler @Inject constructor() : WorkerScheduler {

  override fun isApplicable(): Boolean = true

  override fun enqueue(workManager: WorkManager) {

    workManager.enqueueUniqueWork(
      "breed-downloader",
      REPLACE,
      OneTimeWorkRequestBuilder<BreedDownloaderWorker>().build()
    )
  }
}
