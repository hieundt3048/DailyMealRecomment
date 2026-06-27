package com.example.dailymealrecomment

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailymealrecomment.databinding.ActivityFoodAnalysisBinding
import com.example.dailymealrecomment.model.FoodItem

class FoodAnalysisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodAnalysisBinding
    private lateinit var adapter: FoodResultAdapter
    private val foodItems = mutableListOf<FoodItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getStringExtra(EXTRA_IMAGE_URI)?.let { uri ->
            binding.imgFood.setImageURI(Uri.parse(uri))
        }

        adapter = FoodResultAdapter(foodItems)
        binding.rvFoodItems.layoutManager = LinearLayoutManager(this)
        binding.rvFoodItems.adapter = adapter

        binding.btnRetry.setOnClickListener { analyzeFoodImage(forceSuccessfulRetry = true) }
        binding.btnRetryEmpty.setOnClickListener { analyzeFoodImage(forceSuccessfulRetry = true) }
        binding.btnSave.setOnClickListener {
            Toast.makeText(this, getString(R.string.food_items_ready, foodItems.size), Toast.LENGTH_SHORT).show()
            finish()
        }

        analyzeFoodImage()
    }

    private fun analyzeFoodImage(forceSuccessfulRetry: Boolean = false) {
        showLoading()
        binding.root.post {
            when {
                !forceSuccessfulRetry && intent.getBooleanExtra(EXTRA_FORCE_ERROR, false) -> {
                    showError(getString(R.string.analysis_error_message))
                }
                !forceSuccessfulRetry && intent.getBooleanExtra(EXTRA_FORCE_EMPTY, false) -> {
                    showEmpty()
                }
                else -> showResults(mockRecognizedFoodItems())
            }
        }
    }

    private fun mockRecognizedFoodItems(): List<FoodItem> {
        return listOf(FoodItem("Món ăn nhận diện", 100, 200))
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

    companion object {
        const val EXTRA_IMAGE_URI = "image_uri"
        const val EXTRA_FORCE_EMPTY = "force_empty"
        const val EXTRA_FORCE_ERROR = "force_error"
    }
}
