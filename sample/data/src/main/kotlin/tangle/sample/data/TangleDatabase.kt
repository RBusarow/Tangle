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

package tangle.sample.data

import android.content.Context
import androidx.room.Database
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Room
import androidx.room.RoomDatabase
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dispatch.core.DispatcherProvider
import kotlinx.coroutines.asExecutor
import tangle.sample.core.AppScope
import tangle.sample.core.di.ApplicationContext
import tangle.sample.data.breed.BreedDao
import tangle.sample.data.breed.BreedEntity
import javax.inject.Singleton

@Database(
  entities = [BreedEntity::class],
  version = 1,
  exportSchema = false
)
@RewriteQueriesToDropUnusedColumns
abstract class TangleDatabase : RoomDatabase() {

  abstract val breedDao: BreedDao

  companion object {

    const val DATABASE_NAME = "tangleDatabase.db"
  }
}

@Module
@ContributesTo(AppScope::class)
object RoomModule {

  @Singleton
  @Provides
  fun provideRoom(
    @ApplicationContext
    context: Context,
    dispatcherProvider: DispatcherProvider
  ): TangleDatabase {
    return Room.databaseBuilder(
      context, TangleDatabase::class.java, TangleDatabase.DATABASE_NAME
    )
      .setQueryExecutor(dispatcherProvider.io.asExecutor())
      .setTransactionExecutor(dispatcherProvider.io.asExecutor())
      .fallbackToDestructiveMigration()
      .build()
  }

  @Provides
  fun provideBreedDao(database: TangleDatabase): BreedDao = database.breedDao
}
