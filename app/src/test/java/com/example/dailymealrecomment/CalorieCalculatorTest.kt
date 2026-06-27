package com.example.dailymealrecomment

import com.example.dailymealrecomment.data.model.DietType
import com.example.dailymealrecomment.data.model.Goal
import com.example.dailymealrecomment.data.model.UserProfile
import com.example.dailymealrecomment.utilities.CalorieCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieCalculatorTest {
    private val baseProfile = UserProfile(
        heightCm = 170.0,
        weightKg = 65.0,
        age = 25,
        isMale = true,
        goal = Goal.MAINTAIN_WEIGHT,
        dietType = DietType.NORMAL,
        activityLevel = 1.2,
    )

    @Test
    fun goalAdjustmentsChangeTargetByExpectedAmount() {
        val maintain = CalorieCalculator.calculateDailyCalorieTarget(baseProfile)
        val gain = CalorieCalculator.calculateDailyCalorieTarget(baseProfile.copy(goal = Goal.GAIN_WEIGHT))
        val lose = CalorieCalculator.calculateDailyCalorieTarget(baseProfile.copy(goal = Goal.LOSE_WEIGHT))

        assertEquals(500, gain - maintain)
        assertEquals(500, maintain - lose)
    }

    @Test
    fun maintainWeightGoalReturnsBaseDailyCalories() {
        val target = CalorieCalculator.calculateDailyCalorieTarget(
            baseProfile.copy(goal = Goal.MAINTAIN_WEIGHT),
        )

        assertEquals(1_911, target)
    }

    @Test
    fun gainWeightGoalAddsCalories() {
        val target = CalorieCalculator.calculateDailyCalorieTarget(
            baseProfile.copy(goal = Goal.GAIN_WEIGHT),
        )

        assertEquals(2_411, target)
    }

    @Test
    fun loseWeightGoalSubtractsCalories() {
        val target = CalorieCalculator.calculateDailyCalorieTarget(
            baseProfile.copy(goal = Goal.LOSE_WEIGHT),
        )

        assertEquals(1_411, target)
    }

    @Test
    fun activityLevelChangesDailyCalories() {
        val sedentary = CalorieCalculator.calculateDailyCalorieTarget(
            baseProfile.copy(activityLevel = 1.2),
        )
        val moderate = CalorieCalculator.calculateDailyCalorieTarget(
            baseProfile.copy(activityLevel = 1.55),
        )
        val active = CalorieCalculator.calculateDailyCalorieTarget(
            baseProfile.copy(activityLevel = 1.725),
        )

        assertTrue(moderate > sedentary)
        assertTrue(active > moderate)
        assertEquals(2_468, moderate)
        assertEquals(2_747, active)
    }

    @Test
    fun targetNeverDropsBelowMinimum() {
        val target = CalorieCalculator.calculateDailyCalorieTarget(
            baseProfile.copy(weightKg = 30.0, age = 100, isMale = false, goal = Goal.LOSE_WEIGHT),
        )

        assertTrue(target >= 1_200)
    }
}
