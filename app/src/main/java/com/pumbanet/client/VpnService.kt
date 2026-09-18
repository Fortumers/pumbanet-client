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
import java.io.File
import java.io.FileWriter

class VpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var xrayProcess: Process? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val configJson = intent?.getStringExtra("CONFIG_JSON")
        if (configJson.isNullOrEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Настройка VPN интерфейса
        val builder = Builder()
            .addAddress("10.0.0.2", 24)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("8.8.8.8")
            .addDnsServer("1.1.1.1")
            .setSession("PumbaNET")
            .setMtu(1500)

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
            // В реальном проекте: xray binary в assets или через JNI
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
        stopXray()
        vpnInterface?.close()
        vpnInterface = null
        super.onDestroy()
    }

    override fun onRevoke() {
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

    companion object {
        private const val CHANNEL_ID = "pumbanet_vpn_channel"
        private const val NOTIFICATION_ID = 1
    }
}
