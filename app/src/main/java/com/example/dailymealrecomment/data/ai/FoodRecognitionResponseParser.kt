package com.example.dailymealrecomment.data.ai

import com.example.dailymealrecomment.model.FoodItem
import com.example.dailymealrecomment.utilities.FoodCalorieAdjuster
import kotlin.math.roundToInt
import org.json.JSONArray
import org.json.JSONObject

object FoodRecognitionResponseParser {
    fun parse(responseBody: String): List<FoodItem> {
        val trimmedBody = responseBody.trim()
        if (trimmedBody.isBlank()) return emptyList()

        val itemArray = if (trimmedBody.startsWith("[")) {
            JSONArray(trimmedBody)
        } else {
            val root = JSONObject(trimmedBody)
            root.optJSONArray("items")
                ?: root.optJSONArray("foods")
                ?: root.optJSONArray("results")
                ?: JSONArray()
        }

        return buildList {
            for (index in 0 until itemArray.length()) {
                val foodObject = itemArray.optJSONObject(index) ?: continue
                val name = foodObject.firstNonBlankString(
                    "name",
                    "foodName",
                    "food_name",
                    "dish",
                    "label",
                ) ?: continue
                val weight = foodObject.firstPositiveInt(
                    "weight",
                    "weightGram",
                    "weightGrams",
                    "weight_g",
                    "weight_grams",
                    "grams",
                    "estimatedWeight",
                    "estimatedWeightGrams",
                    "estimated_weight_g",
                ) ?: continue
                val calories = foodObject.firstPositiveInt(
                    "calories",
                    "calorie",
                    "kcal",
                    "estimatedCalories",
                    "estimated_calories",
                ) ?: foodObject.optJSONObject("nutrition")?.firstPositiveInt("calories", "kcal")
                    ?: continue

                if (weight > FoodCalorieAdjuster.MAX_WEIGHT_GRAMS ||
                    calories > FoodCalorieAdjuster.MAX_CALORIES
                ) {
                    continue
                }

                add(
                    FoodItem(
                        name = name,
                        weight = weight,
                        calories = calories,
                    ),
                )
            }
        }
    }

    private fun JSONObject.firstNonBlankString(vararg keys: String): String? {
        for (key in keys) {
            if (!has(key)) continue
            val value = optString(key).trim()
            if (value.isNotBlank() && value != "null") return value
        }
        return null
    }

    private fun JSONObject.firstPositiveInt(vararg keys: String): Int? {
        for (key in keys) {
            if (!has(key)) continue
            val value = optDouble(key, Double.NaN)
            if (value.isFinite() && value > 0.0) return value.roundToInt()
        }
        return null
    }
}
