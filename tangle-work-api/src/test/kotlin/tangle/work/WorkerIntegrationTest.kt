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

package tangle.work

import android.content.Context
import androidx.work.WorkerParameters
import hermit.test.mockk.resetsMockk
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tangle.inject.InternalTangleApi
import tangle.inject.TangleGraph
import tangle.inject.test.utils.BaseTest
import tangle.inject.test.utils.createFunction
import tangle.inject.test.utils.daggerAppComponent

interface WorkerComponent {
  val tangleWorkerFactory: TangleWorkerFactory
}

@OptIn(InternalTangleApi::class)
class WorkerIntegrationTest : BaseTest() {

  val context by resetsMockk<Context>()
  val workerParameters by resetsMockk<WorkerParameters>()

  @Test
  fun `worker is multi-bound into TangleAppScope`() = compileWithDagger(
    """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import android.content.Context
      import androidx.work.Worker
      import androidx.work.WorkerParameters
      import dagger.assisted.Assisted
      import dagger.assisted.AssistedInject
      import tangle.inject.TangleGraph
      import tangle.work.TangleWorker
      import tangle.work.WorkerComponent
      import javax.inject.Singleton

      @TangleWorker
      class MyWorker @AssistedInject constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters
      ) : Worker(context, params) {
        override fun doWork(): Result {
          return Result.success()
        }
      }

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent : WorkerComponent
     """
  ) {

    val component = daggerAppComponent.createFunction()
      .invoke(null) as TangleWorkerComponent

    val mapSubcomponent = component.tangleWorkerMapSubcomponentFactory
      .create()

    val map = mapSubcomponent.workerFactoryMap

    map.size shouldBe 1
    map[myWorkerClass.canonicalName!!]!!
      .create(context, workerParameters)::class.java shouldBe myWorkerClass
  }

  @Test
  fun `TangleWorkerFactory is bound and contains Worker`() = compileWithDagger(
    """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import android.content.Context
      import androidx.work.Worker
      import androidx.work.WorkerParameters
      import dagger.assisted.Assisted
      import dagger.assisted.AssistedInject
      import tangle.inject.TangleGraph
      import tangle.work.TangleWorker
      import tangle.work.WorkerComponent
      import javax.inject.Singleton

      @TangleWorker
      class MyWorker @AssistedInject constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters
      ) : Worker(context, params) {
        override fun doWork(): Result {
          return Result.success()
        }
      }

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent : WorkerComponent
     """
  ) {

    val component = daggerAppComponent.createFunction()
      .invoke(null)!!

    TangleGraph.add(component)

    val workerFactory = TangleGraph.get<WorkerComponent>()
      .tangleWorkerFactory

    val worker = workerFactory.createWorker(
      appContext = context,
      workerClassName = myWorkerClass.canonicalName!!,
      workerParameters = workerParameters
    )!!

    worker::class.java shouldBe myWorkerClass
  }

  @Test
  fun `two components in classpath with same scope should not get duplicate bindings`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import javax.inject.Singleton

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent
     """,
      """
      package tangle.inject.tests.other

      import com.squareup.anvil.annotations.MergeComponent
      import javax.inject.Singleton

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent2
     """
    )

  @Test
  fun `two components in same package with same scope should not get duplicate bindings`() =
    compileWithDagger(
      """
      package tangle.inject.tests

      import com.squareup.anvil.annotations.MergeComponent
      import javax.inject.Singleton

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent

      @Singleton
      @MergeComponent(Unit::class)
      interface AppComponent2
     """
    )
}
