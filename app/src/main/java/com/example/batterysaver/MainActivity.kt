package com.example.batterysaver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.batterysaver.model.NavigationTab
import com.example.batterysaver.ui.BatteryViewModel
import com.example.batterysaver.ui.components.AppDetailSheet
import com.example.batterysaver.ui.components.TurboModal
import com.example.batterysaver.ui.screens.AppsScreen
import com.example.batterysaver.ui.screens.ChargingScreen
import com.example.batterysaver.ui.screens.DashboardScreen
import com.example.batterysaver.ui.screens.HistoryScreen
import com.example.batterysaver.ui.screens.SettingsScreen
import com.example.batterysaver.ui.screens.SplashScreen
import com.example.batterysaver.ui.theme.AccentCyan
import com.example.batterysaver.ui.theme.BatterySaverTheme
import com.example.batterysaver.ui.theme.GoodGreen

class MainActivity : ComponentActivity() {

    private val viewModel: BatteryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            BatterySaverTheme(darkTheme = isDarkMode) {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        onFinish = { showSplash = false }
                    )
                } else {
                    BatterySaverMainApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BatterySaverMainApp(viewModel: BatteryViewModel) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val turboState by viewModel.turboState.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val topApps by viewModel.topApps.collectAsState()
    val filteredApps by viewModel.filteredApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val historyPoints by viewModel.historyPoints.collectAsState()
    val events by viewModel.events.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val lowBatteryAlert by viewModel.lowBatteryAlert.collectAsState()
    val fullChargeAlert by viewModel.fullChargeAlert.collectAsState()
    val isExtremeSaver by viewModel.isExtremeSaver.collectAsState()
    val selectedAppForDetail by viewModel.selectedAppForDetail.collectAsState()
    val showTurboModal by viewModel.showTurboModal.collectAsState()
    val showChargingScreen by viewModel.showChargingScreen.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val pageTitle = when (currentTab) {
        NavigationTab.DASHBOARD -> "Battery"
        NavigationTab.APPS -> "Apps"
        NavigationTab.HISTORY -> "History"
        NavigationTab.SETTINGS -> "Settings"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pageTitle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )

                // Live status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(100.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .scale(dotScale)
                                .clip(CircleShape)
                                .background(if (batteryInfo.isCharging) GoodGreen else AccentCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (batteryInfo.isCharging) "Charging" else "Live",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == NavigationTab.DASHBOARD,
                    onClick = { viewModel.selectTab(NavigationTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.APPS,
                    onClick = { viewModel.selectTab(NavigationTab.APPS) },
                    icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = "Apps") },
                    label = { Text("Apps", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_apps")
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.HISTORY,
                    onClick = { viewModel.selectTab(NavigationTab.HISTORY) },
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = "History") },
                    label = { Text("History", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_history")
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.SETTINGS,
                    onClick = { viewModel.selectTab(NavigationTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                NavigationTab.DASHBOARD -> DashboardScreen(
                    batteryInfo = batteryInfo,
                    turboState = turboState,
                    topApps = topApps,
                    onOpenTurbo = { viewModel.openTurboModal() },
                    onStopTurbo = { viewModel.stopTurboMode() },
                    onOpenAppDetail = { viewModel.openAppDetail(it) },
                    onOpenChargingScreen = { viewModel.openChargingScreen() }
                )
                NavigationTab.APPS -> AppsScreen(
                    appsList = filteredApps,
                    searchQuery = searchQuery,
                    sortOption = sortOption,
                    onSearchChange = { viewModel.updateSearchQuery(it) },
                    onSortChange = { viewModel.updateSortOption(it) },
                    onOpenAppDetail = { viewModel.openAppDetail(it) }
                )
                NavigationTab.HISTORY -> HistoryScreen(
                    historyPoints = historyPoints,
                    events = events
                )
                NavigationTab.SETTINGS -> SettingsScreen(
                    isDarkMode = isDarkMode,
                    lowBatteryAlert = lowBatteryAlert,
                    fullChargeAlert = fullChargeAlert,
                    isExtremeSaver = isExtremeSaver,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onToggleLowBatteryAlert = { viewModel.toggleLowBatteryAlert() },
                    onToggleFullChargeAlert = { viewModel.toggleFullChargeAlert() },
                    onToggleExtremeSaver = { viewModel.toggleExtremeSaver() },
                    onClearHistory = { viewModel.clearHistory() }
                )
            }

            // Toast overlay
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                toastMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(100.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // Modal BottomSheet for App Details
    selectedAppForDetail?.let { app ->
        AppDetailSheet(
            app = app,
            onDismiss = { viewModel.closeAppDetail() },
            onForceStop = { viewModel.forceStopApp(app.id) }
        )
    }

    // Turbo Mode Fullscreen Modal
    if (showTurboModal) {
        TurboModal(
            appsList = filteredApps,
            onDismiss = { viewModel.closeTurboModal() },
            onActivate = { viewModel.activateTurboMode() }
        )
    }

    // Charging Screen Overlay
    if (showChargingScreen) {
        ChargingScreen(
            batteryInfo = batteryInfo,
            onClose = { viewModel.closeChargingScreen() }
        )
    }
}
