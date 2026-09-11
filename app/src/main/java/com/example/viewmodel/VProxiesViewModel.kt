package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ProxyEntity
import com.example.model.AppInfoItem
import com.example.model.FormatUtils
import com.example.model.UiLog
import com.example.model.UpdateCheckState
import com.example.network.IpInfo
import com.example.network.NetworkProbe
import com.example.network.VProxiesAccountInfo
import com.example.network.VProxiesApiClient
import com.example.network.VProxiesGateway
import com.example.vpn.VProxiesVpnService
import com.example.vpn.VpnStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VProxiesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val proxyDao = db.proxyDao()
    val apiClient = VProxiesApiClient()

    private val prefs = application.getSharedPreferences("vproxies_prefs", Context.MODE_PRIVATE)

    private val _clientPlatform = MutableStateFlow(
        prefs.getString("client_platform", "android") ?: "android"
    )
    val clientPlatform: StateFlow<String> = _clientPlatform.asStateFlow()

    fun setClientPlatform(platform: String) {
        val trimmed = platform.trim()
        _clientPlatform.value = trimmed
        prefs.edit().putString("client_platform", trimmed).apply()
        apiClient.setClientPlatform(trimmed)
    }

    // Navigation Tab (0: Dashboard, 1: Proxies, 2: Logs, 3: Settings)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    // Proxy List from Room
    val proxyList: StateFlow<List<ProxyEntity>> = proxyDao.getAllProxies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Proxy
    private val _selectedProxy = MutableStateFlow<ProxyEntity?>(null)
    val selectedProxy: StateFlow<ProxyEntity?> = _selectedProxy.asStateFlow()

    // Filter & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _protocolFilter = MutableStateFlow("ALL")
    val protocolFilter: StateFlow<String> = _protocolFilter.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setProtocolFilter(filter: String) {
        _protocolFilter.value = filter
    }

    // IP Inspection State
    private val _currentIpInfo = MutableStateFlow(IpInfo())
    val currentIpInfo: StateFlow<IpInfo> = _currentIpInfo.asStateFlow()

    private val _isRefreshingIp = MutableStateFlow(false)
    val isRefreshingIp: StateFlow<Boolean> = _isRefreshingIp.asStateFlow()

    // Latency Testing State
    private val _isTestingAll = MutableStateFlow(false)
    val isTestingAll: StateFlow<Boolean> = _isTestingAll.asStateFlow()

    // Routing & DNS Settings
    private val _routingMode = MutableStateFlow(prefs.getInt("routing_mode", 0)) // 0: Full, 1: Web Only, 2: Per App
    val routingMode: StateFlow<Int> = _routingMode.asStateFlow()

    private val _selectedApps = MutableStateFlow<Set<String>>(
        prefs.getStringSet("selected_apps", emptySet()) ?: emptySet()
    )
    val selectedApps: StateFlow<Set<String>> = _selectedApps.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppInfoItem>>(emptyList())
    val installedApps: StateFlow<List<AppInfoItem>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _dnsThroughProxy = MutableStateFlow(false)
    val dnsThroughProxy: StateFlow<Boolean> = _dnsThroughProxy.asStateFlow()

    private val _dnsOption = MutableStateFlow(prefs.getString("selected_dns_option", "CLOUDFLARE") ?: "CLOUDFLARE")
    val dnsOption: StateFlow<String> = _dnsOption.asStateFlow()

    private val _customDnsIp = MutableStateFlow(prefs.getString("custom_dns_ip", "1.1.1.1") ?: "1.1.1.1")
    val customDnsIp: StateFlow<String> = _customDnsIp.asStateFlow()

    fun setDnsOption(option: String) {
        _dnsOption.value = option
        prefs.edit().putString("selected_dns_option", option).apply()
        addLog("INFO", "DNS", "Tùy chọn DNS: $option")
    }

    fun setCustomDnsIp(ip: String) {
        _customDnsIp.value = ip.trim()
        prefs.edit().putString("custom_dns_ip", ip.trim()).apply()
        addLog("INFO", "DNS", "DNS tùy chỉnh: ${ip.trim()}")
    }

    private val _preventDnsLeaks = MutableStateFlow(true)
    val preventDnsLeaks: StateFlow<Boolean> = _preventDnsLeaks.asStateFlow()

    private val _autoReconnect = MutableStateFlow(prefs.getBoolean("always_on_vpn", false))
    val autoReconnect: StateFlow<Boolean> = _autoReconnect.asStateFlow()
    val isAlwaysOnVpn: StateFlow<Boolean> = _autoReconnect

    // GitHub Update Check State
    private val _updateCheckState = MutableStateFlow<UpdateCheckState>(UpdateCheckState.Idle)
    val updateCheckState: StateFlow<UpdateCheckState> = _updateCheckState.asStateFlow()

    // Account & Gateways
    private val _accountInfo = MutableStateFlow<VProxiesAccountInfo?>(null)
    val accountInfo: StateFlow<VProxiesAccountInfo?> = _accountInfo.asStateFlow()

    private val _gateways = MutableStateFlow<List<VProxiesGateway>>(emptyList())
    val gateways: StateFlow<List<VProxiesGateway>> = _gateways.asStateFlow()

    private val _selectedGateway = MutableStateFlow<VProxiesGateway?>(null)
    val selectedGateway: StateFlow<VProxiesGateway?> = _selectedGateway.asStateFlow()

    private val _selectedGatewayFilter = MutableStateFlow("ALL")
    val selectedGatewayFilter: StateFlow<String> = _selectedGatewayFilter.asStateFlow()

    fun setSelectedGatewayFilter(id: String) {
        _selectedGatewayFilter.value = id
    }

    private val _isAccountBusy = MutableStateFlow(false)
    val isAccountBusy: StateFlow<Boolean> = _isAccountBusy.asStateFlow()

    private val _accountError = MutableStateFlow<String?>(null)
    val accountError: StateFlow<String?> = _accountError.asStateFlow()

    // Logs
    private val _logs = MutableStateFlow<List<UiLog>>(emptyList())
    val logs: StateFlow<List<UiLog>> = _logs.asStateFlow()

    private val _logFilter = MutableStateFlow("ALL")
    val logFilter: StateFlow<String> = _logFilter.asStateFlow()

    fun setLogFilter(filter: String) {
        _logFilter.value = filter
    }

    fun addLog(level: String, tag: String, message: String) {
        val entry = UiLog(level = level, tag = tag, message = message)
        val current = _logs.value.toMutableList()
        current.add(0, entry)
        if (current.size > 200) current.removeAt(current.lastIndex)
        _logs.value = current
    }

    fun clearLogs() {
        _logs.value = emptyList()
        addLog("INFO", "System", "Cleared all connection logs.")
    }

    // VPN Service States forwarders
    val vpnStatus: StateFlow<VpnStatus> = VProxiesVpnService.vpnStatus
    val connectedAt: StateFlow<Long> = VProxiesVpnService.connectedAt
    val uploadRate: StateFlow<Long> = VProxiesVpnService.uploadRate
    val downloadRate: StateFlow<Long> = VProxiesVpnService.downloadRate
    val totalUpload: StateFlow<Long> = VProxiesVpnService.totalUpload
    val totalDownload: StateFlow<Long> = VProxiesVpnService.totalDownload

    init {
        apiClient.setClientPlatform(_clientPlatform.value)
        initializeAccountAndProxies()
        refreshPublicIp()
        addLog("INFO", "System", "VProxies Android khởi động thành công.")
    }

    private fun initializeAccountAndProxies() {
        viewModelScope.launch {
            val savedId = savedIdentity
            val savedPass = savedPassword
            if (savedId.isNotBlank() && savedPass.isNotBlank() && isRememberAccount) {
                loginVProxies(savedId, savedPass, true)
            } else {
                val count = proxyDao.getCount()
                if (count == 0) {
                    syncAllGatewaysAndProxies()
                } else {
                    val sel = proxyDao.getSelectedProxy() ?: proxyDao.getAnyProxy()
                    if (sel != null) {
                        _selectedProxy.value = sel
                    }
                }
            }
        }
    }

    fun refreshPublicIp() {
        viewModelScope.launch {
            _isRefreshingIp.value = true
            addLog("INFO", "Network", "Probing current public IP address…")
            val ipInfo = NetworkProbe.fetchPublicIpInfo()
            _currentIpInfo.value = ipInfo
            _isRefreshingIp.value = false
            addLog("SUCCESS", "Network", "Current public IP: ${ipInfo.ip} (${ipInfo.isp}, ${ipInfo.country})")
        }
    }

    fun selectProxy(proxy: ProxyEntity, context: Context? = null) {
        viewModelScope.launch {
            proxyDao.clearSelected()
            proxyDao.setSelected(proxy.id)
            var activeProxy = proxy.copy(isSelected = true)
            _selectedProxy.value = activeProxy
            val loc = FormatUtils.formatLocation(proxy.country, proxy.city)
            addLog("INFO", "Proxy", "Selected location: $loc")

            // If this is a VProxies managed proxy, fetch connection details via POST /connections
            if (proxy.isVProxiesManaged) {
                try {
                    addLog("INFO", "Connection", "Requesting connection credentials…")
                    val conn = apiClient.getConnectionConfig(
                        proxyId = proxy.id,
                        gatewayId = proxy.gatewayId,
                        protocol = proxy.protocol
                    )
                    if (conn.host.isNotBlank() && conn.port > 0) {
                        activeProxy = activeProxy.copy(
                            host = conn.host,
                            port = conn.port,
                            username = conn.username.ifBlank { activeProxy.username },
                            password = conn.password.ifBlank { activeProxy.password }
                        )
                        _selectedProxy.value = activeProxy
                        proxyDao.insertProxy(activeProxy)
                        addLog("SUCCESS", "Connection", "Connection profile ready for $loc")
                    }
                } catch (e: Exception) {
                    addLog("WARN", "Connection", "Could not fetch dynamic connection profile: ${e.message}. Using default parameters.")
                }
            }

            // If VPN is currently connected, easily switch network by reconnecting to the new proxy
            if (vpnStatus.value == VpnStatus.CONNECTED && context != null) {
                addLog("INFO", "Network Switch", "Switching connection to $loc…")
                VProxiesVpnService.start(context, activeProxy)
                refreshPublicIp()
            }
        }
    }

    fun toggleVpn(context: Context) {
        val current = _selectedProxy.value
        if (current == null) {
            addLog("WARN", "VPN", "Vui lòng chọn một máy chủ Proxy trước khi kết nối.")
            return
        }

        if (vpnStatus.value == VpnStatus.CONNECTED || vpnStatus.value == VpnStatus.CONNECTING) {
            addLog("INFO", "VPN", "Disconnecting VPN and restoring direct network…")
            VProxiesVpnService.stop(context)
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    kotlinx.coroutines.delay(800)
                }
                refreshPublicIp()
            }
        } else {
            // Immediate visual feedback on dial
            val currentLoc = FormatUtils.formatLocation(current.country, current.city)
            VProxiesVpnService.setConnecting(current)
            addLog("INFO", "VPN", "Initiating secure tunnel to $currentLoc…")

            viewModelScope.launch {
                var targetProxy = current
                // Call POST /connections for VProxies official nodes to fetch active credentials
                if (targetProxy.isVProxiesManaged) {
                    try {
                        addLog("INFO", "Connection", "Requesting dynamic session config…")
                        val conn = apiClient.getConnectionConfig(
                            proxyId = targetProxy.id,
                            gatewayId = targetProxy.gatewayId,
                            protocol = targetProxy.protocol
                        )
                        if (conn.host.isNotBlank() && conn.port > 0) {
                            targetProxy = targetProxy.copy(
                                host = conn.host,
                                port = conn.port,
                                username = conn.username.ifBlank { targetProxy.username },
                                password = conn.password.ifBlank { targetProxy.password }
                            )
                            _selectedProxy.value = targetProxy
                            proxyDao.insertProxy(targetProxy)
                            addLog("SUCCESS", "Connection", "Secure tunnel connected to $currentLoc")
                        }
                    } catch (e: Exception) {
                        addLog("WARN", "Connection", "Session notice: ${e.message}")
                    }
                }

                VProxiesVpnService.start(context, targetProxy)
                withContext(Dispatchers.IO) {
                    kotlinx.coroutines.delay(1200)
                }
                refreshPublicIp()
            }
        }
    }

    fun testProxy(proxy: ProxyEntity) {
        viewModelScope.launch {
            val testLoc = FormatUtils.formatLocation(proxy.country, proxy.city)
            addLog("INFO", "Latency Test", "Testing latency for $testLoc…")
            val latency = NetworkProbe.testProxyLatency(proxy.host, proxy.port)
            val isOnline = latency != null
            proxyDao.updateLatency(proxy.id, latency, isOnline)
            if (isOnline) {
                addLog("SUCCESS", "Latency Test", "$testLoc: Good response (${latency}ms)")
            } else {
                addLog("ERROR", "Latency Test", "$testLoc: No response or node unreachable.")
            }
        }
    }

    fun testAllProxies() {
        viewModelScope.launch {
            _isTestingAll.value = true
            val list = proxyList.value
            addLog("INFO", "Speed Test", "Testing latency for ${list.size} proxies in parallel…")

            val jobs = list.map { proxy ->
                async(Dispatchers.IO) {
                    val latency = NetworkProbe.testProxyLatency(proxy.host, proxy.port)
                    proxyDao.updateLatency(proxy.id, latency, latency != null)
                }
            }
            jobs.awaitAll()
            _isTestingAll.value = false
            addLog("SUCCESS", "Speed Test", "Completed latency test for all proxies.")
        }
    }

    fun setProxyProtocol(proxy: ProxyEntity, newProtocol: String, context: Context? = null) {
        viewModelScope.launch {
            val proto = newProtocol.uppercase()
            proxyDao.updateProtocol(proxy.id, proto)
            val updated = proxy.copy(protocol = proto)
            if (_selectedProxy.value?.id == proxy.id) {
                _selectedProxy.value = updated
                if (vpnStatus.value == VpnStatus.CONNECTED && context != null) {
                    addLog("INFO", "Protocol", "Chuyển giao thức sang $proto và kết nối lại...")
                    VProxiesVpnService.start(context, updated)
                }
            }
            addLog("SUCCESS", "Protocol", "Đã chuyển đổi giao thức của ${proxy.name} sang $proto")
        }
    }

    fun deleteProxy(proxy: ProxyEntity) {
        viewModelScope.launch {
            proxyDao.deleteProxy(proxy)
            addLog("INFO", "Proxy Manager", "Deleted proxy: ${proxy.name}")
            if (_selectedProxy.value?.id == proxy.id) {
                val firstRemaining = proxyList.value.firstOrNull { it.id != proxy.id }
                if (firstRemaining != null) {
                    selectProxy(firstRemaining)
                } else {
                    _selectedProxy.value = null
                }
            }
        }
    }

    // Account persistence & synchronization
    val savedIdentity: String get() = prefs.getString("saved_identity", "") ?: ""
    val savedPassword: String get() = prefs.getString("saved_password", "") ?: ""
    val isRememberAccount: Boolean get() = prefs.getBoolean("remember_account", true)

    fun saveAccountCredentials(identity: String, pass: String, remember: Boolean) {
        if (remember) {
            prefs.edit()
                .putString("saved_identity", identity)
                .putString("saved_password", pass)
                .putBoolean("remember_account", true)
                .apply()
        } else {
            prefs.edit()
                .remove("saved_identity")
                .remove("saved_password")
                .putBoolean("remember_account", false)
                .apply()
        }
    }

    fun loginVProxies(identity: String, pass: String, rememberAccount: Boolean = true) {
        viewModelScope.launch {
            _isAccountBusy.value = true
            _accountError.value = null
            addLog("INFO", "Account", "Signing in to VProxies with account $identity…")
            try {
                val account = apiClient.login(identity, pass)
                _accountInfo.value = account
                saveAccountCredentials(identity, pass, rememberAccount)
                addLog("SUCCESS", "Account", "Login successful! Plan: ${account.packageName}, Remaining: ${account.remainingDays} days.")

                syncAllGatewaysAndProxies()
            } catch (e: Exception) {
                _accountError.value = e.message ?: "Authentication failed."
                addLog("ERROR", "Account", "Login failed: ${e.message}")
            } finally {
                _isAccountBusy.value = false
            }
        }
    }

    private val _showLoginPrompt = MutableStateFlow(false)
    val showLoginPrompt: StateFlow<Boolean> = _showLoginPrompt.asStateFlow()

    fun dismissLoginPrompt() {
        _showLoginPrompt.value = false
    }

    fun syncAllGatewaysAndProxies() {
        viewModelScope.launch {
            _isAccountBusy.value = true
            addLog("INFO", "Sync", "Đang đồng bộ danh sách proxy từ hệ thống VProxies…")
            try {
                // Auto-login with saved credentials if not yet authenticated
                if (!apiClient.signedIn && savedIdentity.isNotBlank() && savedPassword.isNotBlank()) {
                    addLog("INFO", "Sync", "Đang tự động đăng nhập bằng tài khoản đã lưu…")
                    try {
                        val acc = apiClient.login(savedIdentity, savedPassword)
                        _accountInfo.value = acc
                    } catch (e: Exception) {
                        addLog("WARN", "Sync", "Tự động đăng nhập không thành công: ${e.message}")
                    }
                }

                if (!apiClient.signedIn) {
                    addLog("INFO", "Sync", "Khởi tạo kết nối mạng máy chủ VProxies…")
                    val demoAcc = apiClient.loginDemo("Khách VProxies")
                    _accountInfo.value = demoAcc
                }

                val gws = try {
                    apiClient.getGateways()
                } catch (_: Exception) {
                    apiClient.getCloudGateways()
                }
                _gateways.value = gws
                if (gws.isNotEmpty() && _selectedGateway.value == null) {
                    _selectedGateway.value = gws.first()
                }

                val remoteList = apiClient.getAllProxies(gws)
                val entities = remoteList.map { r ->
                    val cleanLoc = FormatUtils.formatLocation(r.country, r.city)
                    val cleanName = if (r.name.isNotBlank() && !r.name.contains(":") && !r.name.contains("gateway", ignoreCase = true) && !r.name.contains(".app", ignoreCase = true) && !r.name.startsWith("Proxy", ignoreCase = true)) {
                        r.name
                    } else {
                        cleanLoc
                    }
                    ProxyEntity(
                        id = r.id,
                        name = cleanName,
                        protocol = r.protocol,
                        host = r.host.ifBlank { "gateway.vproxies.app" },
                        port = if (r.port > 0) r.port else 1080,
                        country = r.country,
                        city = r.city,
                        latencyMs = r.latency,
                        isVProxiesManaged = true,
                        gatewayId = r.gatewayId
                    )
                }

                if (entities.isNotEmpty()) {
                    proxyDao.deleteManagedProxies()
                    proxyDao.insertAll(entities)
                    if (_selectedProxy.value == null || !entities.any { it.id == _selectedProxy.value?.id }) {
                        _selectedProxy.value = entities.first()
                    }
                    addLog("SUCCESS", "Sync", "Đồng bộ thành công ${entities.size} proxy từ hệ thống VProxies.")
                } else {
                    addLog("WARN", "Sync", "Không có proxy nào khả dụng. Vui lòng kiểm tra lại kết nối mạng.")
                }
            } catch (e: Exception) {
                addLog("ERROR", "Sync", "Đồng bộ thất bại: ${e.message}")
            } finally {
                _isAccountBusy.value = false
            }
        }
    }

    fun importProxiesFromText(rawText: String) {
        viewModelScope.launch {
            if (rawText.isBlank()) return@launch
            val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
            val newProxies = mutableListOf<ProxyEntity>()
            for (line in lines) {
                try {
                    var cleaned = line
                    var protocol = "SOCKS5"
                    if (cleaned.contains("://")) {
                        val parts = cleaned.split("://", limit = 2)
                        protocol = parts[0].uppercase()
                        cleaned = parts[1]
                    }
                    var user = ""
                    var pass = ""
                    if (cleaned.contains("@")) {
                        val authParts = cleaned.split("@", limit = 2)
                        val creds = authParts[0].split(":", limit = 2)
                        user = creds.getOrElse(0) { "" }
                        pass = creds.getOrElse(1) { "" }
                        cleaned = authParts[1]
                    }
                    val tokens = cleaned.split(":")
                    if (tokens.size >= 2) {
                        val host = tokens[0].trim()
                        val port = tokens[1].trim().toIntOrNull() ?: continue
                        if (tokens.size >= 4 && user.isBlank()) {
                            user = tokens[2].trim()
                            pass = tokens[3].trim()
                        }
                        newProxies.add(
                            ProxyEntity(
                                name = "$host:$port",
                                protocol = if (protocol in listOf("HTTP", "HTTPS", "SOCKS4", "SOCKS5")) protocol else "SOCKS5",
                                host = host,
                                port = port,
                                username = user,
                                password = pass,
                                country = "VN",
                                city = "Residential",
                                latencyMs = 25L,
                                isVProxiesManaged = false
                            )
                        )
                    }
                } catch (_: Exception) {}
            }
            if (newProxies.isNotEmpty()) {
                proxyDao.insertAll(newProxies)
                if (_selectedProxy.value == null) {
                    _selectedProxy.value = newProxies.first()
                }
                addLog("SUCCESS", "Proxy", "Đã thêm thành công ${newProxies.size} proxy.")
            } else {
                addLog("WARN", "Proxy", "Không tìm thấy proxy hợp lệ (host:port hoặc host:port:user:pass).")
            }
        }
    }

    fun syncGatewayProxies(gatewayId: String) {
        viewModelScope.launch {
            _isAccountBusy.value = true
            addLog("INFO", "Sync", "Fetching proxies from server…")
            try {
                val remoteList = apiClient.getProxies(gatewayId)
                val entities = remoteList.map { r ->
                    val cleanLoc = FormatUtils.formatLocation(r.country, r.city)
                    val cleanName = if (r.name.isNotBlank() && !r.name.contains(":") && !r.name.contains("gateway", ignoreCase = true) && !r.name.contains(".app", ignoreCase = true) && !r.name.startsWith("Proxy", ignoreCase = true)) {
                        r.name
                    } else {
                        cleanLoc
                    }
                    ProxyEntity(
                        name = cleanName,
                        protocol = r.protocol,
                        host = r.host.ifBlank { "gateway.vproxies.app" },
                        port = if (r.port > 0) r.port else 1080,
                        country = r.country,
                        city = r.city,
                        latencyMs = r.latency,
                        isVProxiesManaged = true,
                        gatewayId = gatewayId
                    )
                }
                if (entities.isNotEmpty()) {
                    proxyDao.insertAll(entities)
                    addLog("SUCCESS", "Sync", "Updated ${entities.size} proxies from VProxies.")
                }
            } catch (e: Exception) {
                addLog("ERROR", "Sync", "Could not fetch proxies: ${e.message}")
            } finally {
                _isAccountBusy.value = false
            }
        }
    }

    fun logoutAccount(context: Context? = null) {
        viewModelScope.launch {
            apiClient.logout()
            _accountInfo.value = null
            _gateways.value = emptyList()
            proxyDao.deleteAll()
            _selectedProxy.value = null
            if (context != null && vpnStatus.value != VpnStatus.DISCONNECTED) {
                VProxiesVpnService.stop(context)
            }
            saveAccountCredentials("", "", false)
            addLog("INFO", "Account", "Đã đăng xuất tài khoản VProxies. Danh sách proxy đã được xóa trắng hoàn toàn.")
        }
    }

    fun setRoutingMode(mode: Int) {
        _routingMode.value = mode
        prefs.edit().putInt("routing_mode", mode).apply()
        val desc = when (mode) {
            0 -> "Global (All Traffic)"
            1 -> "Web Only (TCP 80/443)"
            else -> "Selected Apps Only (${_selectedApps.value.size} apps)"
        }
        addLog("INFO", "Routing", "Chế độ mạng: $desc")
        if (mode == 2 && _installedApps.value.isEmpty()) {
            loadInstalledApps()
        }
    }

    fun loadInstalledApps() {
        if (_isLoadingApps.value) return
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingApps.value = true
            try {
                val app = getApplication<Application>()
                val pm = app.packageManager
                val myPkg = app.packageName

                val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                val result = ArrayList<AppInfoItem>()

                for (info in apps) {
                    if (info.packageName == myPkg) continue
                    val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val name = try {
                        pm.getApplicationLabel(info).toString()
                    } catch (_: Exception) {
                        info.packageName
                    }
                    val bmp = try {
                        val drawable = pm.getApplicationIcon(info)
                        drawable.toBitmap(width = 80, height = 80)
                    } catch (_: Exception) {
                        null
                    }
                    result.add(
                        AppInfoItem(
                            packageName = info.packageName,
                            appName = if (name.isNotBlank()) name else info.packageName,
                            isSystemApp = isSystem,
                            iconBitmap = bmp
                        )
                    )
                }
                val selectedSet = _selectedApps.value
                result.sortWith(
                    compareByDescending<AppInfoItem> { selectedSet.contains(it.packageName) }
                        .thenBy { it.isSystemApp }
                        .thenBy { it.appName.lowercase() }
                )
                _installedApps.value = result
            } catch (e: Exception) {
                addLog("ERROR", "AppPicker", "Lỗi tải danh sách app: ${e.message}")
            } finally {
                _isLoadingApps.value = false
            }
        }
    }

    fun toggleAppSelection(packageName: String) {
        val current = _selectedApps.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        _selectedApps.value = current
        prefs.edit().putStringSet("selected_apps", current).apply()
    }

    fun selectAllUserApps() {
        val userPkgs = _installedApps.value.filter { !it.isSystemApp }.map { it.packageName }
        val current = _selectedApps.value.toMutableSet()
        current.addAll(userPkgs)
        _selectedApps.value = current
        prefs.edit().putStringSet("selected_apps", current).apply()
        addLog("INFO", "Routing", "Đã chọn toàn bộ ứng dụng người dùng (${current.size} apps)")
    }

    fun deselectAllApps() {
        _selectedApps.value = emptySet()
        prefs.edit().putStringSet("selected_apps", emptySet()).apply()
        addLog("INFO", "Routing", "Đã bỏ chọn tất cả ứng dụng")
    }

    fun setDnsThroughProxy(enabled: Boolean) {
        _dnsThroughProxy.value = enabled
        addLog("INFO", "DNS", "DNS via proxy: ${if (enabled) "ON" else "OFF"}")
    }

    fun setPreventDnsLeaks(enabled: Boolean) {
        _preventDnsLeaks.value = enabled
        addLog("INFO", "DNS", "DNS leak protection: ${if (enabled) "ON" else "OFF"}")
    }

    fun setAutoReconnect(enabled: Boolean) {
        setAlwaysOnVpn(enabled)
    }

    fun setAlwaysOnVpn(enabled: Boolean) {
        _autoReconnect.value = enabled
        prefs.edit().putBoolean("always_on_vpn", enabled).apply()
        if (enabled) {
            addLog("SUCCESS", "Always-on VPN", "Đã bật Always-on VPN. Kết nối sẽ tự động duy trì 24/7 và khôi phục khi có mạng.")
        } else {
            addLog("INFO", "Always-on VPN", "Đã tắt Always-on VPN.")
        }
    }

    fun openSystemVpnSettings(context: Context) {
        try {
            val intent = Intent("android.net.vpn.SETTINGS").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            addLog("INFO", "System Settings", "Đã mở Cài đặt Always-on VPN hệ thống.")
        } catch (_: Exception) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_VPN_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                addLog("INFO", "System Settings", "Đã mở Cài đặt Always-on VPN hệ thống.")
            } catch (_: Exception) {
                try {
                    context.startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                } catch (_: Exception) {
                    addLog("WARN", "System Settings", "Vui lòng mở Cài đặt thiết bị -> Mạng & Internet -> VPN.")
                }
            }
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _updateCheckState.value = UpdateCheckState.Checking
            addLog("INFO", "Update", "Checking for application updates from server…")
            try {
                val release = apiClient.checkGitHubUpdate()
                val currentVersion = "1.2.0"
                val releaseTag = release.tagName.removePrefix("v").trim()
                val hasNewer = releaseTag.isNotBlank() && releaseTag != currentVersion
                _updateCheckState.value = UpdateCheckState.Available(release, hasNewer)
                if (hasNewer) {
                    addLog("SUCCESS", "Update", "New update available: ${release.tagName}")
                } else {
                    addLog("INFO", "Update", "Application is up to date (${release.tagName}).")
                }
            } catch (e: Exception) {
                _updateCheckState.value = UpdateCheckState.Error(e.message ?: "Failed to check update")
                addLog("WARN", "Update", "Update check failed: ${e.message}")
            }
        }
    }
}
