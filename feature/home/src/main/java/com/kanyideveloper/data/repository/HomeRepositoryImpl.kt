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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.joelkanyi.mealtime.data.local.sqldelight.MealTimeDatabase
import com.joelkanyi.shared.data.network.utils.Resource
import com.kanyideveloper.core.domain.HomeRepository
import com.kanyideveloper.core.model.Meal
/*import com.kanyideveloper.core_database.dao.MealDao
import com.kanyideveloper.core_database.model.MealEntity*/
import com.kanyideveloper.data.mapper.toMeal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

class HomeRepositoryImpl(
    // private val mealDao: MealDao,
    mealTimeDatabase: MealTimeDatabase,
    private val databaseReference: DatabaseReference,
    private val firebaseAuth: FirebaseAuth
) : HomeRepository {
    private val mealDao = mealTimeDatabase.mealEntityQueries
    override suspend fun getMyMeals(isSubscribed: Boolean): Resource<Flow<List<Meal>>> {
        return if (isSubscribed) {
            /**
             * Do offline caching
             */
            // first read from the local database
            val myMeals = mealDao.getAllMeals().executeAsList()

            try {
                val newMyMeals = withTimeoutOrNull(10000L) {
                    // fetch from the remote database
                    val myMealsRemote: MutableList<Meal> = mutableListOf()
                    val auctions = databaseReference
                        .child("mymeals")
                        .child(firebaseAuth.currentUser?.uid.toString())
                    val auctionsListFromDb = auctions.get().await()
                    for (i in auctionsListFromDb.children) {
                        val result = i.getValue(Meal::class.java)
                        myMealsRemote.add(result!!)
                    }

                    // clear the local database
                    mealDao.deleteAllMeals()

                    // save the remote data to the local database
                    myMealsRemote.forEach { onlineMeal ->
                        mealDao.insertMeal(
                            id = onlineMeal.id ?: UUID.randomUUID().toString(),
                            name = onlineMeal.name ?: "---",
                            imageUrl = onlineMeal.imageUrl ?: "",
                            cookingTime = onlineMeal.cookingTime,
                            category = onlineMeal.category ?: "",
                            cookingDifficulty = onlineMeal.cookingDifficulty,
                            ingredients = onlineMeal.ingredients,
                            cookingInstructions = onlineMeal.cookingDirections,
                            isFavorite = onlineMeal.favorite,
                            servingPeople = onlineMeal.servingPeople
                        )
                    }

                    // read from the local database
                    mealDao.getAllMeals().executeAsList().map { mealEntity ->
                        mealEntity.toMeal()
                    }
                }

                if (newMyMeals == null) {
                    Resource.Error(
                        "Viewing offline data",
                        data = flowOf(
                            myMeals.map { mealEntity ->
                                mealEntity.toMeal()
                            }
                        )
                    )
                } else {
                    Resource.Success(data = flowOf(newMyMeals))
                }
            } catch (e: Exception) {
                Resource.Error(
                    e.localizedMessage ?: "Unknown error occurred",
                    data = flowOf(
                        myMeals.map { mealEntity ->
                            mealEntity.toMeal()
                        }
                    )
                )
            }
        } else {
            Resource.Success(
                data = flowOf(
                    mealDao.getAllMeals().executeAsList().map { mealEntity ->
                        mealEntity.toMeal()
                    }
                )
            )
        }
    }

    override fun getMealById(id: String): LiveData<Meal?> {
        val mutableLiveData = MutableLiveData<Meal?>(null)
        mutableLiveData.postValue(
            mealDao.getSingleMeal(id = id).executeAsOneOrNull()?.toMeal()
        )
        return mutableLiveData
    }
}
