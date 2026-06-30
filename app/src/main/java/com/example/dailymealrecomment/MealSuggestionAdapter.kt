package com.example.dailymealrecomment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dailymealrecomment.data.model.MealSuggestion
import com.example.dailymealrecomment.databinding.ItemMealSuggestionBinding
import kotlin.math.abs

class MealSuggestionAdapter(
    private val onSuggestionClick: (MealSuggestion) -> Unit = {},
) : RecyclerView.Adapter<MealSuggestionAdapter.MealSuggestionViewHolder>() {
    private val items = mutableListOf<MealSuggestion>()
    private var remainingCalories: Int = 0

    fun submitList(
        suggestions: List<MealSuggestion>,
        remainingCalories: Int = this.remainingCalories,
    ) {
        items.clear()
        items.addAll(suggestions)
        this.remainingCalories = remainingCalories.coerceAtLeast(0)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealSuggestionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MealSuggestionViewHolder(
            binding = ItemMealSuggestionBinding.inflate(inflater, parent, false),
            onSuggestionClick = onSuggestionClick,
        )
    }

    override fun onBindViewHolder(holder: MealSuggestionViewHolder, position: Int) {
        holder.bind(items[position], remainingCalories)
    }

    override fun getItemCount(): Int = items.size

    class MealSuggestionViewHolder(
        private val binding: ItemMealSuggestionBinding,
        private val onSuggestionClick: (MealSuggestion) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            suggestion: MealSuggestion,
            remainingCalories: Int,
        ) {
            val context = binding.root.context
            binding.ivSuggestionImage.setImageResource(suggestion.imageResId)
            binding.tvSuggestionName.text = suggestion.name
            binding.tvSuggestionServing.text = suggestion.serving
            binding.tvSuggestionCalories.text = context.getString(
                R.string.home_meal_calories,
                suggestion.calories,
            )
            binding.tvSuggestionDietTag.setText(
                if (suggestion.isVegan) {
                    R.string.home_meal_tag_vegan
                } else {
                    R.string.home_meal_tag_normal
                },
            )
            binding.tvSuggestionFit.text = when {
                remainingCalories <= 0 -> context.getString(R.string.suggestion_fit_goal_reached)
                suggestion.calories <= remainingCalories -> context.getString(
                    R.string.suggestion_fit_within_remaining,
                    remainingCalories - suggestion.calories,
                )
                else -> context.getString(
                    R.string.suggestion_fit_over_remaining,
                    abs(suggestion.calories - remainingCalories),
                )
            }
            binding.root.setOnClickListener { onSuggestionClick(suggestion) }
        }
    }
}
