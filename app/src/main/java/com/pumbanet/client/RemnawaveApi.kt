package com.pumbanet.client

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class RemnawaveApi(private val baseUrl: String, private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Авторизация пользователя по логину/паролю
     * Возвращает токен доступа
     */
    fun login(username: String, password: String): String {
        val requestBody = JSONObject().apply {
            put("username", username)
            put("password", password)
        }

        val request = Request.Builder()
            .url("$baseUrl/api/auth/login")
            .post(requestBody.toString().toRequestBody(mediaType))
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("Login failed: ${response.code}")
        }

        val json = JSONObject(responseBody)
        return json.getString("access_token")
    }

    /**
     * Получение списка ключей доступа пользователя
     */
    fun getUserKeys(token: String): List<UserKey> {
        val request = Request.Builder()
            .url("$baseUrl/api/user/keys")
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("Failed to get keys: ${response.code}")
        }

        val json = JSONObject(responseBody)
        val keysArray = json.getJSONArray("keys")

        val keys = mutableListOf<UserKey>()
        for (i in 0 until keysArray.length()) {
            val keyObj = keysArray.getJSONObject(i)
            keys.add(UserKey(
                id = keyObj.getString("id"),
                name = keyObj.optString("name", "Key"),
                vlessLink = keyObj.getString("vlessLink"),
                expiresAt = keyObj.optString("expiresAt", null)
            ))
        }

        return keys
    }

    /**
     * Создание нового ключа доступа
     */
    fun createKey(token: String, name: String, days: Int = 30): UserKey {
        val requestBody = JSONObject().apply {
            put("name", name)
            put("expirationDays", days)
        }

        val request = Request.Builder()
            .url("$baseUrl/api/user/keys")
            .post(requestBody.toString().toRequestBody(mediaType))
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("Failed to create key: ${response.code}")
        }

        val json = JSONObject(responseBody)
        return UserKey(
            id = json.getString("id"),
            name = json.getString("name"),
            vlessLink = json.getString("vlessLink"),
            expiresAt = json.optString("expiresAt", null)
        )
    }

    /**
     * Удаление ключа доступа
     */
    fun deleteKey(token: String, keyId: String) {
        val request = Request.Builder()
            .url("$baseUrl/api/user/keys/$keyId")
            .delete()
            .addHeader("Authorization", "Bearer $token")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Failed to delete key: ${response.code}")
        }
    }

    /**
     * Получение статистики использования
     */
    fun getUsage(token: String): UsageStats {
        val request = Request.Builder()
            .url("$baseUrl/api/user/usage")
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("Failed to get usage: ${response.code}")
        }

        val json = JSONObject(responseBody)
        return UsageStats(
            usedBytes = json.getLong("usedBytes"),
            totalBytes = json.getLong("totalBytes"),
            resetDate = json.optString("resetDate", null)
        )
    }
}

data class UserKey(
    val id: String,
    val name: String,
    val vlessLink: String,
    val expiresAt: String?
)

data class UsageStats(
    val usedBytes: Long,
    val totalBytes: Long,
    val resetDate: String?
)
