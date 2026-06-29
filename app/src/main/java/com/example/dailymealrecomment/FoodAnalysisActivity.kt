package com.example.dailymealrecomment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailymealrecomment.data.SessionPreferences
import com.example.dailymealrecomment.data.ai.FoodRecognitionFailureReason
import com.example.dailymealrecomment.data.ai.FoodRecognitionRepository
import com.example.dailymealrecomment.data.ai.FoodRecognitionResult
import com.example.dailymealrecomment.data.diary.MealLogRepository
import com.example.dailymealrecomment.data.diary.MealType
import com.example.dailymealrecomment.data.xampp.XamppRepository
import com.example.dailymealrecomment.databinding.ActivityFoodAnalysisBinding
import com.example.dailymealrecomment.model.FoodItem
import com.example.dailymealrecomment.utilities.FoodCalorieAdjuster
import com.example.dailymealrecomment.utilities.FoodNameValidationResult
import com.example.dailymealrecomment.utilities.FoodNameValidator
import kotlinx.coroutines.launch

class FoodAnalysisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodAnalysisBinding
    private lateinit var adapter: FoodResultAdapter
    private lateinit var sessionPreferences: SessionPreferences
    private val foodItems = mutableListOf<FoodItem>()
    private val imageUri: Uri? by lazy {
        intent.getStringExtra(EXTRA_IMAGE_URI)?.let(Uri::parse)
    }
    private val foodRecognitionRepository by lazy {
        FoodRecognitionRepository(
            contentResolver = contentResolver,
            endpointUrl = BuildConfig.FOOD_AI_ENDPOINT,
            timeoutMillis = BuildConfig.FOOD_AI_TIMEOUT_MS,
        )
    }
    private val mealLogRepository by lazy {
        MealLogRepository(XamppRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionPreferences = SessionPreferences(this)

        imageUri?.let(binding.imgFood::setImageURI)

        adapter = FoodResultAdapter(foodItems)
        binding.rvFoodItems.layoutManager = LinearLayoutManager(this)
        binding.rvFoodItems.adapter = adapter

        binding.btnRetry.setOnClickListener { analyzeFoodImage(forceSuccessfulRetry = true) }
        binding.btnRetryEmpty.setOnClickListener { analyzeFoodImage(forceSuccessfulRetry = true) }
        binding.btnSave.setOnClickListener { confirmFoodItems() }

        analyzeFoodImage()
    }

    private fun analyzeFoodImage(forceSuccessfulRetry: Boolean = false) {
        showLoading()

        when {
            !forceSuccessfulRetry && intent.getBooleanExtra(EXTRA_FORCE_ERROR, false) -> {
                binding.root.post { showError(getString(R.string.analysis_error_message)) }
            }
            !forceSuccessfulRetry && intent.getBooleanExtra(EXTRA_FORCE_EMPTY, false) -> {
                binding.root.post { showEmpty() }
            }
            forceSuccessfulRetry || shouldUseDemoRecognitionResult() -> {
                binding.root.post { showResults(demoRecognizedFoodItems()) }
            }
            else -> {
                lifecycleScope.launch {
                    when (val result = foodRecognitionRepository.recognize(imageUri)) {
                        is FoodRecognitionResult.Success -> showResults(result.items)
                        FoodRecognitionResult.Empty -> showEmpty()
                        is FoodRecognitionResult.Failure -> showError(result.toUserMessage())
                    }
                }
            }
        }
    }

    private fun shouldUseDemoRecognitionResult(): Boolean =
        intent.getBooleanExtra(EXTRA_USE_DEMO_RESULT, false) || imageUri == null

    private fun demoRecognizedFoodItems(): List<FoodItem> =
        listOf(FoodItem("Món ăn nhận diện", 100, 200))

    private fun confirmFoodItems() {
        val token = sessionPreferences.authToken
        if (token.isNullOrBlank()) {
            Toast.makeText(this, R.string.analysis_save_login_required, Toast.LENGTH_LONG).show()
            return
        }

        val normalizedItems = mutableListOf<FoodItem>()

        for (foodItem in foodItems) {
            when (val result = FoodNameValidator.validate(foodItem.name)) {
                is FoodNameValidationResult.Valid -> {
                    if (!foodItem.hasSaveableCalories()) {
                        Toast.makeText(this, R.string.analysis_save_invalid_food, Toast.LENGTH_SHORT).show()
                        adapter.notifyDataSetChanged()
                        return
                    }
                    foodItem.name = result.normalizedName
                    normalizedItems.add(foodItem)
                }
                is FoodNameValidationResult.Invalid -> {
                    Toast.makeText(this, R.string.food_name_save_invalid, Toast.LENGTH_SHORT).show()
                    adapter.notifyDataSetChanged()
                    return
                }
            }
        }

        foodItems.clear()
        foodItems.addAll(normalizedItems)
        adapter.notifyDataSetChanged()
        setSaveInProgress(true)
        lifecycleScope.launch {
            runCatching {
                mealLogRepository.saveMealItems(
                    token = token,
                    items = foodItems,
                    mealType = selectedMealType(),
                    sourceImageUri = imageUri?.toString(),
                )
            }.onSuccess {
                setSaveInProgress(false)
                Toast.makeText(this@FoodAnalysisActivity, getString(R.string.food_items_saved, foodItems.size), Toast.LENGTH_SHORT).show()
                openDiary()
            }.onFailure {
                setSaveInProgress(false)
                Toast.makeText(this@FoodAnalysisActivity, R.string.food_items_save_failed, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun FoodItem.hasSaveableCalories(): Boolean =
        weight in FoodCalorieAdjuster.MIN_WEIGHT_GRAMS..FoodCalorieAdjuster.MAX_WEIGHT_GRAMS &&
            calories in FoodCalorieAdjuster.MIN_CALORIES..FoodCalorieAdjuster.MAX_CALORIES

    private fun selectedMealType(): MealType {
        return when (binding.mealChipGroup.checkedChipId) {
            R.id.chipBreakfast -> MealType.BREAKFAST
            R.id.chipDinner -> MealType.DINNER
            R.id.chipSnack -> MealType.SNACK
            else -> MealType.LUNCH
        }
    }

    private fun setSaveInProgress(isSaving: Boolean) {
        binding.btnSave.isEnabled = !isSaving && foodItems.isNotEmpty()
        binding.btnSave.text = getString(
            if (isSaving) R.string.food_items_saving else R.string.food_items_save_to_diary,
        )
        binding.mealChipGroup.isEnabled = !isSaving
        binding.chipBreakfast.isEnabled = !isSaving
        binding.chipLunch.isEnabled = !isSaving
        binding.chipDinner.isEnabled = !isSaving
        binding.chipSnack.isEnabled = !isSaving
    }

    private fun openDiary() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_START_PAGE, MainActivity.PAGE_DIARY)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
        finish()
    }

    private fun showLoading() {
        binding.loadingState.visibility = View.VISIBLE
        binding.errorState.visibility = View.GONE
        binding.emptyState.visibility = View.GONE
        binding.contentState.visibility = View.GONE
        binding.btnSave.isEnabled = false
    }

    private fun showResults(items: List<FoodItem>) {
        foodItems.clear()
        foodItems.addAll(items)
        adapter.notifyDataSetChanged()

        binding.loadingState.visibility = View.GONE
        binding.errorState.visibility = View.GONE
        binding.emptyState.visibility = View.GONE
        binding.contentState.visibility = View.VISIBLE
        binding.btnSave.isEnabled = foodItems.isNotEmpty()
    }

    private fun showEmpty() {
        foodItems.clear()
        adapter.notifyDataSetChanged()

        binding.loadingState.visibility = View.GONE
        binding.errorState.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE
        binding.contentState.visibility = View.GONE
        binding.btnSave.isEnabled = false
    }

    private fun showError(message: String) {
        foodItems.clear()
        adapter.notifyDataSetChanged()

        binding.loadingState.visibility = View.GONE
        binding.errorState.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE
        binding.contentState.visibility = View.GONE
        binding.tvErrorMessage.text = message
        binding.btnSave.isEnabled = false
    }

    private fun FoodRecognitionResult.Failure.toUserMessage(): String {
        return when (reason) {
            FoodRecognitionFailureReason.MISSING_IMAGE -> getString(R.string.analysis_missing_image)
            FoodRecognitionFailureReason.API_NOT_CONFIGURED -> getString(R.string.analysis_api_not_configured)
            FoodRecognitionFailureReason.IMAGE_READ_FAILED -> getString(R.string.analysis_image_read_failed)
            FoodRecognitionFailureReason.TIMEOUT -> getString(R.string.analysis_timeout)
            FoodRecognitionFailureReason.NETWORK_ERROR -> getString(R.string.analysis_network_error)
            FoodRecognitionFailureReason.SERVER_ERROR -> getString(R.string.analysis_server_error)
            FoodRecognitionFailureReason.INVALID_RESPONSE -> getString(R.string.analysis_invalid_response)
        }
    }

    companion object {
        const val EXTRA_IMAGE_URI = "image_uri"
        const val EXTRA_FORCE_EMPTY = "force_empty"
        const val EXTRA_FORCE_ERROR = "force_error"
        const val EXTRA_USE_DEMO_RESULT = "use_demo_result"
    }
}
