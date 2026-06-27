package com.example.dailymealrecomment.ui.profile

import androidx.annotation.IdRes
import com.example.dailymealrecomment.R
import com.example.dailymealrecomment.data.model.DietType

object ProfileDietMapper {
    fun dietTypeFromCheckedChip(@IdRes checkedChipId: Int): DietType {
        return if (checkedChipId == R.id.chipVegan) {
            DietType.VEGAN
        } else {
            DietType.NORMAL
        }
    }

    @IdRes
    fun chipIdForDietType(dietType: DietType): Int {
        return when (dietType) {
            DietType.VEGAN -> R.id.chipVegan
            DietType.NORMAL -> R.id.chipNormal
        }
    }
}
