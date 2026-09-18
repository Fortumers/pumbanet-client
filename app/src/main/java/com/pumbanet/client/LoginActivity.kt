package com.pumbanet.client

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var serverUrlInput: EditText
    private lateinit var apiTokenInput: EditText
    private lateinit var loginButton: Button
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var prefs: SharedPreferences

    companion object {
        private const val PREFS_NAME = "pumbanet_login"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_API_TOKEN = "api_token"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Проверка, есть ли сохранённые данные
        if (isAlreadyLoggedIn()) {
            navigateToMain()
            return
        }

        serverUrlInput = findViewById(R.id.serverUrlInput)
        apiTokenInput = findViewById(R.id.apiTokenInput)
        loginButton = findViewById(R.id.loginButton)
        statusText = findViewById(R.id.statusText)
        progressBar = findViewById(R.id.progressBar)

        // Автозаполнение сохранённых данных
        val savedUrl = prefs.getString(KEY_SERVER_URL, "")
        val savedToken = prefs.getString(KEY_API_TOKEN, "")
        serverUrlInput.setText(savedUrl)
        apiTokenInput.setText(savedToken)

        loginButton.setOnClickListener {
            val serverUrl = serverUrlInput.text.toString().trim()
            val apiToken = apiTokenInput.text.toString().trim()

            if (serverUrl.isEmpty() || apiToken.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(serverUrl, apiToken)
        }
    }

    private fun isAlreadyLoggedIn(): Boolean {
        val savedUrl = prefs.getString(KEY_SERVER_URL, null)
        val savedToken = prefs.getString(KEY_API_TOKEN, null)
        return !savedUrl.isNullOrEmpty() && !savedToken.isNullOrEmpty()
    }

    private fun performLogin(serverUrl: String, apiToken: String) {
        progressBar.visibility = android.view.View.VISIBLE
        statusText.text = "Подключение к Remnawave..."

        lifecycleScope.launch {
            val api = RemnawaveApi(serverUrl, apiToken)
            val result = api.authenticate()

            progressBar.visibility = android.view.View.GONE

            when (result) {
                is ApiAuthResult.Success -> {
                    // Сохранение данных
                    prefs.edit().apply {
                        putString(KEY_SERVER_URL, serverUrl)
                        putString(KEY_API_TOKEN, apiToken)
                        apply()
                    }

                    statusText.text = "Успешно! Пользователь: ${result.username}"
                    Toast.makeText(this@LoginActivity, "Вход выполнен", Toast.LENGTH_SHORT).show()

                    // Переход на главный экран
                    navigateToMain()
                }
                is ApiAuthResult.Error -> {
                    statusText.text = "Ошибка: ${result.message}"
                    Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
