package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit

object NetworkProbe {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    suspend fun testProxyLatency(host: String, port: Int, timeoutMs: Int = 4000): Long? =
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(host, port), timeoutMs)
                }
                val duration = System.currentTimeMillis() - startTime
                duration.coerceAtLeast(1)
            } catch (e: Exception) {
                null
            }
        }

    suspend fun fetchPublicIpInfo(): IpInfo = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        // Try ip-api.com first (provides ip, country, city, isp)
        try {
            val request = Request.Builder()
                .url("http://ip-api.com/json/?fields=status,message,country,countryCode,city,isp,org,query")
                .header("User-Agent", "VProxiesAndroid/1.0")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val json = JSONObject(body)
                    if (json.optString("status") == "success") {
                        val queryTime = System.currentTimeMillis() - startTime
                        return@withContext IpInfo(
                            ip = json.optString("query", "Unknown"),
                            country = json.optString("country", "Unknown"),
                            countryCode = json.optString("countryCode", ""),
                            city = json.optString("city", ""),
                            isp = json.optString("isp", "Unknown ISP"),
                            org = json.optString("org", ""),
                            queryTimeMs = queryTime
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback to ipify
        }

        // Fallback: api.ipify.org
        try {
            val request = Request.Builder()
                .url("https://api.ipify.org?format=json")
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val json = JSONObject(body)
                    val ip = json.optString("ip", "—")
                    val queryTime = System.currentTimeMillis() - startTime
                    return@withContext IpInfo(
                        ip = ip,
                        country = "Detected",
                        countryCode = "",
                        city = "",
                        isp = "Direct Network",
                        queryTimeMs = queryTime
                    )
                }
            }
        } catch (_: Exception) {
            // Unable to reach
        }

        IpInfo(
            ip = "Not detected",
            country = "Offline",
            isp = "Check connection",
            queryTimeMs = 0
        )
    }
}
