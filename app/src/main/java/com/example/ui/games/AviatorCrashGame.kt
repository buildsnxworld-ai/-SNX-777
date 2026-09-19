package com.example.ui.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.example.data.AdminManager
import com.example.model.AppLanguage
import com.example.ui.theme.*
import com.example.util.AviatorSoundManager
import com.example.util.SynchronizedAviatorEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class AviatorGamePhase {
    WAITING,   // 5-second countdown with progress bar
    FLYING,    // Plane is ascending, multiplier climbing exponentially
    CRASHED    // Plane flew away, shows crash multiplier before next loop
}

enum class PanelBetState {
    IDLE,              // No bet placed
    QUEUED_FOR_NEXT,   // Bet placed while plane was flying; starts next round
    WAITING_IN_ROUND,  // Bet placed during 5s countdown
    ACTIVE_FLYING,     // Plane is flying, can Cash Out anytime!
    CASHED_OUT,        // Successfully cashed out this round
    LOST               // Flew away before cashing out
}

data class AviatorHistoryItem(
    val id: Long,
    val multiplier: Double
)

data class LivePlayerBet(
    val id: String,
    val username: String,
    val betAmount: Double,
    val cashOutMultiplier: Double?,
    val isCashedOut: Boolean,
    val winAmount: Double
)

data class UserPastBet(
    val time: String,
    val bet: Double,
    val multiplier: Double,
    val win: Double,
    val isWin: Boolean
)

