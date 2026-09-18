package com.pumbanet.client

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URL

class RemnawaveLoginActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remnawave_login)

        webView = findViewById(R.id.webView)

        setupWebView()
        
        // Загрузка страницы авторизации PumbaNET
        val loginUrl = getPumbaNetLoginUrl()
        webView.loadUrl(loginUrl)
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    // Проверка на редирект после успешного логина
                    if (url.contains("/dashboard") || url.contains("/profile")) {
                        // Авторизация успешна, получаем конфиг
                        fetchUserConfig()
                        return true
                    }
                }
                return false
            }
        }
    }

    private fun getPumbaNetLoginUrl(): String {
        // Замени на свой домен PumbaNET
        return "https://pumbanet.yourdomain.com/auth/login"
    }

    private fun fetchUserConfig() {
        // Получение конфига пользователя через Remnawave API
        // В реальном приложении: использование токена из cookies или localStorage
        
        // Пример: GET /api/v1/user/config
        val apiClient = RemnawaveApiClient(this)
        apiClient.getUserConfig { configJson, error ->
            if (configJson != null) {
                saveConfigAndReturn(configJson)
            } else {
                Toast.makeText(this, "Ошибка: ${error?.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun saveConfigAndReturn(configJson: String) {
        val prefs = getSharedPreferences("pumbanet_config", MODE_PRIVATE)
        prefs.edit().putString("active_config", configJson).apply()

        Toast.makeText(this, "Конфиг получен", Toast.LENGTH_SHORT).show()

        val resultIntent = Intent().apply {
            putExtra("CONFIG_JSON", configJson)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
