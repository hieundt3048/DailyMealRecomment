package com.example.dailymealrecomment.data.model

data class UserProfile(
    val heightCm: Double,
    val weightKg: Double,
    val age: Int,
    val isMale: Boolean,
    val goal: Goal,
    val dietType: DietType,
    val activityLevel: Double,
)
