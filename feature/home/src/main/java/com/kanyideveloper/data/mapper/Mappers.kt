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
package com.kanyideveloper.data.mapper

import com.joelkanyi.shared.data.local.MealEntity
import com.joelkanyi.shared.data.local.OnlineMealCategoryEntity
import com.joelkanyi.shared.data.local.OnlineMealEntity
import com.joelkanyi.shared.data.network.model.CategoriesResponse
import com.joelkanyi.shared.data.network.model.MealDetailsResponse
import com.joelkanyi.shared.data.network.model.MealsResponse
import com.kanyideveloper.core.model.Meal
import com.kanyideveloper.core.util.stringToList
/*
import com.kanyideveloper.core_database.model.MealEntity
*/
/*import com.kanyideveloper.core_database.model.OnlineMealCategoryEntity
import com.kanyideveloper.core_database.model.OnlineMealEntity*/
import com.kanyideveloper.domain.model.Category
import com.kanyideveloper.domain.model.OnlineMeal

internal fun MealEntity.toMeal(): Meal {
    return Meal(
        name = name,
        imageUrl = imageUrl,
        cookingTime = cookingTime ?: 0,
        category = category,
        cookingDifficulty = cookingDifficulty ?: "",
        ingredients = ingredients ?: emptyList(),
        cookingDirections = cookingInstructions ?: emptyList(),
        favorite = isFavorite,
        servingPeople = servingPeople ?: 0,
        localMealId = id
    )
}

internal fun com.joelkanyi.shared.data.network.model.CategoriesResponse.Category.toCategory(): Category {
    return Category(
        categoryId = idCategory,
        categoryName = strCategory,
        categoryDescription = strCategoryDescription,
        categoryImageUrl = strCategoryThumb
    )
}

internal fun com.joelkanyi.shared.data.network.model.MealsResponse.Meal.toMeal(): OnlineMeal {
    return OnlineMeal(
        name = strMeal,
        imageUrl = strMealThumb,
        mealId = idMeal
    )
}

internal fun com.joelkanyi.shared.data.network.model.CategoriesResponse.Category.toEntity() =
    OnlineMealCategoryEntity(
        idCategory = idCategory,
        strCategory = strCategory,
        strCategoryDescription = strCategoryDescription,
        strCategoryThumb = strCategoryThumb
    )

internal fun OnlineMealCategoryEntity.toCategory() = Category(
    categoryId = idCategory,
    categoryName = strCategory,
    categoryDescription = strCategoryDescription,
    categoryImageUrl = strCategoryThumb
)

internal fun com.joelkanyi.shared.data.network.model.MealsResponse.Meal.toEntity(category: String) =
    OnlineMealEntity(
        idMeal = idMeal,
        strMeal = strMeal,
        strMealThumb = strMealThumb,
        strCategory = category
    )

internal fun OnlineMealEntity.toMeal() = OnlineMeal(
    name = strMeal,
    imageUrl = strMealThumb,
    mealId = idMeal
)

internal fun MealDetailsResponse.Meal.toMeal(): Meal {
    return Meal(
        name = strMeal,
        imageUrl = strMealThumb,
        cookingTime = 0,
        category = strCategory,
        cookingDifficulty = "",
        ingredients = listOf(
            strIngredient1 ?: "",
            strIngredient2 ?: "",
            strIngredient3 ?: "",
            strIngredient4 ?: "",
            strIngredient5 ?: "",
            strIngredient6 ?: "",
            strIngredient7 ?: "",
            strIngredient8 ?: "",
            strIngredient9 ?: "",
            strIngredient10 ?: "",
            strIngredient11 ?: "",
            strIngredient12 ?: "",
            strIngredient13 ?: "",
            strIngredient14 ?: "",
            strIngredient15 ?: "",
            strIngredient16 ?: "",
            strIngredient17 ?: "",
            strIngredient18 ?: "",
            strIngredient19 ?: "",
            strIngredient20 ?: "",
        ).filter { it.isNotEmpty() },
        cookingDirections = strInstructions?.stringToList() ?: emptyList(),
        favorite = false,
        servingPeople = 0,
        onlineMealId = idMeal
    )
}
