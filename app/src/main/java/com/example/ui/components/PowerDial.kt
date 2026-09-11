package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FormatUtils
import com.example.ui.theme.VProxiesBlue
import com.example.ui.theme.VProxiesCyan
import com.example.ui.theme.VProxiesDanger
import com.example.ui.theme.VProxiesGood
import com.example.ui.theme.VProxiesNavy
import com.example.ui.theme.VProxiesObsidian
import com.example.ui.theme.VProxiesSurface
import com.example.ui.theme.VProxiesSurfaceHigh
import com.example.ui.theme.VProxiesWarning
import com.example.ui.theme.VProxiesWhite
import com.example.vpn.VpnStatus
import kotlinx.coroutines.launch

@Composable
fun PowerDial(
    vpnStatus: VpnStatus,
    connectedAt: Long,
    currentTime: Long,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val pressScale = remember { Animatable(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseProgress"
    )

    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinAngle"
    )

    // Dynamic glow color matching state
    val dialColor by animateColorAsState(
        targetValue = when (vpnStatus) {
            VpnStatus.CONNECTED -> VProxiesGood
            VpnStatus.CONNECTING -> VProxiesWarning
            VpnStatus.ERROR -> VProxiesDanger
            VpnStatus.DISCONNECTED -> VProxiesCyan
        },
        animationSpec = tween(350),
        label = "dialColor"
    )

    val statusTitle = when (vpnStatus) {
        VpnStatus.CONNECTED -> "CONNECTED"
        VpnStatus.CONNECTING -> "CONNECTING…"
        VpnStatus.ERROR -> "ERROR"
        VpnStatus.DISCONNECTED -> "TAP TO CONNECT"
    }

    val elapsed = if (vpnStatus == VpnStatus.CONNECTED && connectedAt > 0) {
        (currentTime - connectedAt).coerceAtLeast(0)
    } else 0L

    Box(
        modifier = modifier
            .size(246.dp)
            .testTag("vpn_power_dial_container"),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing radar aura (Dual pulse when connected or connecting)
        if (vpnStatus == VpnStatus.CONNECTED || vpnStatus == VpnStatus.CONNECTING) {
            Canvas(modifier = Modifier.size(246.dp)) {
                val radius = (size.minDimension / 2) * (0.80f + (pulseProgress * 0.20f))
                val alpha = (1f - pulseProgress) * 0.45f
                drawCircle(
                    color = dialColor.copy(alpha = alpha),
                    radius = radius,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        } else {
            // Idle breathing ring so user knows it is clickable
            Canvas(modifier = Modifier.size(236.dp)) {
                val radius = (size.minDimension / 2) * (0.88f + (pulseProgress * 0.06f))
                val alpha = (1f - pulseProgress) * 0.20f
                drawCircle(
                    color = VProxiesCyan.copy(alpha = alpha),
                    radius = radius,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Primary clickable power button
        Box(
            modifier = Modifier
                .size(206.dp)
                .scale(pressScale.value)
                .shadow(
                    elevation = if (vpnStatus == VpnStatus.CONNECTED) 24.dp else 16.dp,
                    shape = CircleShape,
                    spotColor = dialColor.copy(alpha = if (vpnStatus == VpnStatus.CONNECTED) 0.8f else 0.5f)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (vpnStatus == VpnStatus.CONNECTED) Color(0xFF064E3B) else VProxiesSurfaceHigh,
                            VProxiesNavy,
                            VProxiesObsidian
                        )
                    )
                )
                .border(
                    BorderStroke(
                        width = if (vpnStatus == VpnStatus.CONNECTED) 3.dp else 2.dp,
                        brush = Brush.sweepGradient(
                            listOf(dialColor, dialColor.copy(alpha = 0.4f), dialColor)
                        )
                    ),
                    CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = dialColor)
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        pressScale.animateTo(0.92f, tween(80))
                        pressScale.animateTo(1f, tween(120))
                    }
                    onToggle()
                }
                .testTag("power_button"),
            contentAlignment = Alignment.Center
        ) {
            // Animated Arc Progress Ring
            Canvas(modifier = Modifier.matchParentSize().padding(12.dp)) {
                // Background track
                drawCircle(
                    color = dialColor.copy(alpha = 0.15f),
                    style = Stroke(4.dp.toPx())
                )

                when (vpnStatus) {
                    VpnStatus.CONNECTED -> {
                        // Full solid glowing circle
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(VProxiesCyan, VProxiesGood, VProxiesCyan)
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    VpnStatus.CONNECTING -> {
                        // Spinning radar arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(VProxiesCyan, VProxiesWarning, Color.Transparent)
                            ),
                            startAngle = spinAngle,
                            sweepAngle = 240f,
                            useCenter = false,
                            style = Stroke(5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    VpnStatus.ERROR -> {
                        drawArc(
                            color = VProxiesDanger,
                            startAngle = -90f,
                            sweepAngle = 180f,
                            useCenter = false,
                            style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    VpnStatus.DISCONNECTED -> {
                        // Idle neon crescent arc
                        drawArc(
                            brush = Brush.linearGradient(
                                listOf(VProxiesCyan, VProxiesBlue)
                            ),
                            startAngle = -90f,
                            sweepAngle = 120f,
                            useCenter = false,
                            style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            // Power Icon & Status Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                val iconScale = if (vpnStatus == VpnStatus.CONNECTING) {
                    0.95f + (pulseProgress * 0.15f)
                } else if (vpnStatus == VpnStatus.CONNECTED) {
                    1.05f
                } else {
                    1f
                }

                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Toggle VPN",
                    tint = dialColor,
                    modifier = Modifier
                        .size(56.dp)
                        .scale(iconScale)
                )

                Spacer(Modifier.height(8.dp))

                AnimatedContent(targetState = statusTitle, label = "dial_label") { title ->
                    Text(
                        text = title,
                        color = dialColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = when (vpnStatus) {
                        VpnStatus.CONNECTED -> "Thời gian: " + FormatUtils.formatDuration(elapsed)
                        VpnStatus.CONNECTING -> "Đang thiết lập VPN…"
                        VpnStatus.ERROR -> "Chạm để thử lại"
                        VpnStatus.DISCONNECTED -> "Chạm để kết nối ngay"
                    },
                    color = VProxiesWhite.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
