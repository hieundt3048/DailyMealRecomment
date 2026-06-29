package com.example.dailymealrecomment

import com.example.dailymealrecomment.data.diary.MealLogEntryFactory
import com.example.dailymealrecomment.data.diary.MealType
import com.example.dailymealrecomment.model.FoodItem
import org.junit.Assert.assertEquals
import org.junit.Test

class MealLogEntryFactoryTest {
    @Test
    fun createEntriesKeepsEditedFoodNameWeightAndCalories() {
        val entries = MealLogEntryFactory.createEntries(
            items = listOf(FoodItem("  Cơm gà  ", 180, 420)),
            mealType = MealType.LUNCH,
            sourceImageUri = "content://foodai/test.jpg",
            nowMillis = 0L,
        )

        assertEquals(1, entries.size)
        assertEquals("  Cơm gà  ", entries.first().name)
        assertEquals(180, entries.first().weight)
        assertEquals(420, entries.first().calories)
        assertEquals(MealType.LUNCH, entries.first().mealType)
        assertEquals("1970-01-01", entries.first().dateKey)
        assertEquals("content://foodai/test.jpg", entries.first().sourceImageUri)
        assertEquals(0L, entries.first().createdAtMillis)
    }

    @Test
    fun createEntriesSeparatesMealTypeForDiaryGrouping() {
        val entries = MealLogEntryFactory.createEntries(
            items = listOf(
                FoodItem("Chuối", 100, 89),
                FoodItem("Sữa chua", 120, 75),
            ),
            mealType = MealType.SNACK,
            sourceImageUri = null,
            nowMillis = 1_700_000_000_000L,
        )

        assertEquals(2, entries.size)
        assertEquals(MealType.SNACK, entries[0].mealType)
        assertEquals(MealType.SNACK, entries[1].mealType)
        assertEquals("Chuối", entries[0].name)
        assertEquals(89, entries[0].calories)
        assertEquals("Sữa chua", entries[1].name)
        assertEquals(75, entries[1].calories)
    }
}
