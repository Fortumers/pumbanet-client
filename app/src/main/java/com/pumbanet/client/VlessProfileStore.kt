package com.pumbanet.client

import android.content.Context
import android.net.Uri
import java.util.UUID

data class VlessProfile(
    val link: String,
    val name: String,
    val host: String,
    val port: Int,
    val security: String,
    val transport: String
)

class VlessProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences("pumbanet_profiles", Context.MODE_PRIVATE)

    fun save(profile: VlessProfile) {
        prefs.edit()
            .putString("active_link", profile.link)
            .apply()
    }

    fun load(): VlessProfile? {
        val link = prefs.getString("active_link", null) ?: return null
        return parse(link).getOrNull()
    }

    fun parse(link: String): Result<VlessProfile> = runCatching {
        require(link.startsWith("vless://")) { "Нужна ссылка формата vless://" }
        val uri = Uri.parse(link)
        val uuid = uri.userInfo ?: error("В ссылке нет UUID")
        UUID.fromString(uuid)
        val host = uri.host ?: error("В ссылке нет адреса сервера")
        val port = uri.port.let { if (it in 1..65535) it else 443 }
        val security = uri.getQueryParameter("security") ?: "none"
        val transport = uri.getQueryParameter("type") ?: "tcp"
        val name = uri.fragment?.takeIf { it.isNotBlank() } ?: host
        VlessProfile(link, name, host, port, security, transport)
    }
}
