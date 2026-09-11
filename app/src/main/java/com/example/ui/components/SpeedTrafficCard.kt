package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FormatUtils
import com.example.ui.theme.VProxiesCyan
import com.example.ui.theme.VProxiesMuted
import com.example.ui.theme.VProxiesSurface
import com.example.ui.theme.VProxiesViolet
import com.example.ui.theme.VProxiesWhite

@Composable
fun SpeedTrafficCard(
    uploadRate: Long,
    downloadRate: Long,
    totalUpload: Long,
    totalDownload: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("speed_traffic_row"),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Download Card
        SingleTrafficCard(
            title = "DOWNLOAD",
            rate = downloadRate,
            total = totalDownload,
            color = VProxiesCyan,
            wavePoints = listOf(0.2f, 0.45f, 0.3f, 0.75f, 0.5f, 0.85f, 0.65f, 0.9f),
            modifier = Modifier.weight(1f)
        )

        // Upload Card
        SingleTrafficCard(
            title = "UPLOAD",
            rate = uploadRate,
            total = totalUpload,
            color = VProxiesViolet,
            wavePoints = listOf(0.3f, 0.2f, 0.55f, 0.35f, 0.6f, 0.4f, 0.7f, 0.5f),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SingleTrafficCard(
    title: String,
    rate: Long,
    total: Long,
    color: Color,
    wavePoints: List<Float>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = VProxiesSurface.copy(alpha = 0.9f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                color = VProxiesMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${FormatUtils.formatBytes(rate)}/s",
                color = VProxiesWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Total: ${FormatUtils.formatBytes(total)}",
                color = VProxiesMuted,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(10.dp))
            // Dynamic sparkline
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
            ) {
                if (wavePoints.size > 1) {
                    val step = size.width / (wavePoints.size - 1)
                    val activeScale = if (rate > 0) 1f else 0.2f
                    for (i in 0 until wavePoints.lastIndex) {
                        val y1 = size.height * (1f - (wavePoints[i] * activeScale))
                        val y2 = size.height * (1f - (wavePoints[i + 1] * activeScale))
                        drawLine(
                            color = color.copy(alpha = 0.85f),
                            start = Offset(step * i, y1),
                            end = Offset(step * (i + 1), y2),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}
