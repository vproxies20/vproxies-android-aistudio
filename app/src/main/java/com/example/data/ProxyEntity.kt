package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proxies")
data class ProxyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val protocol: String, // "HTTP", "HTTPS", "SOCKS4", "SOCKS5"
    val host: String,
    val port: Int,
    val username: String = "",
    val password: String = "",
    val country: String = "US",
    val city: String = "",
    val latencyMs: Long? = null,
    val isOnline: Boolean = true,
    val isSelected: Boolean = false,
    val isVProxiesManaged: Boolean = false,
    val gatewayId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
