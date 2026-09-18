package com.pumbanet.client

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.pumbanet.client.utils.PreferencesManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var darkThemeSwitch: SwitchCompat
    private lateinit var killSwitchSwitch: SwitchCompat
    private lateinit var autoConnectSwitch: SwitchCompat
    private lateinit var splitTunnelSwitch: SwitchCompat
    private lateinit var selectAppsButton: Button
    private lateinit var statsUploaded: TextView
    private lateinit var statsDownloaded: TextView
    private lateinit var statsTotal: TextView
    
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefsManager = PreferencesManager(this)

        darkThemeSwitch = findViewById(R.id.darkThemeSwitch)
        killSwitchSwitch = findViewById(R.id.killSwitchSwitch)
        autoConnectSwitch = findViewById(R.id.autoConnectSwitch)
        splitTunnelSwitch = findViewById(R.id.splitTunnelSwitch)
        selectAppsButton = findViewById(R.id.selectAppsButton)
        statsUploaded = findViewById(R.id.statsUploaded)
        statsDownloaded = findViewById(R.id.statsDownloaded)
        statsTotal = findViewById(R.id.statsTotal)

        loadSettings()
        setupListeners()
        loadStats()
    }

    private fun loadSettings() {
        darkThemeSwitch.isChecked = prefsManager.isDarkTheme()
        killSwitchSwitch.isChecked = prefsManager.isKillSwitchEnabled()
        autoConnectSwitch.isChecked = prefsManager.isAutoConnectEnabled()
        splitTunnelSwitch.isChecked = prefsManager.isSplitTunnelEnabled()
        selectAppsButton.isEnabled = splitTunnelSwitch.isChecked
    }

    private fun setupListeners() {
        darkThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setDarkTheme(isChecked)
            // Применить тему нужно будет в MainActivity
        }

        killSwitchSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setKillSwitch(isChecked)
        }

        autoConnectSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setAutoConnect(isChecked)
        }

        splitTunnelSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setSplitTunnelEnabled(isChecked)
            selectAppsButton.isEnabled = isChecked
        }

        selectAppsButton.setOnClickListener {
            val intent = Intent(this, SelectAppsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadStats() {
        val (uploaded, downloaded) = prefsManager.getSessionStats()
        val total = uploaded + downloaded

        statsUploaded.text = "Загружено: ${formatBytes(uploaded)}"
        statsDownloaded.text = "Скачано: ${formatBytes(downloaded)}"
        statsTotal.text = "Всего: ${formatBytes(total)}"
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}
