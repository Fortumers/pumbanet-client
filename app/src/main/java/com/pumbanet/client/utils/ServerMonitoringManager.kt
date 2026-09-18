package com.pumbanet.client.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Серверный мониторинг (Feature 27)
 * Статус серверов, загрузка, рекомендации
 */
class ServerMonitoringManager(private val context: Context) {

    /**
     * Получить статус всех серверов
     */
    suspend fun getServerStatus(): List<ServerStatus> = withContext(Dispatchers.IO) {
        // В реальном приложении: GET /api/v1/servers/status
        listOf(
            ServerStatus("server-1.pumbanet.com", "Netherlands", true, 45, 65),
            ServerStatus("server-2.pumbanet.com", "Germany", true, 52, 78),
            ServerStatus("server-3.pumbanet.com", "USA", false, 0, 0)
        )
    }

    /**
     * Получить рекомендуемый сервер
     */
    suspend fun getRecommendedServer(): ServerStatus? {
        val servers = getServerStatus()
        return servers
            .filter { it.isOnline }
            .minByOrNull { it.latencyMs ?: Int.MAX_VALUE }
    }

    /**
     * Проверка доступности сервера
     */
    private suspend fun checkServerOnline(address: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress(address, port), 3000)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    data class ServerStatus(
        val address: String,
        val location: String,
        val isOnline: Boolean,
        val latencyMs: Int?,
        val loadPercent: Int
    )
}
