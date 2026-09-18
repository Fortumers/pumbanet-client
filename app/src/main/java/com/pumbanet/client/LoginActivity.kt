package com.pumbanet.client

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEdit = findViewById(R.id.usernameEdit)
        passwordEdit = findViewById(R.id.passwordEdit)
        loginButton = findViewById(R.id.loginButton)
        progressBar = findViewById(R.id.progressBar)

        loginButton.setOnClickListener {
            val username = usernameEdit.text.toString().trim()
            val password = passwordEdit.text.toString().trim()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(username, password)
        }
    }

    private fun performLogin(username: String, password: String) {
        progressBar.visibility = android.view.View.VISIBLE
        loginButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = RemnawaveApi(ApiConfig.REMNAWAVE_BASE_URL, ApiConfig.API_KEY)
                val token = api.login(username, password)

                // Сохранение токена
                val prefs = getSharedPreferences("pumbanet_auth", MODE_PRIVATE)
                prefs.edit().putString("access_token", token).apply()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Успешный вход", Toast.LENGTH_SHORT).show()
                    
                    // Переход к списку ключей
                    val intent = Intent(this@LoginActivity, KeysListActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Ошибка входа: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    progressBar.visibility = android.view.View.GONE
                    loginButton.isEnabled = true
                }
            }
        }
    }
}
