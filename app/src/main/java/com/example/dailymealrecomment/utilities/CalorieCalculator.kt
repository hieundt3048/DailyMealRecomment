package com.example.dailymealrecomment.utilities

import com.example.dailymealrecomment.data.model.Goal
import com.example.dailymealrecomment.data.model.UserProfile
import kotlin.math.roundToInt

object CalorieCalculator {
    fun calculateDailyCalorieTarget(profile: UserProfile): Int {
        val bmr = if (profile.isMale) {
            (10 * profile.weightKg) + (6.25 * profile.heightCm) - (5 * profile.age) + 5
        } else {
            (10 * profile.weightKg) + (6.25 * profile.heightCm) - (5 * profile.age) - 161
        }
        val tdee = bmr * profile.activityLevel
        val adjustedCalories = when (profile.goal) {
            Goal.LOSE_WEIGHT -> tdee - 500
            Goal.GAIN_WEIGHT -> tdee + 500
            Goal.MAINTAIN_WEIGHT -> tdee
        }
        return adjustedCalories.roundToInt().coerceAtLeast(1200)
    }
}
