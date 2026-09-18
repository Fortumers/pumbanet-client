package com.pumbanet.client.model

import java.util.UUID

/**
 * Модель профиля VPN
 */
data class VpnProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val configJson: String,
    val vlessLink: String? = null,
    val serverAddress: String,
    val serverPort: Int,
    val lastUsed: Long = System.currentTimeMillis(),
    val pingMs: Int? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun displayName(): String = name.ifEmpty { "$serverAddress:$serverPort" }
}
