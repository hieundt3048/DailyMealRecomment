package com.example.btl.utils

import com.example.btl.data.model.Goal
import com.example.btl.data.model.UserProfile

object CalorieCalculator {

    /**
     * Tính tổng lượng Calo cần thiết hàng ngày (TDEE) dựa trên hồ sơ người dùng
     */
    fun calculateDailyCalorieTarget(profile: UserProfile): Int {
        // 1. Tính BMR (Tỷ lệ trao đổi chất cơ bản) theo công thức Mifflin-St Jeor
        val bmr = if (profile.isMale) {
            (10 * profile.weightKg) + (6.25 * profile.heightCm) - (5 * profile.age) + 5
        } else {
            (10 * profile.weightKg) + (6.25 * profile.heightCm) - (5 * profile.age) - 161
        }

        // 2. Tính TDEE (Calo để giữ cân) = BMR * Hệ số vận động
        val tdee = bmr * profile.activityLevel

        // 3. Điều chỉnh Calo theo Mục tiêu (Goal)
        val finalCalories = when (profile.goal) {
            Goal.LOSE_WEIGHT -> tdee - 500  // Thâm hụt 500 calo để giảm cân an toàn
            Goal.GAIN_WEIGHT -> tdee + 500  // Thặng dư 500 calo để tăng cân
            Goal.MAINTAIN_WEIGHT -> tdee   // Giữ nguyên calo để giữ dáng
        }

        // Trả về kết quả kiểu Số nguyên (Int), tối thiểu là 1200 calo để đảm bảo sức khỏe
        return maxOf(finalCalories.toInt(), 1200)
    }
}