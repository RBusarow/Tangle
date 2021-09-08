---
title: Compose
sidebar_label: Compose
---

Tangle supports ViewModel "injection" in composables in a manner very similar to Hilt's
navigation/viewModel artifact. It will scope the ViewModel to the composable's `NavBackStackEntry`.

The viewModels are still able to make use of automatic [SavedStateHandle injection](savedStateHandle.md),
including arguments annotated with `@TangleParam`.

```kotlin
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import tangle.viewmodel.compose.tangleViewModel

@Composable
fun MyComposable(
  navController: NavController,
  viewModel: MyViewModel = tangleViewModel()
) { /* ... */ }
```


[Anvil]: https://github.com/square/anvil

[Dagger]: https://dagger.dev

[Hilt]: https://dagger.dev/hilt/view-model.html

[SavedStateHandle]: https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate
