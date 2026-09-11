package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("VProxies", appName)
  }

  @Test
  fun `test always on vpn preference saving`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = context.getSharedPreferences("vproxies_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("always_on_vpn", true).commit()
    assertEquals(true, prefs.getBoolean("always_on_vpn", false))
  }

  @Test
  fun `test routing mode and selected apps saving`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = context.getSharedPreferences("vproxies_prefs", Context.MODE_PRIVATE)
    val testApps = setOf("com.android.chrome", "org.telegram.messenger")
    prefs.edit()
      .putInt("routing_mode", 2)
      .putStringSet("selected_apps", testApps)
      .commit()
    assertEquals(2, prefs.getInt("routing_mode", 0))
    assertEquals(testApps, prefs.getStringSet("selected_apps", emptySet()))
  }
}
