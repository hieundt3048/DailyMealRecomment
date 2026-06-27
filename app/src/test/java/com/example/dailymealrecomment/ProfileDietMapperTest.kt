package com.example.dailymealrecomment

import com.example.dailymealrecomment.data.model.DietType
import com.example.dailymealrecomment.ui.profile.ProfileDietMapper
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileDietMapperTest {
    @Test
    fun normalChipMapsToNormalDiet() {
        assertEquals(DietType.NORMAL, ProfileDietMapper.dietTypeFromCheckedChip(R.id.chipNormal))
        assertEquals(R.id.chipNormal, ProfileDietMapper.chipIdForDietType(DietType.NORMAL))
    }

    @Test
    fun veganChipMapsToVeganDiet() {
        assertEquals(DietType.VEGAN, ProfileDietMapper.dietTypeFromCheckedChip(R.id.chipVegan))
        assertEquals(R.id.chipVegan, ProfileDietMapper.chipIdForDietType(DietType.VEGAN))
    }

    @Test
    fun unknownChipFallsBackToNormalDiet() {
        assertEquals(DietType.NORMAL, ProfileDietMapper.dietTypeFromCheckedChip(0))
    }
}
