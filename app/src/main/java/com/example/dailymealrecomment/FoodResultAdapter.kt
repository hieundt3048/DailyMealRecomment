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
        return ViewHolder(
            ItemFoodResultBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            etFoodName.setText(item.name)
            etWeight.setText(item.weight.toString())
            etCalories.setText(item.calories.toString())
            etFoodName.doAfterTextChanged { item.name = it.toString() }
            etWeight.doAfterTextChanged { item.weight = it.toString().toIntOrNull() ?: 0 }
            etCalories.doAfterTextChanged { item.calories = it.toString().toIntOrNull() ?: 0 }
        }
    }

    override fun getItemCount(): Int = items.size
}
