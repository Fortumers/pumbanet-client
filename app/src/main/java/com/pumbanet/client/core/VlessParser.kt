package com.pumbanet.client.core

import android.net.Uri
import org.json.JSONObject
import java.net.URLDecoder
import java.util.UUID

/**
 * Полный VLESS parser с валидацией и поддержкой всех параметров
 * reality, xhttp, ws, grpc, tcp, http, h2
 */
object VlessParser {

    /**
     * Распарсить vless:// ссылку в Xray JSON конфиг
     */
    fun parse(vlessLink: String): VlessParseResult {
        return try {
            if (!vlessLink.startsWith("vless://")) {
                return VlessParseResult.Error("Invalid scheme")
            }

            val uri = Uri.parse(vlessLink)
            
            // UUID validation
            val uuid = uri.userInfo
            if (!isValidUuid(uuid)) {
                return VlessParseResult.Error("Invalid UUID")
            }

            // Host validation
            val host = uri.host
            if (host.isNullOrBlank()) {
                return VlessParseResult.Error("Invalid host")
            }

            // Port validation
            val port = uri.port.takeIf { it > 0 && it <= 65535 } ?: 443

            // Query parameters
            val query = parseQuery(uri.query ?: "")
            
            // Security
            val security = query["security"] ?: "none"
            val encryption = query["encryption"] ?: "none"
            val flow = query["flow"] ?: ""

            // Network
            val network = query["type"] ?: "tcp"

            // TLS/REALITY parameters
            val sni = query["sni"] ?: host
            val fp = query["fp"] ?: "chrome"
            val alpn = query["alpn"]
            val allowInsecure = query["allowInsecure"]?.toBooleanOrNull()

            // REALITY parameters
            val pbk = query["pbk"]
            val sid = query["sid"]
            val spiderX = query["spx"]

            // Network-specific parameters
            val wsPath = query["path"] ?: "/"
            val wsHost = query["host"] ?: host
            val httpHost = query["host"]
            val httpPath = query["path"] ?: "/"
            val grpcServiceName = query["serviceName"] ?: ""
            val grpcMode = query["mode"] ?: "gun"
            val xhttpPath = query["path"] ?: "/"
            val xhttpHost = query["host"] ?: host
            val xhttpMode = query["mode"] ?: "auto"

            val remark = uri.fragment ?: "PumbaNET"

            // Build Xray config
            val config = buildXrayConfig(
                uuid = uuid,
                host = host,
                port = port,
                remark = remark,
                security = security,
                encryption = encryption,
                flow = flow,
                network = network,
                sni = sni,
                fp = fp,
                alpn = alpn,
                allowInsecure = allowInsecure,
                pbk = pbk,
                sid = sid,
                spiderX = spiderX,
                wsPath = wsPath,
                wsHost = wsHost,
                httpHost = httpHost,
                httpPath = httpPath,
                grpcServiceName = grpcServiceName,
                grpcMode = grpcMode,
                xhttpPath = xhttpPath,
                xhttpHost = xhttpHost,
                xhttpMode = xhttpMode
            )

            VlessParseResult.Success(config, remark)

        } catch (e: Exception) {
            VlessParseResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun parseQuery(query: String): Map<String, String> {
        return query.split("&").filter { it.isNotBlank() }.associate { pair ->
            val parts = pair.split("=", limit = 2)
            val key = URLDecoder.decode(parts[0], "UTF-8")
            val value = if (parts.size > 1) URLDecoder.decode(parts[1], "UTF-8") else ""
            key to value
        }
    }

    private fun String.toBooleanOrNull(): Boolean? {
        return when (this.lowercase()) {
            "true", "1", "yes" -> true
            "false", "0", "no" -> false
            else -> null
        }
    }

    private fun isValidUuid(uuid: String): Boolean {
        return try {
            UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun buildXrayConfig(
        uuid: String,
        host: String,
        port: Int,
        remark: String,
        security: String,
        encryption: String,
        flow: String,
        network: String,
        sni: String,
        fp: String?,
        alpn: String?,
        allowInsecure: Boolean?,
        pbk: String?,
        sid: String?,
        spiderX: String?,
        wsPath: String,
        wsHost: String,
        httpHost: String?,
        httpPath: String,
        grpcServiceName: String,
        grpcMode: String,
        xhttpPath: String,
        xhttpHost: String,
        xhttpMode: String
    ): String {
        val config = JSONObject()
        
        // Log
        config.put("log", JSONObject().apply {
            put("loglevel", "warning")
        })

        // Inbounds (SOCKS proxy)
        config.put("inbounds", org.json.JSONArray().apply {
            put(JSONObject().apply {
                put("tag", "socks")
                put("port", 10808)
                put("listen", "127.0.0.1")
                put("protocol", "socks")
                put("sniffing", JSONObject().apply {
                    put("enabled", true)
                    put("destOverride", org.json.JSONArray().apply {
                        put("http")
                        put("tls")
                        put("quic")
                    })
                    put("routeOnly", false)
                })
                put("settings", JSONObject().apply {
                    put("auth", "noauth")
                    put("udp", true)
                    put("allowTransparent", false)
                })
            })
        })

        // Outbounds (VLESS)
        config.put("outbounds", org.json.JSONArray().apply {
            // Proxy outbound
            put(JSONObject().apply {
                put("tag", "proxy")
                put("protocol", "vless")
                put("settings", JSONObject().apply {
                    put("vnext", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("address", host)
                            put("port", port)
                            put("users", org.json.JSONArray().apply {
                                put(JSONObject().apply {
                                    put("id", uuid)
                                    put("encryption", encryption)
                                    put("flow", flow)
                                    put("level", 8)
                                })
                            })
                        })
                    })
                })

                // Stream settings
                put("streamSettings", buildStreamSettings(
                    network, security, sni, fp, alpn, allowInsecure,
                    pbk, sid, spiderX,
                    wsPath, wsHost, httpHost, httpPath,
                    grpcServiceName, grpcMode,
                    xhttpPath, xhttpHost, xhttpMode
                ))

                // Mux
                put("mux", JSONObject().apply {
                    put("enabled", false)
                    put("concurrency", -1)
                })
            })

            // Direct outbound
            put(JSONObject().apply {
                put("tag", "direct")
                put("protocol", "freedom")
                put("settings", JSONObject())
            })

            // Block outbound
            put(JSONObject().apply {
                put("tag", "block")
                put("protocol", "blackhole")
                put("settings", JSONObject().apply {
                    put("response", JSONObject().apply {
                        put("type", "http")
                    })
                })
            })
        })

        // Routing
        config.put("routing", JSONObject().apply {
            put("domainStrategy", "AsIs")
            put("rules", org.json.JSONArray())
        })

        return config.toString(2)
    }

    private fun buildStreamSettings(
        network: String,
        security: String,
        sni: String,
        fp: String?,
        alpn: String?,
        allowInsecure: Boolean?,
        pbk: String?,
        sid: String?,
        spiderX: String?,
        wsPath: String,
        wsHost: String,
        httpHost: String?,
        httpPath: String,
        grpcServiceName: String,
        grpcMode: String,
        xhttpPath: String,
        xhttpHost: String,
        xhttpMode: String
    ): JSONObject {
        val streamSettings = JSONObject()
        streamSettings.put("network", network)

        // Security settings
        when (security) {
            "tls" -> {
                streamSettings.put("security", "tls")
                streamSettings.put("tlsSettings", JSONObject().apply {
                    put("serverName", sni)
                    put("fingerprint", fp ?: "chrome")
                    put("allowInsecure", allowInsecure ?: false)
                    
                    if (alpn != null) {
                        put("alpn", org.json.JSONArray().apply {
                            alpn.split(",").filter { it.isNotBlank() }.forEach { put(it) }
                        })
                    }
                })
            }
            "reality" -> {
                streamSettings.put("security", "reality")
                streamSettings.put("realitySettings", JSONObject().apply {
                    put("serverName", sni)
                    put("fingerprint", fp ?: "chrome")
                    put("publicKey", pbk ?: "")
                    put("shortId", sid ?: "")
                    put("spiderX", spiderX ?: "")
                })
            }
            else -> {
                streamSettings.put("security", "none")
            }
        }

        // Network settings
        when (network) {
            "ws" -> {
                streamSettings.put("wsSettings", JSONObject().apply {
                    put("path", wsPath)
                    put("host", wsHost)
                })
            }
            "http", "h2" -> {
                streamSettings.put("httpSettings", JSONObject().apply {
                    put("host", org.json.JSONArray().apply {
                        if (httpHost != null) put(httpHost)
                    })
                    put("path", httpPath)
                })
            }
            "grpc" -> {
                streamSettings.put("grpcSettings", JSONObject().apply {
                    put("serviceName", grpcServiceName)
                    put("multiMode", grpcMode == "multi")
                    put("idle_timeout", 15)
                    put("health_check_timeout", 20)
                })
            }
            "xhttp", "httpupgrade" -> {
                streamSettings.put("xhttpSettings", JSONObject().apply {
                    put("path", xhttpPath)
                    put("host", xhttpHost)
                    put("mode", xhttpMode)
                })
            }
        }

        return streamSettings
    }

    sealed class VlessParseResult {
        data class Success(
            val configJson: String,
            val remark: String
        ) : VlessParseResult()
        data class Error(val message: String) : VlessParseResult()
    }
}
