package com.pumbanet.client

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var store: VlessProfileStore
    private lateinit var serverName: TextView
    private lateinit var serverDetails: TextView
    private lateinit var connectButton: Button
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        store = VlessProfileStore(this)
        serverName = findViewById(R.id.serverName)
        serverDetails = findViewById(R.id.serverDetails)
        connectButton = findViewById(R.id.connectButton)

        findViewById<Button>(R.id.importButton).setOnClickListener { showImportDialog() }
        connectButton.setOnClickListener {
            if (isConnected) {
                stopVpn()
            } else {
                startVpn()
            }
        }

        renderProfile()
    }

    private fun showImportDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_import_key, null)
        val input = view.findViewById<EditText>(R.id.vlessInput)

        AlertDialog.Builder(this)
            .setTitle("Вставить ключ PumbaNET")
            .setView(view)
            .setPositiveButton("Сохранить") { _, _ ->
                val link = input.text.toString().trim()
                store.parse(link).onSuccess { profile ->
                    store.save(profile)
                    renderProfile()
                    Toast.makeText(this, "Сервер добавлен", Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Toast.makeText(this, error.message ?: "Неверный ключ", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun renderProfile() {
        val profile = store.load()
        if (profile == null) {
            serverName.text = "Сервер не выбран"
            serverDetails.text = "Вставь vless:// ключ PumbaNET"
            connectButton.isEnabled = false
        } else {
            serverName.text = profile.name
            serverDetails.text = "${profile.host}:${profile.port} • ${profile.transport.uppercase()} • ${profile.security.uppercase()}"
            connectButton.isEnabled = true
        }
    }

    private fun startVpn() {
        val profile = store.load() ?: return

        val permissionIntent = VpnService.prepare(this)
        if (permissionIntent == null) {
            launchVpn(profile)
        } else {
            startActivityForResult(permissionIntent, VPN_PERMISSION_REQUEST)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_PERMISSION_REQUEST && resultCode == RESULT_OK) {
            val profile = store.load() ?: return
            launchVpn(profile)
        } else if (requestCode == VPN_PERMISSION_REQUEST) {
            Toast.makeText(this, "VPN разрешение отклонено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchVpn(profile: VlessProfile) {
        val config = buildSingBoxConfig(profile)
        val intent = Intent(this, PumbaVPNService::class.java).apply {
            putExtra("CONFIG_JSON", config.toJson())
        }
        startForegroundService(intent)
        isConnected = true
        connectButton.text = "Отключить"
        Toast.makeText(this, "Подключение...", Toast.LENGTH_SHORT).show()
    }

    private fun stopVpn() {
        val intent = Intent(this, PumbaVPNService::class.java)
        stopService(intent)
        isConnected = false
        connectButton.text = "Подключить"
        Toast.makeText(this, "Отключено", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val VPN_PERMISSION_REQUEST = 1001
    }
}
