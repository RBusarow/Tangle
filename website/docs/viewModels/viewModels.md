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
the `TangleParam` annotation.

If the constructor argument's type is not nullable, then Tangle will assert that the argument is in
the bundle while creating the ViewModel.

If the argument is marked as nullable, then Tangle will gracefully handle a missing argument and
just inject `null`.

Given this code:

```kotlin
class MyViewModel @VMInject constructor(
  @TangleParam("userId")
  val userId: String,
  @TangleParam("address")
  val addressOrNull: String?
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

@Module
@ContributesTo(TangleAppScope::class)
public object TangleAppScope_VMInject_Module {
  @IntoSet
  @Provides
  @TangleViewModelProviderMap.KeySet
  public fun provideMyViewModelKey(): Class<out androidx.lifecycle.ViewModel> =
      MyViewModel::class.java
}

@Module
@ContributesTo(TangleScope::class)
public interface TangleScope_VMInject_Module {
  @Binds
  @IntoMap
  @ClassKey(MyViewModel::class)
  @TangleViewModelProviderMap
  public fun multibindMyViewModel(viewModel: MyViewModel): ViewModel

  public companion object {
    @Provides
    public fun provideMyViewModel_Factory(factory: MyViewModel_Factory): MyViewModel =
        factory.create()
  }
}
```

[Anvil]: https://github.com/square/anvil

[Dagger]: https://dagger.dev

[Hilt]: https://dagger.dev/hilt/view-model.html

[SavedStateHandle]: https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate
