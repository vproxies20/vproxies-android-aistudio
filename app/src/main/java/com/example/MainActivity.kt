package com.example

import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.tabs.DashboardTab
import com.example.ui.tabs.LogsTab
import com.example.ui.tabs.ProxyManagerTab
import com.example.ui.tabs.SettingsTab
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VProxiesCyan
import com.example.ui.theme.VProxiesMuted
import com.example.ui.theme.VProxiesSurface
import com.example.ui.theme.VProxiesWhite
import com.example.viewmodel.VProxiesViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: VProxiesViewModel by viewModels()

  private val notifLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { /* Handled */ }

  private val vpnPrepareLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.resultCode == RESULT_OK) {
      Toast.makeText(this, "VPN permission granted, connecting…", Toast.LENGTH_SHORT).show()
      viewModel.toggleVpn(this)
    } else {
      Toast.makeText(this, "VPN permission is required to route traffic through proxy", Toast.LENGTH_LONG).show()
      viewModel.addLog("WARN", "VPN", "User declined VPN permission request.")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
        notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
      }
    }

    setContent {
      MyApplicationTheme {
        VProxiesApp(
          viewModel = viewModel,
          onRequestVpnPermission = {
            val status = viewModel.vpnStatus.value
            if (status == com.example.vpn.VpnStatus.CONNECTED || status == com.example.vpn.VpnStatus.CONNECTING) {
              // Ngắt kết nối ngay lập tức mà không cần hỏi lại quyền
              viewModel.toggleVpn(this)
            } else {
              val intent = VpnService.prepare(this)
              if (intent != null) {
                vpnPrepareLauncher.launch(intent)
              } else {
                viewModel.toggleVpn(this)
              }
            }
          }
        )
      }
    }
  }
}

@Composable
fun VProxiesApp(
  viewModel: VProxiesViewModel,
  onRequestVpnPermission: () -> Unit
) {
  val currentTab by viewModel.currentTab.collectAsState()

  Scaffold(
    modifier = Modifier.fillMaxSize().testTag("vproxies_root_scaffold"),
    containerColor = com.example.ui.theme.VProxiesObsidian,
    contentWindowInsets = WindowInsets.navigationBars,
    bottomBar = {
      NavigationBar(
        containerColor = VProxiesSurface,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("main_navigation_bar")
      ) {
        val items = listOf(
          Triple("Dashboard", Icons.Default.Security, "nav_tab_dashboard"),
          Triple("Proxies", Icons.Default.Dns, "nav_tab_proxies"),
          Triple("Logs", Icons.Default.ReceiptLong, "nav_tab_logs"),
          Triple("Settings", Icons.Default.Settings, "nav_tab_settings")
        )

        items.forEachIndexed { index, item ->
          val selected = currentTab == index
          NavigationBarItem(
            selected = selected,
            onClick = { viewModel.setTab(index) },
            icon = {
              Icon(
                imageVector = item.second,
                contentDescription = item.first,
                modifier = Modifier.size(22.dp)
              )
            },
            label = {
              Text(
                text = item.first,
                fontSize = 11.sp,
                color = if (selected) VProxiesCyan else VProxiesMuted
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = VProxiesCyan,
              unselectedIconColor = VProxiesMuted,
              indicatorColor = VProxiesCyan.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag(item.third)
          )
        }
      }
    }
  ) { innerPadding ->
    Crossfade(
      targetState = currentTab,
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      label = "tab_crossfade"
    ) { tab ->
      when (tab) {
        0 -> DashboardTab(
          viewModel = viewModel,
          onRequestVpnPermission = onRequestVpnPermission
        )
        1 -> ProxyManagerTab(viewModel = viewModel)
        2 -> LogsTab(viewModel = viewModel)
        3 -> SettingsTab(viewModel = viewModel)
      }
    }
  }
}

