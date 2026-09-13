package com.example.util

import com.example.ui.games.AviatorGamePhase
import com.example.ui.games.AviatorHistoryItem
import com.example.ui.games.LivePlayerBet
import java.security.MessageDigest
import kotlin.math.sqrt

/**
 * Universal Synchronized Aviator Engine
 *
 * Ensures that EVERY registered user and phone runs the EXACT same Aviator round simultaneously.
 * - Same round number
 * - Same 5-second countdown at the exact same millisecond
 * - Same takeoff moment
 * - Same climbing multiplier curve
 * - Same crash point (FLEW AWAY)
 * - Identical multiplayer social betting feed and identical history pills bar
 */
object SynchronizedAviatorEngine {

    const val WAITING_DURATION_MS = 5000L
    const val CRASHED_DURATION_MS = 3000L

    data class GlobalRoundSnapshot(
        val roundNumber: Long,
        val phase: AviatorGamePhase,
        val currentMultiplier: Double,
        val crashMultiplier: Double,
        val waitingSecondsRemaining: Int,
        val waitingProgress: Float,
        val flightElapsedMs: Long,
        val flightTotalDurationMs: Long,
        val historyList: List<AviatorHistoryItem>,
        val livePlayers: List<LivePlayerBet>,
        val activeOnlineUsers: Int
    )

    /**
     * Solves the quadratic formula: M(t) = 1.00 + 0.22*t + 0.04*t^2
     * for flight duration in milliseconds given crash multiplier.
     */
    fun calculateFlightDurationMs(crashMult: Double): Long {
        if (crashMult <= 1.00) return 300L
        val y = crashMult - 1.00
        // 0.04*t^2 + 0.22*t - y = 0
        val discriminant = 0.0484 + 0.16 * y
        val tSec = (-0.22 + sqrt(discriminant)) / 0.08
        return (tSec * 1000.0).toLong().coerceAtLeast(350L)
    }

    /**
     * Generates a cryptographically deterministic crash multiplier for a global round.
     * Realistic Spribe Aviator crash distribution:
     * - 8% instant bust: 1.00x - 1.15x
     * - 40% low flight: 1.16x - 1.99x
     * - 32% medium flight: 2.00x - 4.99x
     * - 14% high flight: 5.00x - 19.99x
     * - 6% mega moon: 20.00x - 180.00x
     */
    fun getCrashMultiplierForRound(roundNumber: Long): Double {
        val input = "AVIATOR_GLOBAL_SEED_SALT_V2:$roundNumber"
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        var value = 0L
        for (i in 0 until 8) {
            value = (value shl 8) or (digest[i].toLong() and 0xFF)
        }
        val positive = if (value < 0) -value else value
        val roll = (positive % 100).toInt()
        val subSeed = (positive / 100)

        val multiplier = when {
            roll < 8 -> 1.00 + ((subSeed % 16) / 100.0)
            roll < 48 -> 1.16 + ((subSeed % 84) / 100.0)
            roll < 80 -> 2.00 + ((subSeed % 300) / 100.0)
            roll < 94 -> 5.00 + ((subSeed % 1500) / 100.0)
            else -> 20.00 + ((subSeed % 16000) / 100.0)
        }
        return (multiplier * 100).toInt() / 100.0
    }

    fun getRoundTotalDurationMs(roundNumber: Long): Long {
        val crashMult = getCrashMultiplierForRound(roundNumber)
        val flightMs = calculateFlightDurationMs(crashMult)
        return WAITING_DURATION_MS + flightMs + CRASHED_DURATION_MS
    }

