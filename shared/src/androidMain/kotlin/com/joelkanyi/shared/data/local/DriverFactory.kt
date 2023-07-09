package com.joelkanyi.shared.data.local

import android.content.Context
import com.joelkanyi.mealtime.data.local.sqldelight.MealTimeDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(MealTimeDatabase.Schema, context, "outage.db")
    }
}