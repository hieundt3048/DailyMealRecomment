package com.example.dailymealrecomment.data.xampp

import com.example.dailymealrecomment.data.diary.DiaryLogGrouper
import com.example.dailymealrecomment.data.diary.MealLogEntry
import com.example.dailymealrecomment.data.diary.MealLogEntryFactory
import com.example.dailymealrecomment.data.diary.MealType
import com.example.dailymealrecomment.data.model.DietType
import com.example.dailymealrecomment.data.model.Goal
import com.example.dailymealrecomment.data.model.UserProfile
import com.example.dailymealrecomment.model.FoodItem
import org.json.JSONArray
import org.json.JSONObject

class XamppRepository(
    private val apiClient: XamppApiClient = XamppApiClient(),
) {
    suspend fun login(email: String, password: String): XamppAuthSession {
        return parseAuthSession(
            apiClient.postJson(
                path = "login.php",
                body = JSONObject()
                    .put("email", email)
                    .put("password", password),
            ),
        )
    }

    suspend fun register(name: String, email: String, password: String): XamppAuthSession {
        return parseAuthSession(
            apiClient.postJson(
                path = "register.php",
                body = JSONObject()
                    .put("name", name)
                    .put("email", email)
                    .put("password", password),
            ),
        )
    }

    suspend fun logout(token: String) {
        runCatching {
            apiClient.postJson(path = "logout.php", body = JSONObject(), token = token)
        }
    }

    suspend fun fetchProfile(token: String): XamppProfile? {
        val json = apiClient.getJson(path = "profile.php", token = token)
        val profileJson = json.optJSONObject("profile") ?: return null
        return profileJson.toProfile()
    }

    suspend fun saveProfile(
        token: String,
        profile: UserProfile,
        dailyCalorieTarget: Int,
    ) {
        apiClient.postJson(
            path = "profile.php",
            token = token,
            body = JSONObject()
                .put("heightCm", profile.heightCm)
                .put("weightKg", profile.weightKg)
                .put("age", profile.age)
                .put("isMale", profile.isMale)
                .put("goal", profile.goal.name)
                .put("dietType", profile.dietType.name)
                .put("activityLevel", profile.activityLevel)
                .put("dailyCalorieTarget", dailyCalorieTarget),
        )
    }

    suspend fun saveMealItems(
        token: String,
        items: List<FoodItem>,
        mealType: MealType,
        sourceImageUri: String?,
    ) {
        val entries = MealLogEntryFactory.createEntries(
            items = items,
            mealType = mealType,
            sourceImageUri = sourceImageUri,
        )
        val itemsJson = JSONArray()
        entries.forEach { entry ->
            itemsJson.put(
                JSONObject()
                    .put("name", entry.name)
                    .put("weight", entry.weight)
                    .put("calories", entry.calories)
                    .put("mealType", entry.mealType.storageValue)
                    .put("dateKey", entry.dateKey)
                    .put("sourceImageUri", entry.sourceImageUri)
                    .put("createdAtMillis", entry.createdAtMillis),
            )
        }

        apiClient.postJson(
            path = "meal-logs.php",
            token = token,
            body = JSONObject().put("items", itemsJson),
        )
    }

    suspend fun loadMealItemsForDate(token: String, dateKey: String): List<MealLogEntry> {
        val json = apiClient.getJson(
            path = "meal-logs.php",
            query = mapOf("dateKey" to dateKey),
            token = token,
        )
        val items = json.optJSONArray("items") ?: JSONArray()
        val entries = mutableListOf<MealLogEntry>()
        for (index in 0 until items.length()) {
            val item = items.optJSONObject(index) ?: continue
            val entry = item.toMealLogEntry() ?: continue
            entries.add(entry)
        }
        return DiaryLogGrouper.sortEntries(entries)
    }

    suspend fun loadMealItemsForDateAndMeal(
        token: String,
        dateKey: String,
        mealType: MealType,
    ): List<MealLogEntry> {
        val json = apiClient.getJson(
            path = "meal-logs.php",
            query = mapOf(
                "dateKey" to dateKey,
                "mealType" to mealType.storageValue,
            ),
            token = token,
        )
        val items = json.optJSONArray("items") ?: JSONArray()
        val entries = mutableListOf<MealLogEntry>()
        for (index in 0 until items.length()) {
            val item = items.optJSONObject(index) ?: continue
            val entry = item.toMealLogEntry() ?: continue
            entries.add(entry)
        }
        return DiaryLogGrouper.sortEntries(entries)
    }

    suspend fun updateMealItem(
        token: String,
        entry: MealLogEntry,
    ) {
        val id = requireNotNull(entry.id) { "Meal log id is required for update." }
        apiClient.putJson(
            path = "meal-logs.php",
            token = token,
            body = JSONObject()
                .put("id", id)
                .put("name", entry.name)
                .put("weight", entry.weight)
                .put("calories", entry.calories)
                .put("mealType", entry.mealType.storageValue)
                .put("dateKey", entry.dateKey)
                .put("sourceImageUri", entry.sourceImageUri),
        )
    }

    suspend fun deleteMealItem(
        token: String,
        id: Int,
    ) {
        apiClient.deleteJson(
            path = "meal-logs.php",
            query = mapOf("id" to id.toString()),
            token = token,
        )
    }

    private fun parseAuthSession(json: JSONObject): XamppAuthSession {
        val userJson = json.getJSONObject("user")
        val profileJson = json.optJSONObject("profile")
        return XamppAuthSession(
            token = json.getString("token"),
            user = XamppUser(
                id = userJson.getInt("id"),
                name = userJson.getString("name"),
                email = userJson.getString("email"),
                profileCompleted = userJson.optBoolean("profileCompleted", false),
            ),
            profile = profileJson?.toProfile(),
        )
    }

    private fun JSONObject.toProfile(): XamppProfile {
        val profile = UserProfile(
            heightCm = getDouble("heightCm"),
            weightKg = getDouble("weightKg"),
            age = getInt("age"),
            isMale = optBoolean("isMale", true),
            goal = enumValueOrDefault(optString("goal"), Goal.MAINTAIN_WEIGHT),
            dietType = enumValueOrDefault(optString("dietType"), DietType.NORMAL),
            activityLevel = optDouble("activityLevel", 1.2),
        )
        return XamppProfile(
            profile = profile,
            dailyCalorieTarget = optInt("dailyCalorieTarget", 2_000),
        )
    }

    private fun JSONObject.toMealLogEntry(): MealLogEntry? {
        val mealType = MealType.fromStorage(optString("mealType")) ?: return null
        return MealLogEntry(
            id = optInt("id").takeIf { it > 0 },
            name = optString("name").takeIf { it.isNotBlank() } ?: return null,
            weight = optInt("weight").takeIf { it > 0 } ?: return null,
            calories = optInt("calories").takeIf { it > 0 } ?: return null,
            mealType = mealType,
            dateKey = optString("dateKey").takeIf { it.isNotBlank() } ?: return null,
            sourceImageUri = optNullableString("sourceImageUri"),
            createdAtMillis = optLong("createdAtMillis", 0L),
        )
    }

    private fun JSONObject.optNullableString(name: String): String? {
        if (!has(name) || isNull(name)) return null
        return optString(name).takeIf { it.isNotBlank() }
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, fallback: T): T {
        return runCatching { enumValueOf<T>(value.orEmpty()) }.getOrDefault(fallback)
    }
}
