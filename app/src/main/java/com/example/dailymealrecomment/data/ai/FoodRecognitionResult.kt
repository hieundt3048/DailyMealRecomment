package com.example.dailymealrecomment.data.ai

import com.example.dailymealrecomment.model.FoodItem

sealed class FoodRecognitionResult {
    data class Success(val items: List<FoodItem>) : FoodRecognitionResult()
    object Empty : FoodRecognitionResult()
    data class Failure(
        val reason: FoodRecognitionFailureReason,
        val detail: String? = null,
    ) : FoodRecognitionResult()
}

enum class FoodRecognitionFailureReason {
    MISSING_IMAGE,
    API_NOT_CONFIGURED,
    IMAGE_READ_FAILED,
    TIMEOUT,
    NETWORK_ERROR,
    SERVER_ERROR,
    INVALID_RESPONSE,
}
