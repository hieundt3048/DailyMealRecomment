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
    private lateinit var adapter: FoodResultAdapter
    private val foodItems = mutableListOf<FoodItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUriString = intent.getStringExtra("image_uri")
        if (imageUriString != null) {
            binding.imgFood.setImageURI(Uri.parse(imageUriString))
        }

        setupRecyclerView()
        
        // Mock data for now, will be replaced by Gemini API results
        mockData()

        binding.btnSave.setOnClickListener {
            Toast.makeText(this, "Saved ${foodItems.size} items to diary", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = FoodResultAdapter(foodItems)
        binding.rvFoodItems.layoutManager = LinearLayoutManager(this)
        binding.rvFoodItems.adapter = adapter
    }

    private fun mockData() {
        foodItems.add(FoodItem("Grilled Chicken", 200, 330, 31.0, 0.0, 15.0))
        foodItems.add(FoodItem("Salad", 100, 50, 2.0, 10.0, 0.5))
        adapter.notifyDataSetChanged()
    }
}
