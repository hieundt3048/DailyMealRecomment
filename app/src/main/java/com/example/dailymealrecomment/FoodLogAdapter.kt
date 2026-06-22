package com.example.dailymealrecomment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dailymealrecomment.databinding.ItemLoggedFoodBinding
import com.example.dailymealrecomment.model.FoodItem

class FoodLogAdapter(private val items: List<FoodItem>) :
    RecyclerView.Adapter<FoodLogAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLoggedFoodBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLoggedFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvFoodName.text = item.name
            tvMealDetails.text = "Logged • ${item.weight}g"
            tvCalories.text = "${item.calories} kcal"
        }
    }

    override fun getItemCount() = items.size
}
