package com.example.dailymealrecomment

import com.example.dailymealrecomment.data.model.MealSuggestionCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MealSuggestionCatalogTest {
    @Test
    fun catalogProvidesAllHomeSuggestionsWithDetailContent() {
        val suggestions = MealSuggestionCatalog.all

        assertEquals(10, suggestions.size)
        suggestions.forEach { suggestion ->
            assertTrue(suggestion.name.isNotBlank())
            assertTrue(suggestion.calories > 0)
            assertTrue(suggestion.weightGrams > 0)
            assertTrue(suggestion.imageResId != 0)
            assertTrue(suggestion.serving.isNotBlank())
            assertTrue(suggestion.ingredients.isNotEmpty())
            assertTrue(suggestion.recipeSteps.isNotEmpty())
        }
    }

    @Test
    fun catalogCanFindSuggestionById() {
        val first = MealSuggestionCatalog.all.first()

        assertEquals(first, MealSuggestionCatalog.findById(first.id))
        assertNotNull(MealSuggestionCatalog.findById("tofu_salad"))
        assertNotNull(MealSuggestionCatalog.findById("banh_mi"))
        assertNotNull(MealSuggestionCatalog.findById("vit_quay"))
    }

    @Test
    fun suggestionCanBeConvertedToFoodItemForDiary() {
        val suggestion = MealSuggestionCatalog.all.first()
        val foodItem = suggestion.toFoodItem()

        assertEquals(suggestion.name, foodItem.name)
        assertEquals(suggestion.weightGrams, foodItem.weight)
        assertEquals(suggestion.calories, foodItem.calories)
        assertEquals(
            suggestion.calories.toDouble() / suggestion.weightGrams,
            foodItem.caloriesPerGram,
            0.001,
        )
    }
}
