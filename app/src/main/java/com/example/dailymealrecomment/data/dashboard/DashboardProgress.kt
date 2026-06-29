package com.example.dailymealrecomment.data.dashboard

import com.example.dailymealrecomment.data.diary.DiaryLogGrouper
import com.example.dailymealrecomment.data.diary.MealLogEntry

data class DashboardProgress(
    val consumedCalories: Int,
    val targetCalories: Int,
    val remainingCalories: Int,
    val progressPercent: Int,
)

object DailyCalorieBalanceCalculator {
    fun consumedCalories(entries: List<MealLogEntry>): Int {
        return DiaryLogGrouper.totalCalories(entries).coerceAtLeast(0)
    }

    fun remainingCalories(
        targetCalories: Int,
        consumedCalories: Int,
    ): Int {
        // Cong thuc chinh: calo con lai = muc tieu - da an, khong cho am.
        return (targetCalories.coerceAtLeast(0) - consumedCalories.coerceAtLeast(0)).coerceAtLeast(0)
    }

    fun remainingCalories(
        targetCalories: Int,
        entries: List<MealLogEntry>,
    ): Int {
        return remainingCalories(
            targetCalories = targetCalories,
            consumedCalories = consumedCalories(entries),
        )
    }
}

object DashboardProgressCalculator {
    fun calculate(
        entries: List<MealLogEntry>,
        targetCalories: Int,
    ): DashboardProgress =
        calculate(
            consumedCalories = DailyCalorieBalanceCalculator.consumedCalories(entries),
            targetCalories = targetCalories,
        )

    fun calculate(
        consumedCalories: Int,
        targetCalories: Int,
    ): DashboardProgress {
        val safeConsumedCalories = consumedCalories.coerceAtLeast(0)
        val safeTargetCalories = targetCalories.coerceAtLeast(0)
        val remainingCalories = DailyCalorieBalanceCalculator.remainingCalories(
            targetCalories = safeTargetCalories,
            consumedCalories = safeConsumedCalories,
        )
        val progressPercent = if (safeTargetCalories > 0) {
            ((safeConsumedCalories * 100.0) / safeTargetCalories).toInt().coerceIn(0, 100)
        } else {
            0
        }

        return DashboardProgress(
            consumedCalories = safeConsumedCalories,
            targetCalories = safeTargetCalories,
            remainingCalories = remainingCalories,
            progressPercent = progressPercent,
        )
    }
}
