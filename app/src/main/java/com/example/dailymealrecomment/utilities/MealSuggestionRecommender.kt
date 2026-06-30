package com.example.dailymealrecomment.utilities

import com.example.dailymealrecomment.data.model.DietType
import com.example.dailymealrecomment.data.model.MealSuggestion
import kotlin.math.abs

object MealSuggestionRecommender {
    const val MAX_RECOMMENDATIONS = 5

    fun recommend(
        suggestions: List<MealSuggestion>,
        dietType: DietType,
        remainingCalories: Int,
    ): List<MealSuggestion> {
        // Neu hom nay da du calo thi khong can goi y them mon.
        if (remainingCalories <= 0) return emptyList()

        // Loc theo che do an truoc, sau do bo mon bi trung id.
        val filteredSuggestions = DietSuggestionFilter
            .filterForDiet(suggestions, dietType)
            .distinctBy { it.id }

        if (filteredSuggestions.isEmpty()) return emptyList()

        // Mon nao co calo gan voi calo con lai hon thi dua len truoc.
        return filteredSuggestions
            .sortedBy { suggestion -> abs(suggestion.calories - remainingCalories) }
            .take(MAX_RECOMMENDATIONS)
    }
}
