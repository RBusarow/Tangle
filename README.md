![Maven Central](https://img.shields.io/maven-central/v/com.rickbusarow.tangle/tangle-api?style=flat-square)
![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.rickbusarow.tangle?style=flat-square)
[![License](https://img.shields.io/badge/license-apache2.0-blue?style=flat-square.svg)](https://opensource.org/licenses/Apache-2.0)

___

Tangle creates [Dagger] bindings for Android classes using the [Anvil] Kotlin compiler plugin. This
is meant to be an alternative to [Hilt], for those who'd prefer to enjoy the faster compilation and
better flexibility of Anvil.

Since Tangle is an extension upon Anvil, its code generation will be applied to **Kotlin** files
only.

```kotlin
@ContributesViewModel(AppScope::class)
class MyViewModel @VMInject constructor(
  val myRepository: MyRepository,
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
<!--- TOC -->

* [Config](#config)
  * [Gradle plugin](#gradle-plugin)
  * [Explicit dependencies](#explicit-dependencies)
* [SavedStateHandle injection](#savedstatehandle-injection)
* [Compose support](#compose-support)
* [License](#license)

<!--- END -->

## Config

The `TangleComponents` holder needs to be initialized with an application-scoped Dagger Component in
order to complete the graph.

```kotlin
class MyApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    val myAppComponent = DaggerAppComponent.factory()
      .create(this)

    TangleComponents.add(myAppComponent)
  }
}
```

### Gradle plugin

The simple way to apply Tangle is to just apply the gradle plugin. It will automatically add the
dependencies and perform some basic validation of your module's configuration.

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
  id("com.rickbusarow.tangle") version "0.11.0"
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

### Explicit dependencies

You can also just add dependencies yourself, without applying the plugin.

Note that `tangle-api` is an Android library, and `tangle-compiler` generates Android-specific code,
so they should only be added to Android modules.

```kotlin
// any Android module's build.gradle.kts

plugins {
  id("android-library") // or application, etc.
  kotlin("android")
  id("com.squareup.anvil")
}

dependencies {

  api("com.rickbusarow.tangle:tangle-annotations:0.11.0")

  implementation("com.rickbusarow.tangle:tangle-api:0.11.0")

  // optional Compose support
  implementation("com.rickbusarow.tangle:tangle-compose:0.11.0")

  // `anvil` adds the compiler extension to the Anvil plugin's list of code generators
  anvil("com.rickbusarow.tangle:tangle-compiler:0.11.0")
}
```

## SavedStateHandle injection

Tangle supports injecting [SavedStateHandle] into ViewModel constructors, where
the `SavedStateHandle` is provided by the ViewModel's owning `Fragment`/`Activity`
/`NavBackStackEntry`.

In addition to or in lieu of `SavedStateHandle`, Tangle can automatically extract arguments from
the `SavedStateHandle` and inject them into the constructor, through use of
the `FromSavedStateHandle` annotation.

If the constructor argument's type is not nullable, then Tangle will assert that the argument is in
the bundle while creating the ViewModel.

If the argument is marked as nullable, then Tangle will gracefully handle a missing argument and
just inject `null`.

Given this code:

```kotlin
@ContributesViewModel(AppScope::class)
class MyViewModel @VMInject constructor(
  @FromSavedState("userId")
  val userId: String,
  @FromSavedState("address")
  val addressOrNull: String?
) : ViewModel()
```

Tangle will generate this Provider:

```kotlin
public class MyViewModel_Provider @Inject constructor(
  private val savedStateHandle: Provider<SavedStateHandle>
) : Provider<MyViewModel> {
  public override fun `get`(): MyViewModel {
    val userId = savedStateHandle.get().get<String>("userId")
    checkNotNull(userId) {
      "Required parameter with name `userId` " +
      "and type `kotlin.String` is missing from SavedStateHandle."
    }
    val addressOrNull = savedStateHandle.get().get<String?>("address")
    return MyViewModel(userId, addressOrNull, savedStateHandle.get())
  }
}
```

## Compose support

Tangle supports ViewModel "injection" in composables in a manner very similar to Hilt's
navigation/viewModel artifact. It will scope the ViewModel to the composable's `NavBackStackEntry`.

```kotlin
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


[Anvil]: https://github.com/square/anvil

[Dagger]: https://dagger.dev

[Hilt]: https://dagger.dev/hilt/view-model.html

[SavedStateHandle]: https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate
