---
title: Worker Injection

sidebar_label: Worker
---

Tangle is able to leverage Dagger's [AssistedInject] functionality to perform constructor injection
on your [Workers][Worker].  The `@TangleWorker` annotation will automatically multi-bind any Worker,
allowing you to create it via the `TangleWorkerFactory`.

```kotlin
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import tangle.work.TangleWorker

@TangleWorker
class MyWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted params: WorkerParameters,
  val repository: MyRepository
) : CoroutineWorker(context,params){
  override suspend fun doWork(): Result {
    // ...
  }
}
```


## Configuration

Once you've added Tangle as a dependency, implementing [Worker] injection is easy.

### 1. Annotate your ViewModels

`ViewModel` injection is done through the `@VMInject` constructor annotation.

```kotlin
class MyViewModel @VMInject constructor(
  val myRepository: MyRepository
) : ViewModel() {
  // ...
}
```

### 2. Initialize TangleGraph and use TangleWorkerFactory in your WorkManager

`TangleGraph` must be initialized as early as possible -- typically in `Application.onCreate()`.

```kotlin
import android.app.Application
import androidx.work.Configuration
import tangle.inject.TangleGraph
import tangle.work.TangleWorkerFactory
import javax.inject.Inject

class MyApplication : Application(), Configuration.Provider {

  @Inject lateinit var workerFactory: TangleWorkerFactory

  override fun onCreate() {
    super.onCreate()

    val myAppComponent = DaggerAppComponent.factory()
      .create(this)

    TangleGraph.init(myAppComponent)

    // inject your application class after initializing TangleGraph
    (myAppComponent as MyApplicationComponent).inject(this)
  }

  override fun getWorkManagerConfiguration(): Configuration {
    return Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .build()
  }
}
```

[AssistedInject]: https://dagger.dev/dev-guide/assisted-injection
[Worker]: https://developer.android.com/reference/androidx/work/ListenableWorker
