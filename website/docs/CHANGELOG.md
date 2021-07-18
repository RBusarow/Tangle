---
id: changelog
title: Change Log
sidebar_label: Change Log
---

## 0.11.0

- add `require` function which throws TangleCompilationException #6
- Docusaurus #7
- update api dump #47
- replace kotlinter with ktlint-gradle #45
- initial Dokka setup #46
- basic knit setup #48
- automatically deploy website for every merge into main #49

## 0.10.0

Initial release

This release supports multi-bound `ViewModel` injection via the `by tangle()` delegate function,
with Compose support.

Automatic `SavedStateHandle` injection is supported, and arguments can be automatically
constructor-injected via the `@FromSavedState("myKey")` annotation.
