package com.pumbanet.client.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Менеджер аналитики и логов (Feature 15)
 * Логирование подключений, ошибок, метрики использования
 */
class AnalyticsManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val logsDir = File(context.filesDir, "logs")

    init {
        if (!logsDir.exists()) {
            logsDir.mkdirs()
        }
    }

    /**
     * Лог подключения
     */
    fun logConnection(profileId: String, profileName: String, success: Boolean, durationMs: Long? = null) {
        val logEntry = LogEntry(
            timestamp = System.currentTimeMillis(),
            type = "connection",
            data = mapOf(
                "profile_id" to profileId,
                "profile_name" to profileName,
                "success" to success,
                "duration_ms" to (durationMs ?: 0)
            )
        )
        
        saveLog(logEntry)
        sendToServer(logEntry)
    }

    /**
     * Лог ошибки
     */
    fun logError(errorType: String, errorMessage: String, stackTrace: String? = null) {
        val logEntry = LogEntry(
            timestamp = System.currentTimeMillis(),
            type = "error",
            data = mapOf(
                "error_type" to errorType,
                "error_message" to errorMessage,
                "stack_trace" to (stackTrace ?: "")
            )
        )
        
        saveLog(logEntry)
        sendToServer(logEntry)
        
        Log.e("PumbaNET", "Error: $errorType - $errorMessage")
    }

    /**
     * Лог действия пользователя
     */
    fun logEvent(eventName: String, properties: Map<String, Any> = emptyMap()) {
        val logEntry = LogEntry(
            timestamp = System.currentTimeMillis(),
            type = "event",
            data = mapOf("event_name" to eventName) + properties
        )
        
        saveLog(logEntry)
        sendToServer(logEntry)
    }

    /**
     * Метрики использования
     */
    fun trackUsage(sessionId: String, bytesUploaded: Long, bytesDownloaded: Long) {
        val logEntry = LogEntry(
            timestamp = System.currentTimeMillis(),
            type = "usage",
            data = mapOf(
                "session_id" to sessionId,
                "bytes_uploaded" to bytesUploaded,
                "bytes_downloaded" to bytesDownloaded
            )
        )
        
        saveLog(logEntry)
    }

    /**
     * Сохранение лога в файл
     */
    private fun saveLog(logEntry: LogEntry) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val logFile = File(logsDir, "pumbanet_${dateFormat.format(Date())}.log")
        
        val logLine = "${formatTimestamp(logEntry.timestamp)}|${logEntry.type}|${logEntry.data}\n"
        
        FileWriter(logFile, true).use { writer ->
            writer.append(logLine)
        }
    }

    /**
     * Отправка лога на сервер
     */
    private fun sendToServer(logEntry: LogEntry) {
        // В реальном приложении: HTTP POST на сервер аналитики
        // analytics.pumbanet.com/api/v1/log
    }

    /**
     * Получение всех логов за период
     */
    fun getLogs(startDate: Long, endDate: Long): List<LogEntry> {
        val logs = mutableListOf<LogEntry>()
        
        logsDir.listFiles { file -> file.name.endsWith(".log") }?.forEach { file ->
            file.readLines().forEach { line ->
                try {
                    val parts = line.split("|", limit = 3)
                    if (parts.size == 3) {
                        val timestamp = parseTimestamp(parts[0])
                        if (timestamp in startDate..endDate) {
                            logs.add(
                                LogEntry(
                                    timestamp = timestamp,
                                    type = parts[1],
                                    data = parseData(parts[2])
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Пропуск битых строк
                }
            }
        }
        
        return logs
    }

    /**
     * Экспорт логов в файл (для отладки)
     */
    fun exportLogsToFile(outputFile: File, startDate: Long? = null, endDate: Long? = null): Boolean {
        return try {
            val logs = if (startDate != null && endDate != null) {
                getLogs(startDate, endDate)
            } else {
                getAllLogs()
            }
            
            FileWriter(outputFile).use { writer ->
                logs.forEach { log ->
                    writer.append("${formatTimestamp(log.timestamp)}|${log.type}|${log.data}\n")
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Очистка старых логов (старше 30 дней)
     */
    fun cleanupOldLogs(daysToKeep: Int = 30) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        
        logsDir.listFiles { file -> file.name.endsWith(".log") }?.forEach { file ->
            if (file.lastModified() < cutoffTime) {
                file.delete()
            }
        }
    }

    private fun getAllLogs(): List<LogEntry> {
        return getLogs(0, System.currentTimeMillis())
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun parseTimestamp(timestampStr: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return try {
            sdf.parse(timestampStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun parseData(dataStr: String): Map<String, Any> {
        // Простой парсинг, в реальном приложении: JSON
        return mapOf("raw" to dataStr)
    }

    companion object {
        private const val PREFS_NAME = "pumbanet_analytics"
    }

    data class LogEntry(
        val timestamp: Long,
        val type: String,
        val data: Map<String, Any>
    )
}
