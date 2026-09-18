package com.pumbanet.client

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var connectButton: Button
    private lateinit var statusText: TextView
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectButton = findViewById(R.id.connectButton)
        statusText = findViewById(R.id.statusText)

        connectButton.setOnClickListener {
            if (!isConnected) {
                requestVpnPermission()
            } else {
                stopVpnService()
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
        // Загрузка конфига Vless для пользователя
        // В реальном приложении: загрузка с сервера или из SharedPreferences
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
    }
}
