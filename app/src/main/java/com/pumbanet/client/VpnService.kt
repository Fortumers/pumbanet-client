package com.pumbanet.client

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.pumbanet.client.model.ConnectionStats
import com.pumbanet.client.utils.PreferencesManager
import java.io.File
import java.io.FileWriter
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class VpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var xrayProcess: Process? = null
    private var prefsManager: PreferencesManager? = null
    private var currentStats: ConnectionStats? = null
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    
    companion object {
        private var isRunningInstance = false
        fun isRunning(): Boolean = isRunningInstance
    }

    override fun onCreate() {
        super.onCreate()
        prefsManager = PreferencesManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunningInstance = true
        
        val configJson = intent?.getStringExtra("CONFIG_JSON")
        if (configJson.isNullOrEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Проверка Kill Switch
        val isKillSwitchEnabled = prefsManager?.isKillSwitchEnabled() ?: false
        
        // Настройка VPN интерфейса
        val builder = Builder()
            .addAddress("10.0.0.2", 24)
            .setSession("PumbaNET")
            .setMtu(1500)

        // DNS серверы
        builder.addDnsServer("8.8.8.8")
        builder.addDnsServer("1.1.1.1")

        // Split Tunneling
        val isSplitTunnelEnabled = prefsManager?.isSplitTunnelEnabled() ?: false
        if (isSplitTunnelEnabled) {
            val splitApps = prefsManager?.getSplitTunnelApps() ?: emptySet()
            if (splitApps.isNotEmpty()) {
                // Режим: только выбранные приложения через VPN
                splitApps.forEach { packageName ->
                    try {
                        builder.addAllowedApplication(packageName)
                    } catch (e: Exception) {
                        // Приложение не найдено
                    }
                }
            }
        } else {
            // Все приложения через VPN
            builder.addRoute("0.0.0.0", 0)
        }

        // Kill Switch - блокировка всего трафика без VPN
        if (isKillSwitchEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setMetered(false)
                builder.setBlocking(true)
            }
        }

        // Android 10+ требует setMetered(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        vpnInterface = builder.establish() ?: run {
            stopSelf()
            return START_NOT_STICKY
        }

        // Запуск Xray с конфигом
        startXray(configJson)

        // Инициализация статистики
        currentStats = ConnectionStats(
            sessionId = UUID.randomUUID().toString(),
            profileId = prefsManager?.getActiveProfileId() ?: "unknown"
        )

        // Запуск сбора статистики
        startStatsCollection()

        // Запуск foreground сервиса
        startForeground(NOTIFICATION_ID, createNotification())

        return START_STICKY
    }

    private fun startXray(configJson: String) {
        try {
            // Сохранение конфига во временный файл
            val configFile = File(cacheDir, "xray_config.json")
            FileWriter(configFile).use { it.write(configJson) }

            // Запуск xray-core
            val xrayPath = "${applicationContext.filesDir.absolutePath}/xray"
            val processBuilder = ProcessBuilder(
                xrayPath,
                "run",
                "-c",
                configFile.absolutePath
            )
            processBuilder.redirectErrorStream()
            xrayProcess = processBuilder.start()

            // Логирование вывода Xray
            Thread {
                xrayProcess?.inputStream?.bufferedReader()?.forEachLine { line ->
                    android.util.Log.d("Xray", line)
                }
            }.start()

        } catch (e: Exception) {
            android.util.Log.e("VpnService", "Ошибка запуска Xray", e)
            stopSelf()
        }
    }

    private fun startStatsCollection() {
        // Обновление статистики каждые 5 секунд
        executor.scheduleAtFixedRate({
            // В реальном приложении: чтение статистики из Xray
            // Здесь симуляция
            val (uploaded, downloaded) = prefsManager?.getSessionStats() ?: Pair(0L, 0L)
            val newUploaded = uploaded + (1024 * 10) // +10 KB
            val newDownloaded = downloaded + (1024 * 50) // +50 KB
            
            prefsManager?.saveSessionStats(
                currentStats?.sessionId ?: "",
                newUploaded,
                newDownloaded
            )
        }, 0, 5, TimeUnit.SECONDS)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "PumbaNET VPN",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "VPN подключение PumbaNET"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PumbaNET")
            .setContentText("VPN подключен")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        isRunningInstance = false
        stopStatsCollection()
        stopXray()
        vpnInterface?.close()
        vpnInterface = null
        super.onDestroy()
    }

    override fun onRevoke() {
        isRunningInstance = false
        stopStatsCollection()
        stopXray()
        vpnInterface?.close()
        vpnInterface = null
        stopSelf()
        super.onRevoke()
    }

    private fun stopXray() {
        xrayProcess?.destroy()
        xrayProcess = null
    }

    private fun stopStatsCollection() {
        executor.shutdown()
    }

    companion object {
        private const val CHANNEL_ID = "pumbanet_vpn_channel"
        private const val NOTIFICATION_ID = 1
    }
}
