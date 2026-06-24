package com.example.btl.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.btl.data.model.DietType
import com.example.btl.data.model.Goal
import com.example.btl.data.model.UserProfile
import com.example.btl.utils.CalorieCalculator

class ProfileViewModel : ViewModel() {

    private val _targetCalories = MutableLiveData<Int>()
    val targetCalories: LiveData<Int> = _targetCalories

    /**
     * Hàm nhận dữ liệu từ Giao diện, tạo Object UserProfile và tính toán Calo
     */
    fun saveUserProfileAndCalculate(
        height: Double,
        weight: Double,
        age: Int,
        isMale: Boolean,
        goal: Goal,
        dietType: DietType
    ) {
        // Giả định hệ số vận động trung bình là 1.375 (Vận động nhẹ)
        val profile = UserProfile(
            heightCm = height,
            weightKg = weight,
            age = age,
            isMale = isMale,
            goal = goal,
            dietType = dietType,
            activityLevel = 1.375
        )

        // Gọi thuật toán tính toán từ file CalorieCalculator
        val calories = CalorieCalculator.calculateDailyCalorieTarget(profile)

        // Đẩy kết quả ra giao diện hiển thị
        _targetCalories.value = calories

        // TODO: Ở đây bạn có thể dùng `dietType` (Vegan/Normal) và `calories`
        // để gửi lên API AI (OpenAI/Gemini...) để sinh thực đơn tự động.
    }
}