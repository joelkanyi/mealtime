package com.joelkanyi.shared.`data`.local

import com.squareup.sqldelight.Query
import com.squareup.sqldelight.Transacter
import kotlin.Any
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.Unit

public interface FavoriteEntityQueries : Transacter {
  public fun <T : Any> getFavorites(mapper: (
    id: Long,
    onlineMealId: String?,
    localMealId: String?,
    isOnline: Boolean,
    mealName: String,
    mealImageUrl: String,
    isFavorite: Boolean
  ) -> T): Query<T>

  public fun getFavorites(): Query<FavoriteEntity>

  public fun <T : Any> getAFavoriteById(id: Long, mapper: (
    id: Long,
    onlineMealId: String?,
    localMealId: String?,
    isOnline: Boolean,
    mealName: String,
    mealImageUrl: String,
    isFavorite: Boolean
  ) -> T): Query<T>

  public fun getAFavoriteById(id: Long): Query<FavoriteEntity>

  public fun localInFavorites(localMealId: String?): Query<Boolean>

  public fun onlineInFavorites(onlineMealId: String?): Query<Boolean>

  public fun insertAFavorite(
    id: Long?,
    onlineMealId: String?,
    localMealId: String?,
    isOnline: Boolean,
    mealName: String,
    mealImageUrl: String,
    isFavorite: Boolean
  ): Unit

  public fun deleteAFavorite(id: Long): Unit

  public fun deleteALocalFavorite(localMealId: String?): Unit

  public fun deleteAnOnlineFavorite(onlineMealId: String?): Unit

  public fun deleteAllFavorites(): Unit
}
