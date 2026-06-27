package com.example.dailymealrecomment.utilities

import com.example.dailymealrecomment.data.model.DietType
import com.example.dailymealrecomment.data.model.MealSuggestion

object DietSuggestionFilter {
    fun filterForDiet(
        suggestions: List<MealSuggestion>,
        dietType: DietType,
    ): List<MealSuggestion> {
        return when (dietType) {
            DietType.VEGAN -> suggestions.filter { it.isVegan }
            DietType.NORMAL -> suggestions
        }
    }
}
