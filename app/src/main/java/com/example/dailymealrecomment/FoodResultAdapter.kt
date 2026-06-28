package com.example.dailymealrecomment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.dailymealrecomment.databinding.ItemFoodResultBinding
import com.example.dailymealrecomment.model.FoodItem
import com.example.dailymealrecomment.utilities.CalorieValidationError
import com.example.dailymealrecomment.utilities.CalorieValidationResult
import com.example.dailymealrecomment.utilities.FoodCalorieAdjuster
import com.example.dailymealrecomment.utilities.FoodNameValidationError
import com.example.dailymealrecomment.utilities.FoodNameValidationResult
import com.example.dailymealrecomment.utilities.FoodNameValidator
import com.example.dailymealrecomment.utilities.WeightAdjustmentResult
import com.example.dailymealrecomment.utilities.WeightValidationError

class FoodResultAdapter(private val items: MutableList<FoodItem>) :
    RecyclerView.Adapter<FoodResultAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemFoodResultBinding) : RecyclerView.ViewHolder(binding.root) {
        private var isBinding = false

        init {
            binding.etFoodName.doAfterTextChanged {
                if (!isBinding) handleFoodNameChanged(it.toString())
            }

            binding.etFoodName.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus && !isBinding) normalizeFoodNameField()
            }

            binding.etWeight.doAfterTextChanged {
                if (!isBinding) handleWeightChanged(it.toString())
            }

            binding.etCalories.doAfterTextChanged {
                if (!isBinding) handleCaloriesChanged(it.toString())
            }
        }

        fun bind(item: FoodItem) {
            if (item.caloriesPerGram <= 0.0) {
                item.caloriesPerGram = FoodCalorieAdjuster.caloriesPerGram(item.weight, item.calories)
            }

            isBinding = true
            binding.tilFoodName.error = null
            binding.tilWeight.error = null
            binding.tilCalories.error = null
            binding.etFoodName.setText(item.name)
            binding.etWeight.setText(item.weight.takeIf { it > 0 }?.toString().orEmpty())
            binding.etCalories.setText(item.calories.takeIf { it > 0 }?.toString().orEmpty())
            isBinding = false
        }

        private fun handleFoodNameChanged(rawName: String) {
            val item = currentItem() ?: return
            when (val result = FoodNameValidator.validate(rawName)) {
                is FoodNameValidationResult.Valid -> {
                    item.name = result.normalizedName
                    binding.tilFoodName.error = null
                }
                is FoodNameValidationResult.Invalid -> {
                    binding.tilFoodName.error = foodNameErrorMessage(result.error)
                }
            }
        }

        private fun normalizeFoodNameField() {
            val item = currentItem() ?: return
            when (val result = FoodNameValidator.validate(binding.etFoodName.text?.toString().orEmpty())) {
                is FoodNameValidationResult.Valid -> {
                    item.name = result.normalizedName
                    binding.tilFoodName.error = null
                    setFoodNameText(result.normalizedName)
                }
                is FoodNameValidationResult.Invalid -> {
                    binding.tilFoodName.error = foodNameErrorMessage(result.error)
                }
            }
        }

        private fun handleWeightChanged(weightText: String) {
            val item = currentItem() ?: return
            val caloriesPerGram = item.caloriesPerGram.takeIf { it > 0.0 }
                ?: FoodCalorieAdjuster.caloriesPerGram(item.weight, item.calories)

            when (val result = FoodCalorieAdjuster.recalculateCalories(weightText, caloriesPerGram)) {
                is WeightAdjustmentResult.Valid -> {
                    item.weight = result.weight
                    item.calories = result.calories
                    item.caloriesPerGram = caloriesPerGram
                    binding.tilWeight.error = null
                    binding.tilCalories.error = null
                    setCaloriesText(result.calories)
                }
                is WeightAdjustmentResult.Invalid -> {
                    binding.tilWeight.error = weightErrorMessage(result.error)
                }
            }
        }

        private fun handleCaloriesChanged(calorieText: String) {
            val item = currentItem() ?: return
            when (val result = FoodCalorieAdjuster.validateManualCalories(calorieText)) {
                is CalorieValidationResult.Valid -> {
                    item.calories = result.calories
                    item.caloriesPerGram = FoodCalorieAdjuster.caloriesPerGram(item.weight, result.calories)
                    binding.tilCalories.error = null
                }
                is CalorieValidationResult.Invalid -> {
                    binding.tilCalories.error = calorieErrorMessage(result.error)
                }
            }
        }

        private fun setCaloriesText(calories: Int) {
            val caloriesText = calories.toString()
            if (binding.etCalories.text?.toString() == caloriesText) return

            isBinding = true
            binding.etCalories.setText(caloriesText)
            binding.etCalories.setSelection(caloriesText.length)
            isBinding = false
        }

        private fun setFoodNameText(foodName: String) {
            if (binding.etFoodName.text?.toString() == foodName) return

            isBinding = true
            binding.etFoodName.setText(foodName)
            binding.etFoodName.setSelection(foodName.length)
            isBinding = false
        }

        private fun currentItem(): FoodItem? {
            val position = bindingAdapterPosition
            return if (position != RecyclerView.NO_POSITION) items.getOrNull(position) else null
        }

        private fun foodNameErrorMessage(error: FoodNameValidationError): String {
            val context = binding.root.context
            return when (error) {
                FoodNameValidationError.EMPTY -> context.getString(R.string.food_name_error_empty)
                FoodNameValidationError.TOO_LONG -> context.getString(
                    R.string.food_name_error_too_long,
                    FoodNameValidator.MAX_NAME_LENGTH,
                )
            }
        }

        private fun weightErrorMessage(error: WeightValidationError): String {
            val context = binding.root.context
            return when (error) {
                WeightValidationError.NOT_A_NUMBER -> context.getString(R.string.food_weight_error_not_number)
                WeightValidationError.TOO_SMALL -> context.getString(R.string.food_weight_error_too_small)
                WeightValidationError.TOO_LARGE -> context.getString(
                    R.string.food_weight_error_too_large,
                    FoodCalorieAdjuster.MAX_WEIGHT_GRAMS,
                )
                WeightValidationError.MISSING_CALORIE_RATIO -> {
                    context.getString(R.string.food_weight_error_missing_ratio)
                }
            }
        }

        private fun calorieErrorMessage(error: CalorieValidationError): String {
            val context = binding.root.context
            return when (error) {
                CalorieValidationError.NOT_A_NUMBER -> context.getString(R.string.food_calorie_error_not_number)
                CalorieValidationError.TOO_SMALL -> context.getString(R.string.food_calorie_error_too_small)
                CalorieValidationError.TOO_LARGE -> context.getString(
                    R.string.food_calorie_error_too_large,
                    FoodCalorieAdjuster.MAX_CALORIES,
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFoodResultBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
