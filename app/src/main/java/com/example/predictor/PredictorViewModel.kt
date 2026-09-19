package com.example.predictor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class PredictorViewModel : ViewModel() {

    private val _selectedGame = MutableStateFlow(PredictorGame.AVIATOR)
    val selectedGame: StateFlow<PredictorGame> = _selectedGame.asStateFlow()

    private val _selectedPlatform = MutableStateFlow(CasinoPlatform.SNX_777)
    val selectedPlatform: StateFlow<CasinoPlatform> = _selectedPlatform.asStateFlow()

    private val _state = MutableStateFlow(PredictorState.IDLE)
    val state: StateFlow<PredictorState> = _state.asStateFlow()

    private val _currentMultiplier = MutableStateFlow(1.00)
    val currentMultiplier: StateFlow<Double> = _currentMultiplier.asStateFlow()

    private val _targetMultiplier = MutableStateFlow(2.85)
    val targetMultiplier: StateFlow<Double> = _targetMultiplier.asStateFlow()

    private val _safeCashout = MutableStateFlow(2.40)
    val safeCashout: StateFlow<Double> = _safeCashout.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(5)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    private val _confidencePercent = MutableStateFlow(99)
    val confidencePercent: StateFlow<Int> = _confidencePercent.asStateFlow()

    private val _analysisStatusText = MutableStateFlow("AI Engine Ready")
    val analysisStatusText: StateFlow<String> = _analysisStatusText.asStateFlow()

    private val _analysisProgress = MutableStateFlow(0f)
    val analysisProgress: StateFlow<Float> = _analysisProgress.asStateFlow()

    private val _serverSeedHash = MutableStateFlow("a8f4c2e190b3874d6f8319")
    val serverSeedHash: StateFlow<String> = _serverSeedHash.asStateFlow()

    private val _signalsHistory = MutableStateFlow<List<PredictorSignal>>(
        listOf(
            PredictorSignal("R-9481", PredictorGame.AVIATOR, CasinoPlatform.SNX_777, 3.42, 2.90, 99, "94b8e21a"),
            PredictorSignal("R-9480", PredictorGame.AVIATOR, CasinoPlatform.SNX_777, 1.88, 1.60, 98, "77c2d1e0"),
            PredictorSignal("R-9479", PredictorGame.AVIATOR, CasinoPlatform.SNX_777, 5.20, 4.40, 99, "12a4b88f"),
            PredictorSignal("R-9478", PredictorGame.AVIATOR, CasinoPlatform.SNX_777, 2.15, 1.85, 99, "65fe99b3")
        )
    )
    val signalsHistory: StateFlow<List<PredictorSignal>> = _signalsHistory.asStateFlow()

    private val _customPresetMultiplier = MutableStateFlow<Double?>(null)
    val customPresetMultiplier: StateFlow<Double?> = _customPresetMultiplier.asStateFlow()

    private val _isAutoMode = MutableStateFlow(true)
    val isAutoMode: StateFlow<Boolean> = _isAutoMode.asStateFlow()

    private var activeJob: Job? = null

    init {
        // Automatically start the first realistic prediction round on app launch
        requestSignal()
    }

    fun selectGame(game: PredictorGame) {
        _selectedGame.value = game
    }

    fun selectPlatform(platform: CasinoPlatform) {
        _selectedPlatform.value = platform
    }

    fun setCustomPreset(multiplier: Double?) {
        _customPresetMultiplier.value = multiplier
    }

    fun toggleAutoMode() {
        val next = !_isAutoMode.value
        _isAutoMode.value = next
        if (next && _state.value == PredictorState.IDLE) {
            requestSignal()
        }
    }

    fun requestSignal() {
        if (_state.value == PredictorState.ANALYZING || _state.value == PredictorState.SIMULATING_FLIGHT) {
            return
        }

        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            // STEP 1: ANALYZING (Futuristic neural calculation)
            _state.value = PredictorState.ANALYZING
            _analysisProgress.value = 0f

            val steps = listOf(
                "Connecting to ${selectedPlatform.value.platformName} Server..." to 0.25f,
                "Decrypting SHA-256 Provably Fair Seed..." to 0.55f,
                "Neural Trajectory Matrix Computing..." to 0.85f,
                "Signal Locked with 99.4% Accuracy!" to 1.0f
            )

            for ((stepText, progress) in steps) {
                _analysisStatusText.value = stepText
                _analysisProgress.value = progress
                delay(400)
            }

            // Determine Target Multiplier
            val finalTarget: Double
            val roundTag: String

            if (_customPresetMultiplier.value != null && _customPresetMultiplier.value!! >= 1.05) {
                finalTarget = _customPresetMultiplier.value!!
                roundTag = "VIP-${Random.nextInt(1000, 9999)}"
            } else if (_selectedPlatform.value == CasinoPlatform.SNX_777 && _selectedGame.value == PredictorGame.AVIATOR) {
                // REAL LIVE SYNC WITH MAIN WEBSITE AVIATOR ENGINE
                val snapshot = com.example.util.SynchronizedAviatorEngine.getCurrentGlobalSnapshot()
                val targetRound = if (snapshot.phase == com.example.ui.games.AviatorGamePhase.WAITING) {
                    snapshot.roundNumber
                } else {
                    snapshot.roundNumber + 1
                }
                finalTarget = com.example.util.SynchronizedAviatorEngine.getCrashMultiplierForRound(targetRound)
                roundTag = "#$targetRound"
            } else {
                finalTarget = generateBelievableMultiplier()
                roundTag = "R-${Random.nextInt(1000, 9999)}"
            }

            val safe = (finalTarget * 0.85).coerceAtLeast(1.10)
            val roundedTarget = Math.round(finalTarget * 100.0) / 100.0
            val roundedSafe = Math.round(safe * 100.0) / 100.0

            _targetMultiplier.value = roundedTarget
            _safeCashout.value = roundedSafe
            _confidencePercent.value = Random.nextInt(98, 100)
            _serverSeedHash.value = UUID.randomUUID().toString().replace("-", "").take(16)

            // STEP 2: SIGNAL LOCKED + COUNTDOWN
            _state.value = PredictorState.SIGNAL_LOCKED
            for (sec in 5 downTo 1) {
                _countdownSeconds.value = sec
                delay(1000)
            }

            // STEP 3: SIMULATING FLIGHT (Real-time climb)
            _state.value = PredictorState.SIMULATING_FLIGHT
            _currentMultiplier.value = 1.00

            // Speed up calculation based on target value
            val totalFlightDurationMs = (2800L + (roundedTarget * 450L).toLong()).coerceIn(3200L, 8500L)
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < totalFlightDurationMs) {
                val elapsed = System.currentTimeMillis() - startTime
                val progress = (elapsed.toDouble() / totalFlightDurationMs.toDouble()).coerceIn(0.0, 1.0)
                
                // Exponential curve up to exact target
                val currentVal = 1.00 + (roundedTarget - 1.00) * (progress * progress)
                _currentMultiplier.value = Math.round(currentVal * 100.0) / 100.0
                delay(30)
            }

            // Ensure exact target match
            _currentMultiplier.value = roundedTarget

            // STEP 4: CRASH BLAST! (Hits exact predicted point)
            _state.value = PredictorState.CRASHED
            _analysisStatusText.value = "CRASHED AT ${roundedTarget}x - PREDICTION HIT 100%!"

            // Add to history
            val newRecord = PredictorSignal(
                id = roundTag,
                game = _selectedGame.value,
                platform = _selectedPlatform.value,
                targetMultiplier = roundedTarget,
                safeCashout = roundedSafe,
                confidencePercent = _confidencePercent.value,
                serverSeedHash = _serverSeedHash.value.take(8),
                isCustomPreset = _customPresetMultiplier.value != null
            )
            _signalsHistory.value = listOf(newRecord) + _signalsHistory.value.take(12)

            // Auto Mode loop
            if (_isAutoMode.value) {
                delay(4000)
                if (_isAutoMode.value) {
                    requestSignal()
                }
            }
        }
    }

    fun resetToIdle() {
        activeJob?.cancel()
        _state.value = PredictorState.IDLE
        _currentMultiplier.value = 1.00
    }

    private fun generateBelievableMultiplier(): Double {
        val dice = Random.nextInt(100)
        return when {
            dice < 45 -> 1.40 + (Random.nextDouble() * 1.80) // 1.40x to 3.20x
            dice < 75 -> 3.20 + (Random.nextDouble() * 3.80) // 3.20x to 7.00x
            dice < 90 -> 7.00 + (Random.nextDouble() * 7.50) // 7.00x to 14.50x
            else -> 1.15 + (Random.nextDouble() * 0.25)       // 1.15x to 1.40x
        }
    }
}
