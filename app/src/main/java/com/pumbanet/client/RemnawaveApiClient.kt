package com.pumbanet.client

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class RemnawaveApiClient(private val context: Context) {

    private val prefs = context.getSharedPreferences("pumbanet_config", Context.MODE_PRIVATE)
    
    // Замени на свой API endpoint PumbaNET
    private val baseUrl = "https://pumbanet.yourdomain.com/api/v1"

    data class ApiError(val message: String, val code: Int? = null)

    /**
     * Получение конфига пользователя через Remnawave API
     */
    suspend fun getUserConfig(callback: (String?, ApiError?) -> Unit) = withContext(Dispatchers.IO) {
        try {
            val token = prefs.getString("auth_token", null)
            if (token == null) {
                callback(null, ApiError("Требуется авторизация", 401))
                return@withContext
            }

            val url = URL("$baseUrl/user/config")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Content-Type", "application/json")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use {
                    it.readText()
                }
                
                // Парсинг ответа Remnawave API
                val jsonResponse = JSONObject(response)
                val config = jsonResponse.optString("config", "")
                
                if (config.isNotEmpty()) {
                    callback(config, null)
                } else {
                    callback(null, ApiError("Конфиг не найден", 404))
                }
            } else {
                val errorBody = try {
                    BufferedReader(InputStreamReader(connection.errorStream)).use {
                        it.readText()
                    }
                } catch (e: Exception) {
                    "Unknown error"
                }
                Log.e("RemnawaveApi", "Error: $errorBody")
                callback(null, ApiError("Ошибка API: $responseCode", responseCode))
            }

            connection.disconnect()

        } catch (e: Exception) {
            Log.e("RemnawaveApi", "Exception", e)
            callback(null, ApiError("Ошибка сети: ${e.message}"))
        }
    }

    /**
     * Сохранение токена авторизации
     */
    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    /**
     * Очистка токена при выходе
     */
    fun clearAuthToken() {
        prefs.edit().remove("auth_token").apply()
    }

    /**
     * Проверка авторизации
     */
    fun isAuthorized(): Boolean {
        return prefs.contains("auth_token")
    }
}
