package com.pumbanet.client.core

/**
 * VPN State Machine - сердце PumbaNET Client v2
 * Правильная модель состояний VPN
 */
sealed class VpnState {
    
    /**
     * VPN отключен
     */
    object Disconnected : VpnState()
    
    /**
     * Подготовка к подключению (проверка конфига, binary, разрешений)
     */
    object Preparing : VpnState()
    
    /**
     * Запрос VPN permission у пользователя
     */
    object RequestingPermission : VpnState()
    
    /**
     * TUN интерфейс создаётся
     */
    object CreatingTun : VpnState()
    
    /**
     * Xray запускается
     */
    object StartingXray : VpnState()
    
    /**
     * Проверка конфигурации Xray
     */
    object ValidatingConfig : VpnState()
    
    /**
     * Соединение с сервером устанавливается
     */
    object Connecting : VpnState()
    
    /**
     * VPN полностью подключен и работает
     */
    object Connected : VpnState()
    
    /**
     * Отключение (корректное завершение)
     */
    object Disconnecting : VpnState()
    
    /**
     * Ошибка подключения/работы
     */
    data class Error(
        val type: VpnErrorType,
        val message: String,
        val recoverable: Boolean = true
    ) : VpnState()
    
    /**
     * Переподключение (автоматическое)
     */
    data class Reconnecting(
        val attempt: Int,
        val maxAttempts: Int = 3
    ) : VpnState()
}

/**
 * Типы ошибок VPN
 */
enum class VpnErrorType {
    /**
     * Нет конфигурации
     */
    NO_CONFIG,
    
    /**
     * Пользователь отклонил VPN permission
     */
    PERMISSION_DENIED,
    
    /**
     * Xray binary не найден
     */
    XRAY_BINARY_MISSING,
    
    /**
     * Ошибка запуска Xray
     */
    XRAY_START_FAILED,
    
    /**
     * Невалидный конфиг Xray
     */
    XRAY_CONFIG_INVALID,
    
    /**
     * Ошибка TUN интерфейса
     */
    TUN_INTERFACE_FAILED,
    
    /**
     * Сервер недоступен
     */
    SERVER_UNREACHABLE,
    
    /**
     * Истёк срок подписки
     */
    SUBSCRIPTION_EXPIRED,
    
    /**
     * Превышен лимит трафика
     */
    TRAFFIC_LIMIT_EXCEEDED,
    
    /**
     * Сетевая ошибка
     */
    NETWORK_ERROR,
    
    /**
     * Неизвестная ошибка
     */
    UNKNOWN
}

/**
 * Статистика VPN сессии
 */
data class VpnSessionStats(
    val sessionId: String,
    val profileId: String,
    val profileName: String,
    val serverAddress: String,
    val serverPort: Int,
    val startTime: Long = System.currentTimeMillis(),
    val bytesUploaded: Long = 0,
    val bytesDownloaded: Long = 0,
    val durationSeconds: Long = 0,
    val pingMs: Int? = null,
    val jitterMs: Int? = null
) {
    fun formattedDuration(): String {
        val totalSeconds = durationSeconds
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }
    
    fun formattedTraffic(): String {
        val total = bytesUploaded + bytesDownloaded
        return when {
            total < 1024 -> "$total B"
            total < 1024 * 1024 -> "${total / 1024} KB"
            total < 1024 * 1024 * 1024 -> "${total / (1024 * 1024)} MB"
            else -> "${total / (1024 * 1024 * 1024)} GB"
        }
    }
}
