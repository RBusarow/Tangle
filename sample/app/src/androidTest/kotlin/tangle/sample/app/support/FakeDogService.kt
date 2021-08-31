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

package tangle.sample.app.support

import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.ContributesBinding.Priority.HIGHEST
import tangle.sample.core.AppScope
import tangle.sample.data.breed.BreedResponse
import tangle.sample.data.breed.BreedResponse.HeightResponse
import tangle.sample.data.breed.BreedResponse.ImageResponse
import tangle.sample.data.breed.BreedResponse.WeightResponse
import tangle.sample.data.breed.DogService
import javax.inject.Inject

@ContributesBinding(AppScope::class, priority = HIGHEST)
class FakeDogService @Inject constructor() : DogService {
  override suspend fun getAllBreedsPaged(page: Int, limit: Int): List<BreedResponse> {
    return listOf(GoldendoodleResponse)
  }
}

val GoldendoodleResponse = BreedResponse(
  id = 1,
  name = "Goldendoodle",
  bredFor = "sporting",
  breedGroup = "sporting",
  lifeSpan = "13-15 years",
  referenceImageId = "reference",
  temperament = "awesome temperament",
  image = ImageResponse(height = 1, id = "image-id", url = "puppy.png", width = 2),
  height = HeightResponse(imperial = "17 - 24 inches", metric = "43 - 61 cm"),
  weight = WeightResponse(imperial = "45 - 80 pounds", metric = "20.4 - 36.3 kg")
)
