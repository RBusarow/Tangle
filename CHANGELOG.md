# 0.15.0

The `0.15.0` version is built using these major dependencies:

| library             | version |
|:--------------------|:--------|
| Anvil               | 2.4.1   |
| Dagger              | 2.43    |
| Kotlin              | 1.7.0   |
| androidx.compose    | 1.2.0   |
| androidx.core       | 1.2.0   |
| androidx.work       | 2.7.1   |
| androidx.activity   | 1.5.1   |
| androidx.fragment   | 1.5.1   |
| androidx.lifecycle  | 2.5.1   |
| androidx.savedstate | 1.2.0   |

### 🐛 Bug Fixes

- Allow Fragment::arg with nullable parameters [@oldwomanjosiah](https://github.com/oldwomanjosiah) ([#420](https://github.com/rbusarow/Tangle/pull/420))

### Contributors

@RBusarow, @oldwomanjosiah

# 0.14.1

### 🐛 Bug Fixes

- update Anvil to 2.3.8 ([#379](https://github.com/rbusarow/Tangle/pull/379))
- remove inferred nullability for `TangleViewModelFactory.create` ([#378](https://github.com/rbusarow/Tangle/pull/378))

### 🧰 Maintenance

- Bump activity-ktx from 1.3.1 to 1.4.0 ([#372](https://github.com/rbusarow/Tangle/pull/372))
- Bump navigation-ui-ktx from 2.4.0-alpha10 to 2.4.0-beta01 ([#371](https://github.com/rbusarow/Tangle/pull/371))
- Bump auto-common from 1.1.2 to 1.2 ([#364](https://github.com/rbusarow/Tangle/pull/364))
- Bump room-compiler from 2.4.0-alpha04 to 2.4.0-alpha05 ([#357](https://github.com/rbusarow/Tangle/pull/357))
- Bump prismjs from 1.24.1 to 1.25.0 in /website ([#363](https://github.com/rbusarow/Tangle/pull/363))
- Bump axios from 0.21.1 to 0.21.4 in /website ([#362](https://github.com/rbusarow/Tangle/pull/362))

# 0.14.0

### 🐛 Bug Fixes

- Gradle plugin will now automatically enable/disable Tangle features depending upon which Androidx dependencies a module has. ([#353](https://github.com/rbusarow/Tangle/pull/353))
  - see [the Gradle plugin docs](https://rbusarow.github.io/Tangle/docs/gradle-plugin) for more information

### 🧰 Maintenance

- kotlin version-related updates ([#358](https://github.com/rbusarow/Tangle/pull/358))
  - Kotlin 1.5.30
  - Compose 1.0.3
- update KotlinPoet to 1.10.1 ([#359](https://github.com/rbusarow/Tangle/pull/359))

# 0.13.2

### 🐛 Bug Fixes

- make TanglePlugin apply the Anvil plugin eagerly ([#339](https://github.com/rbusarow/Tangle/pull/339))

# 0.13.1

### 🚀 Features

- The Tangle Gradle Plugin will now automatically apply the Anvil Gradle plugin if it hasn't been
  applied already. There is no change to behavior if Anvil was already applied manually, or if Anvil
  is applied later on in configuration. ([#333](https://github.com/rbusarow/Tangle/pull/333))

### 🐛 Bug Fixes

- The Tangle Gradle Plugin will now correctly detect existence of the Android Gradle
  Plugin ([#333](https://github.com/rbusarow/Tangle/pull/333))
- The website's "get started" button point to configuration
  doc ([#331](https://github.com/rbusarow/Tangle/pull/331))

### 🧰 Maintenance

- Bump com.osacky.doctor from 0.7.1 to 0.7.2 ([#329](https://github.com/rbusarow/Tangle/pull/329))
- Bump kotest-property-jvm from 4.6.2 to 4.6.3 ([#328](https://github.com/rbusarow/Tangle/pull/328))
- Bump lifecycle-viewmodel-compose from 1.0.0-alpha07 to
  2.4.0-beta01 ([#327](https://github.com/rbusarow/Tangle/pull/327))
- Bump navigation-runtime-ktx from 2.4.0-alpha08 to
  2.4.0-alpha09 ([#326](https://github.com/rbusarow/Tangle/pull/326))
- update Dokka to 1.5.30 ([#324](https://github.com/rbusarow/Tangle/pull/324))
- update Gradle Plugin Publish to 0.16.0 ([#325](https://github.com/rbusarow/Tangle/pull/325))
- Bump gradle-maven-publish-plugin from 0.17.0 to
  0.18.0 ([#322](https://github.com/rbusarow/Tangle/pull/322))
- Bump junit-jupiter-api from 5.7.2 to 5.8.0 ([#321](https://github.com/rbusarow/Tangle/pull/321))
- Bump ktlint-gradle from 10.1.0 to 10.2.0 ([#312](https://github.com/rbusarow/Tangle/pull/312))

# 0.13.0

### 🚀 Features

- add member injection ([#309](https://github.com/rbusarow/Tangle/pull/309))

```kotlin
@TangleScope(UserScope::class)
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

### 💥 Breaking Changes

- rename `TangleGraph.init()`
  to `TangleGraph.add()` ([#299](https://github.com/rbusarow/Tangle/pull/299))
- make :tangle-api a regular jar artifact ([#289](https://github.com/rbusarow/Tangle/pull/289)) (not
  really a breaking change though?)

### 🧰 Maintenance

- Bump moshi-ksp from 0.13.0 to 0.14.0 ([#307](https://github.com/rbusarow/Tangle/pull/307))
- Bump auto-service-ksp from 0.5.5 to 1.0.0 ([#308](https://github.com/rbusarow/Tangle/pull/308))
- Bump Androidx Work to 2.6.0 ([#277](https://github.com/rbusarow/Tangle/pull/277))
- Bump AGP to 7.0.2 ([#278](https://github.com/rbusarow/Tangle/pull/278))
- Bump compose from 1.0.1 to 1.0.2 ([#262](https://github.com/rbusarow/Tangle/pull/262))

# 0.12.1

### 🐛 Bug Fixes

- fix `by tangleViewModel` ignores `ComponentActivity`
  or `FragmentActivity` ([#228](https://github.com/RBusarow/Tangle/issues/228))
- fix `by tangleViewModel` in a Fragment attempts to initialize eagerly (and
  crashes) ([#227](https://github.com/RBusarow/Tangle/issues/227))

### 🧰 Maintenance

- update Anvil to 2.3.4 ([#251](https://github.com/rbusarow/Tangle/pull/251))

# 0.12.0 - Worker Injection

### 🚀 Features

- WorkManager/Worker Assisted injection is now
  supported ([#180](https://github.com/rbusarow/Tangle/pull/180))

```kotlin
@TangleWorker
class MyWorker @AssistedInject constructor(
  @Assisted
  context: Context,
  @Assisted
  params: WorkerParameters,
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
- `ViewModel`-related s feature toggles in the Gradle plugin have been moved. old:
  ```kotlin
  tangle {
    viewModelsEnabled.set(true)
    composeEnabled.set(true)
  }
  ```
  new:
  ```kotlin
  tangle {
    viewModelOptions {
      enabled = true // default is true
      composeEnabled = true // default is false
    }
  }
  ```

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

### 🐛 Bug Fixes

- fix ViewModel duplicate bindings ([#127](https://github.com/rbusarow/Tangle/pull/127))
- only create FragmentFactory and _Subcomponent.Factory bindings once in each
  classpath ([#124](https://github.com/rbusarow/Tangle/pull/124))
  - issue ([#123](https://github.com/rbusarow/Tangle/pull/123))

### 🧰 Maintenance

- create `release.sh` ([#126](https://github.com/rbusarow/Tangle/pull/126))

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

# 0.11.1

### 🐛 Bug Fixes

- update applied paths in TanglePlugin [#110](https://github.com/rbusarow/Tangle/pull/110)

### 🧰 Maintenance

- Bump constraintlayout from 2.0.4 to 2.1.0 (
  Dependabot) [#108](https://github.com/rbusarow/Tangle/pull/108)
- update version to 0.11.1 [#109](https://github.com/rbusarow/Tangle/pull/109)
- add release-drafter [#106](https://github.com/rbusarow/Tangle/pull/106)

# 0.11.0 - Fragment Injection

- add `require` function which throws
  TangleCompilationException [#6](https://github.com/rbusarow/Tangle/pull/6)
- Docusaurus [#7](https://github.com/rbusarow/Tangle/pull/7)
- update api dump [#47](https://github.com/rbusarow/Tangle/pull/47)
- replace kotlinter with ktlint-gradle [#45](https://github.com/rbusarow/Tangle/pull/45)
- initial Dokka setup [#46](https://github.com/rbusarow/Tangle/pull/46)
- basic knit setup [#48](https://github.com/rbusarow/Tangle/pull/48)
- automatically deploy website for every merge into
  main [#49](https://github.com/rbusarow/Tangle/pull/49)

# 0.10.0 - ViewModel Injection

Initial release

This release supports multi-bound `ViewModel` injection via the `by tangle()` delegate function,
with Compose support.

Automatic `SavedStateHandle` injection is supported, and arguments can be automatically
constructor-injected via the `@FromSavedState("myKey")` annotation.
