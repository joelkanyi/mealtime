/*
 * Copyright 2023 Joel Kanyi.
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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kanyideveloper.core_database.model.OnlineMealCategoryEntity
import com.kanyideveloper.core_database.model.OnlineMealEntity

@Dao
interface OnlineMealsDao {
    @Query("DELETE FROM online_meal_categories")
    suspend fun deleteOnlineMealCategories()

    @Query("DELETE FROM online_meals WHERE strCategory = :category")
    suspend fun deleteOnlineMeals(category: String)

    @Insert
    suspend fun insertOnlineMealCategories(categories: List<OnlineMealCategoryEntity>)

    @Insert
    suspend fun insertOnlineMeals(meals: List<OnlineMealEntity>)

    @Query("SELECT * FROM online_meal_categories")
    suspend fun getOnlineMealCategories(): List<OnlineMealCategoryEntity>

    @Query("SELECT * FROM online_meals WHERE strCategory = :category")
    suspend fun getOnlineMeals(category: String): List<OnlineMealEntity>
}
*/
