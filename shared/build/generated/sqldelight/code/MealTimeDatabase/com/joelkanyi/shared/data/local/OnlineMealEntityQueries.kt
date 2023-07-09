package com.joelkanyi.shared.`data`.local

import com.squareup.sqldelight.Query
import com.squareup.sqldelight.Transacter
import kotlin.Any
import kotlin.String
import kotlin.Unit

public interface OnlineMealEntityQueries : Transacter {
  public fun <T : Any> getOnlineMealCategories(mapper: (
    idCategory: String,
    strCategory: String,
    strCategoryDescription: String,
    strCategoryThumb: String
  ) -> T): Query<T>

  public fun getOnlineMealCategories(): Query<OnlineMealCategoryEntity>

  public fun <T : Any> getOnlineMeals(category: String, mapper: (
    idMeal: String,
    strMeal: String,
    strMealThumb: String,
    strCategory: String
  ) -> T): Query<T>

  public fun getOnlineMeals(category: String): Query<OnlineMealEntity>

  public fun deleteOnlineMealCategories(): Unit

  public fun deleteOnlineMeals(category: String): Unit

  public fun insertOnlineMealCategories(
    idCategory: String,
    strCategory: String,
    strCategoryDescription: String,
    strCategoryThumb: String
  ): Unit

  public fun insertOnlineMeals(
    idMeal: String,
    strMeal: String,
    strMealThumb: String,
    strCategory: String
  ): Unit
}
