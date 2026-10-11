package com.example.batterysaver.model

data class BatteryInfo(
    val level: Int = 78,
    val isCharging: Boolean = false,
    val pluggedSource: String = "Battery",
    val health: String = "Good",
    val temperatureC: Float = 28.5f,
    val voltageV: Float = 4.05f,
    val technology: String = "Li-ion",
    val estimatedTimeRemainingMinutes: Int = 240,
    val estimatedChargeTimeMinutes: Int = 65
)

data class AppConsumption(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val colorHex: Long,
    val packageName: String,
    val percentage: Int,
    val screenTime: String,
    val backgroundTime: String,
    val lastUsed: String,
    val isStoppable: Boolean = true,
    val isStopped: Boolean = false
)

data class BatteryEvent(
    val id: String,
    val icon: String,
    val title: String,
    val subtitle: String,
    val timeLabel: String = "Just now",
    val timestamp: Long = System.currentTimeMillis()
)

data class HistoryPoint(
    val timestamp: Long,
    val level: Int
)

data class TurboState(
    val isActive: Boolean = false,
    val startedAt: Long = 0L,
    val savedMinutes: Int = 0,
    val stoppedAppsCount: Int = 0,
    val savedPercentage: Int = 0
)

enum class AppSortOption {
    PERCENTAGE,
    TIME,
    NAME
}

enum class NavigationTab {
    DASHBOARD,
    APPS,
    HISTORY,
    SETTINGS
}
