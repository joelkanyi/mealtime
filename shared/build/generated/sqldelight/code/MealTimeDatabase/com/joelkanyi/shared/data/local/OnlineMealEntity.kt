package com.joelkanyi.shared.`data`.local

import kotlin.String

public data class OnlineMealEntity(
  public val idMeal: String,
  public val strMeal: String,
  public val strMealThumb: String,
  public val strCategory: String
) {
  public override fun toString(): String = """
  |OnlineMealEntity [
  |  idMeal: $idMeal
  |  strMeal: $strMeal
  |  strMealThumb: $strMealThumb
  |  strCategory: $strCategory
  |]
  """.trimMargin()
}
