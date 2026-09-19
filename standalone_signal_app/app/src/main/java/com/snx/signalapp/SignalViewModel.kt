package com.snx.signalapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class SignalViewModel : ViewModel() {

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
        startSignalLoop()
    }

    private fun startSignalLoop() {
        viewModelScope.launch {
            while (isActive) {
                try {
                    val now = System.currentTimeMillis()
                    val snapshot = SignalEngine.getCurrentSnapshot(now)

                    val activeRound = snapshot.roundNumber
                    val crashForActive = snapshot.crashMultiplier
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

                    _recentSignals.value = (1..8).map { offset ->
                        val round = activeRound - offset
                        ConfirmedSignal(
                            roundNumber = round,
                            multiplier = SignalEngine.getCrashMultiplierForRound(round)
                        )
                    }

                    _isLiveConnected.value = true
                } catch (e: Exception) {
                    _isLiveConnected.value = false
                    _statusText.value = "CALIBRATING SIGNAL..."
                }
                delay(180)
            }
        }
    }
}
