package com.example.dailymealrecomment

import com.example.dailymealrecomment.data.dashboard.DailyCalorieBalanceCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyCalorieBalanceCalculatorTest {
    @Test
    fun remainingCaloriesReturnsFullTargetWhenUserHasNotEaten() {
        assertEquals(
            2_000,
            DailyCalorieBalanceCalculator.remainingCalories(
                targetCalories = 2_000,
                consumedCalories = 0,
            ),
        )
    }

    @Test
    fun remainingCaloriesSubtractsConsumedCaloriesWhenUserHasEatenPartially() {
        assertEquals(
            1_250,
            DailyCalorieBalanceCalculator.remainingCalories(
                targetCalories = 2_000,
                consumedCalories = 750,
            ),
        )
    }

    @Test
    fun remainingCaloriesReturnsZeroWhenUserReachesTarget() {
        assertEquals(
            0,
            DailyCalorieBalanceCalculator.remainingCalories(
                targetCalories = 2_000,
                consumedCalories = 2_000,
            ),
        )
    }

    @Test
    fun remainingCaloriesReturnsZeroWhenUserExceedsTarget() {
        assertEquals(
            0,
            DailyCalorieBalanceCalculator.remainingCalories(
                targetCalories = 2_000,
                consumedCalories = 2_450,
            ),
        )
    }

    @Test
    fun remainingCaloriesHandlesInvalidNegativeInputsSafely() {
        assertEquals(
            0,
            DailyCalorieBalanceCalculator.remainingCalories(
                targetCalories = -1,
                consumedCalories = -100,
            ),
        )
    }
}
