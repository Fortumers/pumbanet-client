package com.pumbanet.client.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

/**
 * Монетизация (Feature 25)
 * Freemium, Google Play Billing, подписка
 */
class MonetizationManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Проверка типа аккаунта
     */
    fun getAccountType(): AccountType {
        val type = prefs.getString(KEY_ACCOUNT_TYPE, AccountType.FREE.name) ?: AccountType.FREE.name
        return AccountType.valueOf(type)
    }

    /**
     * Обновление типа аккаунта
     */
    fun setAccountType(type: AccountType) {
        prefs.edit().putString(KEY_ACCOUNT_TYPE, type.name).apply()
    }

    /**
     * Проверка доступности функции
     */
    fun isFeatureAvailable(feature: PremiumFeature): Boolean {
        val accountType = getAccountType()
        return when (feature) {
            PremiumFeature.BASIC_SERVERS -> true // Доступно всем
            PremiumFeature.ALL_SERVERS -> accountType != AccountType.FREE
            PremiumFeature.P2P -> accountType == AccountType.PREMIUM
            PremiumFeature.GAMING -> accountType == AccountType.PREMIUM
            PremiumFeature.FAMILY -> accountType == AccountType.PREMIUM
        }
    }

    /**
     * Покупка подписки через Google Play Billing
     */
    suspend fun purchaseSubscription(activity: Activity, sku: String): PurchaseResult {
        // В реальном приложении: интеграция с Google Play Billing Library
        // https://developer.android.com/google/play/billing
        return PurchaseResult.Success // Placeholder
    }

    /**
     * Восстановление покупок
     */
    suspend fun restorePurchases(): List<String> {
        // В реальном приложении: запрос к Google Play
        return emptyList() // Placeholder
    }

    /**
     * Крипто-оплата (placeholder)
     */
    suspend fun payWithCrypto(amount: Double, currency: String): PaymentResult {
        // В реальном приложении: интеграция с crypto payment gateway
        return PaymentResult.Success // Placeholder
    }

    companion object {
        private const val PREFS_NAME = "pumbanet_monetization"
        private const val KEY_ACCOUNT_TYPE = "account_type"
    }

    enum class AccountType {
        FREE,
        BASIC,
        PREMIUM
    }

    enum class PremiumFeature {
        BASIC_SERVERS,
        ALL_SERVERS,
        P2P,
        GAMING,
        FAMILY
    }

    sealed class PurchaseResult {
        object Success : PurchaseResult()
        data class Error(val message: String) : PurchaseResult()
    }

    sealed class PaymentResult {
        object Success : PaymentResult()
        data class Error(val message: String) : PaymentResult()
    }
}
