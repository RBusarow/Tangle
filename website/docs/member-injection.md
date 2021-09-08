---
id: member-injection

sidebar_label: Member Injection

title: Member Injection
---


The Android framework has a number of classes which are initialized automatically for us:

- `Application`
- `Activity`
- `View`
- `Service`
- `BroadcastReceiver`
- `Fragment` (these are a special case. See [fragments] for more info)

Because we don't control their initialization, we can't use Dagger's constructor injection to
provide their dependencies. Instead, we often choose to get our dependencies
using [member injection].

```kotlin
import android.app.Activity
import android.os.Bundle
import tangle.inject.TangleGraph
import tangle.inject.TangleScope
import javax.inject.Inject

@TangleScope(UserScope::class) // Dependencies will be provided by the UserScope
class UserActivity : Activity() {

  @Inject
  lateinit var logger: MyLogger

  override fun onCreate(savedInstanceState: Bundle?) {
    // inject MyLogger
    TangleGraph.inject(this)

    super.onCreate(savedInstanceState)

    logger.log("started UserActivity")
  }
}
```

Tangle's member injection is simple to implement.

1. Define your dependencies using `@Inject lateinit var`
2. Annotate your class with `@TangleScope(<your scope>::class)`
3. Call `TangleGraph.inject(this)` in your class's `onCreate(...)`.

The `TangleGraph.inject(...)` function uses the target's class in order to find the appropriate
scoped MemberInjector.

## TangleScope adds scope to target classes

In order to perform member injection with `TangleGraph.inject(target)`, the target of the injection
must be annotated with `@TangleScope(...)`. This is how Tangle determines where the dependencies are
coming from. For instance, your application may have an `AppScope` and a `UserScope`. For those two
scopes, you would use `@TangleScope(AppScope::class)` or `@TangleScope(UserScope::class)`
respectively.

Once a target class has an assigned scope, its dependencies will be validated at compile time. For
example, if you scope an activity to `AppScope` but it requires a dependency which is only available
in `UserScope`, the build will fail with a standard Dagger "MissingBinding" error message.

"Base" classes do not need a TangleScope annotation. The will be injected using the scope of their
subclass.

## Base classes

Large projects frequently have abstract base classes like a `BaseActivity`. These base classes may
have dependencies of their own. Injecting from a base class is supported in Tangle.

```kotlin
@TangleScope(UserScope::class) // Dependencies will be provided by the UserScope
class UserActivity : BaseActivity() {

  @Inject
  lateinit var logger: MyLogger

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    logger.log("started UserActivity")
  }
}

class BaseActivity : Activity() {

  @Inject
  lateinit var fragmentFactory: TangleFragmentFactory

  override fun onCreate(savedInstanceState: Bundle?) {
    // inject this class and the subclass
    TangleGraph.inject(this)

    super.onCreate(savedInstanceState)
  }
}
```

## Components must be added to TangleGraph

Tangle must know about your Component instances in order to inject your classes.

See [setting up the TangleGraph][setting-up-the-tangle-graph] for a simple example.



[member injection]: https://dagger.dev/members-injection.html

[fragments]: fragments/fragments.mdx

[setting-up-the-tangle-graph]: configuration#setting-up-the-tangle-graph
