package com.example.dailymealrecomment.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FoodItem(
    var name: String = "",
    var weight: Int = 0,
    var calories: Int = 0,
    var protein: Double = 0.0,
    var carbs: Double = 0.0,
    var fat: Double = 0.0
) : Parcelable
