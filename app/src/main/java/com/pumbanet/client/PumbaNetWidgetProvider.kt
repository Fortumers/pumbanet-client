package com.pumbanet.client

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.pumbanet.client.utils.PreferencesManager

class PumbaNetWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefsManager = PreferencesManager(context)
        val isConnected = VpnService.isRunning()
        val activeProfileId = prefsManager.getActiveProfileId()
        val profileName = activeProfileId?.let { id ->
            prefsManager.getProfiles().find { it.id == id }?.displayName() ?: "No profile"
        } ?: "No profile"

        val views = RemoteViews(context.packageName, R.layout.widget_pumbanet).apply {
            setTextViewText(R.id.widget_status, if (isConnected) "Подключено" else "Отключено")
            setTextViewText(R.id.widget_profile, profileName)
            
            // Клик по виджету - переключение VPN
            val intent = Intent(context, PumbaNetWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_VPN
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.widget_toggle_button, pendingIntent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_TOGGLE_VPN) {
            val prefsManager = PreferencesManager(context)
            val isConnected = VpnService.isRunning()
            
            if (isConnected) {
                // Отключить VPN
                val stopIntent = Intent(context, VpnService::class.java)
                context.stopService(stopIntent)
            } else {
                // Подключить VPN
                val activeProfileId = prefsManager.getActiveProfileId()
                if (activeProfileId != null) {
                    val startIntent = Intent(context, VpnService::class.java)
                    context.startService(startIntent)
                } else {
                    // Нет активного профиля - открыть приложение
                    val launchIntent = context.packageManager
                        .getLaunchIntentForPackage(context.packageName)
                    context.startActivity(launchIntent)
                }
            }
            
            // Обновить виджет
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, PumbaNetWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_TOGGLE_VPN = "com.pumbanet.client.TOGGLE_VPN"
    }
}
