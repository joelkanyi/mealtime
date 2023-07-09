package com.joelkanyi.shared.core.data.local

import com.joelkanyi.mealtime.data.local.sqldelight.MealTimeDatabase
import com.joelkanyi.shared.core.data.local.adapter.ListOfStringsAdapter
import com.joelkanyi.shared.core.data.local.adapter.listOfMealsAdapter
import com.squareup.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(
    driverFactory: DriverFactory
) = MealTimeDatabase(
    driverFactory.createDriver(),
    mealEntityAdapter = MealEntity.Adapter(
        ingredientsAdapter = ListOfStringsAdapter,
        cookingInstructionsAdapter = ListOfStringsAdapter
    ),
    mealPlanEntityAdapter = MealPlanEntity.Adapter(
        mealsAdapter = listOfMealsAdapter,
    )
)