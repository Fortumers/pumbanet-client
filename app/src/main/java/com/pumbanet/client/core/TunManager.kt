package com.pumbanet.client.core

import android.net.VpnService
import android.os.Build
import android.util.Log

/**
 * TUN Manager - правильный мост между Android VPN и Xray
 * Создаёт TUN интерфейс и передаёт fd в Xray
 */
class TunManager(private val vpnService: VpnService) {

    private var vpnInterface: android.os.ParcelFileDescriptor? = null
    private var tunFd: Int = -1

    /**
     * Настройка TUN интерфейса
     */
    fun setupTun(
        serverAddress: String,
        serverPort: Int,
        isKillSwitchEnabled: Boolean = false,
        splitTunnelApps: Set<String> = emptySet()
    ): TunSetupResult {
        return try {
            val builder = VpnService.Builder()
                .addAddress("10.0.0.2", 24)  // VPN IP
                .setSession("PumbaNET")
                .setMtu(1500)

            // DNS серверы
            builder.addDnsServer("8.8.8.8")
            builder.addDnsServer("1.1.1.1")

            // Маршрутизация
            if (splitTunnelApps.isEmpty()) {
                // Весь трафик через VPN
                builder.addRoute("0.0.0.0", 0)
                
                // IPv6 (если поддерживается)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder.addRoute("::", 0)
                }
            } else {
                // Split tunneling - только выбранные приложения
                splitTunnelApps.forEach { packageName ->
                    try {
                        builder.addAllowedApplication(packageName)
                    } catch (e: Exception) {
                        Log.w("TunManager", "App not found: $packageName")
                    }
                }
            }

            // Kill Switch
            if (isKillSwitchEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setBlocking(true)
                builder.setMetered(false)
            }

            // Android 10+ требует setMetered(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setMetered(false)
            }

            // Создание TUN интерфейса
            vpnInterface = builder.establish()
                ?: return TunSetupResult.Error("Failed to establish VPN interface")

            // Получение TUN file descriptor
            tunFd = vpnInterface?.fd ?: -1
            
            if (tunFd == -1) {
                return TunSetupResult.Error("Invalid TUN fd")
            }

            Log.i("TunManager", "TUN interface created, fd: $tunFd")
            TunSetupResult.Success(tunFd)

        } catch (e: Exception) {
            Log.e("TunManager", "Failed to setup TUN", e)
            TunSetupResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Закрыть TUN интерфейс
     */
    fun closeTun() {
        try {
            vpnInterface?.close()
            vpnInterface = null
            tunFd = -1
            Log.i("TunManager", "TUN interface closed")
        } catch (e: Exception) {
            Log.e("TunManager", "Failed to close TUN", e)
        }
    }

    /**
     * Получить TUN fd
     */
    fun getTunFd(): Int {
        return tunFd
    }

    /**
     * Проверка, активен ли TUN
     */
    fun isActive(): Boolean {
        return tunFd != -1
    }

    sealed class TunSetupResult {
        data class Success(val tunFd: Int) : TunSetupResult()
        data class Error(val message: String) : TunSetupResult()
    }
}
