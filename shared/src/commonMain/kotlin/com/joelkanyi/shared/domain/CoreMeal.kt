package com.joelkanyi.shared.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoreMeal(
    @SerialName("id")
    val id: String? = null,
    @SerialName("name")
    val name: String? = "",
    @SerialName("imageUrl")
    val imageUrl: String? = "",
    @SerialName("cookingTime")
    val cookingTime: Int = -1,
    @SerialName("servingPeople")
    val servingPeople: Int = -1,
    @SerialName("category")
    val category: String? = "",
    @SerialName("cookingDifficulty")
    val cookingDifficulty: String = "",
    @SerialName("ingredients")
    val ingredients: List<String> = emptyList(),
    @SerialName("cookingDirections")
    val cookingDirections: List<String> = emptyList(),
    @SerialName("favorite")
    val favorite: Boolean = false,
    @SerialName("onlineMealId")
    val onlineMealId: String? = null,
    @SerialName("localMealId")
    val localMealId: String? = null,
    @SerialName("mealPlanId")
    val mealPlanId: String? = null
)