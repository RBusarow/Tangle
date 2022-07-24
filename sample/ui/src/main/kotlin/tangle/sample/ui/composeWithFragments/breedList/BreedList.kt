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

package tangle.sample.ui.composeWithFragments.breedList

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.rememberAsyncImagePainter
import tangle.sample.core.isMetric
import tangle.sample.data.breed.BreedSummary
import tangle.sample.ui.composeWithActivities.breedList.BreedListViewModel
import tangle.viewmodel.compose.tangleViewModel
import java.util.Locale

@Composable
internal fun BreedList(
  viewModel: BreedListViewModel = tangleViewModel(),
  onClick: (BreedSummary) -> Unit
) {

  val breedListItems = viewModel.pagingDataFlow
    .collectAsLazyPagingItems()

  Surface(
    modifier = Modifier.fillMaxSize()
  ) {
    LazyColumn(
      Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      items(breedListItems) { summary ->
        if (summary != null) {
          tangle.sample.ui.composeWithActivities.breedList.BreedListItem(
            Locale.getDefault()
              .isMetric(),
            summary, onClick
          )
        }
      }
    }
  }
}

@Composable
internal fun BreedListItem(
  useMetric: Boolean,
  breedSummary: BreedSummary,
  onClick: (BreedSummary) -> Unit
) {
  Card(
    Modifier
      .wrapContentSize()
      .padding(start = 8.dp, end = 8.dp)
      .clickable { onClick(breedSummary) },
    elevation = 2.dp
  ) {
    Row(
      Modifier
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Image(
        painter = rememberAsyncImagePainter(breedSummary.imageUrl),
        contentDescription = "picture of ${breedSummary.name}",
        Modifier
          .size(180.dp)
          .padding(8.dp)
          .clip(MaterialTheme.shapes.small),
        contentScale = ContentScale.Fit
      )
      Column(
        Modifier
          .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          breedSummary.name,
          style = MaterialTheme.typography.h4,
          textAlign = TextAlign.Center
        )
        breedSummary.breedGroup?.let {
          Text(
            it,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
          )
        }

        val (height, weight) = if (useMetric) {
          breedSummary.heightMetric to breedSummary.weightMetric
        } else {
          breedSummary.heightImperial to breedSummary.weightImperial
        }

        if (height != null) {
          Text(
            height,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
          )
        }
        if (weight != null) {
          Text(
            weight,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}

@Preview(
  "Breed list item",
  showSystemUi = true,
  showBackground = true
)
@Composable
fun Preview() {
  MaterialTheme {
    tangle.sample.ui.composeWithActivities.breedList.BreedListItem(
      useMetric = false,
      breedSummary = BreedSummary(
        id = 1,
        name = "Collie",
        breedGroup = "herding",
        imageUrl = "",
        heightImperial = "24-28 inches",
        heightMetric = "10-20 cm",
        weightImperial = "150-170 lbs",
        weightMetric = "150-170 kg"
      )
    ) {}
  }
}
