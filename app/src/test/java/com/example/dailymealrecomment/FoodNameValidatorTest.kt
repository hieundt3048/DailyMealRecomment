package com.example.dailymealrecomment

import com.example.dailymealrecomment.utilities.FoodNameValidationError
import com.example.dailymealrecomment.utilities.FoodNameValidationResult
import com.example.dailymealrecomment.utilities.FoodNameValidator
import org.junit.Assert.assertEquals
import org.junit.Test

class FoodNameValidatorTest {
    @Test
    fun validNameIsAccepted() {
        val result = FoodNameValidator.validate("Cơm gà")

        assertEquals(
            "Cơm gà",
            (result as FoodNameValidationResult.Valid).normalizedName,
        )
    }

    @Test
    fun extraSpacesAreNormalized() {
        val result = FoodNameValidator.validate("  Cơm    gà   áp   chảo  ")

        assertEquals(
            "Cơm gà áp chảo",
            (result as FoodNameValidationResult.Valid).normalizedName,
        )
    }

    @Test
    fun blankNameIsRejected() {
        val result = FoodNameValidator.validate("     ")

        assertEquals(
            FoodNameValidationError.EMPTY,
            (result as FoodNameValidationResult.Invalid).error,
        )
    }

    @Test
    fun tooLongNameIsRejected() {
        val result = FoodNameValidator.validate("a".repeat(FoodNameValidator.MAX_NAME_LENGTH + 1))

        assertEquals(
            FoodNameValidationError.TOO_LONG,
            (result as FoodNameValidationResult.Invalid).error,
        )
    }
}
