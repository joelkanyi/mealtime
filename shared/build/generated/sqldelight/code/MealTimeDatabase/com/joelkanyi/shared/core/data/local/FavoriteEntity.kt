package com.joelkanyi.shared.core.`data`.local

import kotlin.Boolean
import kotlin.Long
import kotlin.String

public data class FavoriteEntity(
  public val id: Long,
  public val onlineMealId: String?,
  public val localMealId: String?,
  public val isOnline: Boolean,
  public val mealName: String,
  public val mealImageUrl: String,
  public val isFavorite: Boolean
) {
  public override fun toString(): String = """
  |FavoriteEntity [
  |  id: $id
  |  onlineMealId: $onlineMealId
  |  localMealId: $localMealId
  |  isOnline: $isOnline
  |  mealName: $mealName
  |  mealImageUrl: $mealImageUrl
  |  isFavorite: $isFavorite
  |]
  """.trimMargin()
}
