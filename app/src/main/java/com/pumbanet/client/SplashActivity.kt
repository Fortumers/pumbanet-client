package com.pumbanet.client

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Splash Screen - крутой экран загрузки с анимацией
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Анимация появления
        val textView = findViewById<TextView>(R.id::class.java.simpleName.lowercase())
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        textView.startAnimation(animation)

        // Переход на главный экран через 2 секунды
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        }, 2000)
    }
}
