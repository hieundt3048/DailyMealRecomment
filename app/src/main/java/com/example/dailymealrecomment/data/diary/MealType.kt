package com.example.dailymealrecomment.data.diary

enum class MealType(
    val storageValue: String,
    val sortOrder: Int,
) {
    BREAKFAST("BREAKFAST", 0),
    LUNCH("LUNCH", 1),
    DINNER("DINNER", 2),
    SNACK("SNACK", 3),
    ;

    companion object {
        fun fromStorage(value: String?): MealType? =
            entries.firstOrNull { it.storageValue == value }
    }
}
