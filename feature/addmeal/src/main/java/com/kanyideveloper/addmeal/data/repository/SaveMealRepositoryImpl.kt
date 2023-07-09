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
package com.kanyideveloper.addmeal.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.joelkanyi.mealtime.data.local.sqldelight.MealTimeDatabase
import com.joelkanyi.shared.data.network.utils.Resource
import com.kanyideveloper.addmeal.data.mapper.toMealEntity
import com.kanyideveloper.addmeal.domain.repository.SaveMealRepository
import com.kanyideveloper.core.model.Meal
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SaveMealRepositoryImpl(
    private val mealTimeDatabase: MealTimeDatabase,
    private val databaseReference: DatabaseReference,
    private val firebaseAuth: FirebaseAuth
) : SaveMealRepository {
    override suspend fun saveMeal(meal: Meal, isSubscribed: Boolean): Resource<Boolean> {
        return if (isSubscribed) {
            saveMealToRemoteDatasource(meal)
        } else {
            mealTimeDatabase.mealEntityQueries.insertMeal(
                name = meal.name,
                imageUrl = meal.imageUrl,
                cookingTime = meal.cookingTime,
                servingPeople = meal.servingPeople,
                category = meal.category,
                cookingDifficulty = meal.cookingDifficulty,
                ingredients = meal.ingredients,
                cookingInstructions = meal.cookingDirections,
                isFavorite = meal.favorite,
                id = meal.id ?: UUID.randomUUID().toString(),
            )
            Resource.Success(data = true)
        }
    }

    private suspend fun saveMealToRemoteDatasource(meal: Meal): Resource<Boolean> {
        return try {
            databaseReference
                .child("mymeals")
                .child(firebaseAuth.currentUser?.uid.toString())
                .child(UUID.randomUUID().toString())
                .setValue(meal).await()
            Resource.Success(data = true)
        } catch (e: Exception) {
            return Resource.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }
}
