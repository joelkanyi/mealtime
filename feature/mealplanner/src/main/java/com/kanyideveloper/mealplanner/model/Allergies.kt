package com.kanyideveloper.mealplanner.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Allergies(
    val allergicTo: List<String>,
) : Parcelable