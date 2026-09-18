package com.pumbanet.client.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Автоматические правила (Feature 18)
 * Подключение по геолокации, времени, Wi-Fi
 */
class AutomationManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Автоподключение по Wi-Fi сети
     */
    fun setAutoConnectOnWifi(ssid: String, profileId: String) {
        val rules = getWifiRules().toMutableMap()
        rules[ssid] = profileId
        saveWifiRules(rules)
    }

    fun getWifiRules(): Map<String, String> {
        val json = prefs.getString(KEY_WIFI_RULES, null) ?: return emptyMap()
        val obj = org.json.JSONObject(json)
        return obj.keys().asSequence().associateWith { obj.getString(it) }
    }

    private fun saveWifiRules(rules: Map<String, String>) {
        val obj = org.json.JSONObject()
        rules.forEach { (ssid, profileId) -> obj.put(ssid, profileId) }
        prefs.edit().putString(KEY_WIFI_RULES, obj.toString()).apply()
    }

    /**
     * Автоподключение по времени
     */
    fun setScheduleRule(
        startTime: Int, // минуты от начала дня
        endTime: Int,
        profileId: String,
        daysOfWeek: Set<Int> // 1=Пн, 7=Вс
    ) {
        val rules = getScheduleRules().toMutableList()
        rules.add(
            ScheduleRule(
                startTime = startTime,
                endTime = endTime,
                profileId = profileId,
                daysOfWeek = daysOfWeek
            )
        )
        saveScheduleRules(rules)
    }

    fun getScheduleRules(): List<ScheduleRule> {
        val json = prefs.getString(KEY_SCHEDULE_RULES, null) ?: return emptyList()
        val array = org.json.JSONArray(json)
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            ScheduleRule(
                startTime = obj.optInt("start_time"),
                endTime = obj.optInt("end_time"),
                profileId = obj.optString("profile_id"),
                daysOfWeek = obj.optString("days", "").split(",").filter { it.isNotEmpty() }.map { it.toInt() }.toSet()
            )
        }
    }

    private fun saveScheduleRules(rules: List<ScheduleRule>) {
        val array = org.json.JSONArray()
        rules.forEach { rule ->
            array.put(
                org.json.JSONObject().apply {
                    put("start_time", rule.startTime)
                    put("end_time", rule.endTime)
                    put("profile_id", rule.profileId)
                    put("days", rule.daysOfWeek.joinToString(","))
                }
            )
        }
        prefs.edit().putString(KEY_SCHEDULE_RULES, array.toString()).apply()
    }

    /**
     * Проверка правил при изменении состояния
     */
    fun checkWifiRule(currentSsid: String): String? {
        return getWifiRules()[currentSsid]
    }

    fun checkScheduleRule(currentTime: Int, currentDay: Int): String? {
        return getScheduleRules().find { rule ->
            currentTime in rule.startTime..rule.endTime && currentDay in rule.daysOfWeek
        }?.profileId
    }

    companion object {
        private const val PREFS_NAME = "pumbanet_automation"
        private const val KEY_WIFI_RULES = "wifi_rules"
        private const val KEY_SCHEDULE_RULES = "schedule_rules"
    }

    data class ScheduleRule(
        val startTime: Int,
        val endTime: Int,
        val profileId: String,
        val daysOfWeek: Set<Int>
    )
}
