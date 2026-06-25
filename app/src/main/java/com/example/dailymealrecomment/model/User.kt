package com.example.dailymealrecomment.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val heightCm: Double = 0.0,
    val weightKg: Double = 0.0,
    val age: Int = 0,
    val isMale: Boolean = true,
    val activityLevel: Double = 1.2,
    val goal: String = "MAINTAIN_WEIGHT",
    val dietType: String = "NORMAL",
    val dailyCalorieTarget: Int = 0,
)
