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

package tangle.sample.data.breed

import androidx.room.ColumnInfo

data class BreedSummary(
  @ColumnInfo(name = "id")
  val id: Int,
  @ColumnInfo(name = "name")
  val name: String,
  @ColumnInfo(name = "breed_group")
  val breedGroup: String?,
  @ColumnInfo(name = "image_url")
  val imageUrl: String,
  @ColumnInfo(name = "height_imperial")
  val heightImperial: String?,
  @ColumnInfo(name = "height_metric")
  val heightMetric: String?,
  @ColumnInfo(name = "weight_imperial")
  val weightImperial: String?,
  @ColumnInfo(name = "weight_metric")
  val weightMetric: String?
)
