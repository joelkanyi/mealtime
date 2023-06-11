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
package com.joelkanyi.shared.data.network

import com.joelkanyi.shared.data.network.model.CategoriesResponse
import com.joelkanyi.shared.data.network.model.IngredientsResponse
import com.joelkanyi.shared.data.network.model.MealDetailsResponse
import com.joelkanyi.shared.data.network.model.MealsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class MealDbApi(
    private val httpClient: HttpClient,
) {
    suspend fun getCategories(): CategoriesResponse {
        return httpClient.get("categories.php").body()
    }

    suspend fun getMeals(category: String = "Beef"): MealsResponse {
        return httpClient.get("filter.php") {
            parameter("c", category)
        }.body()
    }

    suspend fun getMealDetails(mealId: String): MealDetailsResponse {
        return httpClient.get("lookup.php") {
            parameter("i", mealId)
        }.body()
    }

    suspend fun searchMealsByName(query: String): MealsResponse? {
        return httpClient.get("search.php") {
            parameter("s", query)
        }.body()
    }

    suspend fun searchMealsByIngredient(query: String): MealsResponse? {
        return httpClient.get("filter.php") {
            parameter("i", query)
        }.body()
    }

    suspend fun searchMealsByCategory(query: String): MealsResponse? {
        return httpClient.get("filter.php") {
            parameter("c", query)
        }.body()
    }

    suspend fun getRandomMeal(): MealDetailsResponse {
        return httpClient.get("random.php").body()
    }

    suspend fun getAllIngredients(query: String = "list"): IngredientsResponse {
        return httpClient.get("list.php") {
            parameter("i", query)
        }.body()
    }
}
