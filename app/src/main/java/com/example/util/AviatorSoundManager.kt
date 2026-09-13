package com.example.util

/**
 * AviatorSoundManager:
 * Silent Sound Manager stub for Aviator Crash Game.
 *
 * All audio outputs (flight engine, crash explosions, ticks, music, cashout chimes)
 * are completely muted/disabled so that no sound is emitted under any circumstance,
 * even if the user toggles the sound or music switch on.
 *
 * All methods, properties, and signatures are preserved to ensure 100% compatibility
 * with all existing UI components and controls.
 */
class AviatorSoundManager {

    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
    var isMuted: Boolean = false

    fun startBackgroundMusic() {
        // Sound completely silenced as requested
    }

    fun stopBackgroundMusic() {
        // No-op
    }

    fun startFlightEngine() {
        // Sound completely silenced as requested
    }

    fun updateFlightMultiplier(multiplier: Double) {
        // No-op
    }

    fun stopFlightEngine() {
        // No-op
    }

    fun playMultiplierTick(multiplier: Double) {
        // Sound completely silenced as requested
    }

    fun playCrashSound() {
        // Sound completely silenced as requested
    }

    fun playCashOutSound() {
        // Sound completely silenced as requested
    }

    fun release() {
        // No-op
    }
}
