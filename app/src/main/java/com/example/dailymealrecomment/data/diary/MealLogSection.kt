package com.example.dailymealrecomment.data.diary

data class MealLogSection(
    val mealType: MealType,
    val entries: List<MealLogEntry>,
) {
    val totalCalories: Int = entries.sumOf { it.calories }
    val totalWeight: Int = entries.sumOf { it.weight }
}
