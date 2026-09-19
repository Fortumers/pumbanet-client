package com.pumbanet.client

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        store = VlessProfileStore(this)
        serverName = findViewById(R.id.serverName)
        serverDetails = findViewById(R.id.serverDetails)

        findViewById<Button>(R.id.importButton).setOnClickListener { showImportDialog() }
        findViewById<Button>(R.id.connectButton).setOnClickListener {
            Toast.makeText(
                this,
                "VPN-ядро ещё не подключено: ключ сохранён, сервер выбран.",
                Toast.LENGTH_LONG
            ).show()
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
        } else {
            serverName.text = profile.name
            serverDetails.text = "${profile.host}:${profile.port} • ${profile.transport.uppercase()} • ${profile.security.uppercase()}"
        }
    }
}
