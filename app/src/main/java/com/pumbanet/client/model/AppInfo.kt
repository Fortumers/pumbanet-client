package com.pumbanet.client.model

import android.graphics.drawable.Drawable

/**
 * Информация о приложении для split tunneling
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false,
    val isSelected: Boolean = false
)
