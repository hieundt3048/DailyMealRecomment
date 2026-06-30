package com.example.dailymealrecomment

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dailymealrecomment.data.SessionPreferences
import com.example.dailymealrecomment.data.diary.MealLogRepository
import com.example.dailymealrecomment.data.diary.MealType
import com.example.dailymealrecomment.data.model.MealSuggestion
import com.example.dailymealrecomment.data.model.MealSuggestionCatalog
import com.example.dailymealrecomment.databinding.ActivityMealSuggestionDetailBinding
import java.util.Calendar
import kotlinx.coroutines.launch
import kotlin.math.abs

class MealSuggestionDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMealSuggestionDetailBinding
    private lateinit var sessionPreferences: SessionPreferences
    private val mealLogRepository by lazy { MealLogRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealSuggestionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionPreferences = SessionPreferences(this)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = getString(R.string.suggestion_detail_title)

        val suggestion = MealSuggestionCatalog.findById(intent.getStringExtra(EXTRA_SUGGESTION_ID))
        if (suggestion == null) {
            Toast.makeText(this, R.string.suggestion_detail_missing, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        showSuggestion(suggestion)
        binding.btnAddSuggestionToDiary.setOnClickListener {
            saveSuggestionToDiary(suggestion, currentMealType())
        }
    }

    private fun showSuggestion(suggestion: MealSuggestion) {
        val remainingCalories = intent.getIntExtra(EXTRA_REMAINING_CALORIES, 0).coerceAtLeast(0)
        binding.ivSuggestionImage.setImageResource(suggestion.imageResId)
        binding.tvSuggestionName.text = suggestion.name
        binding.tvSuggestionCalories.text = getString(R.string.suggestion_detail_calories, suggestion.calories)
        binding.tvSuggestionServing.text = getString(R.string.suggestion_detail_serving, suggestion.serving)
        binding.tvSuggestionDietTag.setText(
            if (suggestion.isVegan) {
                R.string.home_meal_tag_vegan
            } else {
                R.string.home_meal_tag_normal
            },
        )
        binding.tvSuggestionFit.text = when {
            remainingCalories <= 0 -> getString(R.string.suggestion_fit_goal_reached)
            suggestion.calories <= remainingCalories -> getString(
                R.string.suggestion_fit_within_remaining,
                remainingCalories - suggestion.calories,
            )
            else -> getString(
                R.string.suggestion_fit_over_remaining,
                abs(suggestion.calories - remainingCalories),
            )
        }
        binding.tvIngredients.text = suggestion.ingredients.joinToString(separator = "\n") { ingredient ->
            "• $ingredient"
        }
        binding.tvRecipe.text = suggestion.recipeSteps.mapIndexed { index, step ->
            "${index + 1}. $step"
        }.joinToString(separator = "\n")
    }

    private fun saveSuggestionToDiary(
        suggestion: MealSuggestion,
        mealType: MealType,
    ) {
        val token = sessionPreferences.authToken
        if (token.isNullOrBlank()) {
            Toast.makeText(this, R.string.analysis_save_login_required, Toast.LENGTH_LONG).show()
            return
        }

        setSavingState(isSaving = true)
        lifecycleScope.launch {
            runCatching {
                mealLogRepository.saveMealItems(
                    token = token,
                    items = listOf(suggestion.toFoodItem()),
                    mealType = mealType,
                    sourceImageUri = null,
                )
            }.onSuccess {
                setSavingState(isSaving = false)
                Toast.makeText(
                    this@MealSuggestionDetailActivity,
                    getString(R.string.suggestion_saved_to_diary, suggestion.name),
                    Toast.LENGTH_SHORT,
                ).show()
                openDiary()
            }.onFailure {
                setSavingState(isSaving = false)
                Toast.makeText(
                    this@MealSuggestionDetailActivity,
                    R.string.suggestion_save_failed,
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    private fun currentMealType(): MealType {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..10 -> MealType.BREAKFAST
            in 11..13 -> MealType.LUNCH
            in 14..16 -> MealType.SNACK
            else -> MealType.DINNER
        }
    }

    private fun setSavingState(isSaving: Boolean) {
        binding.btnAddSuggestionToDiary.isEnabled = !isSaving
        binding.btnAddSuggestionToDiary.text = getString(
            if (isSaving) {
                R.string.food_items_saving
            } else {
                R.string.suggestion_add_to_diary
            },
        )
    }

    private fun openDiary() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_START_PAGE, MainActivity.PAGE_DIARY)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
        finish()
    }

    companion object {
        const val EXTRA_SUGGESTION_ID = "com.example.dailymealrecomment.SUGGESTION_ID"
        const val EXTRA_REMAINING_CALORIES = "com.example.dailymealrecomment.REMAINING_CALORIES"
    }
}
