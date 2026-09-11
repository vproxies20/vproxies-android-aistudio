package com.example.model

import com.example.network.GitHubReleaseInfo
import java.util.Locale

sealed class UpdateCheckState {
    object Idle : UpdateCheckState()
    object Checking : UpdateCheckState()
    data class Available(val release: GitHubReleaseInfo, val hasNewerVersion: Boolean) : UpdateCheckState()
    data class Error(val message: String) : UpdateCheckState()
}

data class UiLog(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val level: String, // "INFO", "WARN", "ERROR", "SUCCESS"
    val tag: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

object FormatUtils {
    fun formatDuration(elapsedMs: Long): String {
        val seconds = (elapsedMs / 1000).coerceAtLeast(0)
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
        } else {
            String.format(Locale.US, "%02d:%02d", m, s)
        }
    }

    fun formatBytes(bytes: Long): String = when {
        bytes >= 1_073_741_824L -> String.format(Locale.US, "%.2f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576L -> String.format(Locale.US, "%.2f MB", bytes / 1_048_576.0)
        bytes >= 1024L -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }

    fun getCountryFlag(countryCode: String): String {
        if (countryCode.length != 2) return "🌐"
        val firstChar = Character.codePointAt(countryCode.uppercase(Locale.US), 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(countryCode.uppercase(Locale.US), 1) - 0x41 + 0x1F1E6
        return if (firstChar in 0x1F1E6..0x1F1FF && secondChar in 0x1F1E6..0x1F1FF) {
            String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
        } else "🌐"
    }

    fun getCountryName(countryCode: String): String {
        if (countryCode.isBlank()) return "Global"
        return try {
            val name = Locale("", countryCode.uppercase(Locale.US)).displayCountry
            if (name.isNotBlank()) name else countryCode.uppercase(Locale.US)
        } catch (_: Exception) {
            countryCode.uppercase(Locale.US)
        }
    }

    fun formatLocation(countryCode: String, city: String): String {
        val countryName = getCountryName(countryCode)
        val cleanCity = city.trim()
        return when {
            cleanCity.isNotBlank() && cleanCity.equals(countryName, ignoreCase = true) -> countryName
            cleanCity.isNotBlank() && countryName.isNotBlank() -> "$cleanCity, $countryName"
            cleanCity.isNotBlank() -> cleanCity
            countryName.isNotBlank() -> countryName
            else -> "Global Location"
        }
    }
}

data class AppInfoItem(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    val iconBitmap: android.graphics.Bitmap? = null
)
