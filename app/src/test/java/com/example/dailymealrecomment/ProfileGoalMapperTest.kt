package com.example.dailymealrecomment

import com.example.dailymealrecomment.data.model.Goal
import com.example.dailymealrecomment.ui.profile.ProfileGoalMapper
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileGoalMapperTest {
    @Test
    fun loseChipMapsToLoseWeightGoal() {
        assertEquals(Goal.LOSE_WEIGHT, ProfileGoalMapper.goalFromCheckedChip(R.id.chipLose))
        assertEquals(R.id.chipLose, ProfileGoalMapper.chipIdForGoal(Goal.LOSE_WEIGHT))
    }

    @Test
    fun maintainChipMapsToMaintainWeightGoal() {
        assertEquals(Goal.MAINTAIN_WEIGHT, ProfileGoalMapper.goalFromCheckedChip(R.id.chipMaintain))
        assertEquals(R.id.chipMaintain, ProfileGoalMapper.chipIdForGoal(Goal.MAINTAIN_WEIGHT))
    }

    @Test
    fun gainChipMapsToGainWeightGoal() {
        assertEquals(Goal.GAIN_WEIGHT, ProfileGoalMapper.goalFromCheckedChip(R.id.chipGain))
        assertEquals(R.id.chipGain, ProfileGoalMapper.chipIdForGoal(Goal.GAIN_WEIGHT))
    }

    @Test
    fun unknownChipFallsBackToMaintainGoal() {
        assertEquals(Goal.MAINTAIN_WEIGHT, ProfileGoalMapper.goalFromCheckedChip(0))
    }
}
