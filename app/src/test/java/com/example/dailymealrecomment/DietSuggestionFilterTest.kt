package com.example.dailymealrecomment

import com.example.dailymealrecomment.data.model.DietType
import com.example.dailymealrecomment.data.model.MealSuggestion
import com.example.dailymealrecomment.utilities.DietSuggestionFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DietSuggestionFilterTest {
    private val suggestions = listOf(
        MealSuggestion(id = "chicken_rice", name = "Cơm gà áp chảo", calories = 520, isVegan = false, weightGrams = 250),
        MealSuggestion(id = "tofu_salad", name = "Salad đậu phụ", calories = 430, isVegan = true, weightGrams = 300),
        MealSuggestion(id = "vegan_noodle", name = "Bún rau nấm", calories = 480, isVegan = true, weightGrams = 320),
    )

    @Test
    fun normalDietSortsNormalMealsFirstAndStillKeepsAllSuggestions() {
        val result = DietSuggestionFilter.sortForDiet(suggestions, DietType.NORMAL)

        assertEquals(listOf("chicken_rice", "tofu_salad", "vegan_noodle"), result.map { it.id })
    }

    @Test
    fun veganDietSortsVeganMealsFirstAndStillKeepsAllSuggestions() {
        val result = DietSuggestionFilter.sortForDiet(suggestions, DietType.VEGAN)

        assertEquals(listOf("tofu_salad", "vegan_noodle", "chicken_rice"), result.map { it.id })
    }

    @Test
    fun normalDietKeepsAllSuggestions() {
        val result = DietSuggestionFilter.filterForDiet(suggestions, DietType.NORMAL)

        assertEquals(suggestions.map { it.id }, result.map { it.id })
    }

    @Test
    fun veganDietKeepsOnlyVeganSuggestions() {
        val result = DietSuggestionFilter.filterForDiet(suggestions, DietType.VEGAN)

        assertEquals(listOf("tofu_salad", "vegan_noodle"), result.map { it.id })
        assertTrue(result.all { it.isVegan })
    }

    @Test
    fun emptySuggestionsRemainEmpty() {
        val result = DietSuggestionFilter.filterForDiet(emptyList(), DietType.VEGAN)

        assertTrue(result.isEmpty())
    }
}
