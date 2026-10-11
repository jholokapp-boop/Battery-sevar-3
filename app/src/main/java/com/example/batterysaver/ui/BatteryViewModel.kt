package com.example.batterysaver.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.batterysaver.data.BatteryRepository
import com.example.batterysaver.model.AppConsumption
import com.example.batterysaver.model.AppSortOption
import com.example.batterysaver.model.BatteryEvent
import com.example.batterysaver.model.BatteryInfo
import com.example.batterysaver.model.HistoryPoint
import com.example.batterysaver.model.NavigationTab
import com.example.batterysaver.model.TurboState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BatteryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BatteryRepository(application.applicationContext)

    val batteryInfo: StateFlow<BatteryInfo> = repository.batteryInfo
    val turboState: StateFlow<TurboState> = repository.turboState
    val historyPoints: StateFlow<List<HistoryPoint>> = repository.historyPoints
    val events: StateFlow<List<BatteryEvent>> = repository.events
    val isDarkMode: StateFlow<Boolean> = repository.isDarkMode
    val lowBatteryAlert: StateFlow<Boolean> = repository.lowBatteryAlert
    val fullChargeAlert: StateFlow<Boolean> = repository.fullChargeAlert
    val isExtremeSaver: StateFlow<Boolean> = repository.isExtremeSaver

    private val _currentTab = MutableStateFlow(NavigationTab.DASHBOARD)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(AppSortOption.PERCENTAGE)
    val sortOption: StateFlow<AppSortOption> = _sortOption.asStateFlow()

    private val _selectedAppForDetail = MutableStateFlow<AppConsumption?>(null)
    val selectedAppForDetail: StateFlow<AppConsumption?> = _selectedAppForDetail.asStateFlow()

    private val _showTurboModal = MutableStateFlow(false)
    val showTurboModal: StateFlow<Boolean> = _showTurboModal.asStateFlow()

    private val _showChargingScreen = MutableStateFlow(false)
    val showChargingScreen: StateFlow<Boolean> = _showChargingScreen.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Filtered and sorted apps
    val filteredApps: StateFlow<List<AppConsumption>> = combine(
        repository.appsList,
        _searchQuery,
        _sortOption
    ) { apps, query, sort ->
        val filtered = if (query.isBlank()) {
            apps
        } else {
            apps.filter { it.name.contains(query, ignoreCase = true) }
        }

        when (sort) {
            AppSortOption.PERCENTAGE -> filtered.sortedByDescending { it.percentage }
            AppSortOption.NAME -> filtered.sortedBy { it.name }
            AppSortOption.TIME -> filtered.sortedByDescending { parseMinutes(it.screenTime) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topApps: StateFlow<List<AppConsumption>> = repository.appsList.combine(
        MutableStateFlow(Unit)
    ) { apps, _ ->
        apps.sortedByDescending { it.percentage }.take(4)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortOption(option: AppSortOption) {
        _sortOption.value = option
    }

    fun openAppDetail(app: AppConsumption) {
        _selectedAppForDetail.value = app
    }

    fun closeAppDetail() {
        _selectedAppForDetail.value = null
    }

    fun openTurboModal() {
        _showTurboModal.value = true
    }

    fun closeTurboModal() {
        _showTurboModal.value = false
    }

    fun openChargingScreen() {
        _showChargingScreen.value = true
    }

    fun closeChargingScreen() {
        _showChargingScreen.value = false
    }

    fun forceStopApp(appId: String) {
        val app = repository.appsList.value.find { it.id == appId }
        repository.forceStopApp(appId)
        _selectedAppForDetail.value = null
        showToast("${app?.name ?: "App"} stopped & background RAM freed")
    }

    fun activateTurboMode() {
        val state = repository.activateTurboMode()
        closeTurboModal()
        showToast("🚀 Turbo Mode চালু হয়েছে (+${state.savedMinutes}m gained)")
    }

    fun stopTurboMode() {
        repository.stopTurboMode()
        showToast("🛑 Turbo Mode বন্ধ হয়েছে")
    }

    fun toggleDarkMode() = repository.toggleDarkMode()
    fun toggleLowBatteryAlert() = repository.toggleLowBatteryAlert()
    fun toggleFullChargeAlert() = repository.toggleFullChargeAlert()
    fun toggleExtremeSaver() = repository.toggleExtremeSaver()

    fun clearHistory() {
        repository.clearHistory()
        showToast("History cleared successfully")
    }

    fun showToast(msg: String) {
        viewModelScope.launch {
            _toastMessage.value = msg
            delay(2400)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    private fun parseMinutes(timeStr: String): Int {
        var total = 0
        val hMatch = Regex("(\\d+)\\s*h").find(timeStr)
        val mMatch = Regex("(\\d+)\\s*m").find(timeStr)
        hMatch?.let { total += it.groupValues[1].toInt() * 60 }
        mMatch?.let { total += it.groupValues[1].toInt() }
        return total
    }

    override fun onCleared() {
        super.onCleared()
        repository.unregister()
    }
}
