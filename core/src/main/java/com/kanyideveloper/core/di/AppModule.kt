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
package com.kanyideveloper.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.kanyideveloper.core.data.SubscriptionRepositoryImpl
import com.kanyideveloper.core.domain.SubscriptionRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

fun appModule() = module {

    single { FirebaseDatabase.getInstance().reference }

    single { FirebaseAuth.getInstance() }

    single { Gson() }

    single<SubscriptionRepository> {
        SubscriptionRepositoryImpl(
            context = androidApplication().applicationContext,
        )
    }
}
