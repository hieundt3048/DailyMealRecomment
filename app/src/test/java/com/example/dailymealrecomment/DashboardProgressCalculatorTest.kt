package com.example.dailymealrecomment

import com.example.dailymealrecomment.data.dashboard.DashboardProgressCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardProgressCalculatorTest {
    @Test
    fun calculateReturnsRemainingCaloriesAndPercent() {
        val progress = DashboardProgressCalculator.calculate(
            consumedCalories = 500,
            targetCalories = 2_000,
        )

        assertEquals(500, progress.consumedCalories)
        assertEquals(2_000, progress.targetCalories)
        assertEquals(1_500, progress.remainingCalories)
        assertEquals(25, progress.progressPercent)
    }

    @Test
    fun calculateCapsProgressAtOneHundredWhenConsumedExceedsTarget() {
        val progress = DashboardProgressCalculator.calculate(
            consumedCalories = 2_400,
            targetCalories = 2_000,
        )

        assertEquals(0, progress.remainingCalories)
        assertEquals(100, progress.progressPercent)
    }

    @Test
    fun calculateHandlesMissingTargetSafely() {
        val progress = DashboardProgressCalculator.calculate(
            consumedCalories = 500,
            targetCalories = 0,
        )

        assertEquals(0, progress.targetCalories)
        assertEquals(0, progress.remainingCalories)
        assertEquals(0, progress.progressPercent)
    }
}
