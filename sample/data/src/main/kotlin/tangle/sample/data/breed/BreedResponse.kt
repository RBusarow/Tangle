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

package tangle.sample.data.breed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tangle.sample.data.breed.BreedEntity.Height
import tangle.sample.data.breed.BreedEntity.Image
import tangle.sample.data.breed.BreedEntity.Weight
import tangle.sample.data.breed.BreedResponse.HeightResponse
import tangle.sample.data.breed.BreedResponse.ImageResponse
import tangle.sample.data.breed.BreedResponse.WeightResponse

@JsonClass(generateAdapter = true)
data class BreedResponse(
  @Json(name = "id")
  val id: Int,
  @Json(name = "name")
  val name: String,
  @Json(name = "bred_for")
  val bredFor: String?,
  @Json(name = "breed_group")
  val breedGroup: String?,
  @Json(name = "life_span")
  val lifeSpan: String,
  @Json(name = "reference_image_id")
  val referenceImageId: String,
  @Json(name = "temperament")
  val temperament: String?,
  @Json(name = "image")
  val image: ImageResponse,
  @Json(name = "height")
  val height: HeightResponse,
  @Json(name = "weight")
  val weight: WeightResponse
) {

  @JsonClass(generateAdapter = true)
  data class ImageResponse(
    @Json(name = "height")
    val height: Int,
    @Json(name = "id")
    val id: String,
    @Json(name = "url")
    val url: String,
    @Json(name = "width")
    val width: Int
  )

  @JsonClass(generateAdapter = true)
  data class HeightResponse(
    @Json(name = "imperial")
    val imperial: String,
    @Json(name = "metric")
    val metric: String
  )

  @JsonClass(generateAdapter = true)
  data class WeightResponse(
    @Json(name = "imperial")
    val imperial: String,
    @Json(name = "metric")
    val metric: String
  )
}

fun BreedResponse.toBreedEntity() = BreedEntity(
  id = id,
  name = name,
  bredFor = bredFor,
  breedGroup = breedGroup,
  lifeSpan = lifeSpan,
  referenceImageId = referenceImageId,
  temperament = temperament,
  image = image.toImageEntity(),
  height = height.toHeightEntity(),
  weight = weight.toWeightEntity()
)

fun ImageResponse.toImageEntity() = Image(height, id, url, width)
fun HeightResponse.toHeightEntity() = Height(imperial, metric)
fun WeightResponse.toWeightEntity() = Weight(imperial, metric)
