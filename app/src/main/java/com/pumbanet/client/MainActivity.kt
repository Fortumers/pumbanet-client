package com.pumbanet.client

import android.content.Intent
import android.content.SharedPreferences
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var connectButton: Button
    private lateinit var importConfigButton: Button
    private lateinit var logoutButton: Button
    private lateinit var statusText: TextView
    private lateinit var subscriptionInfoText: TextView
    private var isConnected = false

    private lateinit var prefs: SharedPreferences
    private var remnawaveApi: RemnawaveApi? = null

    companion object {
        private const val VPN_PERMISSION_REQUEST = 1
        private const val IMPORT_CONFIG_REQUEST = 2
        private const val PREFS_NAME = "pumbanet_login"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_API_TOKEN = "api_token"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_updated)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        connectButton = findViewById(R.id.connectButton)
        importConfigButton = findViewById(R.id.importConfigButton)
        logoutButton = findViewById(R.id.logoutButton)
        statusText = findViewById(R.id.statusText)
        subscriptionInfoText = findViewById(R.id.subscriptionInfoText)

        // Инициализация Remnawave API
        initRemnawaveApi()

        connectButton.setOnClickListener {
            if (!isConnected) {
                requestVpnPermission()
            } else {
                stopVpnService()
            }
        }

        importConfigButton.setOnClickListener {
            val intent = Intent(this, ImportConfigActivity::class.java)
            startActivityForResult(intent, IMPORT_CONFIG_REQUEST)
        }

        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun initRemnawaveApi() {
        val serverUrl = prefs.getString(KEY_SERVER_URL, null)
        val apiToken = prefs.getString(KEY_API_TOKEN, null)

        if (!serverUrl.isNullOrEmpty() && !apiToken.isNullOrEmpty()) {
            remnawaveApi = RemnawaveApi(serverUrl, apiToken)
            loadSubscriptionInfo()
        } else {
            // Нет данных для входа - перенаправляем на экран логина
            navigateToLogin()
        }
    }

    private fun loadSubscriptionInfo() {
        lifecycleScope.launch {
            val api = remnawaveApi ?: return@launch
            val result = api.getSubscriptions()

            when (result) {
                is ApiResult.Success -> {
                    val subscriptions = result.data
                    if (subscriptions.isNotEmpty()) {
                        val sub = subscriptions[0]
                        subscriptionInfoText.text = "${sub.name}\nТрафик: ${sub.getTrafficUsedFormatted()} / ${sub.getTrafficTotalFormatted()}"
                    } else {
                        subscriptionInfoText.text = "Нет активных подписок"
                    }
                }
                is ApiResult.Error -> {
                    subscriptionInfoText.text = "Ошибка загрузки подписки"
                }
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
        if (requestCode == VPN_PERMISSION_REQUEST && resultCode == RESULT_OK) {
            startVpnService()
        } else if (requestCode == IMPORT_CONFIG_REQUEST && resultCode == RESULT_OK) {
            // Конфиг импортирован, можно обновить UI
            Toast.makeText(this, "Конфиг готов к использованию", Toast.LENGTH_SHORT).show()
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
        // Загрузка конфига из SharedPreferences (импортированного через QR/ссылку)
        val configPrefs = getSharedPreferences("pumbanet_config", MODE_PRIVATE)
        val vlessLink = configPrefs.getString("vless_link", null)

        if (!vlessLink.isNullOrEmpty()) {
            // Парсинг vless:// ссылки и конвертация в JSON конфиг для Xray
            return parseVlessLinkToJson(vlessLink)
        }

        // Резервный конфиг (заглушка)
        return """{
            "inbounds": [{
                "port": 10808,
                "listen": "127.0.0.1",
                "protocol": "socks",
                "settings": {"auth": "noauth", "udp": true}
            }],
            "outbounds": [{
                "protocol": "vless",
                "settings": {
                    "vnext": [{
                        "address": "your.pumbanet.server",
                        "port": 443,
                        "users": [{"id": "USER_UUID_HERE", "encryption": "none", "flow": ""}]
                    }]
                },
                "streamSettings": {
                    "network": "tcp",
                    "security": "tls",
                    "tlsSettings": {"serverName": "your.pumbanet.server"}
                }
            }]
        }"""
    }

    private fun parseVlessLinkToJson(vlessLink: String): String {
        // Парсинг vless:// UUID@SERVER:PORT?PARAMS
        // Упрощённая реализация - для продакшена нужен полный парсер
        try {
            val uri = android.net.Uri.parse(vlessLink)
            val uuid = uri.userInfo ?: ""
            val host = uri.host ?: ""
            val port = uri.port.toString()
            val security = uri.getQueryParameter("security") ?: "tls"
            val sni = uri.getQueryParameter("sni") ?: host
            val network = uri.getQueryParameter("type") ?: "tcp"

            return """{
                "inbounds": [{
                    "port": 10808,
                    "listen": "127.0.0.1",
                    "protocol": "socks",
                    "settings": {"auth": "noauth", "udp": true}
                }],
                "outbounds": [{
                    "protocol": "vless",
                    "settings": {
                        "vnext": [{
                            "address": "$host",
                            "port": $port,
                            "users": [{"id": "$uuid", "encryption": "none", "flow": ""}]
                        }]
                    },
                    "streamSettings": {
                        "network": "$network",
                        "security": "$security",
                        "tlsSettings": {"serverName": "$sni"}
                    }
                }]
            }"""
        } catch (e: Exception) {
            // Ошибка парсинга - возвращаем заглушку
            return loadVlessConfig()
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

    private fun logout() {
        prefs.edit().clear().apply()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
