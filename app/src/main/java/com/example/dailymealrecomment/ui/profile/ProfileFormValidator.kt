package com.example.dailymealrecomment.ui.profile

object ProfileFormValidator {
    private const val MIN_HEIGHT_CM = 100.0
    private const val MAX_HEIGHT_CM = 250.0
    private const val MIN_WEIGHT_KG = 30.0
    private const val MAX_WEIGHT_KG = 350.0
    private const val MIN_AGE = 13
    private const val MAX_AGE = 100

    fun validate(
        heightText: String,
        weightText: String,
        ageText: String,
    ): ProfileValidationResult {
        val height = heightText.trim().toDoubleOrNull()
        if (height == null || height !in MIN_HEIGHT_CM..MAX_HEIGHT_CM) {
            return ProfileValidationResult.Invalid(ProfileField.HEIGHT)
        }

        val weight = weightText.trim().toDoubleOrNull()
        if (weight == null || weight !in MIN_WEIGHT_KG..MAX_WEIGHT_KG) {
            return ProfileValidationResult.Invalid(ProfileField.WEIGHT)
        }

        val age = ageText.trim().toIntOrNull()
        if (age == null || age !in MIN_AGE..MAX_AGE) {
            return ProfileValidationResult.Invalid(ProfileField.AGE)
        }

        return ProfileValidationResult.Valid(
            heightCm = height,
            weightKg = weight,
            age = age,
        )
    }
}

sealed class ProfileValidationResult {
    data class Valid(
        val heightCm: Double,
        val weightKg: Double,
        val age: Int,
    ) : ProfileValidationResult()

    data class Invalid(val field: ProfileField) : ProfileValidationResult()
}

enum class ProfileField {
    HEIGHT,
    WEIGHT,
    AGE,
}
