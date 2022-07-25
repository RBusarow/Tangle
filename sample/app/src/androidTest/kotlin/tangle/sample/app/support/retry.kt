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

import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.NoMatchingViewException.Builder
import androidx.test.espresso.PerformException
import junit.framework.AssertionFailedError
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

suspend inline fun retry(
  clue: String = "",
  timeout_ms: Long = DEFAULT_RETRY_TIMEOUT,
  crossinline predicate: () -> Any
) {
  var exception: Throwable? = null

  withTimeoutOrNull(timeout_ms) {
    while (true) {
      try {
        predicate()
        return@withTimeoutOrNull
      } catch (e: NoMatchingViewException) {
        exception = Builder()
          .from(e)
          .withCause(AssertionError(clue))
          .build()
        delay(RETRY_POLLING_INTERVAL)
      } catch (e: PerformException) {
        exception = PerformException.Builder()
          .from(e)
          .withCause(AssertionError(clue))
          .build()
        delay(RETRY_POLLING_INTERVAL)
      } catch (e: AssertionFailedError) {
        exception = AssertionFailedError(clue + e.message)
        delay(RETRY_POLLING_INTERVAL)
      } catch (e: AssertionError) {
        exception = AssertionError(clue + e.message)
        delay(RETRY_POLLING_INTERVAL)
      }
    }
  } ?: exception?.let {
    throw it
  }
}

@PublishedApi
internal const val DEFAULT_RETRY_TIMEOUT = 10000L

@PublishedApi
internal const val RETRY_POLLING_INTERVAL = 500L
