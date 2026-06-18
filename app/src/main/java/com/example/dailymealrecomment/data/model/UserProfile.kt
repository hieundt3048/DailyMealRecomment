package com.example.btl.data.model

data class UserProfile(
    val heightCm: Double,
    val weightKg: Double,
    val age: Int,
    val isMale: Boolean,
    val goal: Goal,
    val dietType: DietType,
    val activityLevel: Double // Hệ số vận động (VD: 1.2 cho ít vận động, 1.55 cho vận động vừa)
)