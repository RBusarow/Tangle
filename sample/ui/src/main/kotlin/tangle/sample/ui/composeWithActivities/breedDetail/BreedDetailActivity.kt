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

package tangle.sample.ui.composeWithActivities.breedDetail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import tangle.sample.core.isMetric
import tangle.viewmodel.compose.tangleViewModel
import java.util.Locale

class BreedDetailActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        val viewModel = tangleViewModel<BreedDetailViewModel>()

        val breedDetail = viewModel.detailFlow
          .collectAsState()
          .value

        if (breedDetail != null) {
          BreedDetail(
            Locale.getDefault().isMetric(),
            breedDetail
          ) { viewModel.onTextSelected(it) }
        }
      }
    }
  }
}
