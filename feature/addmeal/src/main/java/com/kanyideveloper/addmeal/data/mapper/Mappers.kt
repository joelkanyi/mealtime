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
package com.kanyideveloper.addmeal.data.mapper

import com.joelkanyi.shared.data.local.MealEntity
import com.kanyideveloper.core.model.Meal
/*
import com.kanyideveloper.core_database.model.MealEntity
*/
import java.util.UUID

internal fun Meal.toMealEntity(): MealEntity {
    return MealEntity(
        id = id ?: UUID.randomUUID().toString(),
        name = name ?: "---",
        imageUrl = imageUrl ?: "",
        cookingTime = cookingTime,
        category = category ?: "",
        cookingDifficulty = cookingDifficulty,
        ingredients = ingredients,
        cookingInstructions = cookingDirections,
        isFavorite = favorite,
        servingPeople = servingPeople
    )
}
