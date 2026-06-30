package com.example.dailymealrecomment.data.model

import com.example.dailymealrecomment.R
import com.example.dailymealrecomment.model.FoodItem

data class MealSuggestion(
    val id: String,
    val name: String,
    val calories: Int,
    val isVegan: Boolean,
    val weightGrams: Int,
    val serving: String = "1 phần",
    val imageResId: Int = R.drawable.img_meal_chicken_rice_photo,
    val ingredients: List<String> = emptyList(),
    val recipeSteps: List<String> = emptyList(),
) {
    fun toFoodItem(): FoodItem {
        return FoodItem(
            name = name,
            weight = weightGrams,
            calories = calories,
            caloriesPerGram = if (weightGrams > 0) {
                calories.toDouble() / weightGrams
            } else {
                0.0
            },
        )
    }
}
