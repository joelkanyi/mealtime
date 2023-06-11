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

import com.joelkanyi.shared.data.network.MealDbApi
import com.joelkanyi.shared.data.network.utils.Resource
import com.joelkanyi.shared.data.network.utils.safeApiCall
import com.kanyideveloper.core.model.Meal
import com.kanyideveloper.core_database.dao.OnlineMealsDao
import com.kanyideveloper.data.mapper.toCategory
import com.kanyideveloper.data.mapper.toEntity
import com.kanyideveloper.data.mapper.toMeal
import com.kanyideveloper.domain.model.Category
import com.kanyideveloper.domain.model.OnlineMeal
import com.kanyideveloper.domain.repository.OnlineMealsRepository
import retrofit2.HttpException
import java.io.IOException

class OnlineMealsRepositoryImpl(
    private val mealDbApi: MealDbApi,
    private val onlineMealsDao: OnlineMealsDao,
) : OnlineMealsRepository {
    override suspend fun getMealCategories(): Resource<List<Category>> {
        val cachedCategories = onlineMealsDao.getOnlineMealCategories().map { it.toCategory() }
        return try {
            val response = mealDbApi.getCategories()
            onlineMealsDao.deleteOnlineMealCategories()
            onlineMealsDao.insertOnlineMealCategories(response.categories.map { it.toEntity() })
            Resource.Success(
                data = onlineMealsDao.getOnlineMealCategories().map { it.toCategory() }
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

    override suspend fun getMeals(category: String): Resource<List<OnlineMeal>> {
        val cachedMeals = onlineMealsDao.getOnlineMeals(category).map { it.toMeal() }
        return try {
            val response = mealDbApi.getMeals(category = category)
            onlineMealsDao.deleteOnlineMeals(category = category)
            onlineMealsDao.insertOnlineMeals(response.meals.map { it.toEntity(category = category) })
            Resource.Success(data = onlineMealsDao.getOnlineMeals(category).map { it.toMeal() })
        } catch (e: IOException) {
            return Resource.Error(
                "Couldn't reach the server. Check your internet connection",
                data = cachedMeals
            )
        } catch (e: HttpException) {
            return Resource.Error("Server error occurred", data = cachedMeals)
        } catch (e: Exception) {
            return Resource.Error("An unknown error occurred", data = cachedMeals)
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