@Composable
fun AviatorCrashGame(
    isOpen: Boolean,
    currentBalance: Double,
    language: AppLanguage,
    onBalanceChange: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val aviatorGame = remember(isOpen) {
        AdminManager.getInstance(context).gamesList.value.firstOrNull { it.id == "aviator_crash" }
    }
    val customPlaneImageUrl = aviatorGame?.imageUrl.orEmpty()

    val decimalFormat = remember { DecimalFormat("#,##0.00") }

    // ================= REAL-TIME ROUND & AUTO-LOOP ENGINE STATE =================
    var currentPhase by remember { mutableStateOf(AviatorGamePhase.WAITING) }
    var currentMultiplier by remember { mutableDoubleStateOf(1.00) }
    var waitingCountdownSeconds by remember { mutableIntStateOf(5) }
    var waitingProgress by remember { mutableFloatStateOf(1.0f) }
    var crashPoint by remember { mutableDoubleStateOf(3.50) }
    var flightDurationMs by remember { mutableLongStateOf(0L) }
    var activeOnlineUsers by remember { mutableIntStateOf(1047) }

    // Multiplier History Bar (Exact matching the photo: 1.03x, 2.34x, 1.85x, 4.99x, 2.04x, 2.24x, 5.01x, 2.05x...)
    var historyList by remember {
        mutableStateOf(
            listOf(
                AviatorHistoryItem(1, 1.03),
                AviatorHistoryItem(2, 2.34),
                AviatorHistoryItem(3, 1.85),
                AviatorHistoryItem(4, 4.99),
                AviatorHistoryItem(5, 2.04),
                AviatorHistoryItem(6, 2.24),
                AviatorHistoryItem(7, 5.01),
                AviatorHistoryItem(8, 2.05),
                AviatorHistoryItem(9, 1.38),
                AviatorHistoryItem(10, 7.66),
                AviatorHistoryItem(11, 148.02),
                AviatorHistoryItem(12, 3.42)
            )
        )
    }

    // ================= DUAL BETTING PANELS STATE (PANEL 1 & PANEL 2) =================
    // Panel 1:
    var p1Mode by remember { mutableStateOf("Bet") } // "Bet" or "Auto"
    var p1Amount by remember { mutableDoubleStateOf(50.00) } // Minimum bet 50 BDT
    var p1State by remember { mutableStateOf(PanelBetState.IDLE) }
    var p1CashedWin by remember { mutableDoubleStateOf(0.0) }
    var p1CashedMult by remember { mutableDoubleStateOf(1.0) }
    var p1AutoBet by remember { mutableStateOf(false) }
    var p1AutoCashOut by remember { mutableStateOf(false) }
    var p1AutoTarget by remember { mutableDoubleStateOf(2.00) }

    // Panel 2:
    var p2Mode by remember { mutableStateOf("Bet") } // "Bet" or "Auto"
    var p2Amount by remember { mutableDoubleStateOf(50.00) } // Minimum bet 50 BDT
    var p2State by remember { mutableStateOf(PanelBetState.IDLE) }
    var p2CashedWin by remember { mutableDoubleStateOf(0.0) }
    var p2CashedMult by remember { mutableDoubleStateOf(1.0) }
    var p2AutoBet by remember { mutableStateOf(false) }
    var p2AutoCashOut by remember { mutableStateOf(false) }
    var p2AutoTarget by remember { mutableDoubleStateOf(3.00) }
    var p2Collapsed by remember { mutableStateOf(false) }

    // Statistics Tabs: "All Bets", "Previous", "Top"
    var selectedStatsTab by remember { mutableStateOf("All Bets") }

    // Live Multiplayer Social Simulation (Matching the photo: 1235/2722 Bets, Total win 122,633.14 BDT)
    var livePlayers by remember { mutableStateOf(generateRealisticAviatorPlayers()) }
    var myPastBets by remember {
        mutableStateOf(
            listOf(
                UserPastBet("17:48", 100.0, 3.50, 350.0, true),
                UserPastBet("17:45", 200.0, 1.03, 0.0, false),
                UserPastBet("17:42", 100.0, 4.99, 499.0, true),
                UserPastBet("17:40", 500.0, 2.34, 1170.0, true)
            )
        )
    }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var lastUserCashOutMult by remember { mutableDoubleStateOf(0.0) }
    var lastUserCashOutWin by remember { mutableDoubleStateOf(0.0) }

    // High-performance synthesized sound effects manager (Exact Spribe Sound + Music)
    val soundManager = remember { AviatorSoundManager() }
    var isSoundEnabled by remember { mutableStateOf(true) }
    var isMusicEnabled by remember { mutableStateOf(true) }

    DisposableEffect(isOpen) {
        onDispose {
            soundManager.release()
        }
    }

    // ================= REAL-TIME SYNCHRONIZED MULTIPLAYER LOOP =================
    var activeRoundNumber by remember { mutableLongStateOf(0L) }
    var previousPhase by remember { mutableStateOf<AviatorGamePhase?>(null) }

    LaunchedEffect(isOpen) {
        while (isActive) {
            val snapshot = SynchronizedAviatorEngine.getCurrentGlobalSnapshot()
            val isNewRound = snapshot.roundNumber != activeRoundNumber
            val isPhaseTransition = snapshot.phase != previousPhase

            if (isNewRound) {
                activeRoundNumber = snapshot.roundNumber
                p1CashedWin = 0.0
                p1CashedMult = 1.0
                p2CashedWin = 0.0
                p2CashedMult = 1.0
                lastUserCashOutMult = 0.0
                lastUserCashOutWin = 0.0
                toastMessage = null
            }

            // Sync global state values across all devices
            currentPhase = snapshot.phase
            currentMultiplier = snapshot.currentMultiplier
            crashPoint = snapshot.crashMultiplier
            waitingCountdownSeconds = snapshot.waitingSecondsRemaining
            waitingProgress = snapshot.waitingProgress
            flightDurationMs = snapshot.flightElapsedMs
            activeOnlineUsers = snapshot.activeOnlineUsers
            historyList = snapshot.historyList
            livePlayers = snapshot.livePlayers

            // Synchronized Phase Transition Events
            if (isPhaseTransition || isNewRound) {
                when (snapshot.phase) {
                    AviatorGamePhase.WAITING -> {
                        soundManager.stopFlightEngine()

                        // Panel 1 auto-transition for WAITING
                        if (p1AutoBet && p1State == PanelBetState.IDLE && p1Amount >= 50.0 && currentBalance >= p1Amount) {
                            p1State = PanelBetState.WAITING_IN_ROUND
                            onBalanceChange(-p1Amount)
                        } else if (p1State == PanelBetState.QUEUED_FOR_NEXT) {
                            p1State = PanelBetState.WAITING_IN_ROUND
                        } else if (p1State != PanelBetState.WAITING_IN_ROUND) {
                            p1State = PanelBetState.IDLE
                        }

                        // Panel 2 auto-transition for WAITING
                        if (p2AutoBet && p2State == PanelBetState.IDLE && p2Amount >= 50.0 && currentBalance >= p2Amount) {
                            p2State = PanelBetState.WAITING_IN_ROUND
                            onBalanceChange(-p2Amount)
                        } else if (p2State == PanelBetState.QUEUED_FOR_NEXT) {
                            p2State = PanelBetState.WAITING_IN_ROUND
                        } else if (p2State != PanelBetState.WAITING_IN_ROUND) {
                            p2State = PanelBetState.IDLE
                        }
                    }
                    AviatorGamePhase.FLYING -> {
                        soundManager.startFlightEngine()
                        if (p1State == PanelBetState.WAITING_IN_ROUND) {
                            p1State = PanelBetState.ACTIVE_FLYING
                        }
                        if (p2State == PanelBetState.WAITING_IN_ROUND) {
                            p2State = PanelBetState.ACTIVE_FLYING
                        }
                    }
                    AviatorGamePhase.CRASHED -> {
                        soundManager.stopFlightEngine()
                        soundManager.playCrashSound()
                        if (p1State == PanelBetState.ACTIVE_FLYING) {
                            p1State = PanelBetState.LOST
                            myPastBets = listOf(UserPastBet("Now", p1Amount, snapshot.crashMultiplier, 0.0, false)) + myPastBets
                        }
                        if (p2State == PanelBetState.ACTIVE_FLYING) {
                            p2State = PanelBetState.LOST
                            myPastBets = listOf(UserPastBet("Now", p2Amount, snapshot.crashMultiplier, 0.0, false)) + myPastBets
                        }
                    }
                }
                previousPhase = snapshot.phase
            }

            // Continuous flight sound & auto-cashout checks during flight
            if (snapshot.phase == AviatorGamePhase.FLYING) {
                soundManager.updateFlightMultiplier(snapshot.currentMultiplier)

                // Panel 1 auto cashout
                if (p1State == PanelBetState.ACTIVE_FLYING && p1AutoCashOut && snapshot.currentMultiplier >= p1AutoTarget) {
                    val win = p1Amount * p1AutoTarget
                    p1CashedWin = win
                    p1CashedMult = p1AutoTarget
                    p1State = PanelBetState.CASHED_OUT
                    lastUserCashOutMult = p1AutoTarget
                    lastUserCashOutWin += win
                    soundManager.playCashOutSound()
                    onBalanceChange(win)
                    myPastBets = listOf(UserPastBet("Now", p1Amount, p1AutoTarget, win, true)) + myPastBets
                }

                // Panel 2 auto cashout
                if (p2State == PanelBetState.ACTIVE_FLYING && p2AutoCashOut && snapshot.currentMultiplier >= p2AutoTarget) {
                    val win = p2Amount * p2AutoTarget
                    p2CashedWin = win
                    p2CashedMult = p2AutoTarget
                    p2State = PanelBetState.CASHED_OUT
                    lastUserCashOutMult = p2AutoTarget
                    lastUserCashOutWin += win
                    soundManager.playCashOutSound()
                    onBalanceChange(win)
                    myPastBets = listOf(UserPastBet("Now", p2Amount, p2AutoTarget, win, true)) + myPastBets
                }
            }

            delay(30L)
        }
    }

    // Full-Screen Dialog with Pure Dark Background
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000))
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("spribe_aviator_game_container")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // ================= 1. TOP HEADER (BRANDING, DROPDOWN, BALANCE, MENU) =================
                AviatorExactTopHeader(
                    balance = currentBalance,
                    decimalFormat = decimalFormat,
                    isSoundEnabled = isSoundEnabled,
                    isMusicEnabled = isMusicEnabled,
                    onToggleSound = {
                        isSoundEnabled = !isSoundEnabled
                        soundManager.isSoundEnabled = isSoundEnabled
                    },
                    onToggleMusic = {
                        isMusicEnabled = !isMusicEnabled
                        soundManager.isMusicEnabled = isMusicEnabled
                    },
                    onClose = onDismiss,
                    language = language
                )

                // ================= 2. MULTIPLIER HISTORY ROW =================
                AviatorExactHistoryBar(historyList = historyList)

                // ================= 3. MAIN GAME SCREEN (RADIAL RAYS + PURPLE GLOW + RED MONOPLANE) =================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF07080B))
                        .border(1.dp, Color(0xFF191B24), RoundedCornerShape(16.dp))
                        .testTag("aviator_canvas_screen"),
                    contentAlignment = Alignment.Center
                ) {
                    // Exact Canvas with Radial Rays, Purple Atmosphere, Red Curtain & Propeller Plane
                    AviatorExactFlightCanvas(
                        phase = currentPhase,
                        multiplier = currentMultiplier,
                        flightDurationMs = flightDurationMs,
                        waitingProgress = waitingProgress,
                        customPlaneImageUrl = customPlaneImageUrl
                    )

                    // Big Center Multiplier or Game State
                    when (currentPhase) {
                        AviatorGamePhase.WAITING -> {
                            // Exact UFC OFFICIAL PARTNERS & SPRIBE Badge from photo_2026-09-12_18-08-23.jpg
                            UfcSpribeOfficialPartnerCard(
                                countdownSeconds = waitingCountdownSeconds,
                                waitingProgress = waitingProgress
                            )
                        }

                        AviatorGamePhase.FLYING -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (lastUserCashOutWin > 0.0 && lastUserCashOutMult > 1.0) {
                                    // Small compact Cashed Out badge only (big card removed per request)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF1B5E20).copy(alpha = 0.92f),
                                        border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                                        modifier = Modifier.testTag("aviator_cashed_out_small_badge")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (language == AppLanguage.BN)
                                                    "আপনি ক্যাশ আউট করেছেন %.2fx এ".format(Locale.US, lastUserCashOutMult)
                                                else
                                                    "YOU CASHED OUT AT %.2fx".format(Locale.US, lastUserCashOutMult),
                                                color = Color(0xFFC8E6C9),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(+৳%.2f)".format(Locale.US, lastUserCashOutWin),
                                                color = Color(0xFFFFD54F),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                // Exact Giant White Multiplier matching photo_2026-09-12_18-07-34.jpg: e.g. 1.12x
                                Text(
                                    text = "%.2fx".format(Locale.US, currentMultiplier),
                                    color = Color.White,
                                    fontSize = 58.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.SansSerif,
                                    letterSpacing = (-1.5).sp,
                                    modifier = Modifier.testTag("aviator_multiplier_text")
                                )
                            }
                        }

                        AviatorGamePhase.CRASHED -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.testTag("aviator_flew_away_overlay")
                            ) {
                                if (lastUserCashOutWin > 0.0 && lastUserCashOutMult > 1.0) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF1B5E20).copy(alpha = 0.92f),
                                        border = BorderStroke(1.dp, Color(0xFF4CAF50))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (language == AppLanguage.BN)
                                                    "আপনি ক্যাশ আউট করেছেন %.2fx এ".format(Locale.US, lastUserCashOutMult)
                                                else
                                                    "YOU CASHED OUT AT %.2fx".format(Locale.US, lastUserCashOutMult),
                                                color = Color(0xFFC8E6C9),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(+৳%.2f)".format(Locale.US, lastUserCashOutWin),
                                                color = Color(0xFFFFD54F),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                Text(
                                    text = if (language == AppLanguage.BN) "উড়ে গেছে!" else "FLEW AWAY!",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.SansSerif,
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "%.2fx".format(Locale.US, crashPoint),
                                    color = Color(0xFFE52338), // Exact bright red from user photo
                                    fontSize = 58.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.SansSerif,
                                    letterSpacing = (-1.5).sp
                                )
                            }
                        }
                    }

                    // Online Active Users Badge at Bottom-Right (Authentic 3 Overlapping Avatars + Users Count)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black.copy(alpha = 0.70f))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AuthenticOverlappingAvatars()

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "%,d".format(Locale.US, activeOnlineUsers),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Toast message
                    if (toastMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF10B981),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 10.dp)
                        ) {
                            Text(
                                text = toastMessage!!,
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // ================= 4. DUAL BETTING PANELS (PANEL 1 & PANEL 2) =================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // PANEL 1
                    AviatorExactBetPanel(
                        panelNumber = 1,
                        currentPhase = currentPhase,
                        currentMultiplier = currentMultiplier,
                        currentBalance = currentBalance,
                        mode = p1Mode,
                        onModeChange = { p1Mode = it },
                        amount = p1Amount,
                        onAmountChange = { p1Amount = it },
                        betState = p1State,
                        autoBet = p1AutoBet,
                        onAutoBetToggle = { p1AutoBet = it },
                        autoCashOut = p1AutoCashOut,
                        onAutoCashOutToggle = { p1AutoCashOut = it },
                        autoTarget = p1AutoTarget,
                        onAutoTargetChange = { p1AutoTarget = it },
                        showCollapseButton = false,
                        isCollapsed = false,
                        onToggleCollapse = {},
                        language = language,
                        onPlaceBet = {
                            if (p1Amount < 50.0) {
                                toastMessage = if (language == AppLanguage.BN) "সর্বনিম্ন বেট ৫০ টাকা!" else "Minimum bet is ৳50!"
                                return@AviatorExactBetPanel
                            }
                            if (currentBalance < 50.0) {
                                toastMessage = if (language == AppLanguage.BN) "বেট ধরার জন্য সর্বনিম্ন ৫০ টাকা ব্যালেন্স থাকতে হবে!" else "Minimum ৳50 balance required to place bet!"
                                return@AviatorExactBetPanel
                            }
                            if (currentBalance < p1Amount) {
                                toastMessage = if (language == AppLanguage.BN) "অপর্যাপ্ত ব্যালেন্স! ডিপোজিট করুন" else "Insufficient balance! Please deposit"
                                return@AviatorExactBetPanel
                            }
                            onBalanceChange(-p1Amount)
                            if (currentPhase == AviatorGamePhase.WAITING) {
                                p1State = PanelBetState.WAITING_IN_ROUND
                            } else {
                                p1State = PanelBetState.QUEUED_FOR_NEXT
                            }
                        },
                        onCancelBet = {
                            onBalanceChange(p1Amount)
                            p1State = PanelBetState.IDLE
                        },
                        onCashOut = {
                            val win = p1Amount * currentMultiplier
                            p1CashedWin = win
                            p1CashedMult = currentMultiplier
                            p1State = PanelBetState.CASHED_OUT
                            lastUserCashOutMult = currentMultiplier
                            lastUserCashOutWin += win
                            soundManager.playCashOutSound()
                            onBalanceChange(win)
                            myPastBets = listOf(UserPastBet("Now", p1Amount, currentMultiplier, win, true)) + myPastBets
                        }
                    )

                    // PANEL 2
                    AviatorExactBetPanel(
                        panelNumber = 2,
                        currentPhase = currentPhase,
                        currentMultiplier = currentMultiplier,
                        currentBalance = currentBalance,
                        mode = p2Mode,
                        onModeChange = { p2Mode = it },
                        amount = p2Amount,
                        onAmountChange = { p2Amount = it },
                        betState = p2State,
                        autoBet = p2AutoBet,
                        onAutoBetToggle = { p2AutoBet = it },
                        autoCashOut = p2AutoCashOut,
                        onAutoCashOutToggle = { p2AutoCashOut = it },
                        autoTarget = p2AutoTarget,
                        onAutoTargetChange = { p2AutoTarget = it },
                        showCollapseButton = true,
                        isCollapsed = p2Collapsed,
                        onToggleCollapse = { p2Collapsed = !p2Collapsed },
                        language = language,
                        onPlaceBet = {
                            if (p2Amount < 50.0) {
                                toastMessage = if (language == AppLanguage.BN) "সর্বনিম্ন বেট ৫০ টাকা!" else "Minimum bet is ৳50!"
                                return@AviatorExactBetPanel
                            }
                            if (currentBalance < 50.0) {
                                toastMessage = if (language == AppLanguage.BN) "বেট ধরার জন্য সর্বনিম্ন ৫০ টাকা ব্যালেন্স থাকতে হবে!" else "Minimum ৳50 balance required to place bet!"
                                return@AviatorExactBetPanel
                            }
                            if (currentBalance < p2Amount) {
                                toastMessage = if (language == AppLanguage.BN) "অপর্যাপ্ত ব্যালেন্স! ডিপোজিট করুন" else "Insufficient balance! Please deposit"
                                return@AviatorExactBetPanel
                            }
                            onBalanceChange(-p2Amount)
                            if (currentPhase == AviatorGamePhase.WAITING) {
                                p2State = PanelBetState.WAITING_IN_ROUND
                            } else {
                                p2State = PanelBetState.QUEUED_FOR_NEXT
                            }
                        },
                        onCancelBet = {
                            onBalanceChange(p2Amount)
                            p2State = PanelBetState.IDLE
                        },
                        onCashOut = {
                            val win = p2Amount * currentMultiplier
                            p2CashedWin = win
                            p2CashedMult = currentMultiplier
                            p2State = PanelBetState.CASHED_OUT
                            lastUserCashOutMult = currentMultiplier
                            lastUserCashOutWin += win
                            soundManager.playCashOutSound()
                            onBalanceChange(win)
                            myPastBets = listOf(UserPastBet("Now", p2Amount, currentMultiplier, win, true)) + myPastBets
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // ================= 5. STATS SECTION (ALL BETS, PREVIOUS, TOP) =================
                AviatorExactStatsSection(
                    selectedTab = selectedStatsTab,
                    onSelectTab = { selectedStatsTab = it },
                    livePlayers = livePlayers,
                    myPastBets = myPastBets,
                    language = language
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Exact Top Header matching the reference photo:
 * Left: Italic Red "Aviator"
 * Center: Dark Blue Pill with Down Triangle ▼
 * Right: "0.00 BDT" in fluorescent green + "☰" hamburger menu/close
 */
@Composable
private fun AviatorExactTopHeader(
    balance: Double,
    decimalFormat: DecimalFormat,
    isSoundEnabled: Boolean,
    isMusicEnabled: Boolean,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit,
    onClose: () -> Unit,
    language: AppLanguage = AppLanguage.EN
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFF000000),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Aviator Branding
            Text(
                text = "Aviator",
                color = Color(0xFFE52338),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-0.5).sp
            )

            // Center: Dark Blue Pill with Downward Arrow ▼
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier.clickable { /* info / provably fair dialog */ }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "▼",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }

            // Right: Sound Button + Green Balance "0.00 BDT" + Hamburger Menu
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Quick Sound Toggle (SFX)
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("toggle_sound_button")
                ) {
                    Icon(
                        imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (isSoundEnabled) "Mute Sound" else "Unmute Sound",
                        tint = if (isSoundEnabled) Color(0xFF22C55E) else Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "${decimalFormat.format(balance)} BDT",
                    color = Color(0xFF22C55E), // Bright fluorescent green
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Spribe Settings Hamburger / Menu button with Dropdown
                Box {
                    IconButton(
                        onClick = { showMenu = !showMenu },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("spribe_aviator_menu")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu / Settings",
                            tint = Color(0xFF8E9297),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Authentic Spribe Aviator Dropdown Menu (as seen on tk999v.com & Spribe Aviator)
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(Color(0xFF1B1D29))
                            .border(1.dp, Color(0xFF2B3144), RoundedCornerShape(8.dp))
                    ) {
                        // Sound Toggle item
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(if (language == AppLanguage.BN) "সাউন্ড" else "Sound", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Switch(
                                        checked = isSoundEnabled,
                                        onCheckedChange = { onToggleSound() },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF22C55E),
                                            uncheckedThumbColor = Color(0xFF94A3B8),
                                            uncheckedTrackColor = Color(0xFF334155)
                                        ),
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            },
                            onClick = { onToggleSound() }
                        )

                        // Music Toggle item
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(if (language == AppLanguage.BN) "মিউজিক" else "Music", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Switch(
                                        checked = isMusicEnabled,
                                        onCheckedChange = { onToggleMusic() },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF22C55E),
                                            uncheckedThumbColor = Color(0xFF94A3B8),
                                            uncheckedTrackColor = Color(0xFF334155)
                                        ),
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            },
                            onClick = { onToggleMusic() }
                        )

                        HorizontalDivider(color = Color(0xFF2B3144), thickness = 0.5.dp)

                        // Exit / Close Game Item
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(if (language == AppLanguage.BN) "গেম থেকে বের হন" else "Exit Game", color = Color(0xFFEF4444), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                showMenu = false
                                onClose()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Multiplier History Bar matching the photo:
 * Clean, colored text pills:
 * < 2.00x: Blue (1.03x, 1.85x)
 * 2.00x - 9.99x: Purple (2.34x, 4.99x, 2.04x, 2.24x, 5.01x, 2.05x)
 * 10.00x+: Hot Pink / Magenta
 * Followed by '...' button at the end
 */
@Composable
private fun AviatorExactHistoryBar(historyList: List<AviatorHistoryItem>) {
    Surface(
        color = Color(0xFF000000),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(historyList, key = { it.id }) { item ->
                    val mult = item.multiplier
                    val textColor = when {
                        mult >= 10.0 -> Color(0xFFE91E63) // Magenta / Pink / Hot
                        mult >= 2.0 -> Color(0xFF9B59B6)  // Purple (exact from photo)
                        else -> Color(0xFF3498DB)         // Blue / Cyan (exact from photo)
                    }

                    Text(
                        text = "%.2fx".format(Locale.US, mult),
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Three dots button '...'
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1A1C23),
                modifier = Modifier.clickable { /* history stats */ }
            ) {
                Text(
                    text = "...",
                    color = Color(0xFF7E8494),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Exact Canvas implementation matching the screenshot:
 * 1. Alternating dark radial rays / sunburst
 * 2. Vibrant purple atmospheric glow in the upper-center
 * 3. Aerodynamic flight curve
 * 4. Red curtain fill dropping STRAIGHT down under the plane
 * 5. Authentic red propeller monoplane silhouette
 */
@Composable
private fun AviatorExactFlightCanvas(
    phase: AviatorGamePhase,
    multiplier: Double,
    flightDurationMs: Long,
    waitingProgress: Float,
    customPlaneImageUrl: String = ""
) {
    val infiniteTransition = rememberInfiniteTransition(label = "canvas_anims")

    // Slow radial ray rotation
    val rayAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rays"
    )

    // Propeller spinning animation
    val propellerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "propeller"
    )

    val progress = remember(multiplier, phase) {
        if (phase != AviatorGamePhase.FLYING && phase != AviatorGamePhase.CRASHED) 0f
        else ((multiplier - 1.0) / 7.0).coerceIn(0.0, 1.0).toFloat()
    }

    val planePainter = if (customPlaneImageUrl.isNotBlank()) {
        rememberAsyncImagePainter(model = customPlaneImageUrl)
    } else null

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val wPx = constraints.maxWidth.toFloat()
        val hPx = constraints.maxHeight.toFloat()

        val startX = 45f
        val startY = hPx - 35f

        val planeX = startX + (wPx - 110f) * progress
        val planeY = startY - (hPx - 85f) * (progress * 0.82f + progress * progress * 0.18f)
        val planeAngle = -18f * (1f - progress * 0.25f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw Radial Rays (Sunburst effect) centered at bottom-left
            val rayCenter = Offset(startXCoord(), h - 20f)
            rotate(degrees = rayAngle, pivot = rayCenter) {
                val numRays = 18
                for (i in 0 until numRays) {
                    val angle = (i * (360f / numRays)) * (Math.PI / 180.0)
                    val rayLength = w * 1.8f
                    val endX = (rayCenter.x + rayLength * cos(angle)).toFloat()
                    val endY = (rayCenter.y + rayLength * sin(angle)).toFloat()
                    drawLine(
                        color = Color(0xFF14161F).copy(alpha = 0.35f),
                        start = rayCenter,
                        end = Offset(endX, endY),
                        strokeWidth = 24f
                    )
                }
            }

            // 2. Cyan / Blue Atmospheric Glow (as seen in photo_2026-09-12_18-07-34.jpg during flight)
            if (phase == AviatorGamePhase.FLYING) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF024873).copy(alpha = 0.55f),
                            Color(0xFF01243A).copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.50f, h * 0.38f),
                        radius = w * 0.48f
                    ),
                    center = Offset(w * 0.50f, h * 0.38f),
                    radius = w * 0.48f
                )
            }

            // 3. Flight Path, Red Curtain & Default Airplane
            when (phase) {
                AviatorGamePhase.WAITING -> {
                    if (planePainter == null) {
                        drawExactSpribeAirplane(
                            center = Offset(startX + 28f, startY - 14f),
                            angleDegrees = 0f,
                            propellerAngle = propellerAngle,
                            scale = 1.9f
                        )
                    }
                }

                AviatorGamePhase.FLYING -> {
                    // Curved trajectory path
                    val curvePath = Path().apply {
                        moveTo(startX, startY)
                        quadraticBezierTo(
                            startX + (planeX - startX) * 0.45f,
                            startY,
                            planeX,
                            planeY
                        )
                    }

                    // RED CURTAIN: Exact straight vertical drop under the plane as in user's photo & video!
                    val curtainPath = Path().apply {
                        moveTo(startX, startY)
                        quadraticBezierTo(
                            startX + (planeX - startX) * 0.45f,
                            startY,
                            planeX,
                            planeY
                        )
                        lineTo(planeX, startY)
                        lineTo(startX, startY)
                        close()
                    }

                    // Fill red curtain with vertical gradient
                    val curtainGradient = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE52338).copy(alpha = 0.60f),
                            Color(0xFFE52338).copy(alpha = 0.18f)
                        ),
                        startY = planeY,
                        endY = startY
                    )
                    drawPath(curtainPath, brush = curtainGradient)

                    // Red glowing trajectory line
                    drawPath(
                        path = curvePath,
                        color = Color(0xFFE52338),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Default Spribe Red Propeller Airplane if no custom image is configured
                    if (planePainter == null) {
                        drawExactSpribeAirplane(
                            center = Offset(planeX, planeY),
                            angleDegrees = planeAngle,
                            propellerAngle = propellerAngle,
                            scale = 1.9f
                        )
                    }
                }

                AviatorGamePhase.CRASHED -> {
                    // Airplane flew away!
                }
            }
        }

        // Overlay Custom Airplane Image uploaded via Admin panel
        if (planePainter != null && (phase == AviatorGamePhase.WAITING || phase == AviatorGamePhase.FLYING)) {
            val (curX, curY, curAng) = when (phase) {
                AviatorGamePhase.WAITING -> Triple(startX + 28f, startY - 14f, 0f)
                AviatorGamePhase.FLYING -> Triple(planeX, planeY, planeAngle)
                AviatorGamePhase.CRASHED -> Triple(0f, 0f, 0f)
            }

            val planeSize = 68.dp
            val xOffsetDp = with(density) { (curX - 34f).toDp() }
            val yOffsetDp = with(density) { (curY - 34f).toDp() }

            Image(
                painter = planePainter,
                contentDescription = "Custom Aviator Aircraft",
                modifier = Modifier
                    .size(planeSize)
                    .offset(x = xOffsetDp, y = yOffsetDp)
                    .graphicsLayer(rotationZ = curAng),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Draws the iconic Spribe Red Propeller Airplane with optional scale:
 * - Red aerodynamic fuselage with "X" signature brace
 * - Elevated wing and tail fin
 * - Spinning propeller blades at the nose with motion blur
 */
private fun DrawScope.drawExactSpribeAirplane(
    center: Offset,
    angleDegrees: Float,
    propellerAngle: Float,
    scale: Float = 1.0f
) {
    scale(scale = scale, pivot = center) {
        rotate(degrees = angleDegrees, pivot = center) {
            val length = 52f

            // Main Fuselage (Body) in Bright Crimson Red
            val bodyPath = Path().apply {
                moveTo(center.x - length * 0.45f, center.y + 2f)
                lineTo(center.x + length * 0.40f, center.y - 2f)
                lineTo(center.x + length * 0.48f, center.y + 1f)
                lineTo(center.x + length * 0.35f, center.y + 7f)
                lineTo(center.x - length * 0.40f, center.y + 5f)
                close()
            }
            drawPath(bodyPath, color = Color(0xFFE52338))

            // Aerodynamic lower fuselage shading
            val bellyPath = Path().apply {
                moveTo(center.x - length * 0.38f, center.y + 4f)
                lineTo(center.x + length * 0.35f, center.y + 6f)
                lineTo(center.x + length * 0.25f, center.y + 8f)
                lineTo(center.x - length * 0.30f, center.y + 7f)
                close()
            }
            drawPath(bellyPath, color = Color(0xFFB71C1C))

            // Cockpit canopy (cyan tinted glass with white glare streak)
            val cockpitPath = Path().apply {
                moveTo(center.x - 2f, center.y - 2f)
                lineTo(center.x + 15f, center.y - 3f)
                lineTo(center.x + 8f, center.y - 9f)
                close()
            }
            drawPath(cockpitPath, color = Color(0xFF4DD0E1).copy(alpha = 0.90f))
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(center.x + 2f, center.y - 4f),
                end = Offset(center.x + 10f, center.y - 4f),
                strokeWidth = 1.5f
            )

            // Wings with "X" signature brace
            val wingPath = Path().apply {
                moveTo(center.x - 4f, center.y - 18f)
                lineTo(center.x + 18f, center.y - 13f)
                lineTo(center.x + 8f, center.y + 9f)
                lineTo(center.x - 12f, center.y + 5f)
                close()
            }
            drawPath(wingPath, color = Color(0xFFD32F2F))

            // "X" marking on wing/body
            drawLine(
                color = Color.White.copy(alpha = 0.95f),
                start = Offset(center.x + 1f, center.y - 9f),
                end = Offset(center.x + 11f, center.y + 2f),
                strokeWidth = 2.2f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.95f),
                start = Offset(center.x + 11f, center.y - 9f),
                end = Offset(center.x + 1f, center.y + 2f),
                strokeWidth = 2.2f
            )

            // Tail Fin (Angled up at the back)
            val tailPath = Path().apply {
                moveTo(center.x - length * 0.45f, center.y + 2f)
                lineTo(center.x - length * 0.54f, center.y - 16f)
                lineTo(center.x - length * 0.36f, center.y - 9f)
                close()
            }
            drawPath(tailPath, color = Color(0xFFB71C1C))

            // Landing gear wheel strut
            drawLine(
                color = Color(0xFF37474F),
                start = Offset(center.x + 4f, center.y + 6f),
                end = Offset(center.x + 4f, center.y + 12f),
                strokeWidth = 2f
            )
            drawCircle(
                color = Color(0xFF1E293B),
                radius = 3f,
                center = Offset(center.x + 4f, center.y + 13f)
            )

            // Propeller Nose Hub & Spinning Blades
            val propHub = Offset(center.x + length * 0.48f, center.y + 1f)
            // Propeller spinning motion blur disc
            drawCircle(color = Color.White.copy(alpha = 0.18f), radius = 17f, center = propHub)

            drawCircle(color = Color(0xFF111827), radius = 4f, center = propHub)
            drawCircle(color = Color(0xFFFFD54F), radius = 2f, center = propHub)

            rotate(degrees = propellerAngle, pivot = propHub) {
                drawLine(
                    color = Color.White,
                    start = Offset(propHub.x, propHub.y - 17f),
                    end = Offset(propHub.x, propHub.y + 17f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Compact UFC OFFICIAL PARTNER & Aviator branding card displayed during WAITING phase
 * neatly taking less vertical space on screen.
 */
@Composable
private fun UfcSpribeOfficialPartnerCard(
    countdownSeconds: Int,
    waitingProgress: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("ufc_official_partner_screen")
    ) {
        // 1. Top Row: UFC | Divider | Plane & Aviator (Compact & clean)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // UFC in bold italic red
            Text(
                text = "UFC",
                color = Color(0xFFE52338),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Thin vertical divider line
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(Color(0xFF707584))
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Airplane and Aviator text underneath
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Red Stunt Propeller Monoplane
                Canvas(modifier = Modifier.size(38.dp, 20.dp)) {
                    drawExactSpribeAirplane(
                        center = Offset(size.width * 0.45f, size.height * 0.50f),
                        angleDegrees = -18f,
                        propellerAngle = 35f,
                        scale = 0.70f
                    )
                }
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "Aviator",
                    color = Color(0xFFE52338),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. "OFFICIAL PARTNERS" in bold white caps
        Text(
            text = "OFFICIAL PARTNERS",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Compact Horizontal Loading Progress Bar
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF262933))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(waitingProgress)
                    .background(Color(0xFFE52338))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Standalone Dark Olive SPRIBE Card (Compact and sleek)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF132214),
            border = BorderStroke(1.dp, Color(0xFF335334)),
            shadowElevation = 4.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // Dotted spiral logo + SPRIBE
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Canvas(modifier = Modifier.size(14.dp)) {
                        val centerPt = Offset(size.width / 2f, size.height / 2f)
                        val r = size.minDimension / 2.4f
                        for (i in 0 until 16) {
                            val a = i * (Math.PI / 8.0)
                            val dotR = r * (0.4f + 0.6f * (i / 16f))
                            val dx = centerPt.x + dotR * cos(a).toFloat()
                            val dy = centerPt.y + dotR * sin(a).toFloat()
                            drawCircle(
                                color = Color.White.copy(alpha = 0.5f + 0.5f * (i / 16f)),
                                radius = 1.0.dp.toPx(),
                                center = Offset(dx, dy)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "SPRIBE",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Pill: Official Game ✔
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0C190D),
                    border = BorderStroke(0.8.dp, Color(0xFF2E6B30))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Official Game",
                            color = Color(0xFF4CAF50),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✔",
                                color = Color.White,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Since 2019",
                    color = Color(0xFF7E8E7E),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 3 Authentic Overlapping Avatars matching the photo:
 * 1. Red Racing Helmet with white racing stripe and visor
 * 2. Dark avatar with cool sunglasses
 * 3. Green gamer / clover avatar
 */
@Composable
private fun AuthenticOverlappingAvatars() {
    Box(modifier = Modifier.width(42.dp).height(18.dp)) {
        // Avatar 1: Red Racing Helmet
        Canvas(
            modifier = Modifier
                .size(18.dp)
                .offset(x = 0.dp)
        ) {
            drawCircle(color = Color(0xFF111216), radius = 9.dp.toPx())
            drawCircle(color = Color(0xFFD32F2F), radius = 8.dp.toPx())
            // White racing stripe
            drawLine(
                color = Color.White,
                start = Offset(size.width * 0.45f, 2.dp.toPx()),
                end = Offset(size.width * 0.45f, size.height - 2.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
            // Visor
            drawRoundRect(
                color = Color(0xFF1A1A1A),
                topLeft = Offset(size.width * 0.35f, size.height * 0.4f),
                size = Size(size.width * 0.55f, size.height * 0.28f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
            )
        }

        // Avatar 2: Cool Sunglasses
        Canvas(
            modifier = Modifier
                .size(18.dp)
                .offset(x = 12.dp)
        ) {
            drawCircle(color = Color(0xFF111216), radius = 9.dp.toPx())
            drawCircle(color = Color(0xFF37474F), radius = 8.dp.toPx())
            // Sunglasses
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(size.width * 0.25f, size.height * 0.4f),
                size = Size(size.width * 0.25f, size.height * 0.22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
            )
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(size.width * 0.55f, size.height * 0.4f),
                size = Size(size.width * 0.25f, size.height * 0.22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
            )
        }

        // Avatar 3: Green Gamer Helmet / Clover
        Canvas(
            modifier = Modifier
                .size(18.dp)
                .offset(x = 24.dp)
        ) {
            drawCircle(color = Color(0xFF111216), radius = 9.dp.toPx())
            drawCircle(color = Color(0xFF2E7D32), radius = 8.dp.toPx())
            // Lucky icon / visor
            drawCircle(
                color = Color(0xFF81C784),
                center = Offset(size.width * 0.5f, size.height * 0.5f),
                radius = 3.5.dp.toPx()
            )
        }
    }
}

/**
 * Exact Betting Panel matching the photo:
 * - Top: 'Bet' and 'Auto' capsule switch (+ optional collapse button on right)
 * - Controls:
 *   Left side:
 *     - Stepper [-] 1.00 [+]
 *     - 2x2 grid of quick amounts:
 *       100     200
 *       500     10,000
 *   Right side:
 *     - Huge vibrant green 'Bet 1.00 BDT' button
 */
@Composable
private fun AviatorExactBetPanel(
    panelNumber: Int,
    currentPhase: AviatorGamePhase,
    currentMultiplier: Double,
    currentBalance: Double,
    mode: String,
    onModeChange: (String) -> Unit,
    amount: Double,
    onAmountChange: (Double) -> Unit,
    betState: PanelBetState,
    autoBet: Boolean,
    onAutoBetToggle: (Boolean) -> Unit,
    autoCashOut: Boolean,
    onAutoCashOutToggle: (Boolean) -> Unit,
    autoTarget: Double,
    onAutoTargetChange: (Double) -> Unit,
    showCollapseButton: Boolean,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onPlaceBet: () -> Unit,
    onCancelBet: () -> Unit,
    onCashOut: () -> Unit,
    language: AppLanguage = AppLanguage.EN
) {
    var showCustomBetDialog by remember { mutableStateOf(false) }
    var customBetInputText by remember { mutableStateOf("") }

    if (showCustomBetDialog) {
        Dialog(
            onDismissRequest = { showCustomBetDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.70f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 380.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161820)),
                    border = BorderStroke(1.dp, Color(0xFFE52338).copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.BN) "কাস্টম বেট এমাউন্ট" else "Custom Bet Amount",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showCustomBetDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8E929E))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (language == AppLanguage.BN)
                                "সর্বনিম্ন বেট ৫০ টাকা। আপনার ইচ্ছামতো যেকোনো অ্যামাউন্ট লিখুন:"
                            else
                                "Minimum bet ৳50. Enter your custom bet amount:",
                            color = Color(0xFFB0B4C0),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customBetInputText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() || it == '.' }) {
                                    customBetInputText = input
                                }
                            },
                            placeholder = {
                                Text(
                                    if (language == AppLanguage.BN) "যেমন: 50, 150, 300, 750..." else "e.g. 50, 150, 300, 750...",
                                    color = Color(0xFF5A5E6B)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_bet_input_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE52338),
                                unfocusedBorderColor = Color(0xFF333742),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick helper chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(50.0, 100.0, 250.0, 500.0, 1000.0).forEach { chipAmt ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF222530),
                                    border = BorderStroke(0.8.dp, Color(0xFF373B4A)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            customBetInputText = "%.0f".format(chipAmt)
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "৳%.0f".format(chipAmt),
                                            color = Color(0xFFE2E4EB),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val entered = customBetInputText.toDoubleOrNull() ?: 50.0
                                val finalAmt = entered.coerceAtLeast(50.0)
                                onAmountChange(finalAmt)
                                showCustomBetDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("confirm_custom_bet_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF22C55E),
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = if (language == AppLanguage.BN) "বেট এমাউন্ট কনফার্ম করুন" else "Confirm Bet Amount",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("aviator_bet_panel_$panelNumber"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141518)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF22242C), Color(0xFF16171B))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // TOP ROW: Capsule Switch with 'Bet' and 'Auto' (+ Collapse button in panel 2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Capsule Switch: Bet | Auto
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0D0E12))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf("Bet", "Auto").forEach { tab ->
                        val isSelected = mode == tab
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Color(0xFF25272F) else Color.Transparent,
                            modifier = Modifier
                                .clickable { onModeChange(tab) }
                                .padding(horizontal = 14.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tab,
                                color = if (isSelected) Color.White else Color(0xFF777A84),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                // Small collapse icon button in panel 2 (as seen in photo)
                if (showCollapseButton) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1C1E26),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onToggleCollapse() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isCollapsed) "+" else "−",
                                color = Color(0xFF777A84),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (!isCollapsed) {
                Spacer(modifier = Modifier.height(6.dp))

                // MAIN CONTROLS ROW: LEFT COLUMN (STEPPER + 2x2 PRESETS) & RIGHT COLUMN (BIG GREEN BET BUTTON)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // LEFT COLUMN
                    Column(
                        modifier = Modifier.weight(1.15f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Stepper Box [-] 50.00 [+] with direct click to customize
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0B0C0E),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF242630), Color(0xFF181A22))
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .padding(horizontal = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Circular Minus Button (Minimum 50 BDT)
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF262832),
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clickable {
                                            if (betState != PanelBetState.ACTIVE_FLYING && amount > 50.0) {
                                                onAmountChange((amount - 50.0).coerceAtLeast(50.0))
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("-", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Amount Display - Tapping opens custom amount dialog!
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            customBetInputText = "%.0f".format(amount)
                                            showCustomBetDialog = true
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "%.2f".format(Locale.US, amount),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                // Circular Plus Button (+50 BDT)
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF262832),
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clickable {
                                            if (betState != PanelBetState.ACTIVE_FLYING && amount <= 50000.0) {
                                                onAmountChange(amount + 50.0)
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // 2x2 Grid of Quick Amount Buttons (Minimum 50 BDT):
                        // Row 1: 50    100
                        // Row 2: 200   500
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                QuickPresetButton(label = "50", value = 50.0, currentAmount = amount, onSelect = onAmountChange, modifier = Modifier.weight(1f))
                                QuickPresetButton(label = "100", value = 100.0, currentAmount = amount, onSelect = onAmountChange, modifier = Modifier.weight(1f))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                QuickPresetButton(label = "200", value = 200.0, currentAmount = amount, onSelect = onAmountChange, modifier = Modifier.weight(1f))
                                QuickPresetButton(label = "500", value = 500.0, currentAmount = amount, onSelect = onAmountChange, modifier = Modifier.weight(1f))
                            }

                            // Dedicated Custom Amount Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E212B),
                                border = BorderStroke(0.8.dp, Color(0xFF333744)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .clickable {
                                        customBetInputText = "%.0f".format(amount)
                                        showCustomBetDialog = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (language == AppLanguage.BN) "কাস্টম বেট" else "Custom Bet",
                                        color = Color(0xFFFFB74D),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // RIGHT COLUMN: HUGE VIBRANT GREEN 'BET' OR ORANGE 'CASH OUT' BUTTON
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                    ) {
                        when {
                            // 1. ACTIVE FLIGHT CASH OUT STATE
                            currentPhase == AviatorGamePhase.FLYING && betState == PanelBetState.ACTIVE_FLYING -> {
                                val liveWinning = amount * currentMultiplier
                                Button(
                                    onClick = onCashOut,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("aviator_cashout_p$panelNumber"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD07A00), // Spribe signature golden orange
                                        contentColor = Color.White
                                    )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Cash Out",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "%.2f BDT".format(Locale.US, liveWinning),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFFFE082)
                                        )
                                    }
                                }
                            }

                            // 2. WAITING IN 5S COUNTDOWN (Can cancel)
                            betState == PanelBetState.WAITING_IN_ROUND -> {
                                Button(
                                    onClick = onCancelBet,
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFC62828),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(text = "Cancel", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "%.2f BDT".format(Locale.US, amount), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // 3. QUEUED FOR NEXT ROUND
                            betState == PanelBetState.QUEUED_FOR_NEXT -> {
                                Button(
                                    onClick = onCancelBet,
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE65100),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(text = "Waiting", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "Cancel ৳${amount.toInt()}", fontSize = 10.sp)
                                    }
                                }
                            }

                            // 4. CASHED OUT OR WON
                            betState == PanelBetState.CASHED_OUT -> {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF1B5E20),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(text = "CASHED OUT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        Text(text = "✓ WON", color = Color(0xFF81C784), fontSize = 13.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            // 5. DEFAULT STATE: BIG VIBRANT GREEN 'Bet 1.00 BDT' BUTTON (EXACT FROM PHOTO!)
                            else -> {
                                Button(
                                    onClick = onPlaceBet,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("aviator_bet_button_p$panelNumber"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF28A745), // Vibrant electric green
                                        contentColor = Color.White
                                    )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Bet",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "%.2f BDT".format(Locale.US, amount),
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // AUTO MODE CONTROLS
                if (mode == "Auto") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C0E14))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Auto Bet toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onAutoBetToggle(!autoBet) }
                        ) {
                            Checkbox(
                                checked = autoBet,
                                onCheckedChange = { onAutoBetToggle(it) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF28A745)),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Auto Bet", color = Color(0xFFCCCCCC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Auto Cash Out toggle & target
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onAutoCashOutToggle(!autoCashOut) }
                        ) {
                            Checkbox(
                                checked = autoCashOut,
                                onCheckedChange = { onAutoCashOutToggle(it) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF28A745)),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Auto Cash Out", color = Color(0xFFCCCCCC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1E222D),
                                modifier = Modifier.clickable {
                                    val next = when (autoTarget) {
                                        1.50 -> 2.00
                                        2.00 -> 3.00
                                        3.00 -> 5.00
                                        5.00 -> 10.00
                                        else -> 1.50
                                    }
                                    onAutoTargetChange(next)
                                }
                            ) {
                                Text(
                                    text = "%.2fx".format(Locale.US, autoTarget),
                                    color = Color(0xFF34B4FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Quick Preset Button (100, 200, 500, 10,000)
 */
@Composable
private fun QuickPresetButton(
    label: String,
    value: Double,
    currentAmount: Double,
    onSelect: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (currentAmount == value) Color(0xFF2B2D38) else Color(0xFF1C1D23),
        modifier = modifier
            .height(26.dp)
            .clickable { onSelect(value) }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (currentAmount == value) Color.White else Color(0xFFA0A4B0),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Bottom Statistics Section matching the photo:
 * - Capsule Tabs: 'All Bets' | 'Previous' | 'Top'
 * - Info Bar:
 *   Left: 3 Overlapping Avatars + "1235/2722 Bets"
 *   Right: "122,633.14" / "Total win BDT"
 * - Live Player Bets table
 */
@Composable
private fun AviatorExactStatsSection(
    selectedTab: String,
    onSelectTab: (String) -> Unit,
    livePlayers: List<LivePlayerBet>,
    myPastBets: List<UserPastBet>,
    language: AppLanguage = AppLanguage.EN
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111216)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF1D1F28), Color(0xFF131418))
            )
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Capsule Tabs: All Bets, Previous, Top (Bilingual display)
            val tabs = listOf(
                "All Bets" to if (language == AppLanguage.BN) "সব বেট" else "All Bets",
                "Previous" to if (language == AppLanguage.BN) "পূর্ববর্তী" else "Previous",
                "Top" to if (language == AppLanguage.BN) "সেরা" else "Top"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { (tabKey, tabLabel) ->
                    val isSelected = selectedTab == tabKey
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF22242B) else Color.Transparent,
                        modifier = Modifier
                            .clickable { onSelectTab(tabKey) }
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tabLabel,
                            color = if (isSelected) Color.White else Color(0xFF757D90),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // Exact Info Bar: 1235/2722 Bets | 122,633.14 Total win BDT
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: 3 Overlapping avatars + "1235/2722 Bets"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AuthenticOverlappingAvatars()
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.BN) "১২৩৫/২৭২২ বেট" else "1235/2722 Bets",
                            color = Color(0xFFCCCCCC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(45.dp)
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(Color(0xFF28A745))
                        )
                    }
                }

                // Right: "122,633.14" and "Total win BDT"
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "122,633.14",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (language == AppLanguage.BN) "সর্বমোট জয় BDT" else "Total win BDT",
                        color = Color(0xFF888888),
                        fontSize = 9.sp
                    )
                }
            }

            Divider(color = Color(0xFF1A1C24), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

            // Bets table list
            when (selectedTab) {
                "All Bets" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        livePlayers.take(6).forEach { p ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = p.username,
                                    color = Color(0xFFCCCCCC),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1.2f)
                                )
                                Text(
                                    text = "%.0f".format(Locale.US, p.betAmount),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (p.isCashedOut && p.cashOutMultiplier != null)
                                        "%.2fx".format(Locale.US, p.cashOutMultiplier)
                                    else "-",
                                    color = if (p.isCashedOut) Color(0xFF3498DB) else Color(0xFF666666),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.weight(0.8f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (p.isCashedOut) "+৳${p.winAmount.toInt()}" else "-",
                                    color = if (p.isCashedOut) Color(0xFF28A745) else Color(0xFF666666),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.weight(1.2f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }

                "Previous" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (myPastBets.isEmpty()) {
                            Text(
                                text = if (language == AppLanguage.BN) "কোন বিগত বেট নেই" else "No previous bets",
                                color = Color(0xFF757D90),
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            myPastBets.take(5).forEach { bet ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = bet.time, color = Color(0xFF999999), fontSize = 10.sp, modifier = Modifier.weight(1.2f))
                                    Text(text = "৳${bet.bet.toInt()}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                    Text(
                                        text = "%.2fx".format(Locale.US, bet.multiplier),
                                        color = if (bet.isWin) Color(0xFF9B59B6) else Color(0xFFE52338),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.weight(0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = if (bet.isWin) "+৳${bet.win.toInt()}" else "-৳${bet.bet.toInt()}",
                                        color = if (bet.isWin) Color(0xFF28A745) else Color(0xFFE52338),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.weight(1.2f),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }

                "Top" -> {
                    val topWins = remember {
                        listOf(
                            Triple("রাকিব_৮৮", 148.02, 14802.0),
                            Triple("আরিফ_রয়্যাল", 76.40, 38200.0),
                            Triple("শাকিল_কিং", 45.20, 22600.0),
                            Triple("নাসির_৭৭৭", 32.15, 16075.0),
                            Triple("মেহেদী_প্রো", 21.80, 10900.0)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        topWins.forEach { (user, mult, win) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = user, color = Color(0xFFEEEEEE), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                                Text(text = "৳500", color = Color(0xFF999999), fontSize = 10.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(
                                    text = "%.2fx".format(Locale.US, mult),
                                    color = Color(0xFFE91E63),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.weight(0.8f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "+৳${win.toInt()}",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.weight(1.2f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun startXCoord(): Float = 25f

private fun generateRealisticAviatorPlayers(): List<LivePlayerBet> {
    val sampleUsers = listOf(
        "t****7", "r****9", "s****2", "m****0", "k****5",
        "b****1", "j****8", "a****3", "h****6", "z****4"
    )
    return sampleUsers.mapIndexed { idx, user ->
        val betAmt = listOf(50.0, 100.0, 200.0, 500.0, 1000.0, 2000.0).random()
        val targetMult = (115..380).random() / 100.0
        LivePlayerBet(
            id = "p_$idx",
            username = user,
            betAmount = betAmt,
            cashOutMultiplier = targetMult,
            isCashedOut = false,
            winAmount = 0.0
        )
    }
}
