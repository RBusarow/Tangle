---
title: Bundle Injection

sidebar_label: Bundle Injection
---

### The goal

Fragment runtime arguments must be passed via a `Bundle` in order for the arguments to be present
if the Fragment is recreated by a [FragmentManager].  For those of us who don't want to rely upon
[Androidx Navigation], there's still quite a lot of boilerplate involved in passing these arguments
and ensuring that it's compile-time safe.

Tangle removes as much of that boilerplate as possible,
while using some Dagger tricks to prevent creating new instances without their arguments.

:::note
Use `@FragmentInject` instead of `@Inject`
:::

```kotlin
@ContributesFragment(AppScope::class)
class MyFragment @FragmentInject constructor() : Fragment() {

  val name by arg<String>("name")

  @FragmentInjectFactory
  interface Factory {
    fun create(@TangleParam("name") name: String): MyFragment
  }
}

class MyActivity : BaseActivity() {

  val myFragmentFactory: MyFragment.Factory = TODO("use your favorite Dagger pattern here")

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val fragment = myFragmentFactory.create(name = "Bigyan")

    supportFragmentManager.beginTransaction()
      .replace(containerId, fragment)
      .commit()
  }
}
```

### Background

Since long before [FragmentFactory] and [Androidx Navigation],
[it has long been common practice](https://stackoverflow.com/a/9245510/7537239) to create static
`newInstance` functions which take the deconstructed Bundle parameters and return
a Fragment instance which already has those arguments injected as a Bundle.

Here's what it may look like in Kotlin:

```kotlin
class MyFragment : Fragment() {

  companion object {
    fun newInstance(name: String): MyFragment {
      val myFragment = MyFragment()

      myFragment.arguments = bundleOf("name" to name)
      return myFragment
    }
  }
}
```

### Tangle's generated factories

For the `MyFragment` definition above, Tangle will generate the following:

```kotlin
public class MyFragment_Factory_Impl(
  public val delegateFactory: MyFragment_Factory
) : MyFragment.Factory {
  public override fun create(name: String): MyFragment {
    val bundle = bundleOf(
          "name" to name
        )
    return delegateFactory.get().apply {
      this@apply.arguments = bundle
    }
  }

  public companion object {
    @JvmStatic
    public fun create(delegateFactory: MyFragment_Factory): Provider<MyFragment.Factory> =
        InstanceFactory.create(MyFragment_Factory_Impl(delegateFactory))
  }
}
```

It will then create a Dagger binding for `MyFragment_Factory_Impl` to `MyFragment.Factory`,
which allows us to use it in our code:

```kotlin
class MyNavigationImpl @Inject constructor(
  // fragments without bundle arguments can be injected in a Provider
  val myListFragmentProvider: Provider<MyListFragment>,
  // fragments with a factory must be injected this way
  val myFragmentFactory: MyFragment.Factory
) : MyNavigation {

  override fun goToMyListFragment(name: String){
    val fragment = myFragmentFactory.create(name)
    // actual navigation logic would go here
  }
  override fun goToMyFragment(name: String){
    val fragment = myFragmentFactory.create(name)
    // actual navigation logic would go here
  }
}
```

These factories are essentially an "entry point" to the [TangleFragmentFactory].  Once the factory
has initialized its Fragment, the arguments are established and cached by the Android framework.
If the Fragment needs to be recreated by the [TangleFragmentFactory], the new instance will be
created using a `Provider` and just invoking the constructor, without recreating the `Bundle`.

### Limiting access

If a Fragment requires a custom factory for bundle arguments,
Tangle _does_ create a `@Provides`-annotated function, but it's hidden behind a qualifier:

```kotlin
@Provides
@TangleFragmentProviderMap
public fun provideMyFragment(): MyFragment = MyFragment_Factory.newInstance()
```

This means that if anyone attempts to inject it like a normal Dagger dependency:
```kotlin
class SomeClass @Inject constructor(
  val myFragmentProvider: Provider<MyFragment>
)
```
...Dagger will fail the build with a very familiar error message:
> [Dagger/MissingBinding] com.example.MyFragment cannot be provided without an @Inject constructor or an @Provides-annotated method.





[Anvil]: https://github.com/square/anvil
[MergeComponent]: https://github.com/square/anvil#scopes

[Dagger]: https://dagger.dev
[AssistedInject]: https://dagger.dev/dev-guide/assisted-injection
[Hilt]: https://dagger.dev/hilt/view-model.html

[Androidx Navigation]: https://developer.android.com/guide/navigation/navigation-getting-started
[FragmentManager]: https://developer.android.com/reference/kotlin/androidx/fragment/app/FragmentManager
[SavedStateHandle]: https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate

[ContributesFragment]: https://rbusarow.github.io/Tangle/api/tangle-fragment-api/tangle.fragment/-contributes-fragment/index.html
[TangleFragmentFactory]: https://rbusarow.github.io/Tangle/api/tangle-fragment-api/tangle.fragment/-tangle-fragment-factory
[FragmentFactory]: https://developer.android.com/reference/kotlin/androidx/fragment/app/FragmentFactory
