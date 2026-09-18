package com.pumbanet.client.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/**
 * PumbaNET API Client - правильная работа с подписками
 * OkHttp + Retrofit + HTTPS only
 */
interface PumbaNetApi {

    /**
     * Получить информацию о подписке
     */
    @GET("/api/v1/subscription")
    suspend fun getSubscription(
        @Header("Authorization") token: String
    ): SubscriptionResponse

    /**
     * Получить список серверов
     */
    @GET("/api/v1/servers")
    suspend fun getServers(
        @Header("Authorization") token: String
    ): List<ServerResponse>

    /**
     * Получить VLESS конфиг для сервера
     */
    @GET("/api/v1/servers/{serverId}/config")
    suspend fun getServerConfig(
        @Header("Authorization") token: String,
        @Path("serverId") serverId: String
    ): ServerConfigResponse

    /**
     * Обновить трафик
     */
    @POST("/api/v1/subscription/usage")
    suspend fun updateUsage(
        @Header("Authorization") token: String,
        @Body usage: UsageRequest
    ): UsageResponse

    companion object {
        /**
         * Создать API клиент
         */
        fun create(baseUrl: String): PumbaNetApi {
            // Проверка HTTPS
            if (!baseUrl.startsWith("https://")) {
                throw IllegalArgumentException("PumbaNET API requires HTTPS")
            }

            // OkHttp client
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()

            // Retrofit
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PumbaNetApi::class.java)
        }
    }
}

/**
 * Ответ API: подписка
 */
data class SubscriptionResponse(
    val id: String,
    val name: String,
    val trafficTotal: Long,
    val trafficUsed: Long,
    val expiryDate: Long,
    val isActive: Boolean
) {
    fun getTrafficPercent(): Int {
        if (trafficTotal == 0L) return 0
        return ((trafficUsed.toDouble() / trafficTotal) * 100).toInt().coerceIn(0, 100)
    }

    fun getTrafficRemaining(): Long {
        return (trafficTotal - trafficUsed).coerceAtLeast(0)
    }

    fun isExpired(): Boolean {
        return System.currentTimeMillis() > expiryDate
    }

    fun formattedExpiryDate(): String {
        return java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(expiryDate))
    }
}

/**
 * Ответ API: сервер
 */
data class ServerResponse(
    val id: String,
    val name: String,
    val address: String,
    val port: Int,
    val country: String,
    val countryFlag: String,
    val isOnline: Boolean,
    val loadPercent: Int,
    val latencyMs: Int?
)

/**
 * Ответ API: конфиг сервера
 */
data class ServerConfigResponse(
    val serverId: String,
    val vlessLink: String,
    val subscriptionLink: String?
)

/**
 * Запрос: обновление трафика
 */
data class UsageRequest(
    val bytesUploaded: Long,
    val bytesDownloaded: Long
)

/**
 * Ответ: обновление трафика
 */
data class UsageResponse(
    val success: Boolean,
    val trafficUsed: Long
)

/**
 * API ошибки
 */
sealed class ApiError {
    data class HttpError(val code: Int, val message: String) : ApiError()
    data class NetworkError(val message: String) : ApiError()
    data class AuthError(val message: String) : ApiError()
    data class ServerError(val message: String) : ApiError()
    data class UnknownError(val message: String) : ApiError()
}
