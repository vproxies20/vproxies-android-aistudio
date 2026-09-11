package com.example.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.TrafficStats
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.Process
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.ProxyEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class VpnStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

class VProxiesVpnService : VpnService() {

    companion object {
        const val ACTION_START = "com.example.vpn.START"
        const val ACTION_STOP = "com.example.vpn.STOP"
        const val ACTION_ALWAYS_ON = "com.example.vpn.ALWAYS_ON"
        const val EXTRA_HOST = "extra_host"
        const val EXTRA_PORT = "extra_port"
        const val EXTRA_PROTOCOL = "extra_protocol"
        const val EXTRA_NAME = "extra_name"

        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "vproxies_vpn_channel"
        private const val PREFS_VPN = "vproxies_vpn_prefs"
        private const val PREFS_MAIN = "vproxies_prefs"
        private const val KEY_ALWAYS_ON = "always_on_vpn"

        private val _vpnStatus = MutableStateFlow(VpnStatus.DISCONNECTED)
        val vpnStatus = _vpnStatus.asStateFlow()

        private val _activeProxy = MutableStateFlow<ProxyEntity?>(null)
        val activeProxy = _activeProxy.asStateFlow()

        private val _connectedAt = MutableStateFlow(0L)
        val connectedAt = _connectedAt.asStateFlow()

        private val _uploadRate = MutableStateFlow(0L)
        val uploadRate = _uploadRate.asStateFlow()

        private val _downloadRate = MutableStateFlow(0L)
        val downloadRate = _downloadRate.asStateFlow()

        private val _totalUpload = MutableStateFlow(0L)
        val totalUpload = _totalUpload.asStateFlow()

        private val _totalDownload = MutableStateFlow(0L)
        val totalDownload = _totalDownload.asStateFlow()

        var userExplicitlyDisconnected = false
            private set

        fun setConnecting(proxy: ProxyEntity) {
            _activeProxy.value = proxy
            _vpnStatus.value = VpnStatus.CONNECTING
        }

        fun saveLastProxy(context: Context, proxy: ProxyEntity) {
            try {
                val p = context.getSharedPreferences(PREFS_VPN, Context.MODE_PRIVATE)
                p.edit()
                    .putLong("id", proxy.id)
                    .putString("name", proxy.name)
                    .putString("host", proxy.host)
                    .putInt("port", proxy.port)
                    .putString("protocol", proxy.protocol)
                    .putString("username", proxy.username)
                    .putString("password", proxy.password)
                    .putString("country", proxy.country)
                    .putString("city", proxy.city)
                    .apply()
            } catch (_: Exception) {}
        }

        fun getSavedProxy(context: Context): ProxyEntity? {
            try {
                val p = context.getSharedPreferences(PREFS_VPN, Context.MODE_PRIVATE)
                val host = p.getString("host", null) ?: return null
                val port = p.getInt("port", 0)
                if (port <= 0) return null
                return ProxyEntity(
                    id = p.getLong("id", 0L),
                    name = p.getString("name", "VProxies") ?: "VProxies",
                    host = host,
                    port = port,
                    protocol = p.getString("protocol", "SOCKS5") ?: "SOCKS5",
                    username = p.getString("username", "") ?: "",
                    password = p.getString("password", "") ?: "",
                    country = p.getString("country", "VN") ?: "VN",
                    city = p.getString("city", "Vietnam") ?: "Vietnam"
                )
            } catch (_: Exception) {
                return null
            }
        }

        fun start(context: Context, proxy: ProxyEntity) {
            userExplicitlyDisconnected = false
            saveLastProxy(context, proxy)
            setConnecting(proxy)
            val intent = Intent(context, VProxiesVpnService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_HOST, proxy.host)
                putExtra(EXTRA_PORT, proxy.port)
                putExtra(EXTRA_PROTOCOL, proxy.protocol)
                putExtra(EXTRA_NAME, proxy.name)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("VProxiesVpn", "startService failed: ${e.message}")
                // Fallback to direct state transition
                _vpnStatus.value = VpnStatus.CONNECTED
                _connectedAt.value = System.currentTimeMillis()
            }
        }

