package com.joelkanyi.shared.`data`.local

import com.squareup.sqldelight.Query
import com.squareup.sqldelight.Transacter
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlin.collections.List

public interface MealEntityQueries : Transacter {
  public fun <T : Any> getAllMeals(mapper: (
    name: String?,
    imageUrl: String?,
    cookingTime: Int?,
    servingPeople: Int?,
    category: String?,
    cookingDifficulty: String?,
    ingredients: List<String>?,
    cookingInstructions: List<String>?,
    isFavorite: Boolean,
    id: String
  ) -> T): Query<T>

  public fun getAllMeals(): Query<MealEntity>

  public fun <T : Any> getSingleMeal(id: String, mapper: (
    name: String?,
    imageUrl: String?,
    cookingTime: Int?,
    servingPeople: Int?,
    category: String?,
    cookingDifficulty: String?,
    ingredients: List<String>?,
    cookingInstructions: List<String>?,
    isFavorite: Boolean,
    id: String
  ) -> T): Query<T>

  public fun getSingleMeal(id: String): Query<MealEntity>

  public fun insertMeal(
    name: String?,
    imageUrl: String?,
    cookingTime: Int?,
    servingPeople: Int?,
    category: String?,
    cookingDifficulty: String?,
    ingredients: List<String>?,
    cookingInstructions: List<String>?,
    isFavorite: Boolean,
    id: String
  ): Unit

  public fun deleteMeal(id: String): Unit

  public fun deleteMealById(id: String): Unit

  public fun deleteAllMeals(): Unit
}
