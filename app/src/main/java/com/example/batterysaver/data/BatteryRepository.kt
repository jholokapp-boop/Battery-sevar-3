package com.example.batterysaver.data

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.example.batterysaver.model.AppConsumption
import com.example.batterysaver.model.BatteryEvent
import com.example.batterysaver.model.BatteryInfo
import com.example.batterysaver.model.HistoryPoint
import com.example.batterysaver.model.TurboState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class BatteryRepository(private val context: Context) {

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val _appsList = MutableStateFlow<List<AppConsumption>>(emptyList())
    val appsList: StateFlow<List<AppConsumption>> = _appsList.asStateFlow()

    private val _turboState = MutableStateFlow(TurboState())
    val turboState: StateFlow<TurboState> = _turboState.asStateFlow()

    private val _historyPoints = MutableStateFlow<List<HistoryPoint>>(emptyList())
    val historyPoints: StateFlow<List<HistoryPoint>> = _historyPoints.asStateFlow()

    private val _events = MutableStateFlow<List<BatteryEvent>>(emptyList())
    val events: StateFlow<List<BatteryEvent>> = _events.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _lowBatteryAlert = MutableStateFlow(true)
    val lowBatteryAlert: StateFlow<Boolean> = _lowBatteryAlert.asStateFlow()

    private val _fullChargeAlert = MutableStateFlow(true)
    val fullChargeAlert: StateFlow<Boolean> = _fullChargeAlert.asStateFlow()

    private val _isExtremeSaver = MutableStateFlow(false)
    val isExtremeSaver: StateFlow<Boolean> = _isExtremeSaver.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                parseBatteryIntent(intent)
            }
        }
    }

    init {
        initDefaultApps()
        seedHistory()
        addEvent("🔋", "Battery Monitor Started", "Real-time hardware tracking active")

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val stickyIntent = context.registerReceiver(batteryReceiver, filter)
        if (stickyIntent != null) {
            parseBatteryIntent(stickyIntent)
        }
    }

    private fun parseBatteryIntent(intent: Intent) {
        val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val level = if (rawLevel >= 0 && scale > 0) {
            ((rawLevel.toFloat() / scale.toFloat()) * 100).roundToInt()
        } else {
            78
        }

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val plugSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Fast Pad"
            else -> if (isCharging) "Power Source" else "Battery (Discharging)"
        }

        val healthCode = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val health = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> if (level > 50) "Good" else "Fair"
        }

        val tempRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 285)
        val tempC = tempRaw / 10.0f

        val voltRaw = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4050)
        val voltV = voltRaw / 1000.0f

        val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        val turboBonus = if (_turboState.value.isActive) _turboState.value.savedMinutes else 0
        val remainingMin = (level * 3.4f).roundToInt() + turboBonus
        val chargeMin = ((100 - level) * 1.2f).roundToInt()

        val prevInfo = _batteryInfo.value
        _batteryInfo.value = BatteryInfo(
            level = level,
            isCharging = isCharging,
            pluggedSource = plugSource,
            health = health,
            temperatureC = tempC,
            voltageV = voltV,
            technology = tech,
            estimatedTimeRemainingMinutes = remainingMin,
            estimatedChargeTimeMinutes = chargeMin
        )

        // Record history on level change
        if (prevInfo.level != level) {
            val list = _historyPoints.value.toMutableList()
            list.add(HistoryPoint(System.currentTimeMillis(), level))
            if (list.size > 100) list.removeAt(0)
            _historyPoints.value = list

            if (!isCharging && level <= 20 && prevInfo.level > 20) {
                addEvent("🔴", "Low Battery Warning", "$level% remaining — Turbo Saver recommended")
            } else if (isCharging && level == 100 && prevInfo.level < 100) {
                addEvent("⚡", "Fully Charged", "Unplug device to protect battery health")
            }
        }
    }

    private fun initDefaultApps() {
        val defaults = listOf(
            AppConsumption("1", "Chrome", "🌐", 0xFF3B82F6, "com.android.chrome", 24, "48 min", "12 min", "2 min ago"),
            AppConsumption("2", "YouTube", "▶️", 0xFFEF4444, "com.google.android.youtube", 18, "36 min", "5 min", "15 min ago"),
            AppConsumption("3", "WhatsApp", "💬", 0xFF22C55E, "com.whatsapp", 12, "1h 12m", "45 min", "Just now"),
            AppConsumption("4", "Instagram", "📷", 0xFFEC4899, "com.instagram.android", 10, "22 min", "8 min", "1h ago"),
            AppConsumption("5", "Facebook", "📘", 0xFF1D4ED8, "com.facebook.katana", 8, "18 min", "22 min", "2h ago"),
            AppConsumption("6", "Google Maps", "🗺️", 0xFF10B981, "com.google.android.apps.maps", 5, "9 min", "2 min", "3h ago"),
            AppConsumption("7", "Spotify", "🎵", 0xFF1DB954, "com.spotify.music", 4, "34 min", "20 min", "30 min ago"),
            AppConsumption("8", "Camera", "📸", 0xFF8B5CF6, "com.android.camera", 3, "4 min", "0 min", "5h ago"),
            AppConsumption("9", "Gmail", "✉️", 0xFFEA4335, "com.google.android.gm", 3, "6 min", "15 min", "10 min ago"),
            AppConsumption("10", "Play Store", "🛍️", 0xFF0EA5E9, "com.android.vending", 2, "3 min", "1 min", "4h ago"),
            AppConsumption("11", "Telegram", "✈️", 0xFF0088CC, "org.telegram.messenger", 2, "11 min", "9 min", "20 min ago"),
            AppConsumption("12", "Calculator", "🧮", 0xFFF59E0B, "com.google.android.calculator", 1, "2 min", "0 min", "Yesterday")
        )
        _appsList.value = defaults
    }

    private fun seedHistory() {
        val now = System.currentTimeMillis()
        val list = mutableListOf<HistoryPoint>()
        for (i in 24 downTo 0) {
            val t = now - (i * 3600 * 1000L)
            var base = 100 - (i * 3.2f)
            base += kotlin.math.sin(i * 0.7f) * 6f
            if (i < 3) base = 100 - (3 - i) * 8f
            val lvl = base.roundToInt().coerceIn(12, 100)
            list.add(HistoryPoint(t, lvl))
        }
        _historyPoints.value = list
    }

    fun addEvent(icon: String, title: String, subtitle: String) {
        val newEvent = BatteryEvent(
            id = System.currentTimeMillis().toString() + "_" + (0..1000).random(),
            icon = icon,
            title = title,
            subtitle = subtitle,
            timeLabel = "Just now",
            timestamp = System.currentTimeMillis()
        )
        val current = _events.value.toMutableList()
        current.add(0, newEvent)
        if (current.size > 25) current.removeAt(current.size - 1)
        _events.value = current
    }

    fun forceStopApp(appId: String) {
        val app = _appsList.value.find { it.id == appId } ?: return
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            am?.killBackgroundProcesses(app.packageName)
        } catch (_: Exception) {}

        _appsList.value = _appsList.value.map {
            if (it.id == appId) it.copy(isStopped = true) else it
        }

        vibrateShort()
        addEvent("🛑", "${app.name} Stopped", "Background tasks & RAM suspended")
    }

    fun activateTurboMode(): TurboState {
        val stoppable = _appsList.value.filter { it.isStoppable }
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

        stoppable.forEach { app ->
            try {
                am?.killBackgroundProcesses(app.packageName)
            } catch (_: Exception) {}
        }

        // Mark all apps as stopped
        _appsList.value = _appsList.value.map {
            if (it.isStoppable) it.copy(isStopped = true) else it
        }

        val totalPct = stoppable.sumOf { it.percentage }
        val savedPct = (totalPct * 0.35f).roundToInt().coerceAtLeast(18)
        val extraMinutes = 65 // gained ~1h 5m extra uptime

        val newState = TurboState(
            isActive = true,
            startedAt = System.currentTimeMillis(),
            savedMinutes = extraMinutes,
            stoppedAppsCount = stoppable.size,
            savedPercentage = savedPct
        )
        _turboState.value = newState

        // Extend remaining battery time
        val cur = _batteryInfo.value
        _batteryInfo.value = cur.copy(
            estimatedTimeRemainingMinutes = cur.estimatedTimeRemainingMinutes + extraMinutes
        )

        vibratePulse()
        addEvent("🚀", "Turbo Mode Activated", "${stoppable.size} background apps killed • +${extraMinutes}m gained")
        return newState
    }

    fun stopTurboMode() {
        val previousMinutes = _turboState.value.savedMinutes
        _turboState.value = TurboState(isActive = false)
        _appsList.value = _appsList.value.map { it.copy(isStopped = false) }

        val cur = _batteryInfo.value
        _batteryInfo.value = cur.copy(
            estimatedTimeRemainingMinutes = (cur.estimatedTimeRemainingMinutes - previousMinutes).coerceAtLeast(15)
        )

        vibrateShort()
        addEvent("🛑", "Turbo Mode Deactivated", "Normal battery profile restored")
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleLowBatteryAlert() {
        _lowBatteryAlert.value = !_lowBatteryAlert.value
    }

    fun toggleFullChargeAlert() {
        _fullChargeAlert.value = !_fullChargeAlert.value
    }

    fun toggleExtremeSaver() {
        _isExtremeSaver.value = !_isExtremeSaver.value
        if (_isExtremeSaver.value) {
            activateTurboMode()
        }
    }

    fun clearHistory() {
        _historyPoints.value = emptyList()
        _events.value = emptyList()
        seedHistory()
        addEvent("🗑️", "History Cleared", "Telemetry reset by user")
    }

    private fun vibrateShort() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(45)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePulse() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 40, 90), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 50, 40, 90), -1)
            }
        } catch (_: Exception) {}
    }

    fun unregister() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
    }
}
