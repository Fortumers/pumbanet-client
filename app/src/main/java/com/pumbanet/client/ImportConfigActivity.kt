package com.pumbanet.client

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ImportConfigActivity : AppCompatActivity() {

    private lateinit var qrScanButton: Button
    private lateinit var manualImportButton: Button
    private lateinit var configEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_config)

        qrScanButton = findViewById(R.id.qrScanButton)
        manualImportButton = findViewById(R.id.manualImportButton)
        configEditText = findViewById(R.id.configEditText)

        qrScanButton.setOnClickListener {
            val intent = Intent(this, QRScannerActivity::class.java)
            startActivityForResult(intent, REQUEST_QR_SCAN)
        }

        manualImportButton.setOnClickListener {
            val configText = configEditText.text.toString()
            if (configText.isNotBlank()) {
                if (configText.startsWith("vless://")) {
                    handleVlessLink(configText.trim())
                } else {
                    try {
                        // Проверка JSON конфига
                        org.json.JSONObject(configText)
                        handleJsonConfig(configText.trim())
                    } catch (e: Exception) {
                        Toast.makeText(this, "Неверный формат конфига", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Введите конфиг", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_QR_SCAN && resultCode == RESULT_OK) {
            val vlessLink = data?.getStringExtra("vless_link")
            if (!vlessLink.isNullOrBlank()) {
                handleVlessLink(vlessLink)
            }
        }
    }

    private fun handleVlessLink(link: String) {
        // Сохранение ссылки и парсинг
        val configManager = ConfigManager(this)
        configManager.saveVlessLink(link)

        Toast.makeText(this, "Конфиг импортирован", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun handleJsonConfig(configJson: String) {
        val configManager = ConfigManager(this)
        configManager.saveJsonConfig(configJson)

        Toast.makeText(this, "JSON конфиг сохранён", Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        private const val REQUEST_QR_SCAN = 1
    }
}
