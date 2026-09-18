package com.pumbanet.client.utils

import android.content.Context
import org.json.JSONObject

/**
 * WireGuard поддержка (Feature 17)
 * Генерация ключей, импорт .conf файлов
 */
class WireGuardManager(private val context: Context) {

    /**
     * Генерация пары ключей WireGuard
     */
    fun generateKeyPair(): KeyPair {
        // В реальном приложении: вызов native библиотеки WireGuard
        // Здесь placeholder
        return KeyPair(
            privateKey = "GENERATED_PRIVATE_KEY",
            publicKey = "GENERATED_PUBLIC_KEY"
        )
    }

    /**
     * Парсинг .conf файла WireGuard
     */
    fun parseConfig(confContent: String): WireGuardConfig? {
        return try {
            val lines = confContent.lines()
            val config = mutableMapOf<String, MutableMap<String, String>>()
            var currentSection = ""

            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("[")) {
                    currentSection = trimmed.removeSurrounding("[", "]")
                    config[currentSection] = mutableMapOf()
                } else if (trimmed.contains("=") && currentSection.isNotEmpty()) {
                    val parts = trimmed.split("=", limit = 2)
                    if (parts.size == 2) {
                        config[currentSection]?.set(parts[0].trim(), parts[1].trim())
                    }
                }
            }

            val interfaceSection = config["Interface"] ?: return null
            val peerSection = config["Peer"] ?: return null

            WireGuardConfig(
                privateKey = interfaceSection["PrivateKey"] ?: "",
                address = interfaceSection["Address"] ?: "",
                dns = interfaceSection["DNS"] ?: "",
                publicKey = peerSection["PublicKey"] ?: "",
                endpoint = peerSection["Endpoint"] ?: "",
                allowedIPs = peerSection["AllowedIPs"]?.split(",")?.map { it.trim() } ?: emptyList()
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Конвертация в JSON для сохранения
     */
    fun configToJson(config: WireGuardConfig): String {
        return JSONObject().apply {
            put("privateKey", config.privateKey)
            put("address", config.address)
            put("dns", config.dns)
            put("publicKey", config.publicKey)
            put("endpoint", config.endpoint)
            put("allowedIPs", org.json.JSONArray(config.allowedIPs))
        }.toString()
    }

    data class KeyPair(
        val privateKey: String,
        val publicKey: String
    )

    data class WireGuardConfig(
        val privateKey: String,
        val address: String,
        val dns: String,
        val publicKey: String,
        val endpoint: String,
        val allowedIPs: List<String>
    )
}
