/*
 * Copyright 2022 Joel Kanyi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kanyideveloper.favorites.presentation.favorites.data.mapper

import com.joelkanyi.shared.core.data.local.FavoriteEntity
import com.kanyideveloper.core.model.Favorite

internal fun FavoriteEntity.toFavorite(): Favorite {
    return Favorite(
        id = id.toInt(),
        onlineMealId = onlineMealId,
        localMealId = localMealId,
        online = isOnline,
        mealName = mealName,
        mealImageUrl = mealImageUrl,
        favorite = isFavorite
    )
}

internal fun Favorite.toEntity(): FavoriteEntity {
    return FavoriteEntity(
        id = id?.toLong() ?: 0,
        onlineMealId = onlineMealId,
        localMealId = localMealId,
        isOnline = online,
        mealName = mealName,
        mealImageUrl = mealImageUrl,
        isFavorite = favorite,
    )
}
