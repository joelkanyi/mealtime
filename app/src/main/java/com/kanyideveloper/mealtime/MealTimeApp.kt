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
package com.kanyideveloper.mealtime

import android.app.Application
import com.joelkanyi.auth.di.authModule
import com.joelkanyi.kitchen_timer.di.kitchenTimerModule
import com.joelkanyi.shared.di.initKoin
import com.kanyideveloper.addmeal.di.addMealModule
import com.kanyideveloper.core.di.analyticsModule
import com.kanyideveloper.core.di.appModule
import com.kanyideveloper.core.util.Constants.QONVERSION_PROJECT_KEY
import com.kanyideveloper.core_database.di.databaseModule
import com.kanyideveloper.di.homeModule
import com.kanyideveloper.favorites.presentation.favorites.di.favoritesModule
import com.kanyideveloper.mealplanner.di.mealPlannerModule
import com.kanyideveloper.mealtime.di.androidModule
import com.kanyideveloper.search.di.searchModule
import com.kanyideveloper.settings.di.userDataModule
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.QonversionConfig
import com.qonversion.android.sdk.dto.QLaunchMode
import org.koin.android.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level
import timber.log.Timber

class MealTimeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        setupTimber()

        // Initialize Qonversion
        val qonversionConfig = QonversionConfig.Builder(
            this,
            QONVERSION_PROJECT_KEY,
            QLaunchMode.SubscriptionManagement
        ).build()
        Qonversion.initialize(qonversionConfig)

        val androidModules = listOf(
            androidModule(),
            appModule(),
            addMealModule(),
            analyticsModule(),
            authModule(),
            databaseModule(),
            favoritesModule(),
            homeModule(),
            kitchenTimerModule(),
            mealPlannerModule(),
            searchModule(),
            userDataModule(),
        )
        initKoin {
            androidLogger(level = if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(androidContext = this@MealTimeApp)
            modules(androidModules)
        }
    }
}

private fun setupTimber() {
    Timber.plant(Timber.DebugTree())
}
