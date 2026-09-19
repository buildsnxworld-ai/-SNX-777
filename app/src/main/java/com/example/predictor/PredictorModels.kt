package com.example.predictor

enum class PredictorGame(val displayName: String, val iconEmoji: String, val primaryColorHex: Long) {
    AVIATOR("Aviator", "✈️", 0xFFFF1A38),
    JETX("JetX", "🚀", 0xFFFFC107),
    LUCKY_JET("Lucky Jet", "🎈", 0xFF00E5FF),
    FLYX("FlyX", "⚡", 0xFFFF4081),
    SPACEMAN("Spaceman", "👨‍🚀", 0xFF7C4DFF),
    CRASH_X("CrashX", "💥", 0xFF00E676)
}

enum class CasinoPlatform(val platformName: String, val badgeColorHex: Long) {
    SNX_777("SNX 777", 0xFFFFD700),
    ONE_WIN("1Win", 0xFF00C0FF),
    ONE_X_BET("1xBet", 0xFF1976D2),
    MOSTBET("Mostbet", 0xFFFF5722),
    PARIMATCH("Parimatch", 0xFFFFEB3B),
    BETWAY("Betway", 0xFF4CAF50)
}

enum class PredictorState {
    IDLE,             // Ready for user to request signal
    ANALYZING,        // Futuristic AI scanning / decryption animation
    SIGNAL_LOCKED,    // Predicted multiplier revealed with countdown timer
    SIMULATING_FLIGHT,// Jet flying towards the predicted target multiplier
    CRASHED           // Plane blasted at exact target multiplier, showing success HUD
}

data class PredictorSignal(
    val id: String,
    val game: PredictorGame,
    val platform: CasinoPlatform,
    val targetMultiplier: Double,
    val safeCashout: Double,
    val confidencePercent: Int,
    val serverSeedHash: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isCustomPreset: Boolean = false
)
