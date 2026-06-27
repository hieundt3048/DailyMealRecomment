package com.example.dailymealrecomment

import com.example.dailymealrecomment.ui.profile.ProfileActivityLevelMapper
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileActivityLevelMapperTest {
    @Test
    fun sedentaryChipMapsToSedentaryLevel() {
        assertEquals(
            ProfileActivityLevelMapper.SEDENTARY,
            ProfileActivityLevelMapper.activityLevelFromCheckedChip(R.id.chipActivitySedentary),
            0.0,
        )
        assertEquals(
            R.id.chipActivitySedentary,
            ProfileActivityLevelMapper.chipIdForActivityLevel(ProfileActivityLevelMapper.SEDENTARY),
        )
    }

    @Test
    fun lightChipMapsToLightLevel() {
        assertEquals(
            ProfileActivityLevelMapper.LIGHT,
            ProfileActivityLevelMapper.activityLevelFromCheckedChip(R.id.chipActivityLight),
            0.0,
        )
        assertEquals(
            R.id.chipActivityLight,
            ProfileActivityLevelMapper.chipIdForActivityLevel(ProfileActivityLevelMapper.LIGHT),
        )
    }

    @Test
    fun moderateChipMapsToModerateLevel() {
        assertEquals(
            ProfileActivityLevelMapper.MODERATE,
            ProfileActivityLevelMapper.activityLevelFromCheckedChip(R.id.chipActivityModerate),
            0.0,
        )
        assertEquals(
            R.id.chipActivityModerate,
            ProfileActivityLevelMapper.chipIdForActivityLevel(ProfileActivityLevelMapper.MODERATE),
        )
    }

    @Test
    fun activeChipMapsToActiveLevel() {
        assertEquals(
            ProfileActivityLevelMapper.ACTIVE,
            ProfileActivityLevelMapper.activityLevelFromCheckedChip(R.id.chipActivityActive),
            0.0,
        )
        assertEquals(
            R.id.chipActivityActive,
            ProfileActivityLevelMapper.chipIdForActivityLevel(ProfileActivityLevelMapper.ACTIVE),
        )
    }

    @Test
    fun unknownChipFallsBackToSedentaryLevel() {
        assertEquals(
            ProfileActivityLevelMapper.SEDENTARY,
            ProfileActivityLevelMapper.activityLevelFromCheckedChip(0),
            0.0,
        )
    }

    @Test
    fun savedActivityLevelSelectsNearestChip() {
        assertEquals(
            R.id.chipActivityModerate,
            ProfileActivityLevelMapper.chipIdForActivityLevel(1.56),
        )
    }
}
