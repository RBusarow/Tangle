### This is a work in progress, in a very early state.  You've been warned.

___

Tangle brings [Anvil](https://github.com/square/anvil) support to the Android Architecture Components `ViewModel`.
The injection is done via multi-binding, with an API surface very much like that of [Hilt](https://dagger.dev/hilt/view-model.html).

``` Kotlin
@ContributesViewModel(AppScope::class)
class MyViewModel @VMInject constructor(
  val myRepository: MyRepository,
  // Tangle will automatically extract arguments from SavedStateHandle
  @FromSavedState("userId")
  val userId: String,
  // nullable SavedStateHandle arguments are just injected as null if missing
  @FromSavedState("address")
  val addressOrNull: String?,
  // SavedStateHandle may be injected directly
  val savedStateHandle: SavedStateHandle
) : ViewModel() {
  // ...
}

class MyFragment : Fragment() {

  // lazily service-located, just like `by viewModels` or `by hiltViewModel`
  val myViewModel: MyViewModel by tangle()
}
```

____
## Config

```kotlin
// settings.gradle.kts

pluginManagement {
  repositories {
    gradlePluginPortal()
  }
}
```

```kotlin
// top-level build.gradle.kts

plugins {
  id("com.rickbusarow.tangle") version "0.10.0"
}
```

```kotlin
// any Android module's build.gradle.kts

plugins {
  id("android-library") // or application, etc.
  kotlin("android")
  id("com.squareup.anvil")
  id("com.rickbusarow.tangle")
}

// optional
tangle {
  composeEnabled.set(true) // default is false
}
```

The `TangleComponents` holder needs to be initialized with an application-scoped Dagger Component in order to complete the graph.

``` Kotlin
class MyApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    val myAppComponent = DaggerAppComponent.factory()
      .create(this)

    TangleComponents.add(myAppComponent)
  }
}
```


____
## Compose support

Tangle supports ViewModel "injection" in composables in a manner very similar to Hilt's navigation/viewModel artifact.  It will scope the ViewModel to the composable's `NavBackStackEntry`.

``` Kotlin
@Composable
fun MyComposable(
  navController: NavController,
  viewModel: MyViewModel = tangle()
) {
  // ...
}
```

## License

``` text
Copyright (C) 2021 Rick Busarow
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
