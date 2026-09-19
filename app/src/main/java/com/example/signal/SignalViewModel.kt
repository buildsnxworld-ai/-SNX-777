package com.example.signal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SnxCloudSyncService
import com.example.util.NetworkMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ConfirmedSignal(
    val roundNumber: Long,
    val multiplier: Double,
    val timestamp: String = "JUST NOW"
)

class SignalViewModel(networkMonitor: NetworkMonitor? = null) : ViewModel() {

    private val _isInternetAvailable = MutableStateFlow(true)
    val isInternetAvailable: StateFlow<Boolean> = _isInternetAvailable.asStateFlow()

    private val monitor = networkMonitor

    private val _currentRoundNumber = MutableStateFlow(0L)
    val currentRoundNumber: StateFlow<Long> = _currentRoundNumber.asStateFlow()

    private val _targetMultiplier = MutableStateFlow(1.00)
    val targetMultiplier: StateFlow<Double> = _targetMultiplier.asStateFlow()

    private val _safeCashout = MutableStateFlow(1.00)
    val safeCashout: StateFlow<Double> = _safeCashout.asStateFlow()

    private val _nextRoundNumber = MutableStateFlow(0L)
    val nextRoundNumber: StateFlow<Long> = _nextRoundNumber.asStateFlow()

    private val _nextMultiplier = MutableStateFlow(1.00)
    val nextMultiplier: StateFlow<Double> = _nextMultiplier.asStateFlow()

    private val _statusText = MutableStateFlow("SCANNING FREQUENCY...")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(0)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    private val _isLiveConnected = MutableStateFlow(true)
    val isLiveConnected: StateFlow<Boolean> = _isLiveConnected.asStateFlow()

    private val _recentSignals = MutableStateFlow<List<ConfirmedSignal>>(emptyList())
    val recentSignals: StateFlow<List<ConfirmedSignal>> = _recentSignals.asStateFlow()

    init {
        monitor?.let { netMon ->
            _isInternetAvailable.value = netMon.isCurrentlyConnected()
            viewModelScope.launch {
                netMon.isOnlineFlow.collect { online ->
                    _isInternetAvailable.value = online
                }
            }
        }
        startSignalLoop()
        startCloudSyncObserver()
    }

    fun refreshNetworkStatus() {
        monitor?.let {
            _isInternetAvailable.value = it.isCurrentlyConnected()
        }
    }

    private fun startCloudSyncObserver() {
        viewModelScope.launch {
            SnxCloudSyncService.isCloudConnected.collect { connected ->
                _isLiveConnected.value = connected
            }
        }
    }

    private fun startSignalLoop() {
        viewModelScope.launch {
            while (isActive) {
                try {
                    val now = System.currentTimeMillis()
                    val snapshot = SignalEngine.getCurrentSnapshot(now)

                    // If Cloud provides active round from website, align seamlessly
                    val cloudRound = SnxCloudSyncService.cloudActiveRoundNumber.value
                    val activeRound = if (cloudRound > 0 && Math.abs(cloudRound - snapshot.roundNumber) <= 1) {
                        cloudRound
                    } else {
                        snapshot.roundNumber
                    }

                    val crashForActive = SignalEngine.getCrashMultiplierForRound(activeRound)
                    val nextRound = activeRound + 1
                    val crashForNext = SignalEngine.getCrashMultiplierForRound(nextRound)

                    _currentRoundNumber.value = activeRound
                    _targetMultiplier.value = crashForActive
                    _safeCashout.value = Math.round((crashForActive * 0.85).coerceAtLeast(1.10) * 100.0) / 100.0

                    _nextRoundNumber.value = nextRound
                    _nextMultiplier.value = crashForNext
                    _countdownSeconds.value = snapshot.waitingSecondsRemaining

                    when (snapshot.phase) {
                        SignalPhase.WAITING -> {
                            _statusText.value = "SIGNAL LOCKED • FLIGHT IN ${snapshot.waitingSecondsRemaining}s"
                        }
                        SignalPhase.FLYING -> {
                            _statusText.value = "IN FLIGHT • CRASH TARGET LOCKED"
                        }
                        SignalPhase.CRASHED -> {
                            _statusText.value = "CRASH CONFIRMED AT ${crashForActive}x"
                        }
                    }

                    // Feed of last 8 confirmed rounds
                    _recentSignals.value = (1..8).map { offset ->
                        val round = activeRound - offset
                        ConfirmedSignal(
                            roundNumber = round,
                            multiplier = SignalEngine.getCrashMultiplierForRound(round)
                        )
                    }

                    _isLiveConnected.value = SnxCloudSyncService.isCloudConnected.value
                } catch (e: Exception) {
                    _isLiveConnected.value = false
                    _statusText.value = "CALIBRATING SIGNAL..."
                }
                delay(180) // Smooth real-time update
            }
        }
    }
}
