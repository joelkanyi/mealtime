package com.joelkanyi.mealtime.`data`.local.sqldelight

import com.joelkanyi.mealtime.`data`.local.sqldelight.shared.newInstance
import com.joelkanyi.mealtime.`data`.local.sqldelight.shared.schema
import com.joelkanyi.shared.`data`.local.FavoriteEntityQueries
import com.joelkanyi.shared.`data`.local.MealEntity
import com.joelkanyi.shared.`data`.local.MealEntityQueries
import com.joelkanyi.shared.`data`.local.MealPlanEntity
import com.joelkanyi.shared.`data`.local.MealPlanEntityQueries
import com.joelkanyi.shared.`data`.local.OnlineMealEntityQueries
import com.squareup.sqldelight.Transacter
import com.squareup.sqldelight.db.SqlDriver

public interface MealTimeDatabase : Transacter {
  public val favoriteEntityQueries: FavoriteEntityQueries

  public val mealEntityQueries: MealEntityQueries

  public val mealPlanEntityQueries: MealPlanEntityQueries

  public val onlineMealEntityQueries: OnlineMealEntityQueries

  public companion object {
    public val Schema: SqlDriver.Schema
      get() = MealTimeDatabase::class.schema

    public operator fun invoke(
      driver: SqlDriver,
      mealEntityAdapter: MealEntity.Adapter,
      mealPlanEntityAdapter: MealPlanEntity.Adapter
    ): MealTimeDatabase = MealTimeDatabase::class.newInstance(driver, mealEntityAdapter,
        mealPlanEntityAdapter)
  }
}
