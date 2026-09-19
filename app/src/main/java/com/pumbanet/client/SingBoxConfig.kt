package com.pumbanet.client

import org.json.JSONObject

data class SingBoxConfig(
    val log: LogConfig = LogConfig(),
    val dns: DnsConfig = DnsConfig(),
    val inbounds: List<InboundConfig> = emptyList(),
    val outbounds: List<OutboundConfig> = emptyList(),
    val route: RouteConfig = RouteConfig()
)

data class LogConfig(
    val disabled: Boolean = false,
    val level: String = "info"
)

data class DnsConfig(
    val servers: List<DnsServerConfig> = emptyList(),
    val final: String = "dns-remote"
)

data class DnsServerConfig(
    val tag: String,
    val address: String,
    val address_resolver: String? = null,
    val detour: String? = null
)

data class InboundConfig(
    val type: String,
    val tag: String,
    val interface_name: String? = null,
    val inet4_address: String? = null,
    val inet6_address: String? = null,
    val mtu: Int? = null,
    val auto_route: Boolean? = null,
    val strict_route: Boolean? = null,
    val endpoint_independent_nat: Boolean? = null,
    val stack: String? = null,
    val sniff: Boolean? = null,
    val sniff_override_destination: Boolean? = null
)

data class OutboundConfig(
    val type: String,
    val tag: String,
    val server: String? = null,
    val server_port: Int? = null,
    val uuid: String? = null,
    val flow: String? = null,
    val packet_encoding: String? = null,
    val tls: TlsConfig? = null,
    val transport: TransportConfig? = null,
    val network: String? = null,
    val detour: String? = null
)

data class TlsConfig(
    val enabled: Boolean = true,
    val server_name: String? = null,
    val insecure: Boolean = false,
    val utls: UtlsConfig? = null,
    val reality: RealityConfig? = null
)

data class UtlsConfig(
    val enabled: Boolean = true,
    val fingerprint: String = "chrome"
)

data class RealityConfig(
    val enabled: Boolean = true,
    val public_key: String? = null,
    val short_id: String? = null
)

data class TransportConfig(
    val type: String,
    val path: String? = null,
    val headers: HeadersConfig? = null
)

data class HeadersConfig(
    val Host: String? = null
)

data class RouteConfig(
    val auto_detect_interface: Boolean = true,
    val final: String? = null,
    val rules: List<RouteRuleConfig> = emptyList()
)

data class RouteRuleConfig(
    val outbound: String,
    val ip_cidr: List<String>? = null,
    val domain: List<String>? = null
)

fun buildSingBoxConfig(profile: VlessProfile, tunInterface: String = "tun0"): SingBoxConfig {
    val uuid = profile.link.substringAfter("vless://").substringBefore("@")

    val tls = when (profile.security.lowercase()) {
        "reality" -> TlsConfig(
            enabled = true,
            server_name = profile.host,
            utls = UtlsConfig(enabled = true, fingerprint = "chrome"),
            reality = RealityConfig(
                enabled = true,
                public_key = profile.link.substringAfter("pbk=").substringBefore("&"),
                short_id = profile.link.substringAfter("sid=").substringBefore("&")
            )
        )
        "tls" -> TlsConfig(
            enabled = true,
            server_name = profile.host,
            utls = UtlsConfig(enabled = true, fingerprint = "chrome")
        )
        else -> TlsConfig(enabled = false)
    }

    val transport = when (profile.transport.lowercase()) {
        "ws" -> TransportConfig(
            type = "ws",
            path = profile.link.substringAfter("path=").substringBefore("&").ifEmpty { "/" },
            headers = HeadersConfig(Host = profile.host)
        )
        "grpc" -> TransportConfig(type = "grpc")
        "http" -> TransportConfig(type = "http")
        else -> TransportConfig(type = "tcp")
    }

    val outbound = OutboundConfig(
        type = "vless",
        tag = "proxy",
        server = profile.host,
        server_port = profile.port,
        uuid = uuid,
        tls = tls,
        transport = transport
    )

    val tunInbound = InboundConfig(
        type = "tun",
        tag = "tun-in",
        interface_name = tunInterface,
        inet4_address = "172.19.0.1/30",
        inet6_address = "fdfe:dcba:9876::1/126",
        mtu = 9000,
        auto_route = true,
        strict_route = true,
        endpoint_independent_nat = false,
        stack = "mixed",
        sniff = true,
        sniff_override_destination = false
    )

    val dnsServers = listOf(
        DnsServerConfig(tag = "dns-remote", address = "tls://1.1.1.1", detour = "proxy"),
        DnsServerConfig(tag = "dns-local", address = "local", detour = "direct")
    )

    val routeRules = listOf(
        RouteRuleConfig(outbound = "dns-out", domain = listOf("dns.google", "1.1.1.1")),
        RouteRuleConfig(outbound = "direct", ip_cidr = listOf("172.19.0.0/30", "fdfe:dcba:9876::/126"))
    )

    return SingBoxConfig(
        inbounds = listOf(tunInbound),
        outbounds = listOf(outbound, OutboundConfig(type = "direct", tag = "direct"), OutboundConfig(type = "dns", tag = "dns-out")),
        dns = DnsConfig(servers = dnsServers),
        route = RouteConfig(auto_detect_interface = true, rules = routeRules)
    )
}