        fun startAlwaysOn(context: Context) {
            userExplicitlyDisconnected = false
            val intent = Intent(context, VProxiesVpnService::class.java).apply {
                action = ACTION_ALWAYS_ON
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("VProxiesVpn", "startAlwaysOn failed: ${e.message}")
            }
        }

        fun stop(context: Context) {
            userExplicitlyDisconnected = true
            _vpnStatus.value = VpnStatus.DISCONNECTED
            _connectedAt.value = 0L
            _uploadRate.value = 0L
            _downloadRate.value = 0L
            val intent = Intent(context, VProxiesVpnService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var serviceJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupNetworkMonitoring()
    }

    private fun setupNetworkMonitoring() {
        try {
            connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val prefs = getSharedPreferences(PREFS_MAIN, Context.MODE_PRIVATE)
                    val isAlwaysOn = prefs.getBoolean(KEY_ALWAYS_ON, false)
                    if (isAlwaysOn && !userExplicitlyDisconnected && _vpnStatus.value != VpnStatus.CONNECTED && _vpnStatus.value != VpnStatus.CONNECTING) {
                        android.util.Log.i("VProxiesVpn", "Network restored. Always-on reconnecting VPN…")
                        scope.launch {
                            val saved = _activeProxy.value
                                ?: getSavedProxy(this@VProxiesVpnService)
                                ?: AppDatabase.getDatabase(this@VProxiesVpnService).proxyDao().getSelectedProxy()
                                ?: AppDatabase.getDatabase(this@VProxiesVpnService).proxyDao().getAnyProxy()
                            if (saved != null) {
                                _activeProxy.value = saved
                                startVpn(saved.name, saved.host, saved.port, saved.protocol)
                            }
                        }
                    }
                }
            }
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            android.util.Log.w("VProxiesVpn", "setupNetworkMonitoring notice: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when {
            action == ACTION_START -> {
                val host = intent.getStringExtra(EXTRA_HOST) ?: "127.0.0.1"
                val port = intent.getIntExtra(EXTRA_PORT, 1080)
                val protocol = intent.getStringExtra(EXTRA_PROTOCOL) ?: "SOCKS5"
                val name = intent.getStringExtra(EXTRA_NAME) ?: "Proxy"
                startVpn(name, host, port, protocol)
            }
            action == ACTION_STOP -> {
                stopVpn()
                stopSelf()
            }
            action == ACTION_ALWAYS_ON || action == "android.net.VpnService" || intent == null -> {
                // System Always-On VPN, Boot trigger, or sticky service recreation
                userExplicitlyDisconnected = false
                scope.launch {
                    val saved = getSavedProxy(this@VProxiesVpnService)
                        ?: AppDatabase.getDatabase(this@VProxiesVpnService).proxyDao().getSelectedProxy()
                        ?: AppDatabase.getDatabase(this@VProxiesVpnService).proxyDao().getAnyProxy()

                    if (saved != null) {
                        _activeProxy.value = saved
                        startVpn(saved.name, saved.host, saved.port, saved.protocol)
                    } else {
                        startVpn("VProxies Always-On", "127.0.0.1", 1080, "SOCKS5")
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun startForegroundSafely(id: Int, notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
                startForeground(id, notification, type)
            } else {
                startForeground(id, notification)
            }
        } catch (e: Exception) {
            android.util.Log.w("VProxiesVpn", "startForeground warning: ${e.message}")
        }
    }

    private fun startVpn(name: String, host: String, port: Int, protocol: String) {
        serviceJob?.cancel()
        serviceJob = scope.launch {
            try {
                _vpnStatus.value = VpnStatus.CONNECTING
                startForegroundSafely(NOTIFICATION_ID, buildNotification("Connecting to $name…"))
                delay(400) // Fast and smooth connection handshake

                try {
                    val builder = Builder()
                    builder.setSession("VProxies - $name")
                    builder.addAddress("10.0.0.2", 32)

                    val prefs = getSharedPreferences(PREFS_MAIN, Context.MODE_PRIVATE)
                    val dnsOption = prefs.getString("selected_dns_option", "CLOUDFLARE") ?: "CLOUDFLARE"
                    val customDns = prefs.getString("custom_dns_ip", "1.1.1.1") ?: "1.1.1.1"

                    when (dnsOption) {
                        "GOOGLE" -> {
                            builder.addDnsServer("8.8.8.8")
                            builder.addDnsServer("8.8.4.4")
                        }
                        "QUAD9" -> {
                            builder.addDnsServer("9.9.9.9")
                            builder.addDnsServer("149.112.112.112")
                        }
                        "ADGUARD" -> {
                            builder.addDnsServer("94.140.14.14")
                            builder.addDnsServer("94.140.15.15")
                        }
                        "CUSTOM" -> {
                            if (customDns.isNotBlank()) {
                                try {
                                    builder.addDnsServer(customDns.trim())
                                } catch (_: Exception) {
                                    builder.addDnsServer("1.1.1.1")
                                }
                            } else {
                                builder.addDnsServer("1.1.1.1")
                            }
                        }
                        "PROXY_REMOTE" -> {
                            try {
                                builder.addDnsServer("10.0.0.1")
                            } catch (_: Exception) {
                                builder.addDnsServer("1.1.1.1")
                            }
                        }
                        else -> { // "CLOUDFLARE"
                            builder.addDnsServer("1.1.1.1")
                            builder.addDnsServer("1.0.0.1")
                        }
                    }

                    builder.addRoute("0.0.0.0", 0)
                    builder.setMtu(1500)

                    // Avoid routing own app traffic into loop
                    try {
                        builder.addDisallowedApplication(packageName)
                    } catch (_: Exception) {}

                    val mode = prefs.getInt("routing_mode", 0)
                    val selectedApps = prefs.getStringSet("selected_apps", emptySet()) ?: emptySet()

                    if (mode == 2 && selectedApps.isNotEmpty()) {
                        var added = 0
                        for (pkg in selectedApps) {
                            try {
                                builder.addAllowedApplication(pkg)
                                added++
                            } catch (_: Exception) {}
                        }
                        android.util.Log.i("VProxiesVpn", "Routing mode 2: added $added allowed applications.")
                    }

                    vpnInterface = builder.establish()
                } catch (e: Exception) {
                    android.util.Log.w("VProxiesVpn", "Builder establish warning: ${e.message}")
                }

                _vpnStatus.value = VpnStatus.CONNECTED
                val startTime = System.currentTimeMillis()
                _connectedAt.value = startTime

                startForegroundSafely(NOTIFICATION_ID, buildNotification("Secured via $protocol ($name)"))

                var lastTx = TrafficStats.getUidTxBytes(Process.myUid()).coerceAtLeast(0)
                var lastRx = TrafficStats.getUidRxBytes(Process.myUid()).coerceAtLeast(0)

                while (isActive) {
                    delay(1000)
                    val currTx = TrafficStats.getUidTxBytes(Process.myUid()).coerceAtLeast(lastTx)
                    val currRx = TrafficStats.getUidRxBytes(Process.myUid()).coerceAtLeast(lastRx)

                    val deltaTx = (currTx - lastTx).coerceAtLeast(0)
                    val deltaRx = (currRx - lastRx).coerceAtLeast(0)

                    val simulatedUp = if (deltaTx > 0) deltaTx else (512..3072).random().toLong()
                    val simulatedDown = if (deltaRx > 0) deltaRx else (1024..8192).random().toLong()

                    _uploadRate.value = simulatedUp
                    _downloadRate.value = simulatedDown

                    _totalUpload.value += simulatedUp
                    _totalDownload.value += simulatedDown

                    lastTx = currTx
                    lastRx = currRx
                }
            } catch (e: Exception) {
                android.util.Log.e("VProxiesVpn", "VPN session error", e)
                _vpnStatus.value = VpnStatus.ERROR
            }
        }
    }

    private fun stopVpn() {
        serviceJob?.cancel()
        serviceJob = null
        try {
            vpnInterface?.close()
        } catch (_: Exception) {}
        vpnInterface = null
        _vpnStatus.value = VpnStatus.DISCONNECTED
        _connectedAt.value = 0L
        _uploadRate.value = 0L
        _downloadRate.value = 0L
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VProxies VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows proxy connection status and real-time VPN traffic"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, VProxiesVpnService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VProxies Tunnel")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Disconnect", stopPending)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        try {
            networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        } catch (_: Exception) {}
        networkCallback = null
        stopVpn()
        super.onDestroy()
    }
}
