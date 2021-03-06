/*
 * Copyright (C) 2021 Rick Busarow
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

module.exports = {
  Docs: [
    "configuration",
    "gradle-plugin",
    "extending-anvil",
    "benchmarks",
    "member-injection",
    {
      type: "category",
      label: "ViewModels",
      collapsed: false,
      items: [
        "viewModels/viewModels",
        "viewModels/savedStateHandle",
        "viewModels/compose",
      ],
    },
    {
      type: "category",
      label: "Fragments",
      collapsed: false,
      items: [
        "fragments/fragments",
        "fragments/bundles",
      ],
    },
    {
      type: "category",
      label: "WorkManager",
      collapsed: false,
      items: [
        "workManager/workManager",
      ],
    },
  ],
};
