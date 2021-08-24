---
title: ViewModels

sidebar_label: ViewModels
---

Once you've added Tangle as a dependency, implementing [ViewModel] injection is easy.

### 1. Annotate your ViewModels

`ViewModel` injection is done through the `@VMInject` constructor annotation.

```kotlin
class MyViewModel @VMInject constructor(
  val myRepository: MyRepository
) : ViewModel() {
  // ...
}
```

### 2. Tell Tangle about the AppComponent

`TangleGraph` must be initialized as early as possible -- typically in `Application.onCreate()`.

```kotlin
import android.app.Application
import tangle.inject.TangleGraph

class MyApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    val myAppComponent = DaggerAppComponent.factory()
      .create(this)

    TangleGraph.init(myAppComponent)
  }
}
```

### 3. Use the `tangleViewModel` delegate

```kotlin
class MyFragment : Fragment() {
  val viewModel by tangleViewModel<MyViewModel>()

  // ...
}
```


[ViewModel]: https://developer.android.com/topic/libraries/architecture/viewmodel
