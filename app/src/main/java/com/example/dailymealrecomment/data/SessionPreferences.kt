package com.example.dailymealrecomment.data

import android.content.Context
import com.example.dailymealrecomment.data.model.DietType
import com.example.dailymealrecomment.data.model.Goal
import com.example.dailymealrecomment.data.model.UserProfile

class SessionPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    val isProfileCompleted: Boolean
        get() = preferences.getBoolean(KEY_PROFILE_COMPLETED, false)

    val dailyCalorieTarget: Int
        get() = preferences.getInt(KEY_DAILY_CALORIE_TARGET, DEFAULT_CALORIE_TARGET)

    fun saveProfile(profile: UserProfile, calorieTarget: Int) {
        preferences.edit()
            .putBoolean(KEY_PROFILE_COMPLETED, true)
            .putFloat(KEY_HEIGHT_CM, profile.heightCm.toFloat())
            .putFloat(KEY_WEIGHT_KG, profile.weightKg.toFloat())
            .putInt(KEY_AGE, profile.age)
            .putBoolean(KEY_IS_MALE, profile.isMale)
            .putString(KEY_GOAL, profile.goal.name)
            .putString(KEY_DIET_TYPE, profile.dietType.name)
            .putFloat(KEY_ACTIVITY_LEVEL, profile.activityLevel.toFloat())
            .putInt(KEY_DAILY_CALORIE_TARGET, calorieTarget)
            .apply()
    }

    fun cachedProfile(): UserProfile? {
        if (!isProfileCompleted) return null
        return UserProfile(
            heightCm = preferences.getFloat(KEY_HEIGHT_CM, 0f).toDouble(),
            weightKg = preferences.getFloat(KEY_WEIGHT_KG, 0f).toDouble(),
            age = preferences.getInt(KEY_AGE, 0),
            isMale = preferences.getBoolean(KEY_IS_MALE, true),
            goal = enumValueOrDefault(preferences.getString(KEY_GOAL, null), Goal.MAINTAIN_WEIGHT),
            dietType = enumValueOrDefault(preferences.getString(KEY_DIET_TYPE, null), DietType.NORMAL),
            activityLevel = preferences.getFloat(KEY_ACTIVITY_LEVEL, 1.2f).toDouble(),
        )
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, fallback: T): T {
        return runCatching { enumValueOf<T>(value.orEmpty()) }.getOrDefault(fallback)
    }

    companion object {
        const val DEFAULT_CALORIE_TARGET = 2_000
        private const val PREFERENCES_NAME = "food_ai_session"
        private const val KEY_PROFILE_COMPLETED = "profile_completed"
        private const val KEY_HEIGHT_CM = "height_cm"
        private const val KEY_WEIGHT_KG = "weight_kg"
        private const val KEY_AGE = "age"
        private const val KEY_IS_MALE = "is_male"
        private const val KEY_GOAL = "goal"
        private const val KEY_DIET_TYPE = "diet_type"
        private const val KEY_ACTIVITY_LEVEL = "activity_level"
        private const val KEY_DAILY_CALORIE_TARGET = "daily_calorie_target"
    }
}
