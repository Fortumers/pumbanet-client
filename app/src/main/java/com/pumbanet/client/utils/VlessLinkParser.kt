package com.pumbanet.client.utils

import android.net.Uri
import org.json.JSONObject
import java.net.URLDecoder

/**
 * Полный парсер VLESS ссылок с поддержкой всех современных параметров
 * reality, xhttp, ws, tcp, grpc, etc.
 */
object VlessLinkParser {

    /**
     * Парсинг vless:// ссылки в JSON конфиг для Xray
     */
    fun parse(vlessLink: String): String? {
        return try {
            if (!vlessLink.startsWith("vless://")) {
                return null
            }

            val uri = Uri.parse(vlessLink)
            
            // Базовые параметры
            val uuid = uri.userInfo ?: return null
            val host = uri.host ?: return null
            val port = uri.port.takeIf { it > 0 } ?: 443
            val remark = uri.fragment ?: "PumbaNET"

            // Query параметры
            val query = uri.query?.let { parseQuery(it) } ?: emptyMap()

            // Security
            val security = query["security"] ?: "none"
            val encryption = query["encryption"] ?: "none"
            val flow = query["flow"] ?: ""

            // Network settings
            val network = query["type"] ?: "tcp"
            
            // TLS/REALITY параметры
            val sni = query["sni"] ?: host
            val fp = query["fp"] ?: "chrome"
            val alpn = query["alpn"]
            val allowInsecure = query["allowInsecure"]?.toBooleanOrNull()
            
            // REALITY параметры
            val pbk = query["pbk"]
            val sid = query["sid"]
            val spiderX = query["spx"]

            // WebSocket параметры
            val wsPath = query["path"] ?: "/"
            val wsHost = query["host"] ?: host
            
            // HTTP/2 параметры
            val httpHost = query["host"]
            val httpPath = query["path"] ?: "/"
            
            // gRPC параметры
            val grpcServiceName = query["serviceName"] ?: ""
            val grpcMode = query["mode"] ?: "gun"
            
            // xhttp параметры
            val xhttpPath = query["path"] ?: "/"
            val xhttpHost = query["host"] ?: host
            val xhttpMode = query["mode"] ?: "auto"

            // Построение JSON конфига
            buildXrayConfig(
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
        } catch (e: Exception) {
            android.util.Log.e("VlessLinkParser", "Error parsing vless link", e)
            null
        }
    }

    private fun parseQuery(query: String): Map<String, String> {
        return query.split("&").associate { pair ->
            val (key, value) = pair.split("=", limit = 2)
            URLDecoder.decode(key, "UTF-8") to URLDecoder.decode(value, "UTF-8")
        }
    }

    private fun String.toBooleanOrNull(): Boolean? {
        return when (this.lowercase()) {
            "true", "1", "yes" -> true
            "false", "0", "no" -> false
            else -> null
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
                    })
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
                    network = network,
                    security = security,
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
                ))

                // Proxy settings
                put("proxySettings", JSONObject().apply {
                    put("tag", "direct")
                })

                // Mux
                put("mux", JSONObject().apply {
                    put("enabled", false)
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
                            alpn.split(",").forEach { put(it) }
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
                })
            }
            "xhttp", "httpupgrade" -> {
                streamSettings.put("xhttpSettings", JSONObject().apply {
                    put("path", xhttpPath)
                    put("host", xhttpHost)
                    put("mode", xhttpMode)
                })
            }
            // TCP by default
            else -> {
                // TCP settings (empty for basic TCP)
            }
        }

        return streamSettings
    }
}
