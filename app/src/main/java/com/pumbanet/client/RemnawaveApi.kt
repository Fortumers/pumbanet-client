package com.pumbanet.client

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Клиент для взаимодействия с Remnawave API
 * Документация: https://docs.rw/api/
 */
class RemnawaveApi(
    private val baseUrl: String,
    private val apiToken: String
) {

    companion object {
        private const val TAG = "RemnawaveApi"
        private const val CONNECT_TIMEOUT = 10000
        private const val READ_TIMEOUT = 30000
    }

    /**
     * Авторизация по API токену и получение информации о пользователе
     */
    suspend fun authenticate(): ApiAuthResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/user")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                setRequestProperty("Authorization", "Bearer $apiToken")
                setRequestProperty("Content-Type", "application/json")
            }

            val responseCode = connection.responseCode
            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }

            if (responseCode == 200) {
                val userJson = JSONObject(responseBody)
                ApiAuthResult.Success(
                    userId = userJson.optString("id", "unknown"),
                    username = userJson.optString("username", "user")
                )
            } else {
                ApiAuthResult.Error("Ошибка авторизации: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка авторизации Remnawave", e)
            ApiAuthResult.Error("Ошибка сети: ${e.message}")
        }
    }

    /**
     * Получение списка подписок пользователя
     */
    suspend fun getSubscriptions(): ApiResult<List<Subscription>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/subscriptions")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                setRequestProperty("Authorization", "Bearer $apiToken")
                setRequestProperty("Content-Type", "application/json")
            }

            val responseCode = connection.responseCode
            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }

            if (responseCode == 200) {
                val subscriptions = parseSubscriptions(responseBody)
                ApiResult.Success(subscriptions)
            } else {
                ApiResult.Error("Ошибка получения подписок: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка получения подписок", e)
            ApiResult.Error("Ошибка сети: ${e.message}")
        }
    }

    /**
     * Получение конфига подписки (Vless ссылка)
     */
    suspend fun getSubscriptionConfig(subscriptionId: String): ApiResult<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/subscriptions/$subscriptionId/config")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                setRequestProperty("Authorization", "Bearer $apiToken")
                setRequestProperty("Content-Type", "application/json")
            }

            val responseCode = connection.responseCode
            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }

            if (responseCode == 200) {
                val configJson = JSONObject(responseBody)
                val vlessLink = configJson.optString("vlessLink", "")
                if (vlessLink.isNotEmpty()) {
                    ApiResult.Success(vlessLink)
                } else {
                    ApiResult.Error("Пустой конфиг")
                }
            } else {
                ApiResult.Error("Ошибка получения конфига: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка получения конфига", e)
            ApiResult.Error("Ошибка сети: ${e.message}")
        }
    }

    /**
     * Получение подписки по ссылке (subscription URL)
     */
    suspend fun getSubscriptionByLink(subscriptionUrl: String): ApiResult<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL(subscriptionUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                instanceFollowRedirects = true
            }

            val responseCode = connection.responseCode
            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }

            if (responseCode == 200) {
                // Возвращает base64 закодированный список подписок
                ApiResult.Success(responseBody)
            } else {
                ApiResult.Error("Ошибка получения подписки: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка получения подписки по ссылке", e)
            ApiResult.Error("Ошибка сети: ${e.message}")
        }
    }

    private fun parseSubscriptions(jsonString: String): List<Subscription> {
        val subscriptions = mutableListOf<Subscription>()
        try {
            val jsonArray = JSONObject(jsonString).getJSONArray("data")
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                subscriptions.add(
                    Subscription(
                        id = obj.optString("id", ""),
                        name = obj.optString("name", "Подписка"),
                        trafficTotal = obj.optLong("trafficTotal", 0),
                        trafficUsed = obj.optLong("trafficUsed", 0),
                        expiryDate = obj.optString("expiryDate", "")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка парсинга подписок", e)
        }
        return subscriptions
    }
}

// Модели данных
data class Subscription(
    val id: String,
    val name: String,
    val trafficTotal: Long,
    val trafficUsed: Long,
    val expiryDate: String
) {
    fun getTrafficPercent(): Float {
        return if (trafficTotal > 0) (trafficUsed.toFloat() / trafficTotal) * 100 else 0f
    }

    fun getTrafficUsedFormatted(): String {
        return formatBytes(trafficUsed)
    }

    fun getTrafficTotalFormatted(): String {
        return formatBytes(trafficTotal)
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}

// Результаты API
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}

sealed class ApiAuthResult {
    data class Success(val userId: String, val username: String) : ApiAuthResult()
    data class Error(val message: String) : ApiAuthResult()
}
