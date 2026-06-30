package com.example.dailymealrecomment

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dailymealrecomment.data.model.MealSuggestion
import com.example.dailymealrecomment.data.model.MealSuggestionCatalog
import com.example.dailymealrecomment.databinding.ActivityMealSuggestionDetailBinding
import kotlin.math.abs

class MealSuggestionDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMealSuggestionDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealSuggestionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = getString(R.string.suggestion_detail_title)

        val suggestion = MealSuggestionCatalog.findById(intent.getStringExtra(EXTRA_SUGGESTION_ID))
        if (suggestion == null) {
            Toast.makeText(this, R.string.suggestion_detail_missing, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        showSuggestion(suggestion)
    }

    private fun showSuggestion(suggestion: MealSuggestion) {
        val remainingCalories = intent.getIntExtra(EXTRA_REMAINING_CALORIES, 0).coerceAtLeast(0)
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

    companion object {
        const val EXTRA_SUGGESTION_ID = "com.example.dailymealrecomment.SUGGESTION_ID"
        const val EXTRA_REMAINING_CALORIES = "com.example.dailymealrecomment.REMAINING_CALORIES"
    }
}
