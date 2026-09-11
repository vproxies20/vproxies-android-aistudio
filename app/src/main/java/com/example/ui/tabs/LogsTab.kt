package com.example.ui.tabs

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UiLog
import com.example.ui.theme.VProxiesCyan
import com.example.ui.theme.VProxiesDanger
import com.example.ui.theme.VProxiesGood
import com.example.ui.theme.VProxiesMuted
import com.example.ui.theme.VProxiesSurface
import com.example.ui.theme.VProxiesSurfaceHigh
import com.example.ui.theme.VProxiesWarning
import com.example.ui.theme.VProxiesWhite
import com.example.viewmodel.VProxiesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogsTab(
    viewModel: VProxiesViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.logs.collectAsState()
    val logFilter by viewModel.logFilter.collectAsState()

    val filteredLogs = remember(logs, logFilter) {
        if (logFilter == "ALL") logs else logs.filter { it.level.equals(logFilter, ignoreCase = true) }
    }

    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("logs_tab")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Connection Logs",
                    color = VProxiesWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredLogs.size} network event entries",
                    color = VProxiesMuted,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = { viewModel.clearLogs() },
                modifier = Modifier.testTag("clear_logs_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear logs",
                    tint = VProxiesCyan
                )
            }
        }

        // Filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp)
        ) {
            val filters = listOf("ALL", "INFO", "SUCCESS", "WARN", "ERROR")
            items(filters) { f ->
                val selected = logFilter == f
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setLogFilter(f) },
                    label = { Text(f, fontSize = 11.sp) },
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

        Spacer(Modifier.height(8.dp))

        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No logs available",
                    color = VProxiesMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    LogItemCard(log = log, timeFormatter = timeFormatter)
                }
            }
        }
    }
}

@Composable
private fun LogItemCard(
    log: UiLog,
    timeFormatter: SimpleDateFormat
) {
    val levelColor = when (log.level) {
        "SUCCESS" -> VProxiesGood
        "WARN" -> VProxiesWarning
        "ERROR" -> VProxiesDanger
        else -> VProxiesCyan
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VProxiesSurface.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Level Badge
            Text(
                text = log.level.take(4),
                color = levelColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(levelColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "[${log.tag}]",
                        color = VProxiesWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = timeFormatter.format(Date(log.timestamp)),
                        color = VProxiesMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = log.message,
                    color = VProxiesWhite.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
