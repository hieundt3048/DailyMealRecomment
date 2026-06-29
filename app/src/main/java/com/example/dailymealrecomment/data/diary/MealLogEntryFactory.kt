package com.example.dailymealrecomment.data.diary

import com.example.dailymealrecomment.model.FoodItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MealLogEntryFactory {
    fun createEntries(
        items: List<FoodItem>,
        mealType: MealType,
        sourceImageUri: String?,
        nowMillis: Long = System.currentTimeMillis(),
    ): List<MealLogEntry> {
        val dateKey = dateKey(nowMillis)
        return items.map { foodItem ->
            MealLogEntry(
                name = foodItem.name,
                weight = foodItem.weight,
                calories = foodItem.calories,
                mealType = mealType,
                dateKey = dateKey,
                sourceImageUri = sourceImageUri,
                createdAtMillis = nowMillis,
            )
        }
    }

    fun dateKey(timeMillis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timeMillis))
}
