package com.example.dailymealrecomment.data.diary

object DiaryLogGrouper {
    fun sortEntries(entries: List<MealLogEntry>): List<MealLogEntry> =
        entries.sortedWith(
            compareBy<MealLogEntry> { it.mealType.sortOrder }
                .thenBy { it.createdAtMillis },
        )

    fun sectionsFor(entries: List<MealLogEntry>): List<MealLogSection> =
        sortEntries(entries)
            .groupBy { it.mealType }
            .toSortedMap(compareBy { it.sortOrder })
            .map { (mealType, mealEntries) ->
                MealLogSection(mealType = mealType, entries = mealEntries)
            }

    fun totalCalories(entries: List<MealLogEntry>): Int =
        entries.sumOf { it.calories }
}
