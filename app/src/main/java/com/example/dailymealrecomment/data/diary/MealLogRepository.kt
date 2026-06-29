package com.example.dailymealrecomment.data.diary

import com.example.dailymealrecomment.data.xampp.XamppRepository
import com.example.dailymealrecomment.model.FoodItem

class MealLogRepository(
    private val xamppRepository: XamppRepository = XamppRepository(),
) {
    suspend fun saveMealItems(
        token: String,
        items: List<FoodItem>,
        mealType: MealType,
        sourceImageUri: String?,
    ) {
        xamppRepository.saveMealItems(
            token = token,
            items = items,
            mealType = mealType,
            sourceImageUri = sourceImageUri,
        )
    }

    suspend fun loadMealItemsForDate(
        token: String,
        dateKey: String,
    ): List<MealLogEntry> =
        xamppRepository.loadMealItemsForDate(token = token, dateKey = dateKey)

    suspend fun loadMealItemsForDateAndMeal(
        token: String,
        dateKey: String,
        mealType: MealType,
    ): List<MealLogEntry> =
        xamppRepository.loadMealItemsForDateAndMeal(
            token = token,
            dateKey = dateKey,
            mealType = mealType,
        )

    suspend fun updateMealItem(
        token: String,
        entry: MealLogEntry,
    ) {
        xamppRepository.updateMealItem(token = token, entry = entry)
    }

    suspend fun deleteMealItem(
        token: String,
        id: Int,
    ) {
        xamppRepository.deleteMealItem(token = token, id = id)
    }
}
