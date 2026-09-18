package com.pumbanet.client.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Обход блокировок (Feature 19)
 * Список доменов для прямого подключения (bypass VPN)
 */
class BypassManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Добавить домен в bypass список
     */
    fun addBypassDomain(domain: String) {
        val domains = getBypassDomains().toMutableSet()
        domains.add(domain)
        saveBypassDomains(domains)
    }

    /**
     * Удалить домен из bypass списка
     */
    fun removeBypassDomain(domain: String) {
        val domains = getBypassDomains().toMutableSet()
        domains.remove(domain)
        saveBypassDomains(domains)
    }

    /**
     * Получить все домены для bypass
     */
    fun getBypassDomains(): Set<String> {
        return prefs.getStringSet(KEY_BYPASS_DOMAINS, emptySet()) ?: emptySet()
    }

    private fun saveBypassDomains(domains: Set<String>) {
        prefs.edit().putStringSet(KEY_BYPASS_DOMAINS, domains).apply()
    }

    /**
     * Включить/выключить bypass режим
     */
    fun setBypassEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BYPASS_ENABLED, enabled).apply()
    }

    fun isBypassEnabled(): Boolean {
        return prefs.getBoolean(KEY_BYPASS_ENABLED, false)
    }

    /**
     * Загрузить список заблокированных доменов (AntiZapret / CensorTracker)
     */
    suspend fun loadBlockedDomainsList(url: String): List<String> {
        // В реальном приложении: скачивание списка с сервера
        // https://antizapret.ru/list.txt
        return emptyList() // Placeholder
    }

    /**
     * Проверка, является ли домен заблокированным
     */
    fun isDomainBlocked(domain: String): Boolean {
        return getBypassDomains().any { blocked ->
            domain.endsWith(blocked) || domain == blocked
        }
    }

    companion object {
        private const val PREFS_NAME = "pumbanet_bypass"
        private const val KEY_BYPASS_DOMAINS = "bypass_domains"
        private const val KEY_BYPASS_ENABLED = "bypass_enabled"
    }
}
