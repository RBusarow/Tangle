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

package tangle.work.samples

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import tangle.inject.test.utils.MyRepository
import tangle.inject.test.utils.Sample
import tangle.work.TangleWorker

class TangleWorkerSample {
  @Sample
  fun tangleWorkerSample() {

    @TangleWorker
    class MyWorker @AssistedInject constructor(
      @Assisted context: Context,
      @Assisted params: WorkerParameters,
      val repository: MyRepository
    ) : ListenableWorker(context, params) {
      override fun startWork(): ListenableFuture<Result> {
        TODO()
      }
    }
  }
}
