package com.pumbanet.client

import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.pumbanet.client.model.VpnProfile
import com.pumbanet.client.utils.PreferencesManager

class MainActivity : AppCompatActivity() {

    private lateinit var connectButton: Button
    private lateinit var profilesButton: Button
    private lateinit var settingsButton: Button
    private lateinit var statusText: TextView
    private var isConnected = false
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefsManager = PreferencesManager(this)
        
        // Применение тёмной темы
        if (prefsManager.isDarkTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        
        setContentView(R.layout.activity_main)

        connectButton = findViewById(R.id.connectButton)
        profilesButton = findViewById(R.id.profilesButton)
        settingsButton = findViewById(R.id.settingsButton)
        statusText = findViewById(R.id.statusText)

        // Проверка сохранённого конфига
        updateProfileStatus()

        connectButton.setOnClickListener {
            if (!isConnected) {
                requestVpnPermission()
            } else {
                stopVpnService()
            }
        }

        profilesButton.setOnClickListener {
            val intent = Intent(this, ProfilesActivity::class.java)
            startActivity(intent)
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Автоподключение при запуске
        if (prefsManager.isAutoConnectEnabled() && !VpnService.isRunning()) {
            val activeProfileId = prefsManager.getActiveProfileId()
            if (activeProfileId != null) {
                requestVpnPermission()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Обработка vless:// ссылки при запуске из браузера
        intent?.data?.let { uri ->
            if (uri.scheme == "vless") {
                val importIntent = Intent(this, ConfigImportActivity::class.java).apply {
                    data = uri
                }
                startActivity(importIntent)
            }
        }
    }

    private fun updateProfileStatus() {
        val activeProfileId = prefsManager.getActiveProfileId()
        if (activeProfileId != null) {
            val profile = prefsManager.getProfiles().find { it.id == activeProfileId }
            profile?.let {
                statusText.text = "Профиль: ${it.displayName()}"
            }
        } else {
            statusText.text = "Профиль не выбран"
        }
    }

    private fun requestVpnPermission() {
        val vpnIntent = VpnService.prepare(this)
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_PERMISSION_REQUEST)
        } else {
            onActivityResult(VPN_PERMISSION_REQUEST, RESULT_OK, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == VPN_PERMISSION_REQUEST && resultCode == RESULT_OK) {
            startVpnService()
        }
    }

    private fun startVpnService() {
        val configJson = loadVlessConfig()
        if (configJson.isEmpty() || configJson == "{}") {
            Toast.makeText(this, "Сначала выберите профиль", Toast.LENGTH_SHORT).show()
            return
        }
        
        val intent = Intent(this, VpnService::class.java).apply {
            putExtra("CONFIG_JSON", configJson)
        }
        startService(intent)
        updateUI(true)
        Toast.makeText(this, "PumbaNET подключен", Toast.LENGTH_SHORT).show()
    }

    private fun stopVpnService() {
        val intent = Intent(this, VpnService::class.java)
        stopService(intent)
        updateUI(false)
        Toast.makeText(this, "PumbaNET отключен", Toast.LENGTH_SHORT).show()
    }

    private fun loadVlessConfig(): String {
        // Загрузка сохранённого конфига
        val prefs = getSharedPreferences("pumbanet_config", MODE_PRIVATE)
        val savedConfig = prefs.getString("active_config", null)
        
        if (savedConfig != null) {
            if (savedConfig.startsWith("vless://")) {
                return parseVlessLink(savedConfig)
            }
            return savedConfig
        }
        
        return ""
    }

    private fun parseVlessLink(vlessLink: String): String {
        return try {
            val uri = Uri.parse(vlessLink)
            val uuid = uri.userInfo
            val host = uri.host ?: ""
            val port = uri.port ?: 443
            
            """{
                "inbounds": [
                    {
                        "port": 10808,
                        "listen": "127.0.0.1",
                        "protocol": "socks",
                        "settings": {
                            "auth": "noauth",
                            "udp": true
                        }
                    }
                ],
                "outbounds": [
                    {
                        "tag": "proxy",
                        "protocol": "vless",
                        "settings": {
                            "vnext": [
                                {
                                    "address": "$host",
                                    "port": $port,
                                    "users": [
                                        {
                                            "id": "$uuid",
                                            "encryption": "none",
                                            "flow": ""
                                        }
                                    ]
                                }
                            ]
                        },
                        "streamSettings": {
                            "network": "tcp",
                            "security": "tls",
                            "tlsSettings": {
                                "serverName": "$host",
                                "alpn": ["http/1.1"]
                            }
                        }
                    }
                ]
            }"""
        } catch (e: Exception) {
            "{}"
        }
    }

    private fun updateUI(connected: Boolean) {
        isConnected = connected
        if (connected) {
            connectButton.text = "Отключить"
        } else {
            connectButton.text = "Подключить"
        }
    }

    companion object {
        private const val VPN_PERMISSION_REQUEST = 1
    }
}