    /**
     * Computes the exact global state at wall-clock timestamp [nowMs].
     * Uses fixed 30-minute block anchors to ensure instantaneous computation (<0.05ms)
     * and exact synchronization across all client devices.
     */
    fun getCurrentGlobalSnapshot(nowMs: Long = System.currentTimeMillis()): GlobalRoundSnapshot {
        val blockDuration = 1800_000L // 30 minutes
        val blockAnchorTime = nowMs - (nowMs % blockDuration)
        var currentRound = (blockAnchorTime / 18_000L)
        var roundStartTime = blockAnchorTime

        while (true) {
            val roundDuration = getRoundTotalDurationMs(currentRound)
            if (roundStartTime + roundDuration > nowMs) {
                break
            }
            roundStartTime += roundDuration
            currentRound++
        }

        val roundElapsed = nowMs - roundStartTime
        val crashMultiplier = getCrashMultiplierForRound(currentRound)
        val flightTotalDurationMs = calculateFlightDurationMs(crashMultiplier)

        val phase: AviatorGamePhase
        val currentMultiplier: Double
        val waitingSeconds: Int
        val waitingProgress: Float
        val flightElapsedMs: Long

        if (roundElapsed < WAITING_DURATION_MS) {
            phase = AviatorGamePhase.WAITING
            waitingProgress = (roundElapsed.toFloat() / WAITING_DURATION_MS).coerceIn(0f, 1f)
            waitingSeconds = 5 - (roundElapsed / 1000).toInt().coerceIn(0, 4)
            currentMultiplier = 1.00
            flightElapsedMs = 0L
        } else if (roundElapsed < WAITING_DURATION_MS + flightTotalDurationMs) {
            phase = AviatorGamePhase.FLYING
            waitingProgress = 1f
            waitingSeconds = 0
            flightElapsedMs = roundElapsed - WAITING_DURATION_MS
            val seconds = flightElapsedMs / 1000.0
            val calculated = 1.00 + (seconds * 0.22) + (seconds * seconds * 0.04)
            currentMultiplier = minOf(crashMultiplier, (calculated * 100).toInt() / 100.0)
        } else {
            phase = AviatorGamePhase.CRASHED
            waitingProgress = 1f
            waitingSeconds = 0
            flightElapsedMs = flightTotalDurationMs
            currentMultiplier = crashMultiplier
        }

        // Generate past 25 rounds history deterministically
        val history = (1..25).map { offset ->
            val prevRound = currentRound - offset
            AviatorHistoryItem(
                id = prevRound,
                multiplier = getCrashMultiplierForRound(prevRound)
            )
        }

        // Active online count deterministic variation around 1047
        val activeUsers = 1040 + ((currentRound * 7L) % 18).toInt()

        // Generate deterministic multiplayer social bet list for current round
        val livePlayers = generateSynchronizedPlayersForRound(currentRound, currentMultiplier, crashMultiplier)

        return GlobalRoundSnapshot(
            roundNumber = currentRound,
            phase = phase,
            currentMultiplier = currentMultiplier,
            crashMultiplier = crashMultiplier,
            waitingSecondsRemaining = waitingSeconds,
            waitingProgress = waitingProgress,
            flightElapsedMs = flightElapsedMs,
            flightTotalDurationMs = flightTotalDurationMs,
            historyList = history,
            livePlayers = livePlayers,
            activeOnlineUsers = activeUsers
        )
    }

    private val playerUsernames = listOf(
        "শাকিল_কিং", "আরিফ_রয়্যাল", "Tanvir_99", "রাকিব_বস", "Fahim_Pro",
        "Sumon_Khan", "Redwan_777", "সাব্বির_বিডি", "Hasan_King", "নিলয়_উইন",
        "সোহেল_রানা", "Alamin_Dhaka", "Mahmud_Boss", "ইমরান_স্টার", "Shahadat_BD",
        "মিজানুর_রহমান", "Ripon_Cash", "Ashik_Vip", "সজল_খান", "Joy_Roy"
    )

    private val betPresets = listOf(50.0, 100.0, 150.0, 200.0, 300.0, 500.0, 1000.0, 1500.0, 2000.0)

    fun generateSynchronizedPlayersForRound(
        roundNumber: Long,
        currentMultiplier: Double,
        crashMultiplier: Double
    ): List<LivePlayerBet> {
        return playerUsernames.mapIndexed { index, name ->
            val seed = (roundNumber * 1337L) + (index * 7919L)
            val betAmt = betPresets[(seed % betPresets.size).toInt().let { if (it < 0) -it else it }]
            val targetMult = when ((seed % 10).toInt().let { if (it < 0) -it else it }) {
                0 -> 1.15
                1 -> 1.25
                2 -> 1.40
                3 -> 1.65
                4 -> 1.85
                5 -> 2.10
                6 -> 2.50
                7 -> 3.20
                8 -> 4.50
                else -> 8.00
            }

            val cashedOut = (currentMultiplier >= targetMult) && (targetMult <= crashMultiplier)
            val winAmt = if (cashedOut) betAmt * targetMult else 0.0

            LivePlayerBet(
                id = "p_${roundNumber}_$index",
                username = name,
                betAmount = betAmt,
                cashOutMultiplier = targetMult,
                isCashedOut = cashedOut,
                winAmount = winAmt
            )
        }
    }
}
