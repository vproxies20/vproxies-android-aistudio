package com.example.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.NetworkStatusCard
import com.example.ui.components.PowerDial
import com.example.ui.components.QuickSwitchCard
import com.example.ui.components.SpeedTrafficCard
import com.example.ui.theme.VProxiesBlue
import com.example.ui.theme.VProxiesCyan
import com.example.ui.theme.VProxiesDanger
import com.example.ui.theme.VProxiesGood
import com.example.ui.theme.VProxiesMuted
import com.example.ui.theme.VProxiesNavy
import com.example.ui.theme.VProxiesSurface
import com.example.ui.theme.VProxiesSurfaceHigh
import com.example.ui.theme.VProxiesWarning
import com.example.ui.theme.VProxiesWhite
import com.example.viewmodel.VProxiesViewModel
import com.example.vpn.VpnStatus
import kotlinx.coroutines.delay

@Composable
fun DashboardTab(
    viewModel: VProxiesViewModel,
    onRequestVpnPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vpnStatus by viewModel.vpnStatus.collectAsState()
    val connectedAt by viewModel.connectedAt.collectAsState()
    val uploadRate by viewModel.uploadRate.collectAsState()
    val downloadRate by viewModel.downloadRate.collectAsState()
    val totalUpload by viewModel.totalUpload.collectAsState()
    val totalDownload by viewModel.totalDownload.collectAsState()
    val currentProxy by viewModel.selectedProxy.collectAsState()
    val allProxies by viewModel.proxyList.collectAsState()
    val currentIpInfo by viewModel.currentIpInfo.collectAsState()
    val isRefreshingIp by viewModel.isRefreshingIp.collectAsState()
    val isAlwaysOnVpn by viewModel.isAlwaysOnVpn.collectAsState()
    val routingMode by viewModel.routingMode.collectAsState()
    val selectedApps by viewModel.selectedApps.collectAsState()

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Real-time second counter for duration
    LaunchedEffect(vpnStatus) {
        if (vpnStatus == VpnStatus.CONNECTED) {
            while (true) {
                delay(1000)
                currentTime = System.currentTimeMillis()
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Brand Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.5.dp, VProxiesCyan.copy(alpha = 0.8f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.vproxies_logo_icon_1789152091058),
                            contentDescription = "VProxies Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "VProxies",
                                color = VProxiesWhite,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Android",
                                color = VProxiesCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "Network Security & Seamless Proxy Switching",
                            color = VProxiesMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Central Connection Power Dial
        item {
            PowerDial(
                vpnStatus = vpnStatus,
                connectedAt = connectedAt,
                currentTime = currentTime,
                onToggle = { onRequestVpnPermission() },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Distinct Status Banner & Direct Connect/Disconnect Action Button
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            when (vpnStatus) {
                                VpnStatus.CONNECTED -> VProxiesGood.copy(alpha = 0.15f)
                                VpnStatus.CONNECTING -> VProxiesWarning.copy(alpha = 0.15f)
                                VpnStatus.ERROR -> VProxiesDanger.copy(alpha = 0.15f)
                                VpnStatus.DISCONNECTED -> VProxiesSurfaceHigh
                            }
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (vpnStatus) {
                                    VpnStatus.CONNECTED -> VProxiesGood
                                    VpnStatus.CONNECTING -> VProxiesWarning
                                    VpnStatus.ERROR -> VProxiesDanger
                                    VpnStatus.DISCONNECTED -> VProxiesMuted
                                }
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when (vpnStatus) {
                            VpnStatus.CONNECTED -> "CONNECTED TO GATEWAY"
                            VpnStatus.CONNECTING -> "CONNECTING…"
                            VpnStatus.ERROR -> "CONNECTION FAILED — TAP TO RETRY"
                            VpnStatus.DISCONNECTED -> "DISCONNECTED"
                        },
                        color = when (vpnStatus) {
                            VpnStatus.CONNECTED -> VProxiesGood
                            VpnStatus.CONNECTING -> VProxiesWarning
                            VpnStatus.ERROR -> VProxiesDanger
                            VpnStatus.DISCONNECTED -> VProxiesWhite.copy(alpha = 0.7f)
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isAlwaysOnVpn) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(VProxiesCyan.copy(alpha = 0.12f))
                            .clickable { viewModel.setTab(3) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = VProxiesCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "Always-on VPN: Active",
                            color = VProxiesCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (routingMode == 2) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(VProxiesBlue.copy(alpha = 0.18f))
                            .clickable { viewModel.setTab(3) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Apps,
                            contentDescription = null,
                            tint = VProxiesCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "Split Tunnel: ${selectedApps.size} apps",
                            color = VProxiesCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else if (routingMode == 1) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(VProxiesSurface)
                            .clickable { viewModel.setTab(3) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Routing: Web Only (80/443)",
                            color = VProxiesMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Action Button (Alternate tap target for full accessibility)
                Button(
                    onClick = { onRequestVpnPermission() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("action_connect_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (vpnStatus) {
                            VpnStatus.CONNECTED -> VProxiesDanger.copy(alpha = 0.85f)
                            VpnStatus.CONNECTING -> VProxiesWarning
                            VpnStatus.ERROR -> VProxiesBlue
                            VpnStatus.DISCONNECTED -> VProxiesCyan
                        },
                        contentColor = when (vpnStatus) {
                            VpnStatus.CONNECTED -> VProxiesWhite
                            VpnStatus.CONNECTING -> Color(0xFF1E1B4B)
                            VpnStatus.ERROR -> VProxiesWhite
                            VpnStatus.DISCONNECTED -> Color(0xFF0F172A)
                        }
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    when (vpnStatus) {
                        VpnStatus.CONNECTED -> {
                            Icon(Icons.Default.PowerOff, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("DISCONNECT VPN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        VpnStatus.CONNECTING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp,
                                color = Color(0xFF1E1B4B)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("CONNECTING…", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        VpnStatus.ERROR -> {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("RETRY CONNECTION", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        VpnStatus.DISCONNECTED -> {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("CONNECT VPN NOW", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        // Quick Proxy & Network Switcher Card
        item {
            QuickSwitchCard(
                currentProxy = currentProxy,
                allProxies = allProxies,
                onSelectProxy = { proxy ->
                    viewModel.selectProxy(proxy, context)
                },
                onProtocolChange = { newProto ->
                    currentProxy?.let { proxy ->
                        viewModel.setProxyProtocol(proxy, newProto, context)
                    }
                },
                onManageClick = {
                    viewModel.setTab(1) // Go to Proxy Manager tab
                }
            )
        }

        // Real Network & IP Inspection Card
        item {
            NetworkStatusCard(
                ipInfo = currentIpInfo,
                vpnStatus = vpnStatus,
                isRefreshing = isRefreshingIp,
                onRefresh = { viewModel.refreshPublicIp() }
            )
        }

        // Speed & Traffic Metrics
        item {
            SpeedTrafficCard(
                uploadRate = uploadRate,
                downloadRate = downloadRate,
                totalUpload = totalUpload,
                totalDownload = totalDownload
            )
        }

        item {
            Spacer(Modifier.height(8.dp))
        }
    }
}
