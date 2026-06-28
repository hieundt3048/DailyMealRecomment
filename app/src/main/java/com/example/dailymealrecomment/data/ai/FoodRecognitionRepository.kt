package com.example.dailymealrecomment.data.ai

import android.content.ContentResolver
import android.net.Uri
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FoodRecognitionRepository(
    private val contentResolver: ContentResolver,
    private val endpointUrl: String,
    private val timeoutMillis: Long,
) {
    suspend fun recognize(imageUri: Uri?): FoodRecognitionResult = withContext(Dispatchers.IO) {
        if (imageUri == null) {
            return@withContext FoodRecognitionResult.Failure(FoodRecognitionFailureReason.MISSING_IMAGE)
        }

        if (endpointUrl.isBlank()) {
            return@withContext FoodRecognitionResult.Failure(FoodRecognitionFailureReason.API_NOT_CONFIGURED)
        }

        val imageBytes = readImageBytes(imageUri)
            ?: return@withContext FoodRecognitionResult.Failure(FoodRecognitionFailureReason.IMAGE_READ_FAILED)
        val mimeType = contentResolver.getType(imageUri) ?: DEFAULT_IMAGE_MIME_TYPE

        runCatching {
            val responseBody = postImage(imageBytes, mimeType)
            val recognizedItems = FoodRecognitionResponseParser.parse(responseBody)
            if (recognizedItems.isEmpty()) {
                FoodRecognitionResult.Empty
            } else {
                FoodRecognitionResult.Success(recognizedItems)
            }
        }.getOrElse { error ->
            when (error) {
                is SocketTimeoutException -> FoodRecognitionResult.Failure(FoodRecognitionFailureReason.TIMEOUT)
                is InvalidRecognitionResponseException -> {
                    FoodRecognitionResult.Failure(FoodRecognitionFailureReason.INVALID_RESPONSE, error.message)
                }
                is ServerRecognitionException -> {
                    FoodRecognitionResult.Failure(FoodRecognitionFailureReason.SERVER_ERROR, error.message)
                }
                is IOException -> FoodRecognitionResult.Failure(FoodRecognitionFailureReason.NETWORK_ERROR, error.message)
                else -> FoodRecognitionResult.Failure(FoodRecognitionFailureReason.INVALID_RESPONSE, error.message)
            }
        }
    }

    private fun readImageBytes(imageUri: Uri): ByteArray? = runCatching {
        contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
    }.getOrNull()

    private fun postImage(imageBytes: ByteArray, mimeType: String): String {
        val boundary = "FoodAI-${System.currentTimeMillis()}"
        val connection = (URL(endpointUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = timeoutMillis.toInt()
            readTimeout = timeoutMillis.toInt()
            doInput = true
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        return try {
            connection.outputStream.use { output ->
                output.writeUtf8("--$boundary\r\n")
                output.writeUtf8(
                    "Content-Disposition: form-data; name=\"image\"; filename=\"food-image\"\r\n",
                )
                output.writeUtf8("Content-Type: $mimeType\r\n\r\n")
                output.write(imageBytes)
                output.writeUtf8("\r\n")
                output.writeUtf8("--$boundary\r\n")
                output.writeUtf8("Content-Disposition: form-data; name=\"expected_fields\"\r\n\r\n")
                output.writeUtf8("name,weight,calories\r\n")
                output.writeUtf8("--$boundary--\r\n")
            }

            val responseCode = connection.responseCode
            if (responseCode !in HTTP_SUCCESS_RANGE) {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                throw ServerRecognitionException("HTTP $responseCode ${errorBody.orEmpty()}".trim())
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            if (responseBody.isBlank()) {
                throw InvalidRecognitionResponseException("Empty AI response")
            }
            responseBody
        } finally {
            connection.disconnect()
        }
    }

    private fun java.io.OutputStream.writeUtf8(value: String) {
        write(value.toByteArray(Charsets.UTF_8))
    }

    companion object {
        private const val DEFAULT_IMAGE_MIME_TYPE = "image/jpeg"
        private val HTTP_SUCCESS_RANGE = 200..299
    }
}

private class ServerRecognitionException(message: String) : IOException(message)

private class InvalidRecognitionResponseException(message: String) : IOException(message)
