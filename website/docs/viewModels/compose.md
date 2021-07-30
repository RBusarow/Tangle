---
title: Compose
sidebar_label: Compose
---


### Compose support

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
