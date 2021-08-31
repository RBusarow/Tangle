[![Maven Central](https://img.shields.io/maven-central/v/com.rickbusarow.tangle/tangle-api?style=flat-square)](https://search.maven.org/search?q=com.rickbusarow.tangle)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.rickbusarow.tangle?style=flat-square)](https://plugins.gradle.org/plugin/com.rickbusarow.tangle)
[![License](https://img.shields.io/badge/license-apache2.0-blue?style=flat-square.svg)](https://opensource.org/licenses/Apache-2.0)

___

Tangle creates [Dagger] bindings for Android classes using the [Anvil] Kotlin compiler plugin. This
is meant to be an alternative to [Hilt], for those who'd prefer to enjoy the faster compilation and
better flexibility of Anvil.

Since Tangle is an extension upon Anvil, its code generation will be applied to **Kotlin** files
only.

### Please see [the project website](https://rbusarow.github.io/Tangle/) for full documentation.

## Features

#### ViewModel Injection

```kotlin
class MyViewModel @VMInject constructor(
  val repository: MyRepository,
  @TangleParam("userId") // pulls the "userId" argument out of SavedStateHandle
  val userId: String
) : ViewModel()

@Composable
fun MyComposable(
  navController: NavController,
  viewModel: MyViewModel = tangleViewModel()
) { /* ... */ }

class MyFragment : Fragment() {
  val viewModel: MyViewModel by tangleViewModel()
}
```
Inject ViewModels, including scoped `SavedStateHandle` arguments. Use the `@TangleParam` annotation to automatically extract navigation/Bundle arguments and inject them directly. [read more](https://rbusarow.github.io/Tangle/docs/viewModels/viewModels)

#### Fragment Injection with Bundle arguments

```kotlin
@ContributesFragment(AppScope::class)
class MyFragment @FragmentInject constructor(
  val repository: MyRepository
) : Fragment() {

  val name: String by arg("name")

  @FragmentInjectFactory
  interface Factory {
    fun create(
      @TangleParam("name") name: String
    ): MyFragment
  }
}
```

Use constructor injection in Fragments, with optional AssistedInject-like factories for type-safe `Bundle` arguments. Bindings are created automatically. [read more](https://rbusarow.github.io/Tangle/docs/next/fragments/fragments)

#### Worker Injection

```kotlin
@TangleWorker
class MyWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted params: WorkerParameters
) : CoroutineWorker(context,params) {
  override suspend fun doWork(): Result {
    /* ... */
  }
}
```

Use Dagger's `@AssistedInject` and `@Assisted` annotations and `@TangleWorker` to inject any `ListenableWorker`. [read more](https://rbusarow.github.io/Tangle/docs/next/workManager/workManager)

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
