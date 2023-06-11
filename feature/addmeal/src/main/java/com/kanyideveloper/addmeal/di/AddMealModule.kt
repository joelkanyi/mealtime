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
package com.kanyideveloper.addmeal.di

import com.google.firebase.storage.FirebaseStorage
import com.kanyideveloper.addmeal.data.repository.SaveMealRepositoryImpl
import com.kanyideveloper.addmeal.data.repository.UploadImageRepositoryImpl
import com.kanyideveloper.addmeal.domain.repository.SaveMealRepository
import com.kanyideveloper.addmeal.domain.repository.UploadImageRepository
import com.kanyideveloper.addmeal.presentation.addmeal.AddMealsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

fun addMealModule() = module {

    single<UploadImageRepository> { UploadImageRepositoryImpl(get(), get()) }

    single<SaveMealRepository> { SaveMealRepositoryImpl(get(), get(), get()) }

    single { FirebaseStorage.getInstance().getReference("meal_images") }

    viewModel {
        AddMealsViewModel(
            uploadImageRepository = get(),
            saveMealRepository = get(),
            analyticsUtil = get(),
            subscriptionRepository = get()
        )
    }
}
