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
package com.kanyideveloper.data.repository

import coil.network.HttpException
import com.joelkanyi.mealtime.data.local.sqldelight.MealTimeDatabase
import com.joelkanyi.shared.core.data.network.MealDbApi
import com.joelkanyi.shared.core.data.network.utils.Resource
import com.joelkanyi.shared.core.data.network.utils.safeApiCall
import com.kanyideveloper.core.model.Meal
/*
import com.kanyideveloper.core_database.dao.OnlineMealsDao
*/
import com.kanyideveloper.data.mapper.toCategory
import com.kanyideveloper.data.mapper.toMeal
import com.kanyideveloper.domain.model.Category
import com.kanyideveloper.domain.model.OnlineMeal
import com.kanyideveloper.domain.repository.OnlineMealsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class OnlineMealsRepositoryImpl(
    private val mealDbApi: MealDbApi,
    mealTimeDatabase: MealTimeDatabase,
) : OnlineMealsRepository {
    private val onlineMealsDao = mealTimeDatabase.onlineMealEntityQueries
    override suspend fun getMealCategories(): Resource<List<Category>> {
        val cachedCategories =
            onlineMealsDao.getOnlineMealCategories().executeAsList().map { it.toCategory() }
        return try {
            val response = mealDbApi.getCategories()
            onlineMealsDao.deleteOnlineMealCategories()
            response.categories.forEach { category ->
                onlineMealsDao.insertOnlineMealCategories(
                    idCategory = category.idCategory,
                    strCategory = category.strCategory,
                    strCategoryDescription = category.strCategoryDescription,
                    strCategoryThumb = category.strCategoryThumb
                )
            }
            Resource.Success(
                data = onlineMealsDao.getOnlineMealCategories().executeAsList()
                    .map { it.toCategory() }
            )
        } catch (e: IOException) {
            return Resource.Error(
                "Couldn't reach the server. Check your internet connection",
                data = cachedCategories
            )
        } catch (e: HttpException) {
            return Resource.Error("Server error occurred", data = cachedCategories)
        } catch (e: Exception) {
            return Resource.Error("An unknown error occurred", data = cachedCategories)
        }
    }

    override suspend fun getMeals(category: String): Flow<Resource<List<OnlineMeal>>> = flow {
        val cachedMeals =
            onlineMealsDao.getOnlineMeals(category).executeAsList().map { it.toMeal() }
        try {
            val response = mealDbApi.getMeals(category = category)
            onlineMealsDao.deleteOnlineMeals(category = category)
            response.meals.forEach { meal ->
                onlineMealsDao.insertOnlineMeals(
                    idMeal = meal.idMeal,
                    strMeal = meal.strMeal,
                    strMealThumb = meal.strMealThumb,
                    strCategory = category,
                )
            }
            emit(
                Resource.Success(
                    data = onlineMealsDao.getOnlineMeals(category).executeAsList()
                        .map { it.toMeal() })
            )
        } catch (e: IOException) {
            emit(
                Resource.Error(
                    "Couldn't reach the server. Check your internet connection",
                    data = cachedMeals
                )
            )
        } catch (e: HttpException) {
            emit(
                Resource.Error("Server error occurred", data = cachedMeals)
            )
        } catch (e: Exception) {
            emit(
                Resource.Error("An unknown error occurred", data = cachedMeals)
            )
        }
    }

    override suspend fun getMealDetails(mealId: String): Resource<List<Meal>> {
        return safeApiCall {
            val response = mealDbApi.getMealDetails(mealId = mealId)
            response.meals?.map { it.toMeal() } ?: emptyList()
        }
    }

    override suspend fun getRandomMeal(): Resource<List<Meal>> {
        return safeApiCall {
            val response = mealDbApi.getRandomMeal()
            response.meals?.map { it.toMeal() } ?: emptyList()
        }
    }
}
