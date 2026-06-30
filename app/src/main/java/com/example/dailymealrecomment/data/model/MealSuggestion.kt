package com.example.dailymealrecomment.data.model

data class MealSuggestion(
    val id: String,
    val name: String,
    val calories: Int,
    val isVegan: Boolean,
    val serving: String = "1 phần",
    val ingredients: List<String> = emptyList(),
    val recipeSteps: List<String> = emptyList(),
)
