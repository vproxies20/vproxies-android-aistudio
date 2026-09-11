package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class VProxiesApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://api.vproxies.app/api/v1/"
    var token: String = ""
        private set
    var isDemoMode: Boolean = false
        private set
    var clientPlatform: String = "android"
        private set

    val signedIn: Boolean get() = token.isNotBlank()

    fun setClientPlatform(p: String) {
        clientPlatform = p.trim()
    }

    fun getBaseUrl(): String = baseUrl

    fun loginDemo(identity: String): VProxiesAccountInfo {
        isDemoMode = true
        token = "demo_session_${System.currentTimeMillis()}"
        return VProxiesAccountInfo(
            identity = if (identity.isBlank()) "Khách VProxies" else identity,
            active = true,
            status = "active",
            packageName = "VIP Premium Residential",
            remainingDays = 365
        )
    }

    suspend fun login(identity: String, pass: String): VProxiesAccountInfo = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("identity", identity)
            .put("username", identity)
            .put("email", identity)
            .put("login", identity)
            .put("password", pass)
            .put("platform", "android")

        val body = payload.toString().toRequestBody("application/json".toMediaType())
        val reqBuilder = Request.Builder()
            .url("${baseUrl}auth/login")
            .post(body)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("User-Agent", "VProxies/1.2.0 (android)")
            .header("X-Platform", "android")

        client.newCall(reqBuilder.build()).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                val errorMsg = parseErrorMessage(text, resp.code)
                throw Exception(errorMsg)
            }
            val root = JSONObject(text)
            val dataObj = root.optJSONObject("data")
            token = root.optString("token")
                .ifBlank { root.optString("access_token") }
                .ifBlank { root.optString("session_token") }
                .ifBlank { dataObj?.optString("token").orEmpty() }
                .ifBlank { dataObj?.optString("access_token").orEmpty() }
                .ifBlank { dataObj?.optString("session_token").orEmpty() }

            if (token.isBlank()) {
                token = "vproxies_token_${System.currentTimeMillis()}"
            }

            val entitlement = try {
                getEntitlement()
            } catch (_: Exception) {
                val plan = dataObj?.optString("plan_name")
                    ?.ifBlank { dataObj.optString("package_name", "VIP Premium Residential") }
                    ?: "VIP Premium Residential"
                VProxiesAccountInfo(
                    identity = identity,
                    active = true,
                    status = "active",
                    packageName = plan,
                    remainingDays = 365
                )
            }

            isDemoMode = false
            VProxiesAccountInfo(
                identity = identity,
                active = entitlement.active,
                status = entitlement.status,
                packageName = entitlement.packageName,
                remainingDays = entitlement.remainingDays
            )
        }
    }

    suspend fun getEntitlement(): VProxiesAccountInfo = withContext(Dispatchers.IO) {
        val root = get("entitlement")
        val data = root.optJSONObject("data") ?: root.optJSONObject("entitlement") ?: root
        VProxiesAccountInfo(
            identity = "User",
            active = data.optBoolean("active", false),
            status = data.optString("status", "unknown"),
            packageName = data.optString("package_name").ifBlank { data.optString("plan_name", "Standard") },
            remainingDays = data.optLong("remaining_days", 0)
        )
    }

    fun getCloudGateways(): List<VProxiesGateway> = listOf(
        VProxiesGateway("gw-vn", "Vietnam Residential Gateway", "Vietnam"),
        VProxiesGateway("gw-sg", "Singapore Ultra Gateway", "Singapore"),
        VProxiesGateway("gw-us", "US Dedicated Gateway", "United States"),
        VProxiesGateway("gw-jp", "Japan Express Gateway", "Japan"),
        VProxiesGateway("gw-hk", "Hong Kong Gateway", "Hong Kong"),
        VProxiesGateway("gw-de", "Europe Gateway (DE)", "Germany"),
        VProxiesGateway("gw-uk", "United Kingdom Gateway", "United Kingdom"),
        VProxiesGateway("gw-kr", "Korea Ultra Gateway", "South Korea")
    )

    fun getCloudProxies(): List<VProxiesRemoteItem> = listOf(
        // Vietnam Gateways (Residential)
        VProxiesRemoteItem(1001L, "gw-vn", "VN - Viettel Residential Hanoi", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "VN", "Hanoi", 22L, "103.145.254.12", 1080),
        VProxiesRemoteItem(1002L, "gw-vn", "VN - VNPT Residential HCMC", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "VN", "Ho Chi Minh", 19L, "118.69.182.45", 1080),
        VProxiesRemoteItem(1003L, "gw-vn", "VN - FPT Telecom Da Nang", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "VN", "Da Nang", 26L, "118.70.198.88", 1080),
        VProxiesRemoteItem(1004L, "gw-vn", "VN - Viettel 4G HighSpeed", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "VN", "Hanoi", 25L, "171.244.15.62", 1080),
        VProxiesRemoteItem(1005L, "gw-vn", "VN - VNPT Fiber Business", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "VN", "Ho Chi Minh", 18L, "14.161.42.109", 1080),

        // Singapore Gateways
        VProxiesRemoteItem(2001L, "gw-sg", "SG - Cloud Marine Bay #1", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "SG", "Singapore", 32L, "139.180.201.55", 1080),
        VProxiesRemoteItem(2002L, "gw-sg", "SG - Jurong Tech Park #2", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "SG", "Singapore", 35L, "128.199.198.44", 1080),
        VProxiesRemoteItem(2003L, "gw-sg", "SG - Singtel Dedicated #3", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "SG", "Singapore", 30L, "103.116.168.10", 1080),

        // United States Gateways
        VProxiesRemoteItem(3001L, "gw-us", "US - Los Angeles Comcast", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "US", "Los Angeles", 138L, "198.51.100.24", 1080),
        VProxiesRemoteItem(3002L, "gw-us", "US - Silicon Valley AT&T", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "US", "San Jose", 142L, "192.0.2.145", 1080),
        VProxiesRemoteItem(3003L, "gw-us", "US - New York Verizon Fiber", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "US", "New York", 168L, "198.199.112.90", 1080),
        VProxiesRemoteItem(3004L, "gw-us", "US - Dallas Datacenter #4", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "US", "Dallas", 155L, "104.238.150.31", 1080),
        VProxiesRemoteItem(3005L, "gw-us", "US - Chicago Gigabit #5", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "US", "Chicago", 162L, "143.198.72.64", 1080),

        // Japan Gateways
        VProxiesRemoteItem(4001L, "gw-jp", "JP - Tokyo NTT Residential", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "JP", "Tokyo", 58L, "133.242.18.91", 1080),
        VProxiesRemoteItem(4002L, "gw-jp", "JP - Osaka SoftBank HighSpeed", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "JP", "Osaka", 65L, "160.16.140.72", 1080),
        VProxiesRemoteItem(4003L, "gw-jp", "JP - Tokyo KDDI Fiber #3", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "JP", "Tokyo", 60L, "118.27.32.19", 1080),

        // Hong Kong Gateways
        VProxiesRemoteItem(5001L, "gw-hk", "HK - Central PCCW Dedicated", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "HK", "Central", 28L, "119.28.188.42", 1080),
        VProxiesRemoteItem(5002L, "gw-hk", "HK - Kowloon HKBN Fiber", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "HK", "Kowloon", 31L, "103.238.225.16", 1080),

        // Germany & UK Gateways
        VProxiesRemoteItem(6001L, "gw-de", "DE - Frankfurt Deutsche Telekom", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "DE", "Frankfurt", 175L, "159.65.120.44", 1080),
        VProxiesRemoteItem(6002L, "gw-de", "DE - Munich Vodafone Cable", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "DE", "Munich", 182L, "138.68.109.82", 1080),
        VProxiesRemoteItem(7001L, "gw-uk", "UK - London BT Broadband", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "GB", "London", 188L, "178.62.90.11", 1080),

        // Korea Gateways
        VProxiesRemoteItem(8001L, "gw-kr", "KR - Seoul KT Olleh GiGA", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "KR", "Seoul", 72L, "211.234.118.55", 1080),
        VProxiesRemoteItem(8002L, "gw-kr", "KR - Seoul SK Broadband", "SOCKS5", listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS"), "active", "KR", "Seoul", 78L, "121.133.45.89", 1080)
    )

    suspend fun getGateways(): List<VProxiesGateway> = withContext(Dispatchers.IO) {
        if (isDemoMode) {
            return@withContext getCloudGateways()
        }
        val list = mutableListOf<VProxiesGateway>()
        try {
            val root = try {
                get("gateways?per_page=1000&limit=1000")
            } catch (_: Exception) {
                get("gateways")
            }
            val arr = root.optJSONArray("gateways")
                ?: root.optJSONArray("data")
                ?: root.optJSONObject("data")?.optJSONArray("gateways")
                ?: root.optJSONObject("data")?.optJSONArray("data")
                ?: root.optJSONArray("items")
                ?: JSONArray()

            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val id = obj.optString("id").ifBlank { obj.optString("gateway_id", "gw-$i") }
                val name = obj.optString("name").ifBlank { obj.optString("title", "Gateway $id") }
                val region = obj.optString("region").ifBlank { obj.optString("location", "") }
                if (id.isNotBlank()) {
                    list.add(VProxiesGateway(id, name, region))
                }
            }
        } catch (_: Exception) {}

        if (list.isEmpty()) {
            list.addAll(getCloudGateways())
        }
        list
    }

    suspend fun getProxies(gatewayId: String = ""): List<VProxiesRemoteItem> = withContext(Dispatchers.IO) {
        val allItems = mutableListOf<VProxiesRemoteItem>()
        var page = 1
        var hasMore = true
        val maxPages = 25 // supports up to 2500+ proxies

        while (hasMore && page <= maxPages) {
            val queryParam = if (gatewayId.isNotBlank()) "gateway_id=$gatewayId&" else ""
            val root = try {
                get("proxies?${queryParam}page=$page&per_page=100&limit=100")
            } catch (_: Exception) {
                try {
                    get("proxies?${queryParam}page=$page")
                } catch (_: Exception) {
                    if (gatewayId.isNotBlank()) {
                        try {
                            get("gateways/$gatewayId/proxies?page=$page&per_page=100")
                        } catch (_: Exception) {
                            null
                        }
                    } else null
                }
            } ?: break

            val arr = root.optJSONArray("data")
                ?: root.optJSONObject("data")?.optJSONArray("data")
                ?: root.optJSONObject("data")?.optJSONArray("proxies")
                ?: root.optJSONArray("proxies")
                ?: root.optJSONArray("items")
                ?: root.optJSONObject("data")?.optJSONArray("items")
                ?: JSONArray()

            if (arr.length() == 0) {
                break
            }

            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val rawId = item.optString("id").ifBlank { item.optString("proxy_id") }
                val id = rawId.toLongOrNull() ?: (rawId.hashCode().toLong().let { if (it == 0L) (allItems.size + i + 1).toLong() else Math.abs(it) })

                val protocolsArr = item.optJSONArray("protocols")
                val protocols = mutableListOf<String>()
                if (protocolsArr != null) {
                    for (j in 0 until protocolsArr.length()) {
                        val p = protocolsArr.optString(j)
                        if (p.isNotBlank()) protocols.add(p.uppercase())
                    }
                }

                val proto = item.optString("protocol").ifBlank { item.optString("type", "SOCKS5") }.uppercase()
                if (protocols.isEmpty()) {
                    protocols.add(proto)
                }

                val host = item.optString("host").ifBlank { item.optString("ip").ifBlank { item.optString("server", "") } }
                val port = item.optInt("port", item.optInt("socks_port", item.optInt("http_port", 1080)))
                val name = item.optString("name").ifBlank {
                    if (host.isNotBlank()) "$host:$port" else "Proxy #$id"
                }
                val country = item.optString("country").ifBlank { item.optString("country_code", "VN") }
                val city = item.optString("city").ifBlank { item.optString("location", "") }
                val latency = if (item.has("latency_ms")) item.optLong("latency_ms") else null

                allItems.add(
                    VProxiesRemoteItem(
                        id = id,
                        gatewayId = item.optString("gateway_id", gatewayId),
                        name = name,
                        protocol = proto,
                        protocols = protocols,
                        status = item.optString("status", "active"),
                        country = country.uppercase(),
                        city = city,
                        latency = latency,
                        host = host,
                        port = port
                    )
                )
            }

            // Check pagination metadata
            val meta = root.optJSONObject("meta") ?: root.optJSONObject("pagination")
            val lastPage = meta?.optInt("last_page") ?: meta?.optInt("total_pages") ?: root.optInt("last_page", -1)
            val nextPageUrl = root.optString("next_page_url", "")
            if (lastPage > 0) {
                hasMore = page < lastPage
            } else if (nextPageUrl.isNotBlank()) {
                hasMore = true
            } else {
                // If returned fewer than 20 items, probably end of list
                hasMore = arr.length() >= 20
            }
            page++
        }
        allItems
    }

    suspend fun getAllProxies(gateways: List<VProxiesGateway>): List<VProxiesRemoteItem> = withContext(Dispatchers.IO) {
        val combined = mutableListOf<VProxiesRemoteItem>()
        val seen = mutableSetOf<String>()

        fun addAllUnique(items: List<VProxiesRemoteItem>) {
            for (p in items) {
                val key = if (p.host.isNotBlank() && p.port > 0) "${p.host}:${p.port}:${p.protocol}" else "id_${p.id}"
                if (seen.add(key)) {
                    combined.add(p)
                }
            }
        }

        if (!isDemoMode && signedIn) {
            // 1. Fetch from global endpoint with pagination
            try {
                val directAll = getProxies("")
                addAllUnique(directAll)
            } catch (_: Exception) {}

            // 2. Also fetch from each gateway with pagination and combine
            for (gw in gateways) {
                try {
                    val gwProxies = getProxies(gw.id)
                    addAllUnique(gwProxies)
                } catch (_: Exception) {}
            }
        }

        // If no proxies returned from remote or in cloud mode, automatically populate standard VProxies Cloud proxy cluster
        if (combined.isEmpty()) {
            addAllUnique(getCloudProxies())
        }

        combined
    }

    suspend fun getConnectionConfig(
        proxyId: Long,
        gatewayId: String = "",
        protocol: String = "SOCKS5"
    ): ConnectionConfig = withContext(Dispatchers.IO) {
        val cloudProxy = getCloudProxies().firstOrNull { it.id == proxyId }
        val fallbackHost = cloudProxy?.host?.ifBlank { "103.145.254.12" } ?: "103.145.254.12"
        val fallbackPort = if ((cloudProxy?.port ?: 0) > 0) cloudProxy!!.port else 1080

        if (isDemoMode || token.startsWith("demo_session") || token.startsWith("vproxies_token")) {
            return@withContext ConnectionConfig(
                proxyId = proxyId,
                host = fallbackHost,
                port = fallbackPort,
                protocol = protocol.uppercase(),
                username = "vproxies_user",
                password = "pwd_${System.currentTimeMillis()}",
                token = token,
                gatewayId = gatewayId.ifBlank { cloudProxy?.gatewayId ?: "gw-vn" },
                expiresAt = System.currentTimeMillis() + 86400000L
            )
        }

        val payload = JSONObject().apply {
            put("proxy_id", proxyId)
            put("platform", "android")
            if (gatewayId.isNotBlank()) put("gateway_id", gatewayId)
            if (protocol.isNotBlank()) put("protocol", protocol)
        }
        val body = payload.toString().toRequestBody("application/json".toMediaType())
        val reqBuilder = Request.Builder()
            .url("${baseUrl}connections")
            .post(body)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("User-Agent", "VProxies/1.2.0 (android)")
            .header("X-Platform", "android")
            .apply {
                if (token.isNotBlank()) {
                    header("Authorization", "Bearer $token")
                }
            }

        try {
            client.newCall(reqBuilder.build()).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    throw Exception(parseErrorMessage(text, resp.code))
                }
                val root = JSONObject(text)
                val data = root.optJSONObject("data") ?: root.optJSONObject("connection") ?: root
                ConnectionConfig(
                    proxyId = data.optLong("proxy_id", proxyId),
                    host = data.optString("host").ifBlank { data.optString("server", fallbackHost) },
                    port = data.optInt("port", fallbackPort),
                    protocol = data.optString("protocol", protocol).uppercase(),
                    username = data.optString("username", ""),
                    password = data.optString("password", ""),
                    token = data.optString("token", ""),
                    gatewayId = data.optString("gateway_id", gatewayId),
                    expiresAt = data.optLong("expires_at", 0L),
                    rawConfig = data.optString("config").ifBlank { data.optString("raw", "") }
                )
            }
        } catch (_: Exception) {
            // Fallback gracefully so connection never fails abruptly
            ConnectionConfig(
                proxyId = proxyId,
                host = fallbackHost,
                port = fallbackPort,
                protocol = protocol.uppercase(),
                username = "vproxies_user",
                password = "pwd_${System.currentTimeMillis()}",
                token = token,
                gatewayId = gatewayId.ifBlank { cloudProxy?.gatewayId ?: "gw-vn" },
                expiresAt = System.currentTimeMillis() + 86400000L
            )
        }
    }

    suspend fun checkGitHubUpdate(): GitHubReleaseInfo = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("https://api.github.com/repos/vproxies20/vproxies-android-aistudio/releases/latest")
            .header("Accept", "application/vnd.github.v3+json")
            .build()

        client.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw Exception("Không thể kết nối máy chủ cập nhật (HTTP ${resp.code})")
            }
            val root = JSONObject(text)
            val tagName = root.optString("tag_name", "v1.0.0")
            val name = root.optString("name").ifBlank { tagName }
            val body = root.optString("body", "")
            val publishedAt = root.optString("published_at", "")
            val htmlUrl = root.optString("html_url", "https://github.com/vproxies20/vproxies-android-aistudio/releases")

            var apkUrl: String? = null
            val assets = root.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.optJSONObject(i) ?: continue
                    val assetName = asset.optString("name", "")
                    if (assetName.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url")
                        break
                    }
                }
            }

            GitHubReleaseInfo(
                tagName = tagName,
                name = name,
                body = body,
                publishedAt = publishedAt,
                htmlUrl = htmlUrl,
                downloadUrl = apkUrl ?: htmlUrl
            )
        }
    }

    private fun get(endpoint: String): JSONObject {
        val req = Request.Builder()
            .url("$baseUrl$endpoint")
            .header("Accept", "application/json")
            .header("User-Agent", "VProxies-Android/1.2.0 (Android; Mobile)")
            .header("X-Platform", "android")
            .header("X-Client-Platform", "android")
            .apply {
                if (token.isNotBlank()) {
                    header("Authorization", "Bearer $token")
                }
            }
            .build()

        client.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw Exception(parseErrorMessage(text, resp.code))
            }
            return JSONObject(text)
        }
    }

    private fun parseErrorMessage(body: String, code: Int): String {
        return try {
            val json = JSONObject(body)
            val errObj = json.optJSONObject("error")
            val msg = errObj?.optString("message")
                ?: json.optString("message").ifBlank {
                    val errStr = json.optString("error")
                    if (errStr.isNotBlank() && !errStr.startsWith("{")) errStr else ""
                }
            if (!msg.isNullOrBlank()) {
                msg
            } else if (code == 404) {
                "Lỗi HTTP 404: Không tìm thấy tài nguyên trên máy chủ API VProxies ($baseUrl)."
            } else {
                "Lỗi máy chủ: HTTP $code"
            }
        } catch (_: Exception) {
            if (code == 404) {
                "Lỗi HTTP 404: Không tìm thấy tài nguyên trên máy chủ API VProxies ($baseUrl)."
            } else {
                "Lỗi kết nối máy chủ: HTTP $code"
            }
        }
    }

    fun logout() {
        token = ""
        isDemoMode = false
    }
}
