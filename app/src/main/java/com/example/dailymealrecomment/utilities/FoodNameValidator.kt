package com.example.dailymealrecomment.utilities

object FoodNameValidator {
    const val MAX_NAME_LENGTH = 80

    fun normalize(rawName: String): String =
        rawName.trim().replace(Regex("\\s+"), " ")

    fun validate(rawName: String): FoodNameValidationResult {
        val normalizedName = normalize(rawName)
        return when {
            normalizedName.isBlank() -> FoodNameValidationResult.Invalid(FoodNameValidationError.EMPTY)
            normalizedName.length > MAX_NAME_LENGTH -> {
                FoodNameValidationResult.Invalid(FoodNameValidationError.TOO_LONG)
            }
            else -> FoodNameValidationResult.Valid(normalizedName)
        }
    }
}

sealed class FoodNameValidationResult {
    data class Valid(val normalizedName: String) : FoodNameValidationResult()
    data class Invalid(val error: FoodNameValidationError) : FoodNameValidationResult()
}

enum class FoodNameValidationError {
    EMPTY,
    TOO_LONG,
}
