package com.pumbanet.client

import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var connectButton: Button
    private lateinit var importConfigButton: Button
    private lateinit var statusText: TextView
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectButton = findViewById(R.id.connectButton)
        importConfigButton = findViewById(R.id.importConfigButton)
        statusText = findViewById(R.id.statusText)

        // Проверка сохранённого конфига
        val savedConfig = getSharedPreferences("pumbanet_config", MODE_PRIVATE)
            .getString("active_config", null)
        if (savedConfig != null) {
            statusText.text = "Конфиг загружен"
        }

        connectButton.setOnClickListener {
            if (!isConnected) {
                requestVpnPermission()
            } else {
                stopVpnService()
            }
        }

        importConfigButton.setOnClickListener {
            val intent = Intent(this, ConfigImportActivity::class.java)
            startActivityForResult(intent, CONFIG_IMPORT_REQUEST)
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
        
        when (requestCode) {
            VPN_PERMISSION_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    startVpnService()
                }
            }
            CONFIG_IMPORT_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    statusText.text = "Конфиг импортирован"
                }
            }
        }
    }

    private fun startVpnService() {
        val configJson = loadVlessConfig()
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
                // Конвертация vless:// ссылки в JSON конфиг Xray
                return parseVlessLink(savedConfig)
            }
            return savedConfig
        }
        
        // Конфиг по умолчанию (если нет сохранённого)
        return """{
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
                    "protocol": "vless",
                    "settings": {
                        "vnext": [
                            {
                                "address": "your.pumbanet.server",
                                "port": 443,
                                "users": [
                                    {
                                        "id": "USER_UUID_HERE",
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
                            "serverName": "your.pumbanet.server"
                        }
                    }
                }
            ]
        }"""
    }

    private fun parseVlessLink(vlessLink: String): String {
        // Парсинг vless:// ссылки в JSON конфиг Xray
        // Формат: vless://uuid@host:port?encryption=none&security=tls#name
        return try {
            val uri = Uri.parse(vlessLink)
            val uuid = uri.userInfo
            val host = uri.host ?: ""
            val port = uri.port ?: 443
            val fragment = uri.fragment ?: "PumbaNET"
            
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
            // Если парсинг не удался, возвращаем пустой конфиг
            "{}"
        }
    }

    private fun updateUI(connected: Boolean) {
        isConnected = connected
        if (connected) {
            connectButton.text = "Отключить"
            statusText.text = "Статус: Подключено к PumbaNET"
        } else {
            connectButton.text = "Подключить"
            statusText.text = "Статус: Отключено"
        }
    }

    companion object {
        private const val VPN_PERMISSION_REQUEST = 1
        private const val CONFIG_IMPORT_REQUEST = 2
    }
}
