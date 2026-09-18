package com.pumbanet.client.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Утилита для замера пинга до сервера
 */
object PingTest {

    /**
     * Замер пинга до сервера
     * @param host адрес сервера
     * @param port порт сервера
     * @return пинг в миллисекундах или null если ошибка
     */
    suspend fun ping(host: String, port: Int, timeout: Int = 5000): Int? = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), timeout)
            val endTime = System.currentTimeMillis()
            socket.close()
            
            (endTime - startTime).toInt()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Замер пинга с несколькими попытками
     * @return средний пинг или null
     */
    suspend fun pingWithRetries(host: String, port: Int, retries: Int = 3): Int? {
        val results = mutableListOf<Int>()
        
        repeat(retries) {
            val ping = ping(host, port)
            if (ping != null) {
                results.add(ping)
            }
        }
        
        return if (results.isNotEmpty()) {
            results.average().toInt()
        } else {
            null
        }
    }
}
