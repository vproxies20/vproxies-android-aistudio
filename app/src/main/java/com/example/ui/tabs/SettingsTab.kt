package com.example.ui.tabs

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppInfoItem
import com.example.model.UpdateCheckState
import com.example.ui.theme.VProxiesBlue
import com.example.ui.theme.VProxiesCyan
import com.example.ui.theme.VProxiesDanger
import com.example.ui.theme.VProxiesGood
import com.example.ui.theme.VProxiesMuted
import com.example.ui.theme.VProxiesNavy
import com.example.ui.theme.VProxiesSurface
import com.example.ui.theme.VProxiesSurfaceHigh
import com.example.ui.theme.VProxiesWhite
import com.example.viewmodel.VProxiesViewModel

@Composable
fun SettingsTab(
    viewModel: VProxiesViewModel,
    modifier: Modifier = Modifier
) {
    val accountInfo by viewModel.accountInfo.collectAsState()
    val isAccountBusy by viewModel.isAccountBusy.collectAsState()
    val accountError by viewModel.accountError.collectAsState()
    val gateways by viewModel.gateways.collectAsState()
    val routingMode by viewModel.routingMode.collectAsState()
    val dnsThroughProxy by viewModel.dnsThroughProxy.collectAsState()
    val preventDnsLeaks by viewModel.preventDnsLeaks.collectAsState()
    val isAlwaysOnVpn by viewModel.isAlwaysOnVpn.collectAsState()
    val selectedApps by viewModel.selectedApps.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isLoadingApps by viewModel.isLoadingApps.collectAsState()
    val updateCheckState by viewModel.updateCheckState.collectAsState()
    val dnsOption by viewModel.dnsOption.collectAsState()
    val customDnsIp by viewModel.customDnsIp.collectAsState()
    val context = LocalContext.current

    var showAppPickerDialog by remember { mutableStateOf(false) }
    var identityInput by remember { mutableStateOf(viewModel.savedIdentity) }
    var passwordInput by remember { mutableStateOf(viewModel.savedPassword) }
    var rememberAccount by remember { mutableStateOf(viewModel.isRememberAccount) }
    var customDnsInput by remember(customDnsIp) { mutableStateOf(customDnsIp) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_tab"),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings & Configuration",
                color = VProxiesWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // VProxies Account Integration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = VProxiesSurface),
                border = BorderStroke(1.dp, VProxiesCyan.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.2.dp, VProxiesCyan.copy(alpha = 0.8f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.vproxies_logo_icon_1789152091058),
                                contentDescription = "VProxies",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "VProxies",
                            color = VProxiesWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    if (accountInfo == null) {
                        Text(
                            text = "Sign in with your VProxies account to synchronize all proxies.",
                            color = VProxiesMuted,
                            fontSize = 12.sp
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = identityInput,
                            onValueChange = { identityInput = it },
                            label = { Text("Email or Username") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        // Lưu tài khoản Checkbox
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { rememberAccount = !rememberAccount }
                                .padding(vertical = 4.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberAccount,
                                onCheckedChange = { rememberAccount = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = VProxiesCyan,
                                    uncheckedColor = VProxiesMuted,
                                    checkmarkColor = VProxiesNavy
                                ),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Lưu tài khoản",
                                color = VProxiesWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (accountError != null) {
                            Spacer(Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = VProxiesDanger.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, VProxiesDanger.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = accountError.orEmpty(),
                                        color = VProxiesDanger,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.loginVProxies(identityInput, passwordInput, rememberAccount)
                            },
                            enabled = !isAccountBusy && identityInput.isNotBlank() && passwordInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = VProxiesBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isAccountBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = VProxiesWhite,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text("Đăng nhập (Sign In)")
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.syncAllGatewaysAndProxies()
                            },
                            enabled = !isAccountBusy,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, VProxiesCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Sync, null, tint = VProxiesCyan, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Đồng bộ nhanh mạng VProxies (Khách)", color = VProxiesCyan, fontSize = 13.sp)
                        }
                    } else {
                        // Already logged in
                        val acc = accountInfo!!
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(VProxiesSurfaceHigh)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = acc.identity,
                                    color = VProxiesWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Plan: ${acc.packageName} · ${acc.remainingDays} days remaining",
                                    color = VProxiesGood,
                                    fontSize = 12.sp
                                )
                            }
                            TextButton(onClick = { viewModel.logoutAccount() }) {
                                Text("Sign Out", color = VProxiesDanger)
                            }
                        }

                        if (gateways.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "Available Gateways (${gateways.size}):",
                                color = VProxiesMuted,
                                fontSize = 12.sp
                            )
                            gateways.forEach { gw ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(gw.display, color = VProxiesWhite, fontSize = 13.sp)
                                    OutlinedButton(
                                        onClick = { viewModel.syncGatewayProxies(gw.id) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Sync, null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Sync", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Traffic Routing Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("routing_mode_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = VProxiesSurface),
                border = BorderStroke(1.dp, VProxiesCyan.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Router, null, tint = VProxiesCyan, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Network Routing Mode",
                            color = VProxiesWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    val modes = listOf(
                        Triple(
                            "Global (Toàn bộ máy)",
                            "Định tuyến 100% lưu lượng của mọi ứng dụng qua Proxy",
                            0
                        ),
                        Triple(
                            "Web Traffic Only (Port 80 & 443)",
                            "Chỉ chuyển hướng lưu lượng HTTP / HTTPS duyệt web",
                            1
                        ),
                        Triple(
                            "Selected Apps Only (Split Tunneling)",
                            "Chỉ các ứng dụng được tick chọn mới đi qua Proxy",
                            2
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        modes.forEach { (title, subtitle, index) ->
                            val isSelected = routingMode == index
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) VProxiesCyan.copy(alpha = 0.12f) else VProxiesSurfaceHigh)
                                    .border(
                                        1.dp,
                                        if (isSelected) VProxiesCyan.copy(alpha = 0.5f) else VProxiesSurfaceHigh,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.setRoutingMode(index)
                                        if (index == 2) {
                                            viewModel.loadInstalledApps()
                                            showAppPickerDialog = true
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setRoutingMode(index)
                                        if (index == 2) {
                                            viewModel.loadInstalledApps()
                                            showAppPickerDialog = true
                                        }
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = VProxiesCyan),
                                    modifier = Modifier.testTag("routing_mode_radio_$index")
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        color = if (isSelected) VProxiesWhite else VProxiesWhite.copy(alpha = 0.85f),
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = subtitle,
                                        color = VProxiesMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // Extra configuration section for Selected Apps Only
                    if (routingMode == 2) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = VProxiesSurfaceHigh),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, VProxiesCyan.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Apps, null, tint = VProxiesCyan, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "Danh sách ứng dụng qua Proxy",
                                            color = VProxiesWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "${selectedApps.size} app đã chọn",
                                        color = if (selectedApps.isNotEmpty()) VProxiesCyan else VProxiesMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(Modifier.height(6.dp))

                                if (selectedApps.isEmpty()) {
                                    Text(
                                        text = "⚠️ Bạn chưa tick chọn ứng dụng nào. Hãy bấm nút bên dưới để chọn các ứng dụng cần định tuyến qua Proxy.",
                                        color = VProxiesDanger,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                } else {
                                    Text(
                                        text = "Đang áp dụng định tuyến riêng cho ${selectedApps.size} ứng dụng. Các ứng dụng khác vẫn kết nối mạng trực tiếp không qua proxy.",
                                        color = VProxiesMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        viewModel.loadInstalledApps()
                                        showAppPickerDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = VProxiesCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("open_app_picker_button")
                                ) {
                                    Icon(Icons.Default.Apps, null, tint = VProxiesNavy, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (selectedApps.isEmpty()) "Chọn ứng dụng ngay" else "Thay đổi ứng dụng (${selectedApps.size} đã chọn)",
                                        color = VProxiesNavy,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Always-on VPN Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("always_on_vpn_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = VProxiesSurface),
                border = BorderStroke(1.dp, if (isAlwaysOnVpn) VProxiesCyan.copy(alpha = 0.5f) else VProxiesCyan.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isAlwaysOnVpn) VProxiesCyan else VProxiesMuted,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Always-on VPN (VPN Luôn Bật)",
                                color = VProxiesWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isAlwaysOnVpn) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(VProxiesCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = VProxiesCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tự động kết nối lại & Duy trì 24/7", color = VProxiesWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "Tự động kết nối VPN khi khởi động máy (Boot), đổi mạng WiFi/4G hoặc khi bị ngắt kết nối đột ngột.",
                                color = VProxiesMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Switch(
                            checked = isAlwaysOnVpn,
                            onCheckedChange = { viewModel.setAlwaysOnVpn(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VProxiesNavy,
                                checkedTrackColor = VProxiesCyan
                            ),
                            modifier = Modifier.testTag("always_on_switch")
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Android System Always-on settings section
                    Card(
                        colors = CardDefaults.cardColors(containerColor = VProxiesSurfaceHigh),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Settings, null, tint = VProxiesCyan, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Cài đặt Always-on VPN cấp hệ thống Android",
                                    color = VProxiesWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Hệ điều hành Android hỗ trợ tính năng Always-on VPN và Kill-switch ở cấp độ nhân (Kernel):\n" +
                                       "1. Nhấn nút bên dưới để mở trang Cài đặt VPN của thiết bị.\n" +
                                       "2. Nhấn biểu tượng bánh răng ⚙️ bên cạnh ứng dụng VProxies.\n" +
                                       "3. Bật mục 'VPN luôn bật' (Always-on VPN) và tùy chọn 'Chặn kết nối không có VPN' (Kill-switch toàn máy).",
                                color = VProxiesMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.openSystemVpnSettings(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = VProxiesBlue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_system_vpn_settings_button")
                            ) {
                                Icon(Icons.Default.Settings, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Mở Cài đặt VPN của Android", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // DNS & Security Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = VProxiesSurface),
                border = BorderStroke(1.dp, VProxiesCyan.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Dns, null, tint = VProxiesCyan, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Tùy chọn kết nối DNS",
                            color = VProxiesWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Máy chủ DNS phân giải:",
                        color = VProxiesMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(8.dp))

                    val dnsServers = listOf(
                        "GOOGLE" to "Google (8.8.8.8)",
                        "CLOUDFLARE" to "Cloudflare (1.1.1.1)",
                        "OPENDNS" to "OpenDNS",
                        "QUAD9" to "Quad9",
                        "PROXY" to "DNS qua Proxy",
                        "CUSTOM" to "Tùy chỉnh"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            dnsServers.take(3).forEach { (key, label) ->
                                val isSelected = dnsOption == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) VProxiesBlue else VProxiesSurfaceHigh)
                                        .clickable { viewModel.setDnsOption(key) }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) VProxiesWhite else VProxiesMuted,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            dnsServers.drop(3).forEach { (key, label) ->
                                val isSelected = dnsOption == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) VProxiesBlue else VProxiesSurfaceHigh)
                                        .clickable { viewModel.setDnsOption(key) }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) VProxiesWhite else VProxiesMuted,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    if (dnsOption == "CUSTOM") {
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customDnsInput,
                            onValueChange = {
                                customDnsInput = it
                                viewModel.setCustomDnsIp(it)
                            },
                            placeholder = { Text("Ví dụ: 1.1.1.1 hoặc 8.8.4.4", color = VProxiesMuted, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = VProxiesWhite,
                                unfocusedTextColor = VProxiesWhite,
                                focusedBorderColor = VProxiesCyan,
                                unfocusedBorderColor = VProxiesSurfaceHigh
                            )
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Chống rò rỉ DNS (Leak Protection)", color = VProxiesWhite, fontSize = 13.sp)
                            Text("Buộc tất cả truy vấn DNS đi qua đường hầm proxy", color = VProxiesMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = preventDnsLeaks,
                            onCheckedChange = { viewModel.setPreventDnsLeaks(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = VProxiesCyan)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Phân giải DNS qua máy chủ Proxy", color = VProxiesWhite, fontSize = 13.sp)
                            Text("Tên miền được phân giải trực tiếp tại IP đầu cuối của Proxy", color = VProxiesMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = dnsThroughProxy,
                            onCheckedChange = { viewModel.setDnsThroughProxy(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = VProxiesCyan)
                        )
                    }
                }
            }
        }

        // Direct Release Update Check Card (No GitHub links shown in UI)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = VProxiesSurface),
                border = BorderStroke(1.dp, VProxiesCyan.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SystemUpdate, null, tint = VProxiesCyan, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Cập nhật ứng dụng",
                                    color = VProxiesWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Kiểm tra phiên bản mới trực tiếp",
                                    color = VProxiesMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.checkForUpdates() },
                            enabled = updateCheckState !is UpdateCheckState.Checking,
                            colors = ButtonDefaults.buttonColors(containerColor = VProxiesCyan.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            if (updateCheckState is UpdateCheckState.Checking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = VProxiesCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(6.dp))
                            } else {
                                Icon(Icons.Default.Refresh, null, tint = VProxiesCyan, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(
                                text = if (updateCheckState is UpdateCheckState.Checking) "Đang kiểm tra…" else "Kiểm tra ngay",
                                color = VProxiesCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    when (val state = updateCheckState) {
                        is UpdateCheckState.Available -> {
                            Spacer(Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (state.hasNewerVersion) VProxiesGood.copy(alpha = 0.15f) else VProxiesSurfaceHigh
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (state.hasNewerVersion) "Có bản cập nhật mới (${state.release.tagName})" else "Ứng dụng đang ở phiên bản mới nhất (${state.release.tagName})",
                                        color = if (state.hasNewerVersion) VProxiesGood else VProxiesWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (state.hasNewerVersion) {
                                        if (state.release.name.isNotBlank()) {
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = state.release.name,
                                                color = VProxiesMuted,
                                                fontSize = 12.sp
                                            )
                                        }
                                        val targetUrl = state.release.downloadUrl ?: state.release.htmlUrl
                                        Spacer(Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                try {
                                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                                    context.startActivity(browserIntent)
                                                } catch (_: Exception) {}
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Download, null, tint = VProxiesCyan, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = "Tải bản cập nhật trực tiếp",
                                                color = VProxiesCyan,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        is UpdateCheckState.Error -> {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Kiểm tra thất bại: ${state.message}",
                                color = VProxiesDanger,
                                fontSize = 12.sp
                            )
                        }
                        else -> {}
                    }
                }
            }
        }

        // Core & System Info Card (No external repository links)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = VProxiesSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = VProxiesCyan, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "VProxies Client",
                                color = VProxiesWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Phiên bản 1.2.0 (Chính thức)",
                            color = VProxiesMuted,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Phần mềm khách độc quyền dành riêng cho hệ sinh thái máy chủ proxy VProxies. Tự động mã hóa lưu lượng, hỗ trợ đa giao thức và phân giải DNS bảo mật cao.",
                        color = VProxiesMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showAppPickerDialog) {
        AppPickerDialog(
            installedApps = installedApps,
            selectedApps = selectedApps,
            isLoading = isLoadingApps,
            onToggleApp = { viewModel.toggleAppSelection(it) },
            onSelectAllUserApps = { viewModel.selectAllUserApps() },
            onDeselectAll = { viewModel.deselectAllApps() },
            onDismiss = { showAppPickerDialog = false }
        )
    }
}

@Composable
fun AppPickerDialog(
    installedApps: List<AppInfoItem>,
    selectedApps: Set<String>,
    isLoading: Boolean,
    onToggleApp: (String) -> Unit,
    onSelectAllUserApps: () -> Unit,
    onDeselectAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf(0) } // 0: Tất cả, 1: Đã chọn, 2: Ứng dụng người dùng

    val filteredApps = remember(installedApps, searchQuery, filterMode, selectedApps) {
        installedApps.filter { app ->
            val matchesSearch = searchQuery.isBlank() ||
                    app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (filterMode) {
                1 -> selectedApps.contains(app.packageName)
                2 -> !app.isSystemApp
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("app_picker_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = VProxiesSurfaceHigh),
            border = BorderStroke(1.dp, VProxiesCyan.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Apps, contentDescription = null, tint = VProxiesCyan, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Chọn ứng dụng qua Proxy",
                                color = VProxiesWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Split Tunneling: Chỉ app được chọn mới qua Proxy",
                                color = VProxiesMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_app_picker_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = VProxiesMuted)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm tên app hoặc package…", color = VProxiesMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = VProxiesCyan, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, null, tint = VProxiesMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VProxiesCyan,
                        unfocusedBorderColor = VProxiesCyan.copy(alpha = 0.3f),
                        focusedTextColor = VProxiesWhite,
                        unfocusedTextColor = VProxiesWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_app_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(10.dp))

                // Action Bar: Count & Quick Select
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Đã chọn: ${selectedApps.size} / ${installedApps.size}",
                        color = if (selectedApps.isNotEmpty()) VProxiesCyan else VProxiesMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = onSelectAllUserApps,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Chọn app người dùng", color = VProxiesCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = onDeselectAll,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Bỏ chọn hết", color = VProxiesDanger, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // App list
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = VProxiesCyan, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(10.dp))
                            Text("Đang đọc danh sách ứng dụng trên thiết bị…", color = VProxiesMuted, fontSize = 12.sp)
                        }
                    }
                } else if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "Không tìm thấy ứng dụng phù hợp" else "Không có ứng dụng nào",
                            color = VProxiesMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(count = filteredApps.size, key = { filteredApps[it].packageName }) { idx ->
                            val app = filteredApps[idx]
                            val isSelected = selectedApps.contains(app.packageName)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) VProxiesCyan.copy(alpha = 0.12f) else VProxiesSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) VProxiesCyan.copy(alpha = 0.4f) else VProxiesSurface,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onToggleApp(app.packageName) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (app.iconBitmap != null) {
                                        Image(
                                            bitmap = app.iconBitmap.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(VProxiesSurfaceHigh),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = app.appName.take(1).uppercase(),
                                                color = VProxiesCyan,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = app.appName,
                                                color = VProxiesWhite,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                            if (app.isSystemApp) {
                                                Spacer(Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(VProxiesMuted.copy(alpha = 0.2f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(text = "System", color = VProxiesMuted, fontSize = 9.sp)
                                                }
                                            }
                                        }
                                        Text(
                                            text = app.packageName,
                                            color = VProxiesMuted,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { onToggleApp(app.packageName) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = VProxiesCyan,
                                        checkmarkColor = VProxiesNavy,
                                        uncheckedColor = VProxiesMuted.copy(alpha = 0.4f)
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Bottom Done Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = VProxiesCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("app_picker_done_button")
                ) {
                    Text(
                        text = if (selectedApps.isEmpty()) "Đóng (Chưa chọn ứng dụng)" else "Lưu cấu hình (${selectedApps.size} ứng dụng)",
                        color = VProxiesNavy,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

