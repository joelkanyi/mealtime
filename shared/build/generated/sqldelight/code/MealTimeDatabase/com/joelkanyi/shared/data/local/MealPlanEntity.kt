package com.joelkanyi.shared.`data`.local

import com.joelkanyi.shared.domain.CoreMeal
import com.squareup.sqldelight.ColumnAdapter
import kotlin.String
import kotlin.collections.List

public data class MealPlanEntity(
  public val mealTypeName: String,
  public val meals: List<CoreMeal>?,
  public val mealDate: String,
  public val id: String
) {
  public override fun toString(): String = """
  |MealPlanEntity [
  |  mealTypeName: $mealTypeName
  |  meals: $meals
  |  mealDate: $mealDate
  |  id: $id
  |]
  """.trimMargin()

  public class Adapter(
    public val mealsAdapter: ColumnAdapter<List<CoreMeal>, String>
  )
}
