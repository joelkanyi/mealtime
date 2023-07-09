package com.joelkanyi.mealtime.`data`.local.sqldelight

import com.joelkanyi.mealtime.`data`.local.sqldelight.shared.newInstance
import com.joelkanyi.mealtime.`data`.local.sqldelight.shared.schema
import com.joelkanyi.shared.`data`.local.FavoriteEntityQueries
import com.joelkanyi.shared.`data`.local.OnlineMealEntityQueries
import com.squareup.sqldelight.Transacter
import com.squareup.sqldelight.db.SqlDriver

public interface MealTimeDatabase : Transacter {
  public val favoriteEntityQueries: FavoriteEntityQueries

  public val onlineMealEntityQueries: OnlineMealEntityQueries

  public companion object {
    public val Schema: SqlDriver.Schema
      get() = MealTimeDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): MealTimeDatabase =
        MealTimeDatabase::class.newInstance(driver)
  }
}
