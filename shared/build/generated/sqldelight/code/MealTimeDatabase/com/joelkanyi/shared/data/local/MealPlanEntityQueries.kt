package com.joelkanyi.shared.`data`.local

import com.joelkanyi.shared.domain.CoreMeal
import com.squareup.sqldelight.Query
import com.squareup.sqldelight.Transacter
import kotlin.Any
import kotlin.String
import kotlin.Unit
import kotlin.collections.List

public interface MealPlanEntityQueries : Transacter {
  public fun <T : Any> getPlanMeals(mealDate: String, mapper: (
    mealTypeName: String,
    meals: List<CoreMeal>?,
    mealDate: String,
    id: String
  ) -> T): Query<T>

  public fun getPlanMeals(mealDate: String): Query<MealPlanEntity>

  public fun insertMealPlan(
    mealTypeName: String,
    meals: List<CoreMeal>?,
    mealDate: String,
    id: String
  ): Unit

  public fun deleteAMealFromPlan(id: String): Unit

  public fun removeMealFromPlan(id: String): Unit

  public fun deleteAllMealsFromPlan(): Unit
}
