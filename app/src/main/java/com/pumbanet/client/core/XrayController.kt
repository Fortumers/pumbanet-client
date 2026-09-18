package com.pumbanet.client.core

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Xray Controller - правильное управление binary и процессом
 * Копирование из assets, chmod, запуск, мониторинг, остановка
 */
class XrayController(private val context: Context) {

    private val xrayBinaryPath: File by lazy {
        File(context.filesDir, "xray")
    }

    private var xrayProcess: Process? = null
    private val executor = Executors.newSingleThreadExecutor()
    
    var onXrayExited: ((exitCode: Int) -> Unit)? = null
    var onXrayLog: ((line: String) -> Unit)? = null

    /**
     * Проверка наличия Xray binary
     */
    suspend fun isXrayAvailable(): Boolean = withContext(Dispatchers.IO) {
        xrayBinaryPath.exists() && xrayBinaryPath.canExecute()
    }

    /**
     * Установка Xray binary из assets
     */
    suspend fun installXray(): XrayInstallResult = withContext(Dispatchers.IO) {
        try {
            // Копирование из assets
            val inputStream = context.assets.open("xray")
            val outputStream = FileOutputStream(xrayBinaryPath)
            
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            
            // Установка executable permission
            xrayBinaryPath.setExecutable(true)
            
            // Проверка
            if (!xrayBinaryPath.canExecute()) {
                return@withContext XrayInstallResult.Error("Failed to set executable permission")
            }
            
            Log.i("XrayController", "Xray binary installed: ${xrayBinaryPath.absolutePath}")
            XrayInstallResult.Success
            
        } catch (e: Exception) {
            Log.e("XrayController", "Failed to install Xray", e)
            XrayInstallResult.Error("Installation failed: ${e.message}")
        }
    }

    /**
     * Проверка конфигурации Xray
     */
    suspend fun validateConfig(configJson: String): XrayConfigValidationResult = withContext(Dispatchers.IO) {
        try {
            // Сохранение конфига во временный файл
            val configFile = createTempConfigFile(configJson)
            
            // Запуск проверки: xray run -test -c config.json
            val processBuilder = ProcessBuilder(
                xrayBinaryPath.absolutePath,
                "run",
                "-test",
                "-c",
                configFile.absolutePath
            )
            
            val process = processBuilder.start()
            val exitCode = process.waitFor()
            
            // Очистка временного файла
            configFile.delete()
            
            if (exitCode == 0) {
                XrayConfigValidationResult.Valid
            } else {
                val errorOutput = process.errorStream.bufferedReader().readText()
                XrayConfigValidationResult.Invalid(errorOutput)
            }
            
        } catch (e: Exception) {
            XrayConfigValidationResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Запуск Xray с TUN fd
     */
    suspend fun startXray(configJson: String, tunFd: Int): XrayStartResult = withContext(Dispatchers.IO) {
        try {
            // Проверка binary
            if (!isXrayAvailable()) {
                return@withContext XrayStartResult.Error("Xray binary not found")
            }
            
            // Проверка конфига
            val validation = validateConfig(configJson)
            if (validation is XrayConfigValidationResult.Invalid) {
                return@withContext XrayStartResult.Error("Invalid config: ${validation.error}")
            }
            
            // Сохранение конфига
            val configFile = createTempConfigFile(configJson)
            
            // Запуск Xray с TUN fd
            val processBuilder = ProcessBuilder(
                xrayBinaryPath.absolutePath,
                "run",
                "-c",
                configFile.absolutePath
            ).apply {
                environment()["XRAY_TUN_FD"] = tunFd.toString()
                redirectErrorStream()
            }
            
            xrayProcess = processBuilder.start()
            
            // Мониторинг процесса
            monitorXrayProcess()
            
            Log.i("XrayController", "Xray started with TUN fd: $tunFd")
            XrayStartResult.Success
            
        } catch (e: Exception) {
            Log.e("XrayController", "Failed to start Xray", e)
            XrayStartResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Мониторинг процесса Xray
     */
    private fun monitorXrayProcess() {
        executor.execute {
            try {
                // Чтение логов
                xrayProcess?.inputStream?.bufferedReader()?.forEachLine { line ->
                    onXrayLog?.invoke(line)
                    Log.d("Xray", line)
                }
                
                // Ожидание завершения
                val exitCode = xrayProcess?.waitFor() ?: -1
                Log.w("XrayController", "Xray exited with code: $exitCode")
                
                onXrayExited?.invoke(exitCode)
                
            } catch (e: Exception) {
                Log.e("XrayController", "Xray monitor error", e)
                onXrayExited?.invoke(-1)
            }
        }
    }

    /**
     * Остановка Xray
     */
    fun stopXray() {
        try {
            xrayProcess?.let { process ->
                if (process.isAlive) {
                    // Попытка корректной остановки
                    process.destroy()
                    
                    // Ожидание завершения
                    if (!process.waitFor(3, TimeUnit.SECONDS)) {
                        // Принудительная остановка
                        process.destroyForcibly()
                    }
                }
            }
            
            xrayProcess = null
            Log.i("XrayController", "Xray stopped")
            
        } catch (e: Exception) {
            Log.e("XrayController", "Failed to stop Xray", e)
        }
    }

    /**
     * Создание временного файла конфига
     */
    private fun createTempConfigFile(configJson: String): File {
        val configFile = File(context.cacheDir, "xray_config_${System.currentTimeMillis()}.json")
        configFile.writeText(configJson)
        return configFile
    }

    /**
     * Очистка временных файлов
     */
    fun cleanup() {
        context.cacheDir.listFiles { file ->
            file.name.startsWith("xray_config_")
        }?.forEach { it.delete() }
    }

    sealed class XrayInstallResult {
        object Success : XrayInstallResult()
        data class Error(val message: String) : XrayInstallResult()
    }

    sealed class XrayConfigValidationResult {
        object Valid : XrayConfigValidationResult()
        data class Invalid(val error: String) : XrayConfigValidationResult()
        data class Error(val message: String) : XrayConfigValidationResult()
    }

    sealed class XrayStartResult {
        object Success : XrayStartResult()
        data class Error(val message: String) : XrayStartResult()
    }
}
