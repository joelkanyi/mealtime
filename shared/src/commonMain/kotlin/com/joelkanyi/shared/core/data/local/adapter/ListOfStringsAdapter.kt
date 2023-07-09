package com.joelkanyi.shared.core.data.local.adapter

import com.squareup.sqldelight.ColumnAdapter

object ListOfStringsAdapter : ColumnAdapter<List<String>, String> {

    override fun decode(databaseValue: String): List<String> {
        if (databaseValue.isEmpty()) return emptyList()
        return databaseValue.split(',')
    }

    override fun encode(value: List<String>): String {
        return value.joinToString(separator = ",")
    }
}