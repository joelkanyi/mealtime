package com.joelkanyi.shared.core.data.local.adapter

import com.joelkanyi.shared.core.domain.CoreMeal
import com.squareup.sqldelight.ColumnAdapter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val listOfMealsAdapter = object : ColumnAdapter<List<CoreMeal>, String> {
    override fun decode(databaseValue: String): List<CoreMeal> =
        Json.decodeFromString(databaseValue)

    override fun encode(value: List<CoreMeal>): String = Json.encodeToString(value)
}