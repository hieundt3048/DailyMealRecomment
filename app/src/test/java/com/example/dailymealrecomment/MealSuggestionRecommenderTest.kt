package com.example.dailymealrecomment

import com.example.dailymealrecomment.data.model.DietType
import com.example.dailymealrecomment.data.model.MealSuggestion
import com.example.dailymealrecomment.data.model.MealSuggestionCatalog
import com.example.dailymealrecomment.utilities.MealSuggestionRecommender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class MealSuggestionRecommenderTest {
    @Test
    fun recommendReturnsThreeToFiveUniqueSuggestionsForNormalDiet() {
        val recommendations = MealSuggestionRecommender.recommend(
            suggestions = MealSuggestionCatalog.all,
            dietType = DietType.NORMAL,
            remainingCalories = 500,
        )

        assertTrue(recommendations.size in 3..5)
        assertEquals(recommendations.size, recommendations.map { it.id }.toSet().size)
    }

    @Test
    fun recommendFiltersVeganDiet() {
        val recommendations = MealSuggestionRecommender.recommend(
            suggestions = MealSuggestionCatalog.all,
            dietType = DietType.VEGAN,
            remainingCalories = 500,
        )

        assertEquals(3, recommendations.size)
        assertTrue(recommendations.all { it.isVegan })
    }

    @Test
    fun recommendRanksByCalorieDistanceFromRemainingCalories() {
        val remainingCalories = 500
        val recommendations = MealSuggestionRecommender.recommend(
            suggestions = MealSuggestionCatalog.all,
            dietType = DietType.NORMAL,
            remainingCalories = remainingCalories,
        )

        val distances = recommendations.map { abs(it.calories - remainingCalories) }
        assertEquals(distances.sorted(), distances)
        assertEquals("chicken_rice", recommendations.first().id)
    }

    @Test
    fun recommendReturnsEmptyWhenRemainingCaloriesIsZero() {
        val recommendations = MealSuggestionRecommender.recommend(
            suggestions = MealSuggestionCatalog.all,
            dietType = DietType.NORMAL,
            remainingCalories = 0,
        )

        assertTrue(recommendations.isEmpty())
    }

    @Test
    fun recommendReturnsEmptyWhenNoDietMatchedSuggestionExists() {
        val recommendations = MealSuggestionRecommender.recommend(
            suggestions = listOf(
                MealSuggestion(
                    id = "fish",
                    name = "Fish",
                    calories = 300,
                    isVegan = false,
                    weightGrams = 180,
                ),
            ),
            dietType = DietType.VEGAN,
            remainingCalories = 500,
        )

        assertTrue(recommendations.isEmpty())
    }
}
