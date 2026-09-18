package com.pumbanet.client.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.pumbanet.client.MainActivity

/**
 * Менеджер push-уведомлений (Feature 9)
 */
class PushNotificationManager(private val context: Context) {

    companion object {
        private const val CHANNEL_ID_CONNECTION = "pumbanet_connection"
        private const val CHANNEL_ID_SUBSCRIPTION = "pumbanet_subscription"
        private const val CHANNEL_ID_NEWS = "pumbanet_news"
        private const val CHANNEL_ID_GENERAL = "pumbanet_general"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = NotificationManagerCompat.from(context)

            // Канал: Статус подключения
            val connectionChannel = NotificationChannel(
                CHANNEL_ID_CONNECTION,
                "Статус подключения",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Уведомления о подключении/отключении VPN"
            }

            // Канал: Подписка
            val subscriptionChannel = NotificationChannel(
                CHANNEL_ID_SUBSCRIPTION,
                "Подписка",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления о конце подписки"
            }

            // Канал: Новости
            val newsChannel = NotificationChannel(
                CHANNEL_ID_NEWS,
                "Новости PumbaNET",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Новости и обновления сервиса"
            }

            // Канал: Общие
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "Общие уведомления",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Прочие уведомления"
            }

            notificationManager.createNotificationChannels(
                listOf(connectionChannel, subscriptionChannel, newsChannel, generalChannel)
            )
        }
    }

    /**
     * Показ уведомления о статусе подключения
     */
    fun showConnectionNotification(isConnected: Boolean, profileName: String) {
        val title = if (isConnected) "VPN подключен" else "VPN отключен"
        val message = if (isConnected) "Профиль: $profileName" else "Соединение разорвано"

        showNotification(
            title = title,
            message = message,
            channelId = CHANNEL_ID_CONNECTION,
            notificationId = 1001
        )
    }

    /**
     * Показ уведомления о конце подписки
     */
    fun showSubscriptionWarning(daysRemaining: Int) {
        showNotification(
            title = "Подписка истекает",
            message = "До конца подписки осталось $daysRemaining дн.",
            channelId = CHANNEL_ID_SUBSCRIPTION,
            notificationId = 1002
        )
    }

    /**
     * Показ новости PumbaNET
     */
    fun showNewsNotification(title: String, message: String) {
        showNotification(
            title = title,
            message = message,
            channelId = CHANNEL_ID_NEWS,
            notificationId = 1003
        )
    }

    /**
     * Общее уведомление
     */
    fun showGeneralNotification(title: String, message: String) {
        showNotification(
            title = title,
            message = message,
            channelId = CHANNEL_ID_GENERAL,
            notificationId = 1004
        )
    }

    private fun showNotification(
        title: String,
        message: String,
        channelId: String,
        notificationId: Int
    ) {
        // Проверка разрешения на уведомления (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return // Нет разрешения
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /**
     * Проверка разрешения на уведомления
     */
    fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true // На старых версиях разрешение не требуется
    }
}
