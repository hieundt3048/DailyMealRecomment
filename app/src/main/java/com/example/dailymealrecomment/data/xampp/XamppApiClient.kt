package com.example.dailymealrecomment.data.xampp

import com.example.dailymealrecomment.BuildConfig
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class XamppApiClient(
    private val baseUrl: String = BuildConfig.XAMPP_API_BASE_URL,
) {
    suspend fun getJson(
        path: String,
        query: Map<String, String> = emptyMap(),
        token: String? = null,
    ): JSONObject = requestJson(
        method = "GET",
        path = path,
        query = query,
        body = null,
        token = token,
    )

    suspend fun postJson(
        path: String,
        body: JSONObject,
        token: String? = null,
    ): JSONObject = requestJson(
        method = "POST",
        path = path,
        query = emptyMap(),
        body = body,
        token = token,
    )

    suspend fun putJson(
        path: String,
        body: JSONObject,
        token: String? = null,
    ): JSONObject = requestJson(
        method = "PUT",
        path = path,
        query = emptyMap(),
        body = body,
        token = token,
    )

    suspend fun deleteJson(
        path: String,
        query: Map<String, String> = emptyMap(),
        token: String? = null,
    ): JSONObject = requestJson(
        method = "DELETE",
        path = path,
        query = query,
        body = null,
        token = token,
    )

    private suspend fun requestJson(
        method: String,
        path: String,
        query: Map<String, String>,
        body: JSONObject?,
        token: String?,
    ): JSONObject = withContext(Dispatchers.IO) {
        val requestQuery = if (token.isNullOrBlank()) {
            query
        } else {
            query + ("_token" to token)
        }
        val connection = (URL(buildUrl(path, requestQuery)).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            if (!token.isNullOrBlank()) {
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("X-Auth-Token", token)
            }
            if (body != null) {
                doOutput = true
            }
        }

        try {
            if (body != null) {
                connection.outputStream.use { output ->
                    output.write(body.toString().toByteArray(Charsets.UTF_8))
                }
            }

            val responseCode = connection.responseCode
            val responseText = readStream(
                if (responseCode in 200..299) connection.inputStream else connection.errorStream,
            )
            val json = runCatching { JSONObject(responseText) }.getOrElse {
                throw XamppApiException("Máy chủ XAMPP trả dữ liệu không hợp lệ.")
            }

            if (responseCode !in 200..299 || !json.optBoolean("success", false)) {
                throw XamppApiException(
                    json.optString("message").ifBlank { "Yêu cầu tới XAMPP thất bại." },
                )
            }

            json
        } finally {
            connection.disconnect()
        }
    }

    private fun buildUrl(path: String, query: Map<String, String>): String {
        val cleanBase = baseUrl.trimEnd('/')
        val cleanPath = path.trimStart('/')
        if (query.isEmpty()) return "$cleanBase/$cleanPath"

        val encodedQuery = query.entries.joinToString("&") { (key, value) ->
            "${key.urlEncode()}=${value.urlEncode()}"
        }
        return "$cleanBase/$cleanPath?$encodedQuery"
    }

    private fun readStream(inputStream: InputStream?): String {
        if (inputStream == null) return ""
        return BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { it.readText() }
    }

    private fun String.urlEncode(): String =
        URLEncoder.encode(this, Charsets.UTF_8.name())

    companion object {
        private const val CONNECT_TIMEOUT_MS = 12_000
        private const val READ_TIMEOUT_MS = 12_000
    }
}
