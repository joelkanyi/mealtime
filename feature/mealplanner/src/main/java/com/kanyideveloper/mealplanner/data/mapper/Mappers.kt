/*
 * Copyright 2023 Joel Kanyi.
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
package com.kanyideveloper.mealplanner.data.mapper

import com.kanyideveloper.core_database.model.MealPlanEntity
import com.kanyideveloper.mealplanner.model.MealPlan

internal fun MealPlan.toEntity(): MealPlanEntity {
    return MealPlanEntity(
        mealTypeName = mealTypeName,
        meals = meals,
        date = date
    )
}

internal fun MealPlanEntity.toMealPlan(): MealPlan {
    return MealPlan(
        mealTypeName = mealTypeName,
        meals = meals,
        date = date,
        id = id
    )
}
