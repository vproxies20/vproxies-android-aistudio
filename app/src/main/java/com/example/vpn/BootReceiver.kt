package com.example.vpn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.i("BootReceiver", "Received broadcast action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON" ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val prefs = context.getSharedPreferences("vproxies_prefs", Context.MODE_PRIVATE)
            val alwaysOn = prefs.getBoolean("always_on_vpn", false)

            if (alwaysOn) {
                Log.i("BootReceiver", "Always-on VPN is enabled, starting VProxies VPN Service…")
                VProxiesVpnService.startAlwaysOn(context)
            } else {
                Log.i("BootReceiver", "Always-on VPN is not enabled, skipping auto-start.")
            }
        }
    }
}
