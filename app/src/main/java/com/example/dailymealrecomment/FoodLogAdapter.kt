package com.example.dailymealrecomment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dailymealrecomment.data.diary.MealLogEntry
import com.example.dailymealrecomment.data.diary.MealLogSection
import com.example.dailymealrecomment.data.diary.MealType
import com.example.dailymealrecomment.databinding.ItemLoggedFoodBinding
import com.example.dailymealrecomment.databinding.ItemMealSectionHeaderBinding

class FoodLogAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<DiaryListItem>()

    fun submitSections(sections: List<MealLogSection>) {
        items.clear()
        sections.forEach { section ->
            items.add(DiaryListItem.SectionHeader(section))
            section.entries.forEach { entry ->
                items.add(DiaryListItem.FoodEntry(entry))
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DiaryListItem.SectionHeader -> VIEW_TYPE_SECTION
            is DiaryListItem.FoodEntry -> VIEW_TYPE_FOOD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SECTION -> SectionViewHolder(
                ItemMealSectionHeaderBinding.inflate(inflater, parent, false),
            )
            else -> FoodViewHolder(
                ItemLoggedFoodBinding.inflate(inflater, parent, false),
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DiaryListItem.SectionHeader -> (holder as SectionViewHolder).bind(item.section)
            is DiaryListItem.FoodEntry -> (holder as FoodViewHolder).bind(item.entry)
        }
    }

    override fun getItemCount(): Int = items.size

    private class SectionViewHolder(
        private val binding: ItemMealSectionHeaderBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(section: MealLogSection) {
            val context = binding.root.context
            binding.tvMealSectionName.setText(section.mealType.titleRes())
            binding.tvMealSectionSummary.text = context.getString(
                R.string.diary_section_summary,
                section.entries.size,
                section.totalCalories,
            )
        }
    }

    private class FoodViewHolder(
        private val binding: ItemLoggedFoodBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: MealLogEntry) {
            val context = binding.root.context
            binding.tvFoodName.text = entry.name
            binding.tvMealDetails.text = context.getString(R.string.diary_food_weight, entry.weight)
            binding.tvCalories.text = context.getString(R.string.diary_food_calories, entry.calories)
        }
    }

    private sealed interface DiaryListItem {
        data class SectionHeader(val section: MealLogSection) : DiaryListItem
        data class FoodEntry(val entry: MealLogEntry) : DiaryListItem
    }

    companion object {
        private const val VIEW_TYPE_SECTION = 0
        private const val VIEW_TYPE_FOOD = 1
    }
}

private fun MealType.titleRes(): Int {
    return when (this) {
        MealType.BREAKFAST -> R.string.meal_breakfast
        MealType.LUNCH -> R.string.meal_lunch
        MealType.DINNER -> R.string.meal_dinner
        MealType.SNACK -> R.string.meal_snack
    }
}
