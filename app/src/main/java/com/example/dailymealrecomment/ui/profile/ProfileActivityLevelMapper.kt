package com.example.dailymealrecomment.ui.profile

import androidx.annotation.IdRes
import com.example.dailymealrecomment.R
import kotlin.math.abs

object ProfileActivityLevelMapper {
    const val SEDENTARY = 1.2
    const val LIGHT = 1.375
    const val MODERATE = 1.55
    const val ACTIVE = 1.725

    fun activityLevelFromCheckedChip(@IdRes checkedChipId: Int): Double {
        return when (checkedChipId) {
            R.id.chipActivityLight -> LIGHT
            R.id.chipActivityModerate -> MODERATE
            R.id.chipActivityActive -> ACTIVE
            else -> SEDENTARY
        }
    }

    @IdRes
    fun chipIdForActivityLevel(activityLevel: Double): Int {
        return listOf(
            SEDENTARY to R.id.chipActivitySedentary,
            LIGHT to R.id.chipActivityLight,
            MODERATE to R.id.chipActivityModerate,
            ACTIVE to R.id.chipActivityActive,
        ).minBy { (level, _) -> abs(level - activityLevel) }.second
    }
}
