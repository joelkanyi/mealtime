/*
 * Copyright 2022 Joel Kanyi.
 *
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
package com.kanyideveloper.core_database.dao

/*
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.kanyideveloper.core_database.model.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Insert
    suspend fun insertAFavorite(favoriteEntity: FavoriteEntity)

    @Query("SELECT * FROM favorites_table ORDER BY id DESC")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites_table WHERE id  == :id")
    fun getAFavoriteById(id: Int): LiveData<FavoriteEntity?>

    @Query("SELECT isFavorite FROM favorites_table WHERE localMealId = :id")
    fun localInFavorites(id: String): LiveData<Boolean>

    @Query("SELECT isFavorite FROM favorites_table WHERE onlineMealId = :id")
    fun onlineInFavorites(id: String): LiveData<Boolean>

    @Delete
    suspend fun deleteAFavorite(favoriteEntity: FavoriteEntity)

    @Query("DELETE FROM favorites_table WHERE localMealId = :localMealId")
    suspend fun deleteALocalFavorite(localMealId: String)

    @Query("DELETE FROM favorites_table WHERE onlineMealId = :mealId")
    suspend fun deleteAnOnlineFavorite(mealId: String)

    @Query("DELETE FROM favorites_table")
    suspend fun deleteAllFavorites()
}
*/
