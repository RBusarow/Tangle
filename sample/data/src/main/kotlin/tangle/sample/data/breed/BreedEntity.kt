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

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Breed")
data class BreedEntity(
  @PrimaryKey
  @ColumnInfo(name = "id")
  val id: Int,
  @ColumnInfo(name = "name")
  val name: String,
  @ColumnInfo(name = "bred_for")
  val bredFor: String?,
  @ColumnInfo(name = "breed_group")
  val breedGroup: String?,
  @ColumnInfo(name = "life_span")
  val lifeSpan: String,
  @ColumnInfo(name = "reference_image_id")
  val referenceImageId: String,
  @ColumnInfo(name = "temperament")
  val temperament: String?,
  @Embedded(prefix = "image_")
  val image: Image,
  @Embedded(prefix = "height_")
  val height: Height,
  @Embedded(prefix = "weight_")
  val weight: Weight
) {

  data class Image(
    @ColumnInfo(name = "height")
    val height: Int,
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "width")
    val width: Int
  )

  data class Height(
    @ColumnInfo(name = "imperial")
    val imperial: String,
    @ColumnInfo(name = "metric")
    val metric: String
  )

  data class Weight(
    @ColumnInfo(name = "imperial")
    val imperial: String,
    @ColumnInfo(name = "metric")
    val metric: String
  )
}
