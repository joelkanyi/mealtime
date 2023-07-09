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
package com.kanyideveloper.di

import com.kanyideveloper.core.domain.HomeRepository
import com.kanyideveloper.data.repository.HomeRepositoryImpl
import com.kanyideveloper.data.repository.OnlineMealsRepositoryImpl
import com.kanyideveloper.domain.repository.OnlineMealsRepository
import com.kanyideveloper.presentation.details.DetailsViewModel
import com.kanyideveloper.presentation.home.HomeViewModel
import com.kanyideveloper.presentation.home.onlinemeal.OnlineMealViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

fun homeModule() = module {

    single<HomeRepository> {
        HomeRepositoryImpl(
            get(),
            get(),
            get()
        )
    }

    single<OnlineMealsRepository> {
        OnlineMealsRepositoryImpl(
            mealDbApi = get(),
            mealTimeDatabase = get(),
            // onlineMealsDao = get()
        )
    }

    viewModel {
        HomeViewModel(
            homeRepository = get(),
            favoritesRepository = get(),
            subscriptionRepository = get(),
            analyticsUtil = get(),
        )
    }

    viewModel {
        OnlineMealViewModel(
            onlineMealsRepository = get(),
            favoritesRepository = get(),
            analyticsUtil = get(),
        )
    }

    viewModel {
        DetailsViewModel(
            onlineMealsRepository = get(),
            favoritesRepository = get(),
            analyticsUtil = get(),
        )
    }
}
