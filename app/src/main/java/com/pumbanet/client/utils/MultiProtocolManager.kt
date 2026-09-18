package com.pumbanet.client.utils

import android.content.Context

/**
 * Поддержка протоколов (Feature 26)
 * Trojan, Shadowsocks, Hysteria2, Tuic (QUIC)
 */
class MultiProtocolManager(private val context: Context) {

    /**
     * Парсинг ссылки Trojan
     */
    fun parseTrojanLink(link: String): TrojanConfig? {
        // trojan://password@server:port#name
        return try {
            val uri = android.net.Uri.parse(link)
            if (uri.scheme != "trojan") return null
            
            TrojanConfig(
                password = uri.userInfo ?: "",
                server = uri.host ?: "",
                port = uri.port ?: 443,
                name = uri.fragment ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Парсинг ссылки Shadowsocks
     */
    fun parseShadowsocksLink(link: String): ShadowsocksConfig? {
        // ss://method:password@server:port#name
        return try {
            val uri = android.net.Uri.parse(link)
            if (uri.scheme != "ss") return null
            
            ShadowsocksConfig(
                method = "aes-256-gcm",
                password = uri.userInfo ?: "",
                server = uri.host ?: "",
                port = uri.port ?: 8388,
                name = uri.fragment ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Парсинг ссылки Hysteria2
     */
    fun parseHysteria2Link(link: String): Hysteria2Config? {
        // hysteria2://uuid@server:port#name
        return try {
            val uri = android.net.Uri.parse(link)
            if (uri.scheme != "hysteria2") return null
            
            Hysteria2Config(
                uuid = uri.userInfo ?: "",
                server = uri.host ?: "",
                port = uri.port ?: 443,
                name = uri.fragment ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Парсинг ссылки Tuic (QUIC)
     */
    fun parseTuicLink(link: String): TuicConfig? {
        // tuic://uuid:password@server:port#name
        return try {
            val uri = android.net.Uri.parse(link)
            if (uri.scheme != "tuic") return null
            
            TuicConfig(
                uuid = uri.userInfo?.split(":")?.get(0) ?: "",
                password = uri.userInfo?.split(":")?.get(1) ?: "",
                server = uri.host ?: "",
                port = uri.port ?: 443,
                name = uri.fragment ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    data class TrojanConfig(
        val password: String,
        val server: String,
        val port: Int,
        val name: String
    )

    data class ShadowsocksConfig(
        val method: String,
        val password: String,
        val server: String,
        val port: Int,
        val name: String
    )

    data class Hysteria2Config(
        val uuid: String,
        val server: String,
        val port: Int,
        val name: String
    )

    data class TuicConfig(
        val uuid: String,
        val password: String,
        val server: String,
        val port: Int,
        val name: String
    )
}
