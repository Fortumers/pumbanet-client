package com.pumbanet.client

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.integration.android.IntentIntegrator

class ConfigImportActivity : AppCompatActivity() {

    private lateinit var importLinkButton: Button
    private lateinit var scanQrButton: Button
    private lateinit var loginRemnawaveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_import)

        importLinkButton = findViewById(R.id.importLinkButton)
        scanQrButton = findViewById(R.id.scanQrButton)
        loginRemnawaveButton = findViewById(R.id.loginRemnawaveButton)

        // Обработка vless:// ссылки из intent
        handleVlessLink(intent)

        importLinkButton.setOnClickListener {
            showImportLinkDialog()
        }

        scanQrButton.setOnClickListener {
            checkCameraPermissionAndScan()
        }

        loginRemnawaveButton.setOnClickListener {
            navigateToRemnawaveLogin()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleVlessLink(intent)
    }

    private fun handleVlessLink(intent: Intent?) {
        val data = intent?.data
        if (data != null && data.scheme == "vless") {
            val vlessLink = data.toString()
            saveConfigAndReturn(vlessLink)
        }
    }

    private fun showImportLinkDialog() {
        val editText = EditText(this).apply {
            hint = "vless://..."
            setLines(3)
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Импорт конфига")
            .setMessage("Вставьте ссылку vless://")
            .setView(editText)
            .setPositiveButton("Импорт") { _, _ ->
                val link = editText.text.toString().trim()
                if (link.startsWith("vless://")) {
                    saveConfigAndReturn(link)
                } else {
                    Toast.makeText(this, "Неверный формат ссылки", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun checkCameraPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            == PackageManager.PERMISSION_GRANTED) {
            startQRScanner()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST && 
            grantResults.isNotEmpty() && 
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startQRScanner()
        } else {
            Toast.makeText(this, "Нужен доступ к камере", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Наведите на QR-код PumbaNET")
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val scannedContent = result.contents
                if (scannedContent.startsWith("vless://")) {
                    saveConfigAndReturn(scannedContent)
                } else {
                    Toast.makeText(this, "Не vless:// QR-код", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveConfigAndReturn(vlessLink: String) {
        // Сохранение конфига в SharedPreferences
        val prefs = getSharedPreferences("pumbanet_config", MODE_PRIVATE)
        prefs.edit().putString("active_config", vlessLink).apply()

        Toast.makeText(this, "Конфиг импортирован", Toast.LENGTH_SHORT).show()
        
        val resultIntent = Intent().apply {
            putExtra("CONFIG_VLESS", vlessLink)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun navigateToRemnawaveLogin() {
        val intent = Intent(this, RemnawaveLoginActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
    }
}
