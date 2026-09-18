package com.pumbanet.client.utils

import android.content.Context
import android.content.SharedPreferences
import com.pumbanet.client.model.VpnProfile
import org.json.JSONArray
import org.json.JSONObject

/**
 * Менеджер синхронизации между устройствами (Feature 11)
 * Поддержка: Google Drive, экспорт/импорт конфигов
 */
class CloudSyncManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Экспорт всех профилей в JSON
     */
    fun exportProfilesToJson(profiles: List<VpnProfile>): String {
        val array = JSONArray()
        profiles.forEach { profile ->
            val obj = JSONObject().apply {
                put("id", profile.id)
                put("name", profile.name)
                put("configJson", profile.configJson)
                put("vlessLink", profile.vlessLink)
                put("serverAddress", profile.serverAddress)
                put("serverPort", profile.serverPort)
                put("isFavorite", profile.isFavorite)
                put("createdAt", profile.createdAt)
            }
            array.put(obj)
        }
        return array.toString()
    }

    /**
     * Импорт профилей из JSON
     */
    fun importProfilesFromJson(jsonString: String): List<VpnProfile> {
        val array = JSONArray(jsonString)
        val profiles = mutableListOf<VpnProfile>()
        
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val profile = VpnProfile(
                id = obj.optString("id"),
                name = obj.optString("name"),
                configJson = obj.optString("configJson"),
                vlessLink = obj.optString("vlessLink", null),
                serverAddress = obj.optString("serverAddress"),
                serverPort = obj.optInt("serverPort"),
                isFavorite = obj.optBoolean("isFavorite"),
                createdAt = obj.optLong("createdAt")
            )
            profiles.add(profile)
        }
        
        return profiles
    }

    /**
     * Экспорт в файл (для Google Drive)
     */
    fun exportToFile(profiles: List<VpnProfile>): String {
        val timestamp = System.currentTimeMillis()
        val filename = "pumbanet_backup_$timestamp.json"
        val json = exportProfilesToJson(profiles)
        
        // В реальном приложении: сохранение в файл и загрузка в Google Drive
        // Здесь возвращаем JSON для демонстрации
        return json
    }

    /**
     * Импорт из файла (из Google Drive)
     */
    fun importFromFile(jsonString: String): List<VpnProfile> {
        return importProfilesFromJson(jsonString)
    }

    /**
     * Синхронизация с облаком
     */
    suspend fun syncWithCloud(
        profiles: List<VpnProfile>,
        cloudClient: CloudApiClient
    ): SyncResult {
        // Загрузка последней версии из облака
        val cloudData = cloudClient.downloadBackup()
        
        if (cloudData == null) {
            // Нет данных в облаке - загружаем текущие
            val json = exportToFile(profiles)
            cloudClient.uploadBackup(json)
            return SyncResult.Success("Загружено в облако")
        }

        // Merge локальных и облачных данных
        val localProfiles = profiles.associateBy { it.id }.toMutableMap()
        val cloudProfiles = importFromFile(cloudData).associateBy { it.id }

        // Объединение: более новые записи побеждают
        cloudProfiles.forEach { (id, cloudProfile) ->
            val localProfile = localProfiles[id]
            if (localProfile == null || cloudProfile.createdAt > localProfile.createdAt) {
                localProfiles[id] = cloudProfile
            }
        }

        // Загрузка обновлённых данных обратно
        val mergedProfiles = localProfiles.values.toList()
        val json = exportToFile(mergedProfiles)
        cloudClient.uploadBackup(json)

        return SyncResult.Success("Синхронизация завершена", mergedProfiles)
    }

    /**
     * QR-передача между устройствами
     */
    fun generateQrData(profile: VpnProfile): String {
        // Кодируем профиль в QR-код
        return "pumbanet://profile?${profile.vlessLink}"
    }

    /**
     * Парсинг QR-кода от другого устройства
     */
    fun parseQrData(qrData: String): VpnProfile? {
        return try {
            if (!qrData.startsWith("pumbanet://profile?")) return null
            val vlessLink = qrData.removePrefix("pumbanet://profile?")
            
            // Парсинг vless:// ссылки
            val uri = android.net.Uri.parse(vlessLink)
            VpnProfile(
                name = uri.fragment ?: "Imported",
                configJson = "",
                vlessLink = vlessLink,
                serverAddress = uri.host ?: "",
                serverPort = uri.port ?: 443
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Проверка последней синхронизации
     */
    fun getLastSyncTime(): Long {
        return prefs.getLong(KEY_LAST_SYNC, 0)
    }

    /**
     * Сохранение времени синхронизации
     */
    fun saveLastSyncTime() {
        prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply()
    }

    companion object {
        private const val PREFS_NAME = "pumbanet_sync"
        private const val KEY_LAST_SYNC = "last_sync_time"
    }

    sealed class SyncResult {
        data class Success(val message: String, val profiles: List<VpnProfile>? = null) : SyncResult()
        data class Error(val message: String) : SyncResult()
    }
}

/**
 * API клиент для облачного хранилища
 */
class CloudApiClient {

    suspend fun uploadBackup(json: String): Boolean {
        // В реальном приложении: загрузка в Google Drive / Dropbox
        return true // Placeholder
    }

    suspend fun downloadBackup(): String? {
        // В реальном приложении: скачивание из Google Drive / Dropbox
        return null // Placeholder
    }
}
