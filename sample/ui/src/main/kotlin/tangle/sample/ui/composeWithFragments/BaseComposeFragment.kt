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

package tangle.sample.ui.composeWithFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnLifecycleDestroyed
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import tangle.sample.core.safeAs

abstract class BaseComposeFragment : Fragment() {

  abstract val ui: @Composable () -> Unit

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    activity.safeAs<AppCompatActivity>()
      ?.supportActionBar
      ?.hide()

    return ComposeView(requireContext()).apply {

      // Dispose the Composition when viewLifecycleOwner is destroyed
      setViewCompositionStrategy(
        DisposeOnLifecycleDestroyed(viewLifecycleOwner)
      )

      setContent {

        CompositionLocalProvider(
          // sets the parentFragmentManager in the scoped LocalFragmentManager instance.
          // This can be accessed by any compose function with this `ui.invoke()` in its callstack
          LocalFragmentManager provides parentFragmentManager
        ) {
          MaterialTheme {
            ui.invoke()
          }
        }
      }
    }
  }
}

val LocalFragmentManager = staticCompositionLocalOf<FragmentManager> {
  error("FragmentManager not provided")
}
