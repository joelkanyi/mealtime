package com.joelkanyi.shared.`data`.local

import com.squareup.sqldelight.ColumnAdapter
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List

public data class MealEntity(
  public val name: String?,
  public val imageUrl: String?,
  public val cookingTime: Int?,
  public val servingPeople: Int?,
  public val category: String?,
  public val cookingDifficulty: String?,
  public val ingredients: List<String>?,
  public val cookingInstructions: List<String>?,
  public val isFavorite: Boolean,
  public val id: String
) {
  public override fun toString(): String = """
  |MealEntity [
  |  name: $name
  |  imageUrl: $imageUrl
  |  cookingTime: $cookingTime
  |  servingPeople: $servingPeople
  |  category: $category
  |  cookingDifficulty: $cookingDifficulty
  |  ingredients: $ingredients
  |  cookingInstructions: $cookingInstructions
  |  isFavorite: $isFavorite
  |  id: $id
  |]
  """.trimMargin()

  public class Adapter(
    public val ingredientsAdapter: ColumnAdapter<List<String>, String>,
    public val cookingInstructionsAdapter: ColumnAdapter<List<String>, String>
  )
}
