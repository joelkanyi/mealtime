package com.joelkanyi.shared.data.local

import com.joelkanyi.mealtime.data.local.sqldelight.MealTimeDatabase
import com.joelkanyi.shared.data.local.adapter.ListOfStringsAdapter
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
    )
)