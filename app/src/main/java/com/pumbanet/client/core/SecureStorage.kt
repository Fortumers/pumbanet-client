package com.pumbanet.client.core

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Secure Storage - безопасное хранение credential'ов
 * Android Keystore + EncryptedSharedPreferences
 */
class SecureStorage private constructor(context: Context) {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs: EncryptedSharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "pumbanet_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
    }

    /**
     * Сохранить API токен (шифруется)
     */
    fun saveApiToken(apiToken: String) {
        encryptedPrefs.edit().apply {
            putString(KEY_API_TOKEN, apiToken)
            apply()
        }
    }

    /**
     * Получить API токен
     */
    fun getApiToken(): String? {
        return encryptedPrefs.getString(KEY_API_TOKEN, null)
    }

    /**
     * Сохранить VLESS конфиг (шифруется)
     */
    fun saveVlessConfig(vlessLink: String) {
        encryptedPrefs.edit().apply {
            putString(KEY_VLESS_CONFIG, vlessLink)
            putLong(KEY_CONFIG_TIMESTAMP, System.currentTimeMillis())
            apply()
        }
    }

    /**
     * Получить VLESS конфиг
     */
    fun getVlessConfig(): String? {
        return encryptedPrefs.getString(KEY_VLESS_CONFIG, null)
    }

    /**
     * Получить время последнего обновления конфига
     */
    fun getConfigTimestamp(): Long {
        return encryptedPrefs.getLong(KEY_CONFIG_TIMESTAMP, 0)
    }

    /**
     * Проверить наличие конфига
     */
    fun hasConfig(): Boolean {
        return getVlessConfig() != null
    }

    /**
     * Очистить все credential'ы
     */
    fun clearAll() {
        encryptedPrefs.edit().apply {
            clear()
            apply()
        }
    }

    /**
     * Удалить API токен
     */
    fun removeApiToken() {
        encryptedPrefs.edit().apply {
            remove(KEY_API_TOKEN)
            apply()
        }
    }

    /**
     * Удалить конфиг
     */
    fun removeConfig() {
        encryptedPrefs.edit().apply {
            remove(KEY_VLESS_CONFIG)
            remove(KEY_CONFIG_TIMESTAMP)
            apply()
        }
    }

    /**
     * Проверка доступности Keystore
     */
    fun isKeystoreAvailable(): Boolean {
        return try {
            keyStore.load(null)
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val KEY_API_TOKEN = "api_token"
        private const val KEY_VLESS_CONFIG = "vless_config"
        private const val KEY_CONFIG_TIMESTAMP = "config_timestamp"

        @Volatile
        private var instance: SecureStorage? = null

        fun getInstance(context: Context): SecureStorage {
            return instance ?: synchronized(this) {
                instance ?: SecureStorage(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
