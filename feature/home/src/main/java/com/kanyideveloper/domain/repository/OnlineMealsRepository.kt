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
package com.kanyideveloper.domain.repository

import com.joelkanyi.shared.core.data.network.utils.Resource
import com.kanyideveloper.core.model.Meal
import com.kanyideveloper.domain.model.Category
import com.kanyideveloper.domain.model.OnlineMeal
import kotlinx.coroutines.flow.Flow

interface OnlineMealsRepository {
    suspend fun getMealCategories(): Resource<List<Category>>
    suspend fun getMeals(category: String): Flow<Resource<List<OnlineMeal>>>
    suspend fun getMealDetails(mealId: String): Resource<List<Meal>>
    suspend fun getRandomMeal(): Resource<List<Meal>>
}
