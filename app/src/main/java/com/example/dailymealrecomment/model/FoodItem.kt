package com.example.dailymealrecomment.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FoodItem(
    var name: String,
    var weight: Int,
    var calories: Int,
    var protein: Double,
    var carbs: Double,
    var fat: Double
) : Parcelable