package com.pumbanet.client

import android.net.Uri
import org.json.JSONObject
import java.net.URLDecoder

class VlessLinkParser {

    companion object {
        fun parse(vlessLink: String): String {
            // Парсинг vless:// ссылки в JSON конфиг Xray
            val uri = Uri.parse(vlessLink)
            val uuid = uri.host ?: throw IllegalArgumentException("Invalid vless link: no UUID")
            val port = uri.port ?: 443

            val queryParams = uri.queryParameterNames.associateWith { name ->
                uri.getQueryParameter(name) ?: ""
            }

            val address = queryParams["address"] ?: queryParams["host"] ?: throw IllegalArgumentException("No address")
            val security = queryParams["security"] ?: "tls"
            val network = queryParams["type"] ?: "tcp"
            val flow = queryParams["flow"] ?: ""
            val encryption = queryParams["encryption"] ?: "none"

            // TLS настройки
            val tlsSettings = when (security) {
                "tls", "reality" -> {
                    val sni = queryParams["sni"] ?: address
                    val fp = queryParams["fp"] ?: "chrome"
                    val pbk = queryParams["pbk"]
                    val sid = queryParams["sid"]

                    JSONObject().apply {
                        put("serverName", sni)
                        put("fingerprint", fp)
                        if (security == "reality" && !pbk.isNullOrBlank()) {
                            put("realitySettings", JSONObject().apply {
                                put("publicKey", pbk)
                                put("shortId", sid ?: "")
                            })
                        }
                    }
                }
                else -> JSONObject()
            }

            // Stream settings
            val streamSettings = JSONObject().apply {
                put("network", network)
                put("security", security)
                if (security == "tls" || security == "reality") {
                    put("tlsSettings", tlsSettings)
                }
            }

            // Vnext settings
            val userSettings = JSONObject().apply {
                put("id", uuid)
                put("encryption", encryption)
                if (flow.isNotBlank()) {
                    put("flow", flow)
                }
            }

            val vnext = JSONObject().apply {
                put("address", address)
                put("port", port)
                put("users", org.json.JSONArray().apply {
                    put(userSettings)
                })
            }

            val outbound = JSONObject().apply {
                put("protocol", "vless")
                put("settings", JSONObject().apply {
                    put("vnext", org.json.JSONArray().apply {
                        put(vnext)
                    })
                })
                put("streamSettings", streamSettings)
            }

            // Полный конфиг
            val config = JSONObject().apply {
                put("inbounds", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("port", 10808)
                        put("listen", "127.0.0.1")
                        put("protocol", "socks")
                        put("settings", JSONObject().apply {
                            put("auth", "noauth")
                            put("udp", true)
                        })
                    })
                })
                put("outbounds", org.json.JSONArray().apply {
                    put(outbound)
                })
                put("routing", JSONObject().apply {
                    put("domainStrategy", "AsIs")
                })
            }

            return config.toString(2)
        }
    }
}
