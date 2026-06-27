package com.example.dailymealrecomment.ui.profile

import androidx.annotation.IdRes
import com.example.dailymealrecomment.R
import com.example.dailymealrecomment.data.model.Goal

object ProfileGoalMapper {
    fun goalFromCheckedChip(@IdRes checkedChipId: Int): Goal {
        return when (checkedChipId) {
            R.id.chipLose -> Goal.LOSE_WEIGHT
            R.id.chipGain -> Goal.GAIN_WEIGHT
            else -> Goal.MAINTAIN_WEIGHT
        }
    }

    @IdRes
    fun chipIdForGoal(goal: Goal): Int {
        return when (goal) {
            Goal.LOSE_WEIGHT -> R.id.chipLose
            Goal.GAIN_WEIGHT -> R.id.chipGain
            Goal.MAINTAIN_WEIGHT -> R.id.chipMaintain
        }
    }
}
