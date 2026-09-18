package com.pumbanet.client.utils

import android.content.Context
import java.io.File

/**
 * Командная строка (CLI) (Feature 28)
 * Управление через termux/adb, скрипты, Tasker
 */
class CliManager(private val context: Context) {

    private val cliDir = File(context.filesDir, "cli")

    init {
        if (!cliDir.exists()) {
            cliDir.mkdirs()
        }
    }

    /**
     * Обработка CLI команд
     */
    fun executeCommand(command: String): CliResult {
        val parts = command.split(" ")
        val cmd = parts.firstOrNull()?.lowercase() ?: ""
        val args = parts.drop(1)

        return when (cmd) {
            "connect" -> handleConnect(args)
            "disconnect" -> handleDisconnect()
            "status" -> handleStatus()
            "list" -> handleList()
            "select" -> handleSelect(args)
            "help" -> handleHelp()
            else -> CliResult.Error("Unknown command: $cmd")
        }
    }

    private fun handleConnect(args: List<String>): CliResult {
        // В реальном приложении: запуск VPN сервиса
        return CliResult.Success("VPN connected")
    }

    private fun handleDisconnect(): CliResult {
        // В реальном приложении: остановка VPN сервиса
        return CliResult.Success("VPN disconnected")
    }

    private fun handleStatus(): CliResult {
        val isConnected = VpnService.isRunning()
        return CliResult.Success("VPN Status: ${if (isConnected) "Connected" else "Disconnected"}")
    }

    private fun handleList(): CliResult {
        val profiles = PreferencesManager(context).getProfiles()
        val output = buildString {
            appendLine("Available profiles:")
            profiles.forEachIndexed { i, profile ->
                appendLine("  $i. ${profile.displayName()} (${profile.serverAddress}:${profile.serverPort})")
            }
        }
        return CliResult.Success(output)
    }

    private fun handleSelect(args: List<String>): CliResult {
        if (args.isEmpty()) {
            return CliResult.Error("Usage: select <profile_id>")
        }
        val profileId = args.first()
        PreferencesManager(context).setActiveProfile(profileId)
        return CliResult.Success("Selected profile: $profileId")
    }

    private fun handleHelp(): CliResult {
        val helpText = """
            PumbaNET CLI Commands:
            - connect [profile_id]  : Connect to VPN
            - disconnect            : Disconnect VPN
            - status                : Show VPN status
            - list                  : List available profiles
            - select <profile_id>   : Select active profile
            - help                  : Show this help
        """.trimIndent()
        return CliResult.Success(helpText)
    }

    /**
     * Экспорт CLI скрипта для Tasker
     */
    fun exportTaskerScript(): String {
        return """
            #!/system/bin/sh
            # PumbaNET Tasker Script
            am start-activity -n com.pumbanet.client/.MainActivity --ei command connect
        """.trimIndent()
    }

    sealed class CliResult {
        data class Success(val output: String) : CliResult()
        data class Error(val message: String) : CliResult()
    }
}
