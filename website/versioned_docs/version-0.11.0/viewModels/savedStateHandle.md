---
title: SavedStateHandle injection
sidebar_label: SavedStateHandle Injection
---

When using the `tangleViewModel` delegate function, a scoped subcomponent is created
with a binding for [SavedStateHandle].  This `SavedStateHandle` is provided
by the ViewModel's owning `Fragment`, `Activity`, or `NavBackStackEntry`.

This `SavedStateHandle` may then be included as a dependency in injected constructors,
just as it can in [Hilt].

```kotlin
class MyViewModel @VMInject constructor(
  val savedState: SavedStateHandle
) : ViewModel()
```

In addition, Tangle can automatically extract arguments from the `SavedStateHandle`
and inject them into the constructor, through use of the `TangleParam` annotation.

If the constructor argument's type is not nullable, then Tangle will assert that the argument is in
the bundle while creating the ViewModel.

If the argument is marked as nullable, then Tangle will gracefully handle a missing argument and
just inject `null`.

Given this code:

```kotlin
class MyViewModel @VMInject constructor(
  @TangleParam("userId")
  val userId: String, // must be present in the SavedStateHandle
  @TangleParam("address")
  val addressOrNull: String? // can safely be null
) : ViewModel()
```

Tangle will generate the following:

```kotlin
public class MyViewModel_Factory @Inject constructor(
  internal val savedStateHandleProvider: Provider<SavedStateHandle>
) {
  public fun create(): MyViewModel {
    val userId = savedStateHandleProvider.get().get<String>("userId")
    checkNotNull(userId) {
      "Required parameter with name `userId` " +
      "and type `kotlin.String` is missing from SavedStateHandle."
    }
    val addressOrNull = savedStateHandleProvider.get().get<String?>("address")
    return MyViewModel(userId, addressOrNull)
  }
}
```

[Anvil]: https://github.com/square/anvil

[Dagger]: https://dagger.dev

[Hilt]: https://dagger.dev/hilt/view-model.html

[SavedStateHandle]: https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate
