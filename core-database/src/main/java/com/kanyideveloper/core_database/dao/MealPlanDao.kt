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
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kanyideveloper.core_database.model.MealPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(mealPlanEntity: MealPlanEntity)

    @Query("SELECT * FROM meal_plan_table WHERE mealDate = :filterDay")
    fun getPlanMeals(filterDay: String): Flow<List<MealPlanEntity>>

    @Query("DELETE FROM meal_plan_table WHERE id = :id")
    suspend fun deleteAMealFromPlan(id: String)

    @Query("DELETE FROM meal_plan_table WHERE id = :id")
    suspend fun removeMealFromPlan(id: Int)

    @Query("DELETE FROM meal_plan_table")
    suspend fun deleteAllMealsFromPlan()
}
*/
