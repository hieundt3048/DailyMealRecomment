package com.example.dailymealrecomment

import com.example.dailymealrecomment.utilities.CalorieValidationError
import com.example.dailymealrecomment.utilities.CalorieValidationResult
import com.example.dailymealrecomment.utilities.FoodCalorieAdjuster
import com.example.dailymealrecomment.utilities.WeightAdjustmentResult
import com.example.dailymealrecomment.utilities.WeightValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodCalorieAdjusterTest {
    @Test
    fun caloriesPerGramUsesAiWeightAndCalories() {
        val ratio = FoodCalorieAdjuster.caloriesPerGram(weight = 250, calories = 520)

        assertEquals(2.08, ratio, 0.001)
    }

    @Test
    fun weightChangeRecalculatesCaloriesAndRoundsResult() {
        val ratio = FoodCalorieAdjuster.caloriesPerGram(weight = 250, calories = 520)

        val result = FoodCalorieAdjuster.recalculateCalories("180", ratio)

        assertTrue(result is WeightAdjustmentResult.Valid)
        result as WeightAdjustmentResult.Valid
        assertEquals(180, result.weight)
        assertEquals(374, result.calories)
    }

    @Test
    fun weightChangeRejectsZeroWeight() {
        val result = FoodCalorieAdjuster.recalculateCalories("0", caloriesPerGram = 2.0)

        assertEquals(
            WeightValidationError.TOO_SMALL,
            (result as WeightAdjustmentResult.Invalid).error,
        )
    }

    @Test
    fun weightChangeRejectsTooLargeWeight() {
        val result = FoodCalorieAdjuster.recalculateCalories("5001", caloriesPerGram = 2.0)

        assertEquals(
            WeightValidationError.TOO_LARGE,
            (result as WeightAdjustmentResult.Invalid).error,
        )
    }

    @Test
    fun weightChangeRejectsMissingCalorieRatio() {
        val result = FoodCalorieAdjuster.recalculateCalories("150", caloriesPerGram = 0.0)

        assertEquals(
            WeightValidationError.MISSING_CALORIE_RATIO,
            (result as WeightAdjustmentResult.Invalid).error,
        )
    }

    @Test
    fun manualCaloriesRejectsInvalidValues() {
        val zeroResult = FoodCalorieAdjuster.validateManualCalories("0")
        val hugeResult = FoodCalorieAdjuster.validateManualCalories("10001")

        assertEquals(
            CalorieValidationError.TOO_SMALL,
            (zeroResult as CalorieValidationResult.Invalid).error,
        )
        assertEquals(
            CalorieValidationError.TOO_LARGE,
            (hugeResult as CalorieValidationResult.Invalid).error,
        )
    }
}