fun SingBoxConfig.toJson(): String {
    return JSONObject().apply {
        put("log", JSONObject().apply {
            put("disabled", log.disabled)
            put("level", log.level)
        })
        put("dns", JSONObject().apply {
            put("servers", JSONArray(dns.servers.map { server ->
                JSONObject().apply {
                    put("tag", server.tag)
                    put("address", server.address)
                    server.address_resolver?.let { put("address_resolver", it) }
                    server.detour?.let { put("detour", it) }
                }
            }))
            put("final", dns.final)
        })
        put("inbounds", JSONArray(inbounds.map { inbound ->
            JSONObject().apply {
                put("type", inbound.type)
                put("tag", inbound.tag)
                inbound.interface_name?.let { put("interface_name", it) }
                inbound.inet4_address?.let { put("inet4_address", it) }
                inbound.inet6_address?.let { put("inet6_address", it) }
                inbound.mtu?.let { put("mtu", it) }
                inbound.auto_route?.let { put("auto_route", it) }
                inbound.strict_route?.let { put("strict_route", it) }
                inbound.endpoint_independent_nat?.let { put("endpoint_independent_nat", it) }
                inbound.stack?.let { put("stack", it) }
                inbound.sniff?.let { put("sniff", it) }
                inbound.sniff_override_destination?.let { put("sniff_override_destination", it) }
            }
        }))
        put("outbounds", JSONArray(outbounds.map { outbound ->
            JSONObject().apply {
                put("type", outbound.type)
                put("tag", outbound.tag)
                outbound.server?.let { put("server", it) }
                outbound.server_port?.let { put("server_port", it) }
                outbound.uuid?.let { put("uuid", it) }
                outbound.flow?.let { put("flow", it) }
                outbound.packet_encoding?.let { put("packet_encoding", it) }
                outbound.network?.let { put("network", it) }
                outbound.detour?.let { put("detour", it) }
                outbound.tls?.let { tls ->
                    put("tls", JSONObject().apply {
                        put("enabled", tls.enabled)
                        tls.server_name?.let { put("server_name", it) }
                        put("insecure", tls.insecure)
                        tls.utls?.let { utls ->
                            put("utls", JSONObject().apply {
                                put("enabled", utls.enabled)
                                put("fingerprint", utls.fingerprint)
                            })
                        }
                        tls.reality?.let { reality ->
                            put("reality", JSONObject().apply {
                                put("enabled", reality.enabled)
                                reality.public_key?.let { put("public_key", it) }
                                reality.short_id?.let { put("short_id", it) }
                            })
                        }
                    })
                }
                outbound.transport?.let { transport ->
                    put("transport", JSONObject().apply {
                        put("type", transport.type)
                        transport.path?.let { put("path", it) }
                        transport.headers?.let { headers ->
                            put("headers", JSONObject().apply {
                                headers.Host?.let { put("Host", it) }
                            })
                        }
                    })
                }
            }
        }))
        put("route", JSONObject().apply {
            put("auto_detect_interface", route.auto_detect_interface)
            route.final?.let { put("final", it) }
            put("rules", JSONArray(route.rules.map { rule ->
                JSONObject().apply {
                    put("outbound", rule.outbound)
                    rule.ip_cidr?.let { put("ip_cidr", JSONArray(it)) }
                    rule.domain?.let { put("domain", JSONArray(it)) }
                }
            }))
        })
    }.toString()
}
