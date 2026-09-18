package com.pumbanet.client.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Единый источник правды для конфигов
 * Использует EncryptedSharedPreferences для безопасности
 */
class ConfigManager private constructor(context: Context) {

    private val prefs: SharedPreferences

    init {
        // Создаём EncryptedSharedPreferences для безопасного хранения
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            "pumbanet_config_encrypted",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Сохранить активный конфиг (JSON)
     */
    fun saveActiveConfig(configJson: String) {
        prefs.edit().apply {
            putString(KEY_ACTIVE_CONFIG, configJson)
            putLong(KEY_CONFIG_TIMESTAMP, System.currentTimeMillis())
            apply()
        }
    }

    /**
     * Сохранить vless:// ссылку
     */
    fun saveVlessLink(vlessLink: String) {
        prefs.edit().putString(KEY_VLESS_LINK, vlessLink).apply()
    }

    /**
     * Получить активный конфиг
     */
    fun getActiveConfig(): String? {
        return prefs.getString(KEY_ACTIVE_CONFIG, null)
    }

    /**
     * Получить vless:// ссылку
     */
    fun getVlessLink(): String? {
        return prefs.getString(KEY_VLESS_LINK, null)
    }

    /**
     * Проверить, есть ли конфиг
     */
    fun hasConfig(): Boolean {
        return getActiveConfig() != null || getVlessLink() != null
    }

    /**
     * Получить время последнего обновления конфига
     */
    fun getConfigTimestamp(): Long {
        return prefs.getLong(KEY_CONFIG_TIMESTAMP, 0)
    }

    /**
     * Очистить конфиг
     */
    fun clearConfig() {
        prefs.edit().apply {
            remove(KEY_ACTIVE_CONFIG)
            remove(KEY_VLESS_LINK)
            remove(KEY_CONFIG_TIMESTAMP)
            apply()
        }
    }

    /**
     * Сохранить сервер и токен (для Remnawave API)
     */
    fun saveApiCredentials(serverUrl: String, apiToken: String) {
        prefs.edit().apply {
            putString(KEY_SERVER_URL, serverUrl)
            putString(KEY_API_TOKEN, apiToken)
            apply()
        }
    }

    /**
     * Получить сервер
     */
    fun getServerUrl(): String? {
        return prefs.getString(KEY_SERVER_URL, null)
    }

    /**
     * Получить API токен
     */
    fun getApiToken(): String? {
        return prefs.getString(KEY_API_TOKEN, null)
    }

    /**
     * Проверить авторизацию
     */
    fun isAuthorized(): Boolean {
        return getServerUrl() != null && getApiToken() != null
    }

    /**
     * Очистить авторизацию
     */
    fun clearAuth() {
        prefs.edit().apply {
            remove(KEY_SERVER_URL)
            remove(KEY_API_TOKEN)
            apply()
        }
    }

    companion object {
        private const val KEY_ACTIVE_CONFIG = "active_config"
        private const val KEY_VLESS_LINK = "vless_link"
        private const val KEY_CONFIG_TIMESTAMP = "config_timestamp"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_API_TOKEN = "api_token"

        @Volatile
        private var instance: ConfigManager? = null

        fun getInstance(context: Context): ConfigManager {
            return instance ?: synchronized(this) {
                instance ?: ConfigManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
