package com.example.dailymealrecomment.model

data class FoodItem(
    var name: String = "",
    var weight: Int = 0,
    var calories: Int = 0,
    var caloriesPerGram: Double = if (weight > 0) calories.toDouble() / weight else 0.0,
)
