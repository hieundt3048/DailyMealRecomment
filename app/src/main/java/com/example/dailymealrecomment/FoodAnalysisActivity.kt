package com.example.dailymealrecomment

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailymealrecomment.databinding.ActivityFoodAnalysisBinding
import com.example.dailymealrecomment.model.FoodItem

class FoodAnalysisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodAnalysisBinding
    private val foodItems = mutableListOf(FoodItem("Món ăn nhận diện", 100, 200))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getStringExtra(EXTRA_IMAGE_URI)?.let { uri ->
            binding.imgFood.setImageURI(Uri.parse(uri))
        }
        binding.rvFoodItems.layoutManager = LinearLayoutManager(this)
        binding.rvFoodItems.adapter = FoodResultAdapter(foodItems)
        binding.btnSave.setOnClickListener {
            Toast.makeText(this, getString(R.string.food_items_ready, foodItems.size), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        const val EXTRA_IMAGE_URI = "image_uri"
    }
}
