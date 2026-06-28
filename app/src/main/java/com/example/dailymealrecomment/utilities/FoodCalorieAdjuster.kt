package com.example.dailymealrecomment.utilities

import kotlin.math.roundToInt

object FoodCalorieAdjuster {
    const val MIN_WEIGHT_GRAMS = 1
    const val MAX_WEIGHT_GRAMS = 5_000
    const val MIN_CALORIES = 1
    const val MAX_CALORIES = 10_000

    fun caloriesPerGram(weight: Int, calories: Int): Double {
        return if (weight > 0 && calories > 0) calories.toDouble() / weight else 0.0
    }

    fun recalculateCalories(weightText: String, caloriesPerGram: Double): WeightAdjustmentResult {
        val weight = weightText.trim().toIntOrNull()
            ?: return WeightAdjustmentResult.Invalid(WeightValidationError.NOT_A_NUMBER)

        return when {
            weight < MIN_WEIGHT_GRAMS -> WeightAdjustmentResult.Invalid(WeightValidationError.TOO_SMALL)
            weight > MAX_WEIGHT_GRAMS -> WeightAdjustmentResult.Invalid(WeightValidationError.TOO_LARGE)
            caloriesPerGram <= 0.0 -> WeightAdjustmentResult.Invalid(WeightValidationError.MISSING_CALORIE_RATIO)
            else -> {
                val recalculatedCalories = (weight * caloriesPerGram)
                    .roundToInt()
                    .coerceIn(MIN_CALORIES, MAX_CALORIES)
                WeightAdjustmentResult.Valid(weight, recalculatedCalories)
            }
        }
    }

    fun validateManualCalories(calorieText: String): CalorieValidationResult {
        val calories = calorieText.trim().toIntOrNull()
            ?: return CalorieValidationResult.Invalid(CalorieValidationError.NOT_A_NUMBER)

        return when {
            calories < MIN_CALORIES -> CalorieValidationResult.Invalid(CalorieValidationError.TOO_SMALL)
            calories > MAX_CALORIES -> CalorieValidationResult.Invalid(CalorieValidationError.TOO_LARGE)
            else -> CalorieValidationResult.Valid(calories)
        }
    }
}

sealed class WeightAdjustmentResult {
    data class Valid(val weight: Int, val calories: Int) : WeightAdjustmentResult()
    data class Invalid(val error: WeightValidationError) : WeightAdjustmentResult()
}

enum class WeightValidationError {
    NOT_A_NUMBER,
    TOO_SMALL,
    TOO_LARGE,
    MISSING_CALORIE_RATIO,
}

sealed class CalorieValidationResult {
    data class Valid(val calories: Int) : CalorieValidationResult()
    data class Invalid(val error: CalorieValidationError) : CalorieValidationResult()
}

enum class CalorieValidationError {
    NOT_A_NUMBER,
    TOO_SMALL,
    TOO_LARGE,
}
