package com.pumbanet.client

import android.content.Context
import libbox.BoxService
import libbox.CommandClient
import libbox.CommandClientHandler
import libbox.CommandClientOptions
import libbox.CommandServer
import libbox.CommandServerHandler

class PumbaBoxService(private val context: Context) {
    private var boxService: BoxService? = null
    private var commandServer: CommandServer? = null
    private var commandClient: CommandClient? = null

    fun start(configJson: String, vpnFd: Int, onStatusChange: (Boolean, String?) -> Unit) {
        val options = CommandClientOptions().apply {
            config = configJson
            platform = "android"
        }

        commandServer = CommandServer(CommandServerHandler { status, message ->
            onStatusChange(status == 1, message)
        })
        commandServer?.start()

        boxService = BoxService(context, options, commandServer!!)
        boxService?.start(vpnFd)
    }

    fun stop() {
        boxService?.stop()
        boxService = null
        commandServer?.stop()
        commandServer = null
        commandClient?.close()
        commandClient = null
    }
}
