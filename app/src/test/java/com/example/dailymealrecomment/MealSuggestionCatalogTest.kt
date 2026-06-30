package com.example.dailymealrecomment

import com.example.dailymealrecomment.data.model.MealSuggestionCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MealSuggestionCatalogTest {
    @Test
    fun catalogProvidesThreeToFiveSuggestionsWithDetailContent() {
        val suggestions = MealSuggestionCatalog.all

        assertTrue(suggestions.size in 3..5)
        suggestions.forEach { suggestion ->
            assertTrue(suggestion.name.isNotBlank())
            assertTrue(suggestion.calories > 0)
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
    }
}
