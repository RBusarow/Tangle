# 0.11.3
### Changes

### 🐛 Bug Fixes

- only create FragmentFactory and _Subcomponent.Factory bindings once in each classpath [@RBusarow](https://github.com/RBusarow) ([#124](https://github.com/rbusarow/Tangle/pull/124))
  - issue ([#123](https://github.com/rbusarow/Tangle/pull/123))

## Contributors

[@RBusarow](https://github.com/RBusarow)

# 0.11.2
### Changes

### 🐛 Bug Fixes

- create default multi-bindings for ViewModels [@RBusarow](https://github.com/RBusarow) ([#116](https://github.com/rbusarow/Tangle/pull/116))

### 🧰 Maintenance

- create a github action for releasing [@RBusarow](https://github.com/RBusarow) ([#118](https://github.com/rbusarow/Tangle/pull/118))
- update changelogs [@RBusarow](https://github.com/RBusarow) ([#117](https://github.com/rbusarow/Tangle/pull/117))

### ℹ️ Website

- update version to 0.11.2 [@RBusarow](https://github.com/RBusarow) ([#119](https://github.com/rbusarow/Tangle/pull/119))
- update changelogs [@RBusarow](https://github.com/RBusarow) ([#117](https://github.com/rbusarow/Tangle/pull/117))

### Contributors

[@RBusarow](https://github.com/RBusarow)

# 0.11.1
### Changes

- create 0.11.1 version for website docs [@RBusarow](https://github.com/RBusarow) [#111](https://github.com/rbusarow/Tangle/pull/111)

### 🐛 Bug Fixes

- update applied paths in TanglePlugin [@RBusarow](https://github.com/RBusarow) [#110](https://github.com/rbusarow/Tangle/pull/110)

### 🧰 Maintenance

- Bump constraintlayout from 2.0.4 to 2.1.0 (Dependabot) [#108](https://github.com/rbusarow/Tangle/pull/108)
- update version to 0.11.1 [@RBusarow](https://github.com/RBusarow) [#109](https://github.com/rbusarow/Tangle/pull/109)
- add release-drafter [@RBusarow](https://github.com/RBusarow) [#106](https://github.com/rbusarow/Tangle/pull/106)

# 0.11.0

- add `require` function which throws TangleCompilationException [#6](https://github.com/rbusarow/Tangle/pull/6)
- Docusaurus [#7](https://github.com/rbusarow/Tangle/pull/7)
- update api dump [#47](https://github.com/rbusarow/Tangle/pull/47)
- replace kotlinter with ktlint-gradle [#45](https://github.com/rbusarow/Tangle/pull/45)
- initial Dokka setup [#46](https://github.com/rbusarow/Tangle/pull/46)
- basic knit setup [#48](https://github.com/rbusarow/Tangle/pull/48)
- automatically deploy website for every merge into main [#49](https://github.com/rbusarow/Tangle/pull/49)

# 0.10.0

Initial release

This release supports multi-bound `ViewModel` injection via the `by tangle()` delegate function,
with Compose support.

Automatic `SavedStateHandle` injection is supported, and arguments can be automatically
constructor-injected via the `@FromSavedState("myKey")` annotation.
