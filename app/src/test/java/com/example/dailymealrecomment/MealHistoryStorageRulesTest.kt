package com.example.dailymealrecomment

import com.example.dailymealrecomment.data.diary.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MealHistoryStorageRulesTest {
    @Test
    fun mealTypesRoundTripThroughStorageValues() {
        MealType.entries.forEach { mealType ->
            assertEquals(mealType, MealType.fromStorage(mealType.storageValue))
        }
    }

    @Test
    fun mealTypesKeepDiarySortOrder() {
        assertEquals(
            listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK),
            MealType.entries.sortedBy { it.sortOrder },
        )
    }

    @Test
    fun unknownMealTypeIsRejectedByParser() {
        assertNull(MealType.fromStorage("BRUNCH"))
    }
}
