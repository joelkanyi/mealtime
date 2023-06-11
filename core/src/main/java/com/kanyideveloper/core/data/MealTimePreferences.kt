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
package com.kanyideveloper.core.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.kanyideveloper.core.model.MealPlanPreference
import com.kanyideveloper.core.util.Constants
import com.kanyideveloper.core.util.Constants.ALLERGIES
import com.kanyideveloper.core.util.Constants.DISH_TYPES
import com.kanyideveloper.core.util.Constants.NUMBER_OF_PEOPLE
import com.kanyideveloper.core.util.Constants.THEME_OPTIONS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(Constants.MEALTIME_PREFERENCES)

class MealTimePreferences(
    private val context: Context,
    private val databaseReference: DatabaseReference,
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun saveTheme(themeValue: Int) {
        context.dataStore.edit { preferences ->
            preferences[THEME_OPTIONS] = themeValue
        }
    }

    suspend fun saveMealPlanPreferences(
        allergies: List<String>,
        numberOfPeople: String,
        dishTypes: List<String>
    ) {
        context.dataStore.edit { preferences ->
            preferences[ALLERGIES] = allergies.toSet()
            preferences[NUMBER_OF_PEOPLE] = numberOfPeople
            preferences[DISH_TYPES] = dishTypes.toSet()
        }
    }

    val getTheme: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[THEME_OPTIONS] ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

    suspend fun mealPlanPreferences(isSubscribed: Boolean): Flow<MealPlanPreference?> {
        return if (isSubscribed) {
            fetchPrefsFromRemoteDatasource()
        } else {
            context.dataStore.data.map { preferences ->
                MealPlanPreference(
                    numberOfPeople = preferences[NUMBER_OF_PEOPLE] ?: "0",
                    dishTypes = preferences[DISH_TYPES]?.toList() ?: listOf(""),
                    allergies = preferences[ALLERGIES]?.toList() ?: listOf("")
                )
            }
        }
    }

    private suspend fun fetchPrefsFromRemoteDatasource(): Flow<MealPlanPreference> {
        /**
         * Do offline caching
         */
        val prefsFromLocal = context.dataStore.data.map { preferences ->
            MealPlanPreference(
                numberOfPeople = preferences[NUMBER_OF_PEOPLE] ?: "0",
                dishTypes = preferences[DISH_TYPES]?.toList() ?: listOf(""),
                allergies = preferences[ALLERGIES]?.toList() ?: listOf("")
            )
        }

        return try {
            val newPrefsFromRemote = withTimeoutOrNull(10000L) {
                // fetch from the remote database
                val result = databaseReference
                    .child("mealPlannerPreferences")
                    .child(firebaseAuth.currentUser?.uid.toString())
                    .get()
                    .await()

                result.getValue(MealPlanPreference::class.java)
            }

            return if (newPrefsFromRemote == null) {
                prefsFromLocal
            } else {
                flowOf(newPrefsFromRemote)
            }
        } catch (e: Exception) {
            prefsFromLocal
        }
    }
}
