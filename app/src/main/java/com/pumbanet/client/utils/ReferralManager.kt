package com.pumbanet.client.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

/**
 * Менеджер реферальной программы (Feature 10)
 */
class ReferralManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Получить реферальный код пользователя
     */
    fun getReferralCode(): String {
        return prefs.getString(KEY_REFERRAL_CODE, null) ?: generateReferralCode()
    }

    /**
     * Сохранить реферальный код
     */
    fun saveReferralCode(code: String) {
        prefs.edit().putString(KEY_REFERRAL_CODE, code).apply()
    }

    /**
     * Получить количество рефералов
     */
    fun getReferralCount(): Int {
        return prefs.getInt(KEY_REFERRAL_COUNT, 0)
    }

    /**
     * Обновить количество рефералов
     */
    fun updateReferralCount(count: Int) {
        prefs.edit().putInt(KEY_REFERRAL_COUNT, count).apply()
    }

    /**
     * Получить бонусы (баллы)
     */
    fun getBonusPoints(): Int {
        return prefs.getInt(KEY_BONUS_POINTS, 0)
    }

    /**
     * Добавить бонусы
     */
    fun addBonusPoints(points: Int) {
        val current = getBonusPoints()
        prefs.edit().putInt(KEY_BONUS_POINTS, current + points).apply()
    }

    /**
     * Получить реферальную ссылку
     */
    fun getReferralLink(): String {
        val code = getReferralCode()
        return "https://pumbanet.yourdomain.com/ref/$code"
    }

    /**
     * Поделиться реферальной ссылкой
     */
    fun shareReferralLink(): String {
        val link = getReferralLink()
        return "Присоединяйся к PumbaNET по моей ссылке: $link"
    }

    /**
     * Проверить и применить реферальный код при регистрации
     */
    fun applyReferralCode(code: String): Boolean {
        // Валидация кода
        if (code.isBlank() || code.length < 6) {
            return false
        }

        // Сохранение применённого кода
        prefs.edit().putString(KEY_APPLIED_REFERRAL_CODE, code).apply()
        return true
    }

    /**
     * Проверить, был ли применён реферальный код
     */
    fun hasAppliedReferralCode(): Boolean {
        return prefs.contains(KEY_APPLIED_REFERRAL_CODE)
    }

    /**
     * Получить применённый реферальный код
     */
    fun getAppliedReferralCode(): String? {
        return prefs.getString(KEY_APPLIED_REFERRAL_CODE, null)
    }

    /**
     * Генерация уникального реферального кода
     */
    private fun generateReferralCode(): String {
        // Генерация кода вида PUMB-XXXX
        val random = UUID.randomUUID().toString().replace("-", "").substring(0, 4).uppercase()
        val code = "PUMB-$random"
        saveReferralCode(code)
        return code
    }

    /**
     * Синхронизация с сервером (получение актуальных данных)
     */
    suspend fun syncWithServer(apiClient: ReferralApiClient) {
        val serverData = apiClient.getReferralData(getReferralCode())
        
        serverData?.let {
            saveReferralCode(it.code)
            updateReferralCount(it.referralCount)
            prefs.edit().putInt(KEY_BONUS_POINTS, it.bonusPoints).apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "pumbanet_referral"
        private const val KEY_REFERRAL_CODE = "referral_code"
        private const val KEY_REFERRAL_COUNT = "referral_count"
        private const val KEY_BONUS_POINTS = "bonus_points"
        private const val KEY_APPLIED_REFERRAL_CODE = "applied_referral_code"
    }

    data class ReferralData(
        val code: String,
        val referralCount: Int,
        val bonusPoints: Int
    )
}

/**
 * API клиент для реферальной программы
 */
class ReferralApiClient(private val baseUrl: String) {

    suspend fun getReferralData(code: String): ReferralManager.ReferralData? {
        // В реальном приложении: HTTP запрос к API
        // GET /api/v1/referral/{code}
        return null // Placeholder
    }

    suspend fun applyReferralCode(userId: String, referralCode: String): Boolean {
        // В реальном приложении: HTTP запрос к API
        // POST /api/v1/referral/apply
        return true // Placeholder
    }
}
