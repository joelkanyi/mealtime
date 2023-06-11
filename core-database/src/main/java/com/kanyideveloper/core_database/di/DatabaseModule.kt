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
package com.kanyideveloper.core_database.di

import androidx.room.Room
import com.kanyideveloper.core.util.Constants
import com.kanyideveloper.core_database.DatabaseMigrations.migration_1_3
import com.kanyideveloper.core_database.MealTimeDatabase
import org.koin.dsl.module

fun databaseModule() = module {

    single { com.kanyideveloper.core_database.converters.Converters(get()) }

    single {
        Room.databaseBuilder(get(), MealTimeDatabase::class.java, Constants.MEALTIME_DATABASE)
            .addMigrations(migration_1_3)
            .fallbackToDestructiveMigration()
            .addTypeConverter(get<com.kanyideveloper.core_database.converters.Converters>())
            .build()
    }

    single { get<MealTimeDatabase>().mealDao }

    single { get<MealTimeDatabase>().favoritesDao }

    single { get<MealTimeDatabase>().mealPlanDao }

    single { get<MealTimeDatabase>().onlineMealsDao }
}
