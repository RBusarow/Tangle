# 0.12.0

### 🚀 Features

- WorkManager/Worker Assisted injection is now
  supported ([#180](https://github.com/rbusarow/Tangle/pull/180))

```kotlin
@TangleWorker
class MyWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted params: WorkerParameters,
  val repository: MyRepository
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    // ...
  }
}
```

### 💥 Breaking Changes

- The `by tangleViewModel()` delegate functions for `Activity` and `Fragment` have been moved to
  their own modules. ([#204](https://github.com/rbusarow/Tangle/pull/204))
  This was done to prevent leaking the Androidx transitive dependencies into projects which don't
  otherwise use them. The new artifacts are:
  - `com.rickbusarow.tangle:tangle-viewmodel-activity`
  - `com.rickbusarow.tangle:tangle-viewmodel-fragment`
- `TangleGraph` has
  moved `:tangle-api` (`tangle.inject.TangleGraph`) ([#169](https://github.com/rbusarow/Tangle/pull/169))

### 🐛 Bug Fixes

- fix wrapping of very long checkNotNull error
  messages ([#162](https://github.com/rbusarow/Tangle/pull/162))

# 0.11.5

### 🐛 Bug Fixes

- fix the package for the compose
  tangleViewModel ([#146](https://github.com/rbusarow/Tangle/pull/146))
- disable BuildConfig for Android libraries ([#145](https://github.com/rbusarow/Tangle/pull/145))
- fix compose tangleViewModel package ([#147](https://github.com/rbusarow/Tangle/pull/147))

### 🧰 Maintenance

- kotlin dependency updates ([#143](https://github.com/rbusarow/Tangle/pull/143))

### ℹ️ Website

- prepare for 0.11.5 release ([#148](https://github.com/rbusarow/Tangle/pull/148))
- fix the package for the compose
  tangleViewModel ([#146](https://github.com/rbusarow/Tangle/pull/146))
- create version 0.11.4 ([#128](https://github.com/rbusarow/Tangle/pull/128))

# 0.11.4

### Changes

### 🐛 Bug Fixes

- fix ViewModel duplicate bindings ([#127](https://github.com/rbusarow/Tangle/pull/127))
- only create FragmentFactory and _Subcomponent.Factory bindings once in each
  classpath ([#124](https://github.com/rbusarow/Tangle/pull/124))
  - issue ([#123](https://github.com/rbusarow/Tangle/pull/123))

### 🧰 Maintenance

- create `release.sh` ([#126](https://github.com/rbusarow/Tangle/pull/126))

### Contributors

[@RBusarow](https://github.com/RBusarow)

# 0.11.2

### Changes

### 🐛 Bug Fixes

- create default multi-bindings for ViewModels ([#116](https://github.com/rbusarow/Tangle/pull/116))

### 🧰 Maintenance

- create a github action for releasing ([#118](https://github.com/rbusarow/Tangle/pull/118))
- update changelogs ([#117](https://github.com/rbusarow/Tangle/pull/117))

### ℹ️ Website

- update version to 0.11.2 ([#119](https://github.com/rbusarow/Tangle/pull/119))
- update changelogs ([#117](https://github.com/rbusarow/Tangle/pull/117))

### Contributors

[@RBusarow](https://github.com/RBusarow)

# 0.11.1

### Changes

- create 0.11.1 version for website docs [#111](https://github.com/rbusarow/Tangle/pull/111)

### 🐛 Bug Fixes

- update applied paths in TanglePlugin [#110](https://github.com/rbusarow/Tangle/pull/110)

### 🧰 Maintenance

- Bump constraintlayout from 2.0.4 to 2.1.0 (
  Dependabot) [#108](https://github.com/rbusarow/Tangle/pull/108)
- update version to 0.11.1 [#109](https://github.com/rbusarow/Tangle/pull/109)
- add release-drafter [#106](https://github.com/rbusarow/Tangle/pull/106)

# 0.11.0

- add `require` function which throws
  TangleCompilationException [#6](https://github.com/rbusarow/Tangle/pull/6)
- Docusaurus [#7](https://github.com/rbusarow/Tangle/pull/7)
- update api dump [#47](https://github.com/rbusarow/Tangle/pull/47)
- replace kotlinter with ktlint-gradle [#45](https://github.com/rbusarow/Tangle/pull/45)
- initial Dokka setup [#46](https://github.com/rbusarow/Tangle/pull/46)
- basic knit setup [#48](https://github.com/rbusarow/Tangle/pull/48)
- automatically deploy website for every merge into
  main [#49](https://github.com/rbusarow/Tangle/pull/49)

# 0.10.0

Initial release

This release supports multi-bound `ViewModel` injection via the `by tangle()` delegate function,
with Compose support.

Automatic `SavedStateHandle` injection is supported, and arguments can be automatically
constructor-injected via the `@FromSavedState("myKey")` annotation.
