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
package com.kanyideveloper.settings.di

import com.kanyideveloper.core.data.MealTimePreferences
import com.kanyideveloper.core.domain.UserDataRepository
import com.kanyideveloper.settings.data.UserDataRepositoryImpl
import com.kanyideveloper.settings.presentation.SettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

fun userDataModule() = module {

    single {
        MealTimePreferences(
            context = androidApplication().applicationContext,
            databaseReference = get(),
            firebaseAuth = get()
        )
    }

    single<UserDataRepository> {
        UserDataRepositoryImpl(
            mealTimePreferences = get()
        )
    }

    viewModel {
        SettingsViewModel(
            userDataRepository = get(),
            subscriptionRepository = get(),
            analyticsUtil = get(),
        )
    }
}
