package com.pumbanet.client.model

/**
 * Статистика подключения
 */
data class ConnectionStats(
    val sessionId: String,
    val profileId: String,
    val startTime: Long = System.currentTimeMillis(),
    val bytesUploaded: Long = 0,
    val bytesDownloaded: Long = 0,
    val durationSeconds: Long = 0
) {
    fun totalBytes(): Long = bytesUploaded + bytesDownloaded

    fun formattedTotal(): String {
        val total = totalBytes()
        return when {
            total < 1024 -> "$total B"
            total < 1024 * 1024 -> "${total / 1024} KB"
            total < 1024 * 1024 * 1024 -> "${total / (1024 * 1024)} MB"
            else -> "${total / (1024 * 1024 * 1024)} GB"
        }
    }
}
