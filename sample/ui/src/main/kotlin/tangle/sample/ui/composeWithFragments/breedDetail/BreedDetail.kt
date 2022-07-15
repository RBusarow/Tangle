/*
 * Copyright (C) 2022 Rick Busarow
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

package tangle.sample.ui.composeWithFragments.breedDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import tangle.sample.data.breed.BreedDetail

@OptIn(ExperimentalCoilApi::class)
@Composable
internal fun BreedDetail(
  useMetric: Boolean,
  breedDetail: BreedDetail,
  onSelectText: (String) -> Unit
) {

  Surface(
    modifier = Modifier.fillMaxSize()
  ) {
    Column(
      Modifier
        .fillMaxHeight()
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      Image(
        painter = rememberImagePainter(breedDetail.imageUrl),
        contentDescription = "picture of ${breedDetail.name}",
        modifier = Modifier
          .padding(8.dp)
          .sizeIn(minHeight = 240.dp)
          .fillMaxWidth(),
        contentScale = ContentScale.Fit
      )

      SpokenText(
        breedDetail.name,
        style = MaterialTheme.typography.h4,
        onSelectText = onSelectText
      )

      breedDetail.breedGroup?.let {
        SpokenText(
          it,
          onSelectText = onSelectText
        )
      }

      breedDetail.bredFor?.let {
        SpokenText(
          it,
          onSelectText = onSelectText
        )
      }

      SpokenText(
        breedDetail.lifeSpan,
        onSelectText = onSelectText
      )

      breedDetail.temperament?.let {
        SpokenText(
          it,
          onSelectText = onSelectText
        )
      }

      val (height, weight) = if (useMetric) {
        "${breedDetail.heightMetric} cm" to "${breedDetail.weightMetric} kg"
      } else {
        "${breedDetail.heightImperial} inches" to "${breedDetail.weightImperial} pounds"
      }

      SpokenText(
        height,
        onSelectText = onSelectText
      )

      SpokenText(
        weight,
        onSelectText = onSelectText
      )
    }
  }
}

@Composable
private fun SpokenText(
  text: String,
  style: TextStyle = MaterialTheme.typography.body1,
  onSelectText: (String) -> Unit
) {
  Text(
    text,
    Modifier
      .padding(16.dp)
      .clickable { onSelectText(text) },
    style = style,
    textAlign = TextAlign.Center
  )
}
