package com.example.dailymealrecomment

import com.example.dailymealrecomment.data.diary.DiaryLogGrouper
import com.example.dailymealrecomment.data.diary.MealLogEntry
import com.example.dailymealrecomment.data.diary.MealType
import org.junit.Assert.assertEquals
import org.junit.Test

class DiaryLogGrouperTest {
    @Test
    fun sectionsForGroupsEntriesByMealTypeInDiaryOrder() {
        val entries = listOf(
            entry(name = "Chuối", mealType = MealType.SNACK, calories = 89, createdAtMillis = 3L),
            entry(name = "Cơm gà", mealType = MealType.LUNCH, calories = 520, createdAtMillis = 2L),
            entry(name = "Sữa chua", mealType = MealType.BREAKFAST, calories = 150, createdAtMillis = 1L),
        )

        val sections = DiaryLogGrouper.sectionsFor(entries)

        assertEquals(listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.SNACK), sections.map { it.mealType })
        assertEquals(150, sections[0].totalCalories)
        assertEquals(520, sections[1].totalCalories)
        assertEquals(89, sections[2].totalCalories)
    }

    @Test
    fun totalCaloriesSumsAllDiaryEntries() {
        val entries = listOf(
            entry(name = "Bữa sáng", mealType = MealType.BREAKFAST, calories = 300),
            entry(name = "Bữa trưa", mealType = MealType.LUNCH, calories = 600),
            entry(name = "Bữa phụ", mealType = MealType.SNACK, calories = 120),
        )

        assertEquals(1_020, DiaryLogGrouper.totalCalories(entries))
    }

    private fun entry(
        name: String,
        mealType: MealType,
        calories: Int,
        createdAtMillis: Long = 0L,
    ): MealLogEntry =
        MealLogEntry(
            name = name,
            weight = 100,
            calories = calories,
            mealType = mealType,
            dateKey = "2026-06-29",
            sourceImageUri = null,
            createdAtMillis = createdAtMillis,
        )
}
