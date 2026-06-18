package com.example.dailymealrecomment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.dailymealrecomment.databinding.ItemFoodResultBinding
import com.example.dailymealrecomment.model.FoodItem

class FoodResultAdapter(private val items: MutableList<FoodItem>) :
    RecyclerView.Adapter<FoodResultAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemFoodResultBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            etFoodName.setText(item.name)
            etWeight.setText(item.weight.toString())
            etCalories.setText(item.calories.toString())
            etProtein.setText(item.protein.toString())
            etCarbs.setText(item.carbs.toString())
            etFat.setText(item.fat.toString())

            etFoodName.doAfterTextChanged { item.name = it.toString() }
            etWeight.doAfterTextChanged { item.weight = it.toString().toIntOrNull() ?: 0 }
            etCalories.doAfterTextChanged { item.calories = it.toString().toIntOrNull() ?: 0 }
            etProtein.doAfterTextChanged { item.protein = it.toString().toDoubleOrNull() ?: 0.0 }
            etCarbs.doAfterTextChanged { item.carbs = it.toString().toDoubleOrNull() ?: 0.0 }
            etFat.doAfterTextChanged { item.fat = it.toString().toDoubleOrNull() ?: 0.0 }
        }
    }

    override fun getItemCount() = items.size
}
