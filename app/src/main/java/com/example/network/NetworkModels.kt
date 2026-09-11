package com.example.network

data class IpInfo(
    val ip: String = "—",
    val country: String = "Unknown",
    val countryCode: String = "",
    val city: String = "",
    val isp: String = "Direct Network",
    val org: String = "",
    val queryTimeMs: Long = 0
)

data class VProxiesGateway(
    val id: String,
    val name: String,
    val region: String
) {
    val cleanName: String get() {
        val s = name.replace("gateway.proxies.app", "Gateway", ignoreCase = true)
            .replace("gateway.vproxies.app", "Gateway", ignoreCase = true)
            .replace(".proxies.app", "", ignoreCase = true)
            .trim()
        return if (s.isNotBlank()) s else "Gateway $id"
    }

    val display: String get() = if (region.isNotBlank()) "$cleanName ($region)" else cleanName
}

data class VProxiesRemoteItem(
    val id: Long,
    val gatewayId: String,
    val name: String,
    val protocol: String,
    val protocols: List<String>,
    val status: String,
    val country: String,
    val city: String,
    val latency: Long?,
    val host: String,
    val port: Int
)

data class VProxiesAccountInfo(
    val identity: String,
    val active: Boolean,
    val status: String,
    val packageName: String,
    val remainingDays: Long
)

data class ConnectionConfig(
    val proxyId: Long = 0,
    val host: String = "",
    val port: Int = 1080,
    val protocol: String = "SOCKS5",
    val username: String = "",
    val password: String = "",
    val token: String = "",
    val gatewayId: String = "",
    val expiresAt: Long = 0L,
    val rawConfig: String = ""
)

data class GitHubReleaseInfo(
    val tagName: String,
    val name: String,
    val body: String,
    val publishedAt: String,
    val htmlUrl: String,
    val downloadUrl: String?
)
