package com.pumbanet.client.utils

import android.content.Context
import android.content.SharedPreferences
import com.pumbanet.client.model.VpnProfile
import org.json.JSONArray
import org.json.JSONObject

/**
 * Менеджер настроек приложения
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Мультипрофильность
    fun saveProfile(profile: VpnProfile) {
        val profiles = getProfiles().toMutableList()
        val existingIndex = profiles.indexOfFirst { it.id == profile.id }
        
        if (existingIndex >= 0) {
            profiles[existingIndex] = profile
        } else {
            profiles.add(profile)
        }
        
        saveProfiles(profiles)
    }

    fun getProfiles(): List<VpnProfile> {
        val json = prefs.getString(KEY_PROFILES, null) ?: return emptyList()
        val array = JSONArray(json)
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            VpnProfile(
                id = obj.optString("id"),
                name = obj.optString("name"),
                configJson = obj.optString("configJson"),
                vlessLink = obj.optString("vlessLink", null),
                serverAddress = obj.optString("serverAddress"),
                serverPort = obj.optInt("serverPort"),
                lastUsed = obj.optLong("lastUsed"),
                pingMs = if (obj.has("pingMs")) obj.optInt("pingMs") else null,
                isFavorite = obj.optBoolean("isFavorite")
            )
        }
    }

    private fun saveProfiles(profiles: List<VpnProfile>) {
        val array = JSONArray()
        profiles.forEach { profile ->
            val obj = JSONObject().apply {
                put("id", profile.id)
                put("name", profile.name)
                put("configJson", profile.configJson)
                put("vlessLink", profile.vlessLink)
                put("serverAddress", profile.serverAddress)
                put("serverPort", profile.serverPort)
                put("lastUsed", profile.lastUsed)
                put("pingMs", profile.pingMs)
                put("isFavorite", profile.isFavorite)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_PROFILES, array.toString()).apply()
    }

    fun deleteProfile(profileId: String) {
        val profiles = getProfiles().filter { it.id != profileId }
        saveProfiles(profiles)
    }

    fun getActiveProfileId(): String? = prefs.getString(KEY_ACTIVE_PROFILE, null)

    fun setActiveProfile(profileId: String?) {
        prefs.edit().putString(KEY_ACTIVE_PROFILE, profileId).apply()
    }

    // Тёмная тема
    fun isDarkTheme(): Boolean = prefs.getBoolean(KEY_DARK_THEME, false)

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
    }

    // Kill Switch
    fun isKillSwitchEnabled(): Boolean = prefs.getBoolean(KEY_KILL_SWITCH, false)

    fun setKillSwitch(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KILL_SWITCH, enabled).apply()
    }

    // Автоподключение
    fun isAutoConnectEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_CONNECT, false)

    fun setAutoConnect(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CONNECT, enabled).apply()
    }

    // Split Tunneling
    fun getSplitTunnelApps(): Set<String> = prefs.getStringSet(KEY_SPLIT_TUNNEL_APPS, emptySet()) ?: emptySet()

    fun setSplitTunnelApps(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_SPLIT_TUNNEL_APPS, packages).apply()
    }

    fun isSplitTunnelEnabled(): Boolean = prefs.getBoolean(KEY_SPLIT_TUNNEL_ENABLED, false)

    fun setSplitTunnelEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SPLIT_TUNNEL_ENABLED, enabled).apply()
    }

    // Статистика
    fun saveSessionStats(sessionId: String, bytesUploaded: Long, bytesDownloaded: Long) {
        prefs.edit()
            .putLong(KEY_SESSION_BYTES_UP, bytesUploaded)
            .putLong(KEY_SESSION_BYTES_DOWN, bytesDownloaded)
            .putString(KEY_CURRENT_SESSION, sessionId)
            .apply()
    }

    fun getSessionStats(): Pair<Long, Long> {
        val up = prefs.getLong(KEY_SESSION_BYTES_UP, 0)
        val down = prefs.getLong(KEY_SESSION_BYTES_DOWN, 0)
        return Pair(up, down)
    }

    fun clearSessionStats() {
        prefs.edit()
            .remove(KEY_SESSION_BYTES_UP)
            .remove(KEY_SESSION_BYTES_DOWN)
            .remove(KEY_CURRENT_SESSION)
            .apply()
    }

    // Пинг сервера
    fun saveProfilePing(profileId: String, pingMs: Int) {
        val profiles = getProfiles().toMutableList()
        val index = profiles.indexOfFirst { it.id == profileId }
        if (index >= 0) {
            profiles[index] = profiles[index].copy(pingMs = pingMs)
            saveProfiles(profiles)
        }
    }

    companion object {
        private const val PREFS_NAME = "pumbanet_prefs"
        private const val KEY_PROFILES = "vpn_profiles"
        private const val KEY_ACTIVE_PROFILE = "active_profile_id"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_KILL_SWITCH = "kill_switch"
        private const val KEY_AUTO_CONNECT = "auto_connect"
        private const val KEY_SPLIT_TUNNEL_APPS = "split_tunnel_apps"
        private const val KEY_SPLIT_TUNNEL_ENABLED = "split_tunnel_enabled"
        private const val KEY_SESSION_BYTES_UP = "session_bytes_up"
        private const val KEY_SESSION_BYTES_DOWN = "session_bytes_down"
        private const val KEY_CURRENT_SESSION = "current_session"
    }
}
