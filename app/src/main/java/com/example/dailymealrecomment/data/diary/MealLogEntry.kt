package com.example.dailymealrecomment.data.diary

data class MealLogEntry(
    val id: Int? = null,
    val name: String,
    val weight: Int,
    val calories: Int,
    val mealType: MealType,
    val dateKey: String,
    val sourceImageUri: String?,
    val createdAtMillis: Long,
)
