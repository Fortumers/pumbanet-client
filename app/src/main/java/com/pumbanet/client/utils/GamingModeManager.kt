package com.pumbanet.client.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Игровой режим (Feature 22)
 * Низкий пинг, приоритет трафика, статистика ping/jitter
 */
class GamingModeManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Получить серверы с низким пингом для гейминга
     */
    fun getGamingServers(): List<GamingServer> {
        // В реальном приложении: запрос к API с фильтрацией по ping
        return listOf(
            GamingServer("game-server-1.pumbanet.com", 15, "Frankfurt"),
            GamingServer("game-server-2.pumbanet.com", 22, "Amsterdam")
        )
    }

    /**
     * Включить игровой режим (приоритет трафика)
     */
    fun setGamingMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GAMING_MODE, enabled).apply()
    }

    fun isGamingModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_GAMING_MODE, false)
    }

    /**
     * Замер ping/jitter в реальном времени
     */
    suspend fun measurePingJitter(serverAddress: String): GamingStats {
        val pings = mutableListOf<Long>()
        
        repeat(10) {
            val start = System.currentTimeMillis()
            val isReachable = checkServerReachable(serverAddress)
            val end = System.currentTimeMillis()
            
            if (isReachable) {
                pings.add(end - start)
            }
        }

        if (pings.isEmpty()) {
            return GamingStats(0, 0, 0)
        }

        val avgPing = pings.average().toLong()
        val minPing = pings.min()
        val maxPing = pings.max()
        val jitter = maxPing - minPing

        return GamingStats(avgPing, jitter, pings.size)
    }

    private fun checkServerReachable(host: String, port: Int = 443): Boolean {
        return try {
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress(host, port), 2000)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val PREFS_NAME = "pumbanet_gaming"
        private const val KEY_GAMING_MODE = "gaming_mode"
    }

    data class GamingServer(
        val address: String,
        val pingMs: Int,
        val location: String
    )

    data class GamingStats(
        val avgPing: Long,
        val jitter: Long,
        val samples: Int
    )
}
