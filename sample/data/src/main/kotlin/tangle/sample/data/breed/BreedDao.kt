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

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
@RewriteQueriesToDropUnusedColumns
abstract class BreedDao {

  @Insert
  abstract suspend fun insert(entity: BreedEntity)

  @Query("SELECT * FROM Breed WHERE id = :id LIMIT 1")
  abstract suspend fun getById(id: Int): BreedDetail

  @Query("SELECT * FROM Breed")
  abstract fun pagingSource(): PagingSource<Int, BreedSummary>

  @Query("SELECT * FROM Breed")
  abstract fun breedFlow(): Flow<BreedSummary>

  @Insert
  abstract suspend fun insertAll(vararg entity: BreedEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertAll(entities: List<BreedEntity>)

  @Update
  abstract suspend fun update(entity: BreedEntity)

  @Delete
  abstract suspend fun deleteEntity(entity: BreedEntity)

  suspend fun insertOrUpdate(entity: BreedEntity) {
    if (entity.id == 0) {
      insert(entity)
    } else {
      update(entity)
    }
  }

  @Transaction
  open suspend fun insertOrUpdate(entities: List<BreedEntity>) {
    entities.forEach {
      insertOrUpdate(it)
    }
  }
}
