---
title: ViewModels
sidebar_label: ViewModels
---

### SavedStateHandle injection

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


[Anvil]: https://github.com/square/anvil

[Dagger]: https://dagger.dev

[Hilt]: https://dagger.dev/hilt/view-model.html

[SavedStateHandle]: https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate
