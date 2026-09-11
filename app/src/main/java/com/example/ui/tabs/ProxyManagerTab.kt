package com.example.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ProxyEntity
import com.example.model.FormatUtils
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

@Composable
fun ProxyManagerTab(
    viewModel: VProxiesViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allProxies by viewModel.proxyList.collectAsState()
    val selectedProxy by viewModel.selectedProxy.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val protocolFilter by viewModel.protocolFilter.collectAsState()
    val isTestingAll by viewModel.isTestingAll.collectAsState()
    val isAccountBusy by viewModel.isAccountBusy.collectAsState()
    val gateways by viewModel.gateways.collectAsState()
    val selectedGatewayFilter by viewModel.selectedGatewayFilter.collectAsState()
    val accountInfo by viewModel.accountInfo.collectAsState()
    val showLoginPrompt by viewModel.showLoginPrompt.collectAsState()

    val filteredProxies = remember(allProxies, searchQuery, protocolFilter, selectedGatewayFilter) {
        allProxies.filter { proxy ->
            val matchQuery = searchQuery.isBlank() ||
                    proxy.name.contains(searchQuery, ignoreCase = true) ||
                    proxy.country.contains(searchQuery, ignoreCase = true) ||
                    proxy.city.contains(searchQuery, ignoreCase = true) ||
                    FormatUtils.getCountryName(proxy.country).contains(searchQuery, ignoreCase = true)

            val matchProtocol = protocolFilter == "ALL" ||
                    proxy.protocol.equals(protocolFilter, ignoreCase = true)

            val matchGateway = selectedGatewayFilter == "ALL" ||
                    proxy.gatewayId.equals(selectedGatewayFilter, ignoreCase = true)

            matchQuery && matchProtocol && matchGateway
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("proxy_manager_tab")
    ) {
        // Top Header: Logo + VProxies + Sync + Ping All
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_vproxies_logo),
                        contentDescription = "VProxies Logo",
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "VProxies",
                        color = VProxiesWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sync Button
                    Button(
                        onClick = { viewModel.syncAllGatewaysAndProxies() },
                        enabled = !isAccountBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = VProxiesBlue),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("sync_proxies_button")
                    ) {
                        if (isAccountBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(15.dp),
                                color = VProxiesWhite,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Sync, null, modifier = Modifier.size(15.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(if (isAccountBusy) "Đang tải…" else "Đồng bộ", fontSize = 11.sp)
                    }

                    // Ping All Button
                    OutlinedButton(
                        onClick = { viewModel.testAllProxies() },
                        enabled = !isTestingAll && allProxies.isNotEmpty(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VProxiesCyan),
                        border = BorderStroke(1.dp, VProxiesCyan.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("test_all_proxies_button")
                    ) {
                        if (isTestingAll) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(15.dp),
                                color = VProxiesCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Speed, null, modifier = Modifier.size(15.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(if (isTestingAll) "Đang kiểm tra…" else "Ping All", fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by country, city, location…", color = VProxiesMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = VProxiesMuted) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Refresh, "Clear", tint = VProxiesMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VProxiesCyan,
                    unfocusedBorderColor = VProxiesSurfaceHigh,
                    focusedContainerColor = VProxiesSurface,
                    unfocusedContainerColor = VProxiesSurface,
                    focusedTextColor = VProxiesWhite,
                    unfocusedTextColor = VProxiesWhite
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("search_proxy_input")
            )

            // Account & Proxy Status Banner
            Spacer(Modifier.height(8.dp))
            if (accountInfo == null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = VProxiesNavy.copy(alpha = 0.7f)),
                    border = BorderStroke(1.dp, VProxiesCyan.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mạng độc quyền VProxies",
                                color = VProxiesWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Vui lòng đăng nhập tài khoản để đồng bộ danh sách proxy",
                                color = VProxiesMuted,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.setTab(3) },
                            colors = ButtonDefaults.buttonColors(containerColor = VProxiesBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Đăng nhập", fontSize = 11.sp)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(VProxiesSurfaceHigh.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tài khoản: ${accountInfo?.identity}",
                        color = VProxiesWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${allProxies.size} proxy khả dụng",
                        color = VProxiesCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Gateways Filter Chips
            if (gateways.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item {
                        val isAll = selectedGatewayFilter == "ALL"
                        FilterChip(
                            selected = isAll,
                            onClick = { viewModel.setSelectedGatewayFilter("ALL") },
                            label = { Text("All Gateways (${allProxies.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VProxiesBlue.copy(alpha = 0.3f),
                                selectedLabelColor = VProxiesCyan,
                                containerColor = VProxiesSurfaceHigh,
                                labelColor = VProxiesMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isAll,
                                borderColor = if (isAll) VProxiesCyan else Color.Transparent
                            )
                        )
                    }
                    items(gateways) { gw ->
                        val isSelected = selectedGatewayFilter == gw.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedGatewayFilter(gw.id) },
                            label = { Text(gw.cleanName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VProxiesBlue.copy(alpha = 0.3f),
                                selectedLabelColor = VProxiesCyan,
                                containerColor = VProxiesSurfaceHigh,
                                labelColor = VProxiesMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) VProxiesCyan else Color.Transparent
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Protocol Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                val protocols = listOf("ALL", "SOCKS5", "HTTP", "HTTPS", "SOCKS4")
                items(protocols) { proto ->
                    val selected = protocolFilter == proto
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.setProtocolFilter(proto) },
                        label = { Text(if (proto == "ALL") "All Protocols" else proto, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VProxiesCyan.copy(alpha = 0.2f),
                            selectedLabelColor = VProxiesCyan,
                            containerColor = VProxiesSurfaceHigh,
                            labelColor = VProxiesMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = if (selected) VProxiesCyan else Color.Transparent
                        )
                    )
                }
            }
        }

        // Proxy List
        if (filteredProxies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = VProxiesCyan,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Chưa có proxy nào trong danh sách",
                        color = VProxiesWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Nhấn 'Đồng bộ danh sách' để tải toàn bộ các cụm máy chủ proxy độc quyền từ mạng lưới VProxies (Việt Nam, Singapore, Mỹ, Nhật Bản, Châu Âu).",
                        color = VProxiesMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Spacer(Modifier.height(18.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.syncAllGatewaysAndProxies() },
                            colors = ButtonDefaults.buttonColors(containerColor = VProxiesBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("empty_sync_button")
                        ) {
                            Icon(Icons.Default.Sync, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Đồng bộ danh sách")
                        }

                        if (accountInfo == null) {
                            OutlinedButton(
                                onClick = { viewModel.setTab(3) },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, VProxiesCyan.copy(alpha = 0.5f))
                            ) {
                                Text("Đăng nhập tài khoản", color = VProxiesCyan, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 24.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProxies, key = { it.id }) { proxy ->
                    val isSelected = proxy.id == selectedProxy?.id
                    ProxyCardItem(
                        proxy = proxy,
                        isSelected = isSelected,
                        onSelect = { viewModel.selectProxy(proxy, context) },
                        onTest = { viewModel.testProxy(proxy) },
                        onDelete = { viewModel.deleteProxy(proxy) },
                        onProtocolChange = { newProto ->
                            viewModel.setProxyProtocol(proxy, newProto, context)
                        }
                    )
                }
            }
        }
    }

    // Login Prompt Dialog
    if (showLoginPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLoginPrompt() },
            icon = { Icon(Icons.Default.Info, null, tint = VProxiesCyan, modifier = Modifier.size(32.dp)) },
            title = {
                Text("Đồng bộ máy chủ VProxies", color = VProxiesWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Bạn có thể đồng bộ ngay danh sách máy chủ proxy mạng VProxies hoặc đăng nhập tài khoản cá nhân tại tab Cài đặt để đồng bộ proxy riêng của bạn.",
                    color = VProxiesMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissLoginPrompt()
                        viewModel.syncAllGatewaysAndProxies()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VProxiesBlue)
                ) {
                    Text("Đồng bộ ngay")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.dismissLoginPrompt()
                        viewModel.setTab(3)
                    }
                ) {
                    Text("Đến Đăng nhập", color = VProxiesCyan)
                }
            },
            containerColor = VProxiesSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun ProxyCardItem(
    proxy: ProxyEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onTest: () -> Unit,
    onDelete: () -> Unit,
    onProtocolChange: (String) -> Unit
) {
    val borderColor = if (isSelected) VProxiesCyan else VProxiesCyan.copy(alpha = 0.15f)
    val locationText = FormatUtils.formatLocation(proxy.country, proxy.city)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("proxy_item_${proxy.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) VProxiesNavy.copy(alpha = 0.85f) else VProxiesSurface
        ),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = FormatUtils.getCountryFlag(proxy.country),
                        fontSize = 24.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = locationText,
                            color = VProxiesWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = proxy.protocol,
                                color = VProxiesCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(VProxiesCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            val countryName = FormatUtils.getCountryName(proxy.country)
                            if (countryName.isNotBlank()) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = countryName,
                                    color = VProxiesMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Latency Badge (Ping)
                val (badgeColor, textLatency) = when {
                    proxy.latencyMs == null -> VProxiesMuted to "–"
                    proxy.latencyMs < 100 -> VProxiesGood to "${proxy.latencyMs} ms"
                    proxy.latencyMs < 250 -> VProxiesWarning to "${proxy.latencyMs} ms"
                    else -> VProxiesDanger to "${proxy.latencyMs} ms"
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = textLatency,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Multi-protocol selector for this IP
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(VProxiesSurfaceHigh.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Giao thức IP:",
                        color = VProxiesMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${proxy.host}:${proxy.port}",
                        color = VProxiesCyan.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val availableProtocols = listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS")
                    availableProtocols.forEach { proto ->
                        val isCurrentProto = proxy.protocol.equals(proto, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isCurrentProto) VProxiesBlue else VProxiesNavy.copy(alpha = 0.5f)
                                )
                                .clickable {
                                    if (!isCurrentProto) {
                                        onProtocolChange(proto)
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = proto,
                                color = if (isCurrentProto) VProxiesWhite else VProxiesMuted,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrentProto) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Ping Button
                OutlinedButton(
                    onClick = onTest,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, VProxiesCyan.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, tint = VProxiesCyan, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ping", color = VProxiesCyan, fontSize = 11.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Select / Connect Button
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) VProxiesGood else VProxiesBlue
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.Check else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = VProxiesWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isSelected) "Đang chọn" else "Chọn IP",
                            color = VProxiesWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = VProxiesMuted.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
