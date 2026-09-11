package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProxyEntity
import com.example.model.FormatUtils
import com.example.ui.theme.VProxiesBlue
import com.example.ui.theme.VProxiesCyan
import com.example.ui.theme.VProxiesGood
import com.example.ui.theme.VProxiesMuted
import com.example.ui.theme.VProxiesSurface
import com.example.ui.theme.VProxiesSurfaceHigh
import com.example.ui.theme.VProxiesWarning
import com.example.ui.theme.VProxiesWhite

@Composable
fun QuickSwitchCard(
    currentProxy: ProxyEntity?,
    allProxies: List<ProxyEntity>,
    onSelectProxy: (ProxyEntity) -> Unit,
    onManageClick: () -> Unit,
    onProtocolChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_switch_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = VProxiesSurface.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, VProxiesCyan.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(6.dp)
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
                        imageVector = Icons.Default.AltRoute,
                        contentDescription = null,
                        tint = VProxiesCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "QUICK NETWORK SWITCH",
                        color = VProxiesCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "Quản lý (${allProxies.size})",
                    color = VProxiesMuted,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onManageClick() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Selector Button
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(VProxiesSurfaceHigh)
                        .border(1.dp, VProxiesCyan.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("proxy_selector_trigger"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Protocol badge
                    val proto = currentProxy?.protocol ?: "SOCKS5"
                    Text(
                        text = proto,
                        color = VProxiesCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(VProxiesCyan.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    )

                    Spacer(Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentProxy?.country?.let { FormatUtils.getCountryFlag(it) } ?: "🌐",
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            val displayLoc = if (currentProxy != null) {
                                FormatUtils.formatLocation(currentProxy.country, currentProxy.city)
                            } else {
                                "No proxy selected"
                            }
                            Text(
                                text = displayLoc,
                                color = VProxiesWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        val subText = if (currentProxy != null) {
                            FormatUtils.getCountryName(currentProxy.country)
                        } else {
                            "Tap to select proxy"
                        }
                        Text(
                            text = subText,
                            color = VProxiesMuted,
                            fontSize = 11.sp
                        )
                    }

                    // Latency indicator
                    if (currentProxy?.latencyMs != null) {
                        val latency = currentProxy.latencyMs
                        val latencyColor = when {
                            latency < 100 -> VProxiesGood
                            latency < 300 -> VProxiesWarning
                            else -> Color(0xFFFF667A)
                        }
                        Text(
                            text = "${latency}ms",
                            color = latencyColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(latencyColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select proxy",
                        tint = VProxiesMuted
                    )
                }

                // Dropdown menu for quick 1-tap switching
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(VProxiesSurfaceHigh)
                        .fillMaxWidth(0.9f)
                ) {
                    if (allProxies.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No proxies available", color = VProxiesMuted) },
                            onClick = { expanded = false }
                        )
                    } else {
                        allProxies.forEach { proxy ->
                            val isCurrent = proxy.id == currentProxy?.id
                            DropdownMenuItem(
                                text = {
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
                                                fontSize = 16.sp
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Column {
                                                val dropLoc = FormatUtils.formatLocation(proxy.country, proxy.city)
                                                Text(
                                                    text = dropLoc,
                                                    color = if (isCurrent) VProxiesCyan else VProxiesWhite,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                val dropSub = FormatUtils.getCountryName(proxy.country)
                                                if (dropSub.isNotBlank()) {
                                                    Text(
                                                        text = dropSub,
                                                        color = VProxiesMuted,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }

                                        if (proxy.latencyMs != null) {
                                            val latency = proxy.latencyMs
                                            val color = when {
                                                latency < 100 -> VProxiesGood
                                                latency < 300 -> VProxiesWarning
                                                else -> Color(0xFFFF667A)
                                            }
                                            Text(
                                                text = "${latency}ms",
                                                color = color,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onSelectProxy(proxy)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Quick protocol switcher for current proxy
            if (currentProxy != null && onProtocolChange != null) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(VProxiesSurfaceHigh)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val availableProtocols = listOf("SOCKS5", "SOCKS4", "HTTP", "HTTPS")
                    availableProtocols.forEach { proto ->
                        val isCurrent = currentProxy.protocol.equals(proto, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) VProxiesBlue else Color.Transparent)
                                .clickable {
                                    if (!isCurrent) {
                                        onProtocolChange(proto)
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = proto,
                                color = if (isCurrent) VProxiesWhite else VProxiesMuted,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
