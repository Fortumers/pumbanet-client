package com.pumbanet.client

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.io.InputStream

class ImportConfigActivity : AppCompatActivity() {

    private lateinit var vlessLinkInput: EditText
    private lateinit var importButton: Button
    private lateinit var scanQrButton: Button
    private lateinit var pickImageButton: Button

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startQRScanner()
        } else {
            Toast.makeText(this, "Нужен доступ к камере для сканирования QR", Toast.LENGTH_SHORT).show()
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { decodeQRFromImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_config)

        vlessLinkInput = findViewById(R.id.vlessLinkInput)
        importButton = findViewById(R.id.importButton)
        scanQrButton = findViewById(R.id.scanQrButton)
        pickImageButton = findViewById(R.id.pickImageButton)

        importButton.setOnClickListener {
            val link = vlessLinkInput.text.toString().trim()
            if (link.startsWith("vless://", ignoreCase = true)) {
                saveConfig(link)
            } else {
                Toast.makeText(this, "Неверный формат ссылки vless://", Toast.LENGTH_SHORT).show()
            }
        }

        scanQrButton.setOnClickListener {
            checkCameraPermission()
        }

        pickImageButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                startQRScanner()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Наведите камеру на QR-код PumbaNET")
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_SHORT).show()
            } else {
                val scannedLink = result.contents
                if (scannedLink.startsWith("vless://", ignoreCase = true)) {
                    vlessLinkInput.setText(scannedLink)
                    saveConfig(scannedLink)
                } else {
                    Toast.makeText(this, "Это не vless:// ссылка", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun decodeQRFromImage(uri: Uri) {
        try {
            val inputStream: InputStream = contentResolver.openInputStream(uri) ?: return
            val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val intArray = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
            val binaryBitmap: BinaryBitmap = BinaryBitmap(HybridBinarizer(source))

            val reader = MultiFormatReader()
            val result = reader.decodeWithState(binaryBitmap)
            val scannedLink = result.text

            if (scannedLink.startsWith("vless://", ignoreCase = true)) {
                vlessLinkInput.setText(scannedLink)
                saveConfig(scannedLink)
            } else {
                Toast.makeText(this, "QR не содержит vless:// ссылку", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка чтения QR: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveConfig(vlessLink: String) {
        // Сохранение конфига в SharedPreferences или базу данных
        val prefs = getSharedPreferences("pumbanet_config", MODE_PRIVATE)
        prefs.edit().putString("vless_link", vlessLink).apply()

        Toast.makeText(this, "Конфиг импортирован!", Toast.LENGTH_SHORT).show()

        // Возврат на главный экран
        setResult(RESULT_OK)
        finish()
    }
}
