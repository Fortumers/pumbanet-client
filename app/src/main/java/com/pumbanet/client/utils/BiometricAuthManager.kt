package com.pumbanet.client.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Биометрическая аутентификация (Feature 16)
 * Face ID / Touch ID / Fingerprint + PIN
 */
class BiometricAuthManager(private val context: Context) {

    private val biometricManager = BiometricManager.from(context)

    fun canAuthenticate(): Boolean {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("PumbaNET")
            .setSubtitle("Проверка подлинности")
            .setNegativeButtonText("Использовать PIN")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun isAppLockEnabled(): Boolean {
        val prefs = context.getSharedPreferences("pumbanet_security", Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_APP_LOCK, false)
    }

    fun setAppLock(enabled: Boolean) {
        val prefs = context.getSharedPreferences("pumbanet_security", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_APP_LOCK, enabled).apply()
    }

    companion object {
        private const val KEY_APP_LOCK = "app_lock_enabled"
    }
}
