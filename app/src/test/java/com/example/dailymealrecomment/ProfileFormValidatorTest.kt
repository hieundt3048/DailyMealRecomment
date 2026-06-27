package com.example.dailymealrecomment

import com.example.dailymealrecomment.ui.profile.ProfileField
import com.example.dailymealrecomment.ui.profile.ProfileFormValidator
import com.example.dailymealrecomment.ui.profile.ProfileValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileFormValidatorTest {
    @Test
    fun validBodyMetricsAreAccepted() {
        val result = ProfileFormValidator.validate(
            heightText = "170.5",
            weightText = "65.2",
            ageText = "25",
        )

        assertTrue(result is ProfileValidationResult.Valid)
        val valid = result as ProfileValidationResult.Valid
        assertEquals(170.5, valid.heightCm, 0.0)
        assertEquals(65.2, valid.weightKg, 0.0)
        assertEquals(25, valid.age)
    }

    @Test
    fun boundaryBodyMetricsAreAccepted() {
        assertTrue(ProfileFormValidator.validate("100", "30", "13") is ProfileValidationResult.Valid)
        assertTrue(ProfileFormValidator.validate("250", "350", "100") is ProfileValidationResult.Valid)
    }

    @Test
    fun invalidHeightIsRejectedBeforeOtherFields() {
        assertInvalidField(
            result = ProfileFormValidator.validate("99", "65", "25"),
            expectedField = ProfileField.HEIGHT,
        )
    }

    @Test
    fun invalidWeightIsRejected() {
        assertInvalidField(
            result = ProfileFormValidator.validate("170", "29", "25"),
            expectedField = ProfileField.WEIGHT,
        )
    }

    @Test
    fun invalidAgeIsRejected() {
        assertInvalidField(
            result = ProfileFormValidator.validate("170", "65", "12"),
            expectedField = ProfileField.AGE,
        )
    }

    @Test
    fun surroundingSpacesAreIgnored() {
        val result = ProfileFormValidator.validate(" 170 ", " 65 ", " 25 ")

        assertTrue(result is ProfileValidationResult.Valid)
    }

    private fun assertInvalidField(
        result: ProfileValidationResult,
        expectedField: ProfileField,
    ) {
        assertTrue(result is ProfileValidationResult.Invalid)
        assertEquals(expectedField, (result as ProfileValidationResult.Invalid).field)
    }
}
