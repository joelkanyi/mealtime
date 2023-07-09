package com.joelkanyi.shared.data.local

import com.joelkanyi.mealtime.data.local.sqldelight.MealTimeDatabase
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(MealTimeDatabase.Schema, "outage.db")
    }
}