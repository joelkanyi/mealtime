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
package com.kanyideveloper.mealplanner.domain.repository

import com.joelkanyi.shared.data.network.utils.Resource
import com.joelkanyi.shared.domain.CoreMeal
import com.kanyideveloper.core.model.Meal
import com.kanyideveloper.core.model.MealPlanPreference
import com.kanyideveloper.mealplanner.model.MealPlan
import kotlinx.coroutines.flow.Flow

interface MealPlannerRepository {
    suspend fun hasMealPlanPref(isSubscribed: Boolean): Flow<MealPlanPreference?>

    suspend fun saveMealPlannerPreferences(
        allergies: List<String>,
        numberOfPeople: String,
        dishTypes: List<String>,
        isSubscribed: Boolean
    )

    suspend fun getMealsInMyPlan(
        filterDay: String,
        isSubscribed: Boolean
    ): Resource<Flow<List<MealPlan>>>

    fun getExistingMeals(mealType: String, date: String): List<CoreMeal>

    suspend fun deleteAMealFromPlan(id: String)

    suspend fun saveMealToPlan(mealPlan: MealPlan, isSubscribed: Boolean)

    suspend fun searchMeal(
        source: String,
        searchBy: String,
        searchString: String,
        isSubscribed: Boolean
    ): Resource<Flow<List<CoreMeal>>>

    suspend fun removeMealFromPlan(id: String, isSubscribed: Boolean)

    suspend fun getAllIngredients(): Resource<List<String>>

    fun setAlarm(isSubscribed: Boolean)
}
