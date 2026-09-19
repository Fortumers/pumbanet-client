package com.pumbanet.client

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream

class PumbaVPNService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var boxService: PumbaBoxService? = null
    private var configJson: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        configJson = intent?.getStringExtra("CONFIG_JSON")

        if (vpnInterface == null) {
            vpnInterface = Builder()
                .setSession(getString(R.string.app_name))
                .setMtu(9000)
                .addAddress("172.19.0.1", 30)
                .addAddress("fdfe:dcba:9876::1", 126)
                .addRoute("0.0.0.0", 0)
                .addRoute("::", 0)
                .addDnsServer("1.1.1.1")
                .addDnsServer("8.8.8.8")
                .setBlocking(false)
                .establish()

            vpnInterface?.let { fd ->
                val config = configJson?.let { buildSingBoxConfigFromJson(it) }
                if (config != null) {
                    boxService = PumbaBoxService(this)
                    boxService?.start(config.toJson(), fd.fd) { connected, message ->
                        updateNotification(connected, message)
                    }
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun buildSingBoxConfigFromJson(json: String): SingBoxConfig {
        return SingBoxConfig()
    }

    override fun onDestroy() {
        boxService?.stop()
        boxService = null
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onRevoke() {
        stopSelf()
        super.onRevoke()
    }

    override fun onBind(intent: Intent?): IBinder? = super.onBind(intent)

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.vpn_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.vpn_channel_description)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(getString(R.string.vpn_notification_title))
        .setContentText(getString(R.string.vpn_notification_text))
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        .build()

    private fun updateNotification(connected: Boolean, message: String?) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.vpn_notification_title))
            .setContentText(if (connected) "Подключено" else message ?: "Ошибка")
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()

        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "pumbanet_vpn"
        private const val NOTIFICATION_ID = 100
    }
}
