package com.pumbanet.client

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

class ConfigManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "pumbanet_config",
        Context.MODE_PRIVATE
    )

    fun saveVlessLink(link: String) {
        prefs.edit().putString("vless_link", link).apply()
        prefs.edit().remove("json_config").apply()
    }

    fun saveJsonConfig(configJson: String) {
        prefs.edit().putString("json_config", configJson).apply()
        prefs.edit().remove("vless_link").apply()
    }

    fun getConfigJson(): String? {
        val vlessLink = prefs.getString("vless_link", null)
        if (!vlessLink.isNullOrBlank()) {
            return VlessLinkParser.parse(vlessLink)
        }
        return prefs.getString("json_config", null)
    }

    fun hasConfig(): Boolean {
        return !prefs.getString("vless_link", null).isNullOrBlank() ||
               !prefs.getString("json_config", null).isNullOrBlank()
    }

    fun clearConfig() {
        prefs.edit().clear().apply()
    }
}
