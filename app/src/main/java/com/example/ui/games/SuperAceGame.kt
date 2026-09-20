package com.example.ui.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.AppLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Card type enum for 5-reel Super Ace
enum class SuperAceCardType {
    ACE, KING, QUEEN, JACK, SPADE, HEART, DIAMOND, CLUB, WILD
}

data class SuperAceSlotItem(
    val type: SuperAceCardType,
    val isGolden: Boolean = false,
    val isTransformedWild: Boolean = false
)

@Composable
fun SuperAceGame(
    isOpen: Boolean,
    currentBalance: Double,
    language: AppLanguage,
    onBalanceChange: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    // Screen state: First show the Continue / Rules screen (IMG-20260920-WA0002.jpg)
    // Then upon tapping Continue, transition to Game screen (IMG-20260920-WA0001.jpg)
    var showContinueScreen by remember { mutableStateOf(true) }
    var dontShowNextTime by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (showContinueScreen) {
                SuperAceContinueScreen(
                    onContinue = { showContinueScreen = false },
                    dontShowNextTime = dontShowNextTime,
                    onToggleDontShow = { dontShowNextTime = !dontShowNextTime },
                    onClose = onDismiss
                )
            } else {
                SuperAcePlayScreen(
                    currentBalance = currentBalance,
                    onBalanceChange = onBalanceChange,
                    onClose = onDismiss
                )
            }
        }
    }
}

// ============================================================================
// SCREEN 1: Exact Super Ace Continue / Splash Screen (IMG-20260920-WA0002.jpg)
// ============================================================================
@Composable
private fun SuperAceContinueScreen(
    onContinue: () -> Unit,
    dontShowNextTime: Boolean,
    onToggleDontShow: () -> Unit,
    onClose: () -> Unit
) {
    // Green gradient background with radial light in center
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF26B03B),
                        Color(0xFF147A25),
                        Color(0xFF094E15),
                        Color(0xFF04280B)
                    ),
                    radius = 1200f
                )
            )
    ) {
        // Floating Gold Coins in the background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Coin 1 (Top Left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFEE55), Color(0xFFF59E0B), Color(0xFFB45309)),
                    center = Offset(width * 0.05f, height * 0.12f),
                    radius = 70f
                ),
                radius = 70f,
                center = Offset(width * 0.05f, height * 0.12f)
            )

            // Coin 2 (Top Right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFEE55), Color(0xFFF59E0B), Color(0xFFB45309)),
                    center = Offset(width * 0.88f, height * 0.07f),
                    radius = 65f
                ),
                radius = 65f,
                center = Offset(width * 0.88f, height * 0.07f)
            )

            // Coin 3 (Mid Right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFEE55), Color(0xFFF59E0B), Color(0xFFB45309)),
                    center = Offset(width * 0.95f, height * 0.20f),
                    radius = 55f
                ),
                radius = 55f,
                center = Offset(width * 0.95f, height * 0.20f)
            )

            // Coin 4 (Bottom Left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFEE55), Color(0xFFF59E0B), Color(0xFFB45309)),
                    center = Offset(width * 0.08f, height * 0.63f),
                    radius = 60f
                ),
                radius = 60f,
                center = Offset(width * 0.08f, height * 0.63f)
            )

            // Coin 5 (Bottom Right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFEE55), Color(0xFFF59E0B), Color(0xFFB45309)),
                    center = Offset(width * 0.92f, height * 0.68f),
                    radius = 65f
                ),
                radius = 65f,
                center = Offset(width * 0.92f, height * 0.68f)
            )

            // Coin 6 (Bottom Edge)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFEE55), Color(0xFFF59E0B), Color(0xFFB45309)),
                    center = Offset(width * 0.15f, height * 0.96f),
                    radius = 60f
                ),
                radius = 60f,
                center = Offset(width * 0.15f, height * 0.96f)
            )
        }

        // Top Status Header (Time, Signal, Close button)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Simulated Top status bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "10:09",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                // Notch capsule
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "▼",
                            color = Color(0xFF4A90E2),
                            fontSize = 8.sp
                        )
                    }
                }

                // Close button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SuperAce 3D Metallic Title
            SuperAceLogoText(fontSize = 38.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Volatility row: Volatility: [Flame indicators]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Volatility: ",
                    color = Color(0xFFE0E0B0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                // 2 Active flame indicators
                repeat(2) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Active Volatility",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(16.dp)
                    )
                }
                // 3 Inactive flame indicators
                repeat(3) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Inactive Volatility",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp).alpha(0.35f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Central Feature Showcase Card with Golden Poker -> WILD demonstration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Left Arrow
                Text(
                    text = "❮",
                    color = Color(0xFFFFD54F),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp)
                )

                // Showcase Board Frame
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .shadow(elevation = 20.dp, shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3B48)),
                    border = BorderStroke(2.5.dp, Color(0xFF4FA0B5))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Illustrated 4x4 Mini Reel demonstration
                        SuperAceMiniReelDemo()

                        Spacer(modifier = Modifier.height(10.dp))

                        // Explanatory Caption
                        Text(
                            text = "After the Golden poker wins,\nit will turn into wild in the same place.",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 6 Pagination dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            repeat(5) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.35f))
                                )
                            }
                        }
                    }
                }

                // Right Arrow
                Text(
                    text = "❯",
                    color = Color(0xFFFFD54F),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CONTINUE Button (Rich green gradient with glossy bevel)
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .height(52.dp)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0xFF00FF66))
                    .testTag("super_ace_continue_btn"),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2CB544)),
                border = BorderStroke(1.5.dp, Brush.verticalGradient(listOf(Color(0xFF8BFF9E), Color(0xFF147A25)))),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF48D361), Color(0xFF259E3C), Color(0xFF167B29))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Continue",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // "Don't show next time" checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onToggleDontShow() }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(3.dp))
                        .background(if (dontShowNextTime) Color(0xFF259E3C) else Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (dontShowNextTime) {
                        Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Don't show next time",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // White bottom drag handle
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.7f))
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Mini Reel Demo in the Continue screen showing King converting to Wild
@Composable
private fun SuperAceMiniReelDemo() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .background(Color(0xFF102833), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF285669), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Grid of sample cards
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Row 1
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MiniCard("K", "K", isGold = true)
                MiniCard("A", "♠")
                MiniCard("K", "K")
                MiniWildCrown()
                MiniCard("♦", "♦", isRed = true)
            }
            // Row 2
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MiniCard("A", "ACE")
                MiniCard("Q", "Q", isRed = true)
                MiniCard("Q", "Q", isRed = true)
                MiniCard("A", "ACE")
                MiniCard("♥", "♥", isRed = true)
            }
            // Row 3
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MiniCard("K", "K")
                MiniCard("K", "K")
                MiniWildCrown()
                MiniCard("Q", "Q", isRed = true)
                MiniWildCrown()
            }
        }

        // Overlay: Floating BIG Golden Wild Crown with Red curving arrows
        Box(
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.CenterStart)
                .offset(x = 42.dp, y = (-8).dp)
                .shadow(16.dp, RoundedCornerShape(8.dp), spotColor = Color(0xFFFFD54F)),
            contentAlignment = Alignment.Center
        ) {
            SuperAceWildCrownBadge(sizeDp = 68)
        }
    }
}

@Composable
private fun MiniCard(corner: String, symbol: String, isGold: Boolean = false, isRed: Boolean = false) {
    Card(
        modifier = Modifier
            .size(width = 44.dp, height = 48.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGold) Color(0xFFFFFBE8) else Color(0xFFFFFFFF)
        ),
        border = BorderStroke(if (isGold) 1.5.dp else 0.5.dp, if (isGold) Color(0xFFFFB300) else Color(0xFFCBD5E1))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (corner) {
                "K" -> Image(
                    painter = painterResource(id = R.drawable.card_king),
                    contentDescription = "King Card",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                "Q" -> Image(
                    painter = painterResource(id = R.drawable.card_queen),
                    contentDescription = "Queen Card",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                "J" -> Image(
                    painter = painterResource(id = R.drawable.card_jack),
                    contentDescription = "Jack Card",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                "A" -> Image(
                    painter = painterResource(id = R.drawable.card_ace),
                    contentDescription = "Ace Card",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                else -> {
                    Text(
                        text = corner,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRed) Color(0xFFD32F2F) else Color.Black,
                        modifier = Modifier.padding(2.dp).align(Alignment.TopStart)
                    )
                    Text(
                        text = symbol,
                        fontSize = 13.sp,
                        color = if (isRed) Color(0xFFD32F2F) else Color.Black,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniWildCrown() {
    Image(
        painter = painterResource(id = R.drawable.card_wild),
        contentDescription = "Wild Crown",
        modifier = Modifier.size(width = 44.dp, height = 48.dp),
        contentScale = ContentScale.FillBounds
    )
}

// JILI Wild Crown Badge (Red & Gold Crown with "WILD" banner)
@Composable
private fun SuperAceWildCrownBadge(sizeDp: Int) {
    Image(
        painter = painterResource(id = R.drawable.card_wild),
        contentDescription = "Wild Crown Badge",
        modifier = Modifier.size(sizeDp.dp),
        contentScale = ContentScale.FillBounds
    )
}

// SuperAce Metallic Text Logo
@Composable
private fun SuperAceLogoText(fontSize: androidx.compose.ui.unit.TextUnit) {
    Box(contentAlignment = Alignment.Center) {
        // Outline shadow
        Text(
            text = "SuperAce",
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 0.5.sp,
            color = Color.Black,
            modifier = Modifier.offset(x = 2.dp, y = 2.dp)
        )
        // Foreground metallic gold & white
        Text(
            text = "SuperAce",
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 0.5.sp,
            style = androidx.compose.ui.text.TextStyle(
                brush = Brush.verticalGradient(
                    listOf(Color.White, Color(0xFFFFF0B8), Color(0xFFFFC107), Color(0xFFD97706))
                )
            )
        )
    }
}


// ============================================================================
// SCREEN 2: Exact JILI Super Ace Gameplay Screen (IMG-20260920-WA0001.jpg)
// ============================================================================
@Composable
private fun SuperAcePlayScreen(
    currentBalance: Double,
    onBalanceChange: (Double) -> Unit,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // 5 Columns x 4 Rows = 20 Card Slots matching screenshot
    val defaultGrid = remember {
        listOf(
            // Row 1
            SuperAceSlotItem(SuperAceCardType.ACE),
            SuperAceSlotItem(SuperAceCardType.KING, isGolden = true), // Golden King as in screenshot!
            SuperAceSlotItem(SuperAceCardType.QUEEN),
            SuperAceSlotItem(SuperAceCardType.JACK),
            SuperAceSlotItem(SuperAceCardType.SPADE),
            // Row 2
            SuperAceSlotItem(SuperAceCardType.ACE),
            SuperAceSlotItem(SuperAceCardType.KING),
            SuperAceSlotItem(SuperAceCardType.QUEEN),
            SuperAceSlotItem(SuperAceCardType.JACK),
            SuperAceSlotItem(SuperAceCardType.SPADE),
            // Row 3
            SuperAceSlotItem(SuperAceCardType.ACE),
            SuperAceSlotItem(SuperAceCardType.KING),
            SuperAceSlotItem(SuperAceCardType.QUEEN),
            SuperAceSlotItem(SuperAceCardType.JACK),
            SuperAceSlotItem(SuperAceCardType.SPADE),
            // Row 4
            SuperAceSlotItem(SuperAceCardType.ACE),
            SuperAceSlotItem(SuperAceCardType.KING),
            SuperAceSlotItem(SuperAceCardType.QUEEN),
            SuperAceSlotItem(SuperAceCardType.JACK),
            SuperAceSlotItem(SuperAceCardType.SPADE)
        )
    }

    var gridCards by remember { mutableStateOf(defaultGrid) }
    var betAmount by remember { mutableStateOf(2.0) } // Default Bet 2 from screenshot!
    val availableBets = listOf(2.0, 5.0, 10.0, 20.0, 50.0, 100.0, 200.0, 500.0)

    var currentMultiplierIndex by remember { mutableStateOf(0) } // 0: x1, 1: x2, 2: x3, 3: x5
    var winAmount by remember { mutableStateOf(0.0) }
    var isSpinning by remember { mutableStateOf(false) }
    var isTurbo by remember { mutableStateOf(false) }
    var isAutoSpin by remember { mutableStateOf(false) }
    var showBuyBonusDialog by remember { mutableStateOf(false) }
    var messageBanner by remember { mutableStateOf("Eliminate Golden Cards to win WILD!") }

    // Spin animation rotation state for center gold spin button
    val spinButtonRotation = remember { Animatable(0f) }

    fun generateRandomItem(): SuperAceSlotItem {
        val types = listOf(
            SuperAceCardType.ACE,
            SuperAceCardType.KING,
            SuperAceCardType.QUEEN,
            SuperAceCardType.JACK,
            SuperAceCardType.SPADE,
            SuperAceCardType.HEART,
            SuperAceCardType.DIAMOND,
            SuperAceCardType.CLUB
        )
        val selectedType = types.random()
        val roll = Random.nextFloat()
        val isGold = roll < 0.20f && (selectedType == SuperAceCardType.KING || selectedType == SuperAceCardType.QUEEN || selectedType == SuperAceCardType.JACK || selectedType == SuperAceCardType.ACE)
        val isWild = roll > 0.94f
        return if (isWild) {
            SuperAceSlotItem(SuperAceCardType.WILD, isTransformedWild = true)
        } else {
            SuperAceSlotItem(selectedType, isGolden = isGold)
        }
    }

    fun executeSpin() {
        if (currentBalance < betAmount) {
            messageBanner = "Insufficient balance! Please deposit to play."
            isAutoSpin = false
            return
        }

        // Deduct bet from real balance
        onBalanceChange(-betAmount)

        isSpinning = true
        winAmount = 0.0
        currentMultiplierIndex = 0

        coroutineScope.launch {
            // Spin the button arrow
            launch {
                spinButtonRotation.animateTo(
                    spinButtonRotation.value + 360f * (if (isTurbo) 2 else 4),
                    animationSpec = tween(if (isTurbo) 400 else 900, easing = LinearEasing)
                )
            }

            // Shuffle reels animation
            val stepCount = if (isTurbo) 3 else 6
            val delayMs = if (isTurbo) 70L else 110L
            for (step in 1..stepCount) {
                gridCards = List(20) { generateRandomItem() }
                delay(delayMs)
            }

            // Final landed grid
            val finalGrid = List(20) { generateRandomItem() }
            gridCards = finalGrid

            // Payout calculation
            val isWin = Random.nextFloat() < 0.46f
            if (isWin) {
                val goldPokerCards = finalGrid.filter { it.isGolden }
                val wildCards = finalGrid.filter { it.type == SuperAceCardType.WILD }

                // Super Ace elimination cascade: Golden poker transforms to WILD!
                if (goldPokerCards.isNotEmpty()) {
                    delay(250)
                    gridCards = gridCards.map {
                        if (it.isGolden) it.copy(type = SuperAceCardType.WILD, isTransformedWild = true)
                        else it
                    }
                }

                currentMultiplierIndex = when {
                    goldPokerCards.size >= 2 || wildCards.size >= 2 -> 3 // x5
                    goldPokerCards.size >= 1 || wildCards.size >= 1 -> 2 // x3
                    finalGrid.count { it.type == SuperAceCardType.ACE } >= 4 -> 1 // x2
                    else -> 0 // x1
                }

                val multFactor = when (currentMultiplierIndex) {
                    3 -> 5.0
                    2 -> 3.0
                    1 -> 2.0
                    else -> 1.5
                }

                val payout = betAmount * multFactor
                winAmount = payout
                onBalanceChange(payout)
                messageBanner = "WIN ৳${String.format("%.2f", payout)}!"
            } else {
                currentMultiplierIndex = 0
                messageBanner = "Eliminate Golden Cards to win WILD!"
            }

            isSpinning = false

            // Auto-spin loop
            if (isAutoSpin && currentBalance >= betAmount) {
                delay(if (isTurbo) 400 else 800)
                executeSpin()
            } else {
                isAutoSpin = false
            }
        }
    }

    // Fullscreen Layout matching IMG-20260920-WA0001.jpg
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1824)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // -------------------------------------------------------------
        // 1. TOP HEADER: Status Bar + JILI Badge + BUY BONUS
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF070B10), Color(0xFF131D28))
                    )
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Dropdown arrow in top pill (interactive menu / exit)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1E2D3D),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clickable { onClose() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("▼", color = Color(0xFF5B88B2), fontSize = 10.sp)
                }
            }

            // Left: JILI Golden Metal Plate Badge & Close Button
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFFFD54F)
                    )
                }

                // Authentic JILI Vertical Gold Plate
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF4A3416),
                    border = BorderStroke(1.5.dp, Color(0xFFFFC107)),
                    modifier = Modifier.shadow(4.dp)
                ) {
                    Text(
                        text = "JILI",
                        color = Color(0xFFFFEE55),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Right: BUY BONUS 3D Sphere Badge
            Surface(
                shape = CircleShape,
                color = Color(0xFFC62828),
                border = BorderStroke(2.dp, Brush.radialGradient(listOf(Color(0xFFFFEE55), Color(0xFFF59E0B)))),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .shadow(8.dp, CircleShape, spotColor = Color(0xFFFF5252))
                    .clickable { showBuyBonusDialog = true }
                    .testTag("buy_bonus_btn")
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "BUY",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "BONUS",
                        color = Color(0xFFFFEE55),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 2. WOODEN SHELF & SUPER ACE MULTIPLIER BAR
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF3E1F11), Color(0xFF26130A), Color(0xFF160B06))
                    )
                )
                .padding(top = 4.dp, bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SuperAce 3D Logo
            SuperAceLogoText(fontSize = 24.sp)

            Spacer(modifier = Modifier.height(4.dp))

            // Multiplier Pill Capsule: [x1]  [x2]  [x3]  [x5]
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFE89A38),
                border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFBBF24), Color(0xFFD97706), Color(0xFFB45309))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val mults = listOf("x1", "x2", "x3", "x5")
                        mults.forEachIndexed { idx, mText ->
                            val isActive = idx == currentMultiplierIndex
                            Text(
                                text = mText,
                                fontSize = if (isActive) 20.sp else 17.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isActive) Color(0xFFB91C1C) else Color(0xFF291807),
                                modifier = Modifier
                                    .shadow(if (isActive) 4.dp else 0.dp)
                                    .then(
                                        if (isActive) Modifier.background(
                                            Color(0xFFFFEE55).copy(alpha = 0.4f),
                                            RoundedCornerShape(4.dp)
                                        ).padding(horizontal = 4.dp) else Modifier
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Ticker announcement bar
            Text(
                text = messageBanner,
                color = Color(0xFFCBD5E1),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }

        // -------------------------------------------------------------
        // 3. 5-REEL x 4-ROW CARD SLOT GRID (20 Cards)
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0C141E))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                userScrollEnabled = false
            ) {
                items(gridCards) { slot ->
                    SuperAceSlotCard(slot = slot)
                }
            }
        }

        // -------------------------------------------------------------
        // 4. WIN DISPLAY CONSOLE
        // -------------------------------------------------------------
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF121B27),
            border = BorderStroke(1.dp, Color(0xFF2A3D52))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WIN",
                    color = Color(0xFFFFD54F),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (winAmount > 0) String.format("%.3f", winAmount) else "0.000",
                    color = if (winAmount > 0) Color(0xFF00FF66) else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // -------------------------------------------------------------
        // 5. BOTTOM CONTROL DECK: Settings, Bet, Giant Gold Spin, Auto, Turbo
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E140F), Color(0xFF120C09), Color(0xFF0A0604))
                    )
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Settings Icon Button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF3D3242),
                    border = BorderStroke(1.dp, Color(0xFF5E4F63)),
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { showBuyBonusDialog = true }
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // 2. Bet Adjuster Button: (-) / (+) Coin with "Bet 2" underneath
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        if (!isSpinning) {
                            val currentIndex = availableBets.indexOf(betAmount)
                            val nextIndex = (currentIndex + 1) % availableBets.size
                            betAmount = availableBets[nextIndex]
                        }
                    }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1F4A30),
                        border = BorderStroke(1.5.dp, Color(0xFF2CB544)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "±",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Bet ${betAmount.toInt()}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 3. GIANT CENTER GOLDEN SPIN BUTTON
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFB300),
                    border = BorderStroke(3.dp, Brush.radialGradient(listOf(Color(0xFFFFEE55), Color(0xFFB45309)))),
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(16.dp, CircleShape, spotColor = Color(0xFFFFC107))
                        .clickable(enabled = !isSpinning) { executeSpin() }
                        .testTag("super_ace_main_spin_btn")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFEE55),
                                        Color(0xFFF59E0B),
                                        Color(0xFFD97706),
                                        Color(0xFF92400E)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Curved rotating arrow
                        Canvas(
                            modifier = Modifier
                                .size(58.dp)
                                .rotate(spinButtonRotation.value)
                        ) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(Color.White, Color.Transparent)
                                ),
                                startAngle = 30f,
                                sweepAngle = 260f,
                                useCenter = false,
                                style = Stroke(width = 7f, cap = StrokeCap.Round)
                            )
                        }

                        // Embossed "JILI" in golden relief
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFB45309),
                            border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "JILI",
                                    color = Color(0xFFFFEE55),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                // 4. Auto-Spin Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        isAutoSpin = !isAutoSpin
                        if (isAutoSpin && !isSpinning) executeSpin()
                    }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isAutoSpin) Color(0xFFD97706) else Color(0xFF3B281B),
                        border = BorderStroke(1.5.dp, if (isAutoSpin) Color(0xFFFFEE55) else Color(0xFF785434)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Auto",
                            tint = if (isAutoSpin) Color.White else Color(0xFFFFB300),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isAutoSpin) "AUTO ON" else "Auto",
                        color = if (isAutoSpin) Color(0xFF00FF66) else Color(0xFFCBD5E1),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 5. TURBO Spin Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { isTurbo = !isTurbo }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isTurbo) Color(0xFFDC2626) else Color(0xFF382319),
                        border = BorderStroke(1.5.dp, if (isTurbo) Color(0xFFFFEE55) else Color(0xFF785434)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Turbo",
                            tint = if (isTurbo) Color.White else Color(0xFFFFB300),
                            modifier = Modifier.padding(7.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isTurbo) "TURBO" else "Press turbo",
                        color = if (isTurbo) Color(0xFFFF5252) else Color(0xFF94A3B8),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 6. BOTTOMMOST FOOTER: v_186_0007 | LV0 | Balance 0.000 | WiFi
        // -------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF070B10))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Version
            Text(
                text = "v_186_0007",
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Center: LV0 pill + Real Balance
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = "LV0",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                Text(
                    text = "Balance  ৳${String.format("%.2f", currentBalance)}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Green WiFi Icon
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Connection",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(14.dp)
            )
        }
    }

    // BUY BONUS POPUP DIALOG
    if (showBuyBonusDialog) {
        AlertDialog(
            onDismissRequest = { showBuyBonusDialog = false },
            title = {
                Text(
                    text = "⭐ BUY BONUS FEATURE",
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column {
                    Text(
                        text = "Buy 10 Free Spins with guaranteed Golden Poker & Wild Cascade Multipliers!",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Price: ৳${(betAmount * 50).toInt()} (50x Bet)",
                        color = Color(0xFF00FF66),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cost = betAmount * 50
                        if (currentBalance >= cost) {
                            onBalanceChange(-cost)
                            showBuyBonusDialog = false
                            executeSpin()
                        } else {
                            messageBanner = "Insufficient balance for Buy Bonus!"
                            showBuyBonusDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("BUY NOW", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBuyBonusDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF131D28)
        )
    }
}

// ----------------------------------------------------------------------------
// Slot Card Composable (Ace, King, Queen, Jack, Spade, Wild)
// ----------------------------------------------------------------------------
@Composable
private fun SuperAceSlotCard(slot: SuperAceSlotItem) {
    val cardBg = when {
        slot.type == SuperAceCardType.KING -> Color(0xFFFFF7DA)
        slot.type == SuperAceCardType.QUEEN -> Color(0xFFF8FAFC)
        slot.isGolden -> Color(0xFFFFFBE8)
        else -> Color(0xFFFFFFFF)
    }
    val borderColor = when {
        slot.type == SuperAceCardType.WILD -> Color(0xFFFFB300)
        slot.isGolden || slot.type == SuperAceCardType.KING -> Color(0xFFFFB300)
        else -> Color(0xFFCBD5E1)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .shadow(
                elevation = if (slot.isGolden || slot.type == SuperAceCardType.WILD) 6.dp else 1.5.dp,
                shape = RoundedCornerShape(5.dp),
                spotColor = if (slot.isGolden) Color(0xFFFFC107) else Color.Transparent
            ),
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(if (slot.isGolden) 2.5.dp else if (slot.type == SuperAceCardType.KING) 2.dp else 1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (slot.type) {
                SuperAceCardType.ACE -> {
                    Image(
                        painter = painterResource(id = R.drawable.card_ace),
                        contentDescription = "Ace Card",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                SuperAceCardType.KING -> {
                    Image(
                        painter = painterResource(id = R.drawable.card_king),
                        contentDescription = "King Card",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                SuperAceCardType.QUEEN -> {
                    Image(
                        painter = painterResource(id = R.drawable.card_queen),
                        contentDescription = "Queen Card",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                SuperAceCardType.JACK -> {
                    Image(
                        painter = painterResource(id = R.drawable.card_jack),
                        contentDescription = "Jack Card",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                SuperAceCardType.SPADE -> {
                    CardSpadeIllustration(modifier = Modifier.fillMaxSize().padding(2.dp))
                }

                SuperAceCardType.HEART -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("♥", fontSize = 36.sp, color = Color(0xFFDC2626))
                    }
                }

                SuperAceCardType.DIAMOND -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("♦", fontSize = 36.sp, color = Color(0xFFDC2626))
                    }
                }

                SuperAceCardType.CLUB -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("♣", fontSize = 36.sp, color = Color(0xFF0F172A))
                    }
                }

                SuperAceCardType.WILD -> {
                    Image(
                        painter = painterResource(id = R.drawable.card_wild),
                        contentDescription = "Wild Card",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }
            }

            // Golden indicator badge for golden cards
            if (slot.isGolden && slot.type != SuperAceCardType.WILD) {
                Surface(
                    shape = RoundedCornerShape(bottomStart = 4.dp),
                    color = Color(0xFFFFB300),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "★",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 3.dp)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// Precise Vector Illustrations for A, K, Q, J, Spade, and Wild
// ----------------------------------------------------------------------------

@Composable
private fun CardAceIllustration(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize(0.85f)) {
            val w = size.width
            val h = size.height

            // Classical Spade Shape
            val spadePath = Path().apply {
                moveTo(w * 0.50f, h * 0.10f)
                cubicTo(w * 0.72f, h * 0.24f, w * 0.95f, h * 0.48f, w * 0.82f, h * 0.72f)
                cubicTo(w * 0.72f, h * 0.84f, w * 0.55f, h * 0.74f, w * 0.50f, h * 0.65f)
                cubicTo(w * 0.45f, h * 0.74f, w * 0.28f, h * 0.84f, w * 0.18f, h * 0.72f)
                cubicTo(w * 0.05f, h * 0.48f, w * 0.28f, h * 0.24f, w * 0.50f, h * 0.10f)
                close()
            }
            drawPath(
                spadePath,
                brush = Brush.verticalGradient(listOf(Color(0xFF2D3748), Color(0xFF1A202C), Color(0xFF0F172A)))
            )
            drawPath(spadePath, color = Color(0xFFFFD54F), style = Stroke(width = 2.2f))

            // Spade Pedestal Base
            val basePath = Path().apply {
                moveTo(w * 0.46f, h * 0.63f)
                lineTo(w * 0.32f, h * 0.94f)
                lineTo(w * 0.68f, h * 0.94f)
                lineTo(w * 0.54f, h * 0.63f)
                close()
            }
            drawPath(basePath, color = Color(0xFF0F172A))
            drawPath(basePath, color = Color(0xFFFFD54F), style = Stroke(width = 1.5f))
        }

        // Golden "ACE" Banner across middle of spade
        Surface(
            shape = RoundedCornerShape(3.dp),
            color = Color.Black,
            border = BorderStroke(1.2.dp, Color(0xFFFFD54F)),
            modifier = Modifier.offset(y = 2.dp)
        ) {
            Text(
                text = "ACE",
                color = Color(0xFFFFEE55),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.5.dp)
            )
        }
    }
}

@Composable
private fun CardKingIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Crown Body (Golden-Yellow, tall sharp peaks matching IMG-20260920-WA0004)
        val crownPath = Path().apply {
            moveTo(w * 0.35f, h * 0.36f)
            lineTo(w * 0.35f, h * 0.08f) // Left edge peak
            lineTo(w * 0.52f, h * 0.20f) // Valley 1
            lineTo(w * 0.67f, h * 0.06f) // Peak 2 (center tallest)
            lineTo(w * 0.82f, h * 0.20f) // Valley 2
            lineTo(w * 0.97f, h * 0.08f) // Peak 3 (right edge)
            lineTo(w * 0.97f, h * 0.36f) // Right edge bottom
            close()
        }
        drawPath(crownPath, color = Color(0xFFF59E0B)) // Rich golden yellow
        drawPath(crownPath, color = Color(0xFF1E293B), style = Stroke(width = 2.5f))

        // 4 Black Diamonds on Crown Body (◆ ◆ ◆ ◆)
        val diamondY = h * 0.26f
        val diamondXs = listOf(w * 0.43f, w * 0.57f, w * 0.72f, w * 0.86f)
        for (dx in diamondXs) {
            val dPath = Path().apply {
                moveTo(dx, diamondY - h * 0.035f)
                lineTo(dx + w * 0.035f, diamondY)
                lineTo(dx, diamondY + h * 0.035f)
                lineTo(dx - w * 0.035f, diamondY)
                close()
            }
            drawPath(dPath, color = Color(0xFF1E293B))
        }

        // Crown Base: Solid Bright Blue Band (Horizontal rectangle)
        val blueBand = Path().apply {
            moveTo(w * 0.35f, h * 0.30f)
            lineTo(w * 0.97f, h * 0.30f)
            lineTo(w * 0.97f, h * 0.37f)
            lineTo(w * 0.35f, h * 0.37f)
            close()
        }
        drawPath(blueBand, color = Color(0xFF1D4ED8))
        drawPath(blueBand, color = Color(0xFF1E293B), style = Stroke(width = 2f))

        // 2. Hair (Ochre / Brown)
        // Right side hair (vertical straight strands)
        val rightHairPath = Path().apply {
            moveTo(w * 0.77f, h * 0.37f)
            lineTo(w * 0.97f, h * 0.37f)
            lineTo(w * 0.97f, h * 0.72f)
            lineTo(w * 0.77f, h * 0.72f)
            close()
        }
        drawPath(rightHairPath, color = Color(0xFFB45309))
        drawPath(rightHairPath, color = Color(0xFF1E293B), style = Stroke(width = 2f))
        drawLine(Color(0xFF1E293B), Offset(w * 0.84f, h * 0.37f), Offset(w * 0.84f, h * 0.72f), strokeWidth = 1.5f)
        drawLine(Color(0xFF1E293B), Offset(w * 0.90f, h * 0.37f), Offset(w * 0.90f, h * 0.72f), strokeWidth = 1.5f)

        // Left side hair with distinctive scroll curl
        val leftHairPath = Path().apply {
            moveTo(w * 0.35f, h * 0.37f)
            lineTo(w * 0.42f, h * 0.37f)
            lineTo(w * 0.42f, h * 0.58f)
            cubicTo(w * 0.30f, h * 0.58f, w * 0.24f, h * 0.68f, w * 0.32f, h * 0.72f)
            cubicTo(w * 0.38f, h * 0.74f, w * 0.42f, h * 0.68f, w * 0.36f, h * 0.64f)
            cubicTo(w * 0.32f, h * 0.62f, w * 0.30f, h * 0.50f, w * 0.35f, h * 0.37f)
            close()
        }
        drawPath(leftHairPath, color = Color(0xFFB45309))
        drawPath(leftHairPath, color = Color(0xFF1E293B), style = Stroke(width = 2f))

        // 3. Face (Clean white with black line art)
        val facePath = Path().apply {
            moveTo(w * 0.41f, h * 0.37f)
            lineTo(w * 0.78f, h * 0.37f)
            lineTo(w * 0.78f, h * 0.58f)
            lineTo(w * 0.41f, h * 0.58f)
            close()
        }
        drawPath(facePath, color = Color.White)
        drawPath(facePath, color = Color(0xFF1E293B), style = Stroke(width = 2f))

        // Forehead line
        drawLine(Color(0xFF1E293B), Offset(w * 0.43f, h * 0.41f), Offset(w * 0.76f, h * 0.41f), strokeWidth = 1.5f)

        // Eyes (Classic playing card heavy-lidded eyes)
        // Left eye
        drawOval(Color(0xFF1E293B), topLeft = Offset(w * 0.46f, h * 0.44f), size = Size(w * 0.10f, h * 0.05f))
        drawOval(Color.White, topLeft = Offset(w * 0.47f, h * 0.455f), size = Size(w * 0.08f, h * 0.025f))
        drawCircle(Color(0xFF1E293B), radius = 2.5f, center = Offset(w * 0.51f, h * 0.465f))
        drawArc(Color(0xFF1E293B), 0f, 180f, false, Offset(w * 0.45f, h * 0.46f), Size(w * 0.12f, h * 0.04f), style = Stroke(width = 1.5f))

        // Right eye
        drawOval(Color(0xFF1E293B), topLeft = Offset(w * 0.63f, h * 0.44f), size = Size(w * 0.10f, h * 0.05f))
        drawOval(Color.White, topLeft = Offset(w * 0.64f, h * 0.455f), size = Size(w * 0.08f, h * 0.025f))
        drawCircle(Color(0xFF1E293B), radius = 2.5f, center = Offset(w * 0.68f, h * 0.465f))
        drawArc(Color(0xFF1E293B), 0f, 180f, false, Offset(w * 0.62f, h * 0.46f), Size(w * 0.12f, h * 0.04f), style = Stroke(width = 1.5f))

        // Nose (Long straight nose)
        val nosePath = Path().apply {
            moveTo(w * 0.59f, h * 0.42f)
            lineTo(w * 0.56f, h * 0.54f)
            lineTo(w * 0.63f, h * 0.54f)
        }
        drawPath(nosePath, color = Color(0xFF1E293B), style = Stroke(width = 2f, cap = StrokeCap.Round))

        // Mustache (White handlebar with black outline, curling out)
        val mustachePath = Path().apply {
            moveTo(w * 0.43f, h * 0.60f)
            cubicTo(w * 0.48f, h * 0.54f, w * 0.56f, h * 0.56f, w * 0.59f, h * 0.60f)
            cubicTo(w * 0.62f, h * 0.56f, w * 0.70f, h * 0.54f, w * 0.75f, h * 0.60f)
            cubicTo(w * 0.68f, h * 0.64f, w * 0.50f, h * 0.64f, w * 0.43f, h * 0.60f)
            close()
        }
        drawPath(mustachePath, color = Color.White)
        drawPath(mustachePath, color = Color(0xFF1E293B), style = Stroke(width = 2f))
        drawLine(Color(0xFF1E293B), Offset(w * 0.54f, h * 0.62f), Offset(w * 0.64f, h * 0.62f), strokeWidth = 2f)

        // Beard (White with vertical combed grooves)
        val beardPath = Path().apply {
            moveTo(w * 0.41f, h * 0.64f)
            lineTo(w * 0.78f, h * 0.64f)
            lineTo(w * 0.78f, h * 0.75f)
            cubicTo(w * 0.70f, h * 0.80f, w * 0.50f, h * 0.80f, w * 0.41f, h * 0.75f)
            close()
        }
        drawPath(beardPath, color = Color.White)
        drawPath(beardPath, color = Color(0xFF1E293B), style = Stroke(width = 2f))

        // Vertical grooves in beard (exact comb lines in photo!)
        for (i in 1..6) {
            val bx = w * (0.41f + i * 0.052f)
            drawLine(Color(0xFF1E293B), Offset(bx, h * 0.65f), Offset(bx, h * 0.76f), strokeWidth = 1.5f)
        }

        // 4. Robe / Shoulders (Deep Royal Blue with Black Diamonds & White V-collar)
        val robePath = Path().apply {
            moveTo(w * 0.12f, h * 0.98f)
            lineTo(w * 0.35f, h * 0.72f)
            lineTo(w * 0.85f, h * 0.72f)
            lineTo(w * 0.98f, h * 0.98f)
            close()
        }
        drawPath(robePath, color = Color(0xFF1565C0)) // Royal blue
        drawPath(robePath, color = Color(0xFF1E293B), style = Stroke(width = 2f))

        // Center White V-Collar
        val vCollar = Path().apply {
            moveTo(w * 0.46f, h * 0.77f)
            lineTo(w * 0.60f, h * 0.88f)
            lineTo(w * 0.74f, h * 0.77f)
            lineTo(w * 0.74f, h * 0.83f)
            lineTo(w * 0.60f, h * 0.94f)
            lineTo(w * 0.46f, h * 0.83f)
            close()
        }
        drawPath(vCollar, color = Color.White)
        drawPath(vCollar, color = Color(0xFF1E293B), style = Stroke(width = 1.8f))

        // Black Diamonds on Blue Robe (◆ ◆ ◆)
        val robeDiamonds = listOf(Offset(w * 0.28f, h * 0.84f), Offset(w * 0.38f, h * 0.89f), Offset(w * 0.82f, h * 0.84f))
        for (pt in robeDiamonds) {
            val rdPath = Path().apply {
                moveTo(pt.x, pt.y - h * 0.035f)
                lineTo(pt.x + w * 0.035f, pt.y)
                lineTo(pt.x, pt.y + h * 0.035f)
                lineTo(pt.x - w * 0.035f, pt.y)
                close()
            }
            drawPath(rdPath, color = Color(0xFF1E293B))
        }

        // White border trim stripes on robe
        drawLine(Color.White, Offset(w * 0.18f, h * 0.94f), Offset(w * 0.36f, h * 0.76f), strokeWidth = 2.5f)
        drawLine(Color.White, Offset(w * 0.74f, h * 0.76f), Offset(w * 0.92f, h * 0.94f), strokeWidth = 2.5f)
    }
}

@Composable
private fun CardQueenIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Dark Burgundy Flat-Topped Polygonal Headdress (matching IMG-20260920-WA0003)
        val bonnetPath = Path().apply {
            moveTo(w * 0.32f, h * 0.31f)
            lineTo(w * 0.32f, h * 0.13f)
            lineTo(w * 0.60f, h * 0.08f) // Peak center
            lineTo(w * 0.88f, h * 0.13f)
            lineTo(w * 0.88f, h * 0.31f)
            close()
        }
        drawPath(bonnetPath, color = Color(0xFF4A0404)) // Deep dark burgundy
        drawPath(bonnetPath, color = Color(0xFF1E293B), style = Stroke(width = 2f))

        // Red Cross / Fleur-de-lis Band across the base of the headdress
        val redBand = Path().apply {
            moveTo(w * 0.32f, h * 0.24f)
            lineTo(w * 0.88f, h * 0.24f)
            lineTo(w * 0.88f, h * 0.31f)
            lineTo(w * 0.32f, h * 0.31f)
            close()
        }
        drawPath(redBand, color = Color(0xFFDC2626))
        drawPath(redBand, color = Color(0xFF1E293B), style = Stroke(width = 1.8f))

        // 5 Black Crosses on the Red Band (✝ ✝ ✝ ✝ ✝)
        val crossXs = listOf(w * 0.38f, w * 0.49f, w * 0.60f, w * 0.71f, w * 0.82f)
        for (cx in crossXs) {
            drawLine(Color(0xFF1E293B), Offset(cx - w * 0.02f, h * 0.275f), Offset(cx + w * 0.02f, h * 0.275f), strokeWidth = 2f)
            drawLine(Color(0xFF1E293B), Offset(cx, h * 0.25f), Offset(cx, h * 0.30f), strokeWidth = 2f)
        }

        // 2. Red Hood draping down sides of head
        val leftHood = Path().apply {
            moveTo(w * 0.32f, h * 0.31f)
            lineTo(w * 0.38f, h * 0.31f)
            lineTo(w * 0.36f, h * 0.68f)
            lineTo(w * 0.22f, h * 0.78f)
            lineTo(w * 0.22f, h * 0.42f)
            close()
        }
        drawPath(leftHood, color = Color(0xFFDC2626))
        drawPath(leftHood, color = Color(0xFF1E293B), style = Stroke(width = 2f))

        val rightHood = Path().apply {
            moveTo(w * 0.88f, h * 0.31f)
            lineTo(w * 0.82f, h * 0.31f)
            lineTo(w * 0.84f, h * 0.68f)
            lineTo(w * 0.94f, h * 0.78f)
            lineTo(w * 0.94f, h * 0.42f)
            close()
        }
        drawPath(rightHood, color = Color(0xFFDC2626))
        drawPath(rightHood, color = Color(0xFF1E293B), style = Stroke(width = 2f))

        // 3. Silver-Grey Hair (parted in the center)
        val hairPath = Path().apply {
            moveTo(w * 0.38f, h * 0.31f)
            lineTo(w * 0.82f, h * 0.31f)
            lineTo(w * 0.82f, h * 0.76f)
            lineTo(w * 0.70f, h * 0.74f)
            lineTo(w * 0.60f, h * 0.46f) // Center parting peak
            lineTo(w * 0.50f, h * 0.74f)
            lineTo(w * 0.38f, h * 0.76f)
            close()
        }
        drawPath(hairPath, color = Color(0xFF94A3B8)) // Silver grey
        drawPath(hairPath, color = Color(0xFF1E293B), style = Stroke(width = 2f))

        // Hair fine strand lines
        drawLine(Color(0xFF64748B), Offset(w * 0.44f, h * 0.34f), Offset(w * 0.44f, h * 0.70f), strokeWidth = 1.5f)
        drawLine(Color(0xFF64748B), Offset(w * 0.76f, h * 0.34f), Offset(w * 0.76f, h * 0.70f), strokeWidth = 1.5f)

        // 4. White Face
        val facePath = Path().apply {
            moveTo(w * 0.42f, h * 0.46f)
            lineTo(w * 0.60f, h * 0.38f)
            lineTo(w * 0.78f, h * 0.46f)
            lineTo(w * 0.72f, h * 0.70f)
            quadraticTo(w * 0.60f, h * 0.76f, w * 0.48f, h * 0.70f)
            close()
        }
        drawPath(facePath, color = Color.White)
        drawPath(facePath, color = Color(0xFF1E293B), style = Stroke(width = 2f))

        // Eyes (Almond serene eyes with shadow line)
        // Left Eye
        drawOval(Color(0xFF1E293B), topLeft = Offset(w * 0.47f, h * 0.50f), size = Size(w * 0.09f, h * 0.045f))
        drawOval(Color.White, topLeft = Offset(w * 0.48f, h * 0.51f), size = Size(w * 0.07f, h * 0.025f))
        drawCircle(Color(0xFF1E293B), radius = 2f, center = Offset(w * 0.515f, h * 0.52f))
        drawArc(Color(0xFF1E293B), 0f, 180f, false, Offset(w * 0.46f, h * 0.52f), Size(w * 0.11f, h * 0.035f), style = Stroke(width = 1.5f))
        drawArc(Color(0xFF1E293B), 180f, 180f, false, Offset(w * 0.45f, h * 0.46f), Size(w * 0.12f, h * 0.04f), style = Stroke(width = 2f))

        // Right Eye
        drawOval(Color(0xFF1E293B), topLeft = Offset(w * 0.64f, h * 0.50f), size = Size(w * 0.09f, h * 0.045f))
        drawOval(Color.White, topLeft = Offset(w * 0.65f, h * 0.51f), size = Size(w * 0.07f, h * 0.025f))
        drawCircle(Color(0xFF1E293B), radius = 2f, center = Offset(w * 0.685f, h * 0.52f))
        drawArc(Color(0xFF1E293B), 0f, 180f, false, Offset(w * 0.63f, h * 0.52f), Size(w * 0.11f, h * 0.035f), style = Stroke(width = 1.5f))
        drawArc(Color(0xFF1E293B), 180f, 180f, false, Offset(w * 0.63f, h * 0.46f), Size(w * 0.12f, h * 0.04f), style = Stroke(width = 2f))

        // Straight Nose
        val nosePath = Path().apply {
            moveTo(w * 0.60f, h * 0.48f)
            lineTo(w * 0.58f, h * 0.60f)
            lineTo(w * 0.62f, h * 0.60f)
        }
        drawPath(nosePath, color = Color(0xFF1E293B), style = Stroke(width = 1.8f, cap = StrokeCap.Round))

        // Neutral Lips
        val lips = Path().apply {
            moveTo(w * 0.55f, h * 0.66f)
            quadraticTo(w * 0.60f, h * 0.65f, w * 0.65f, h * 0.66f)
        }
        drawPath(lips, color = Color(0xFF1E293B), style = Stroke(width = 2f, cap = StrokeCap.Round))

        // 5. White Ermine Collar with 5 Black Diamonds (◆ ◆ ◆ ◆ ◆)
        val collarPath = Path().apply {
            moveTo(w * 0.36f, h * 0.74f)
            quadraticTo(w * 0.60f, h * 0.84f, w * 0.84f, h * 0.74f)
            lineTo(w * 0.80f, h * 0.86f)
            quadraticTo(w * 0.60f, h * 0.92f, w * 0.40f, h * 0.86f)
            close()
        }
        drawPath(collarPath, color = Color.White)
        drawPath(collarPath, color = Color(0xFF1E293B), style = Stroke(width = 2f))

        // Black Diamond Ermine Spots along the collar
        val ermineXs = listOf(w * 0.44f, w * 0.52f, w * 0.60f, w * 0.68f, w * 0.76f)
        for (ex in ermineXs) {
            val ey = h * 0.81f
            val edPath = Path().apply {
                moveTo(ex, ey - h * 0.025f)
                lineTo(ex + w * 0.022f, ey)
                lineTo(ex, ey + h * 0.025f)
                lineTo(ex - w * 0.022f, ey)
                close()
            }
            drawPath(edPath, color = Color(0xFF1E293B))
        }

        // 6. Scarlet Royal Gown with V-neckline & white piping
        val dressPath = Path().apply {
            moveTo(w * 0.14f, h * 0.98f)
            lineTo(w * 0.36f, h * 0.78f)
            lineTo(w * 0.84f, h * 0.78f)
            lineTo(w * 0.96f, h * 0.98f)
            close()
        }
        drawPath(dressPath, color = Color(0xFFDC2626))
        drawPath(dressPath, color = Color(0xFF1E293B), style = Stroke(width = 2f))

        // Center V-neck burgundy insert
        val vNeck = Path().apply {
            moveTo(w * 0.52f, h * 0.88f)
            lineTo(w * 0.60f, h * 0.98f)
            lineTo(w * 0.68f, h * 0.88f)
            close()
        }
        drawPath(vNeck, color = Color(0xFF4A0404))
        drawPath(vNeck, color = Color(0xFF1E293B), style = Stroke(width = 1.5f))

        // White diagonal piping on gown
        drawLine(Color.White, Offset(w * 0.24f, h * 0.92f), Offset(w * 0.42f, h * 0.84f), strokeWidth = 2f)
        drawLine(Color.White, Offset(w * 0.78f, h * 0.84f), Offset(w * 0.90f, h * 0.92f), strokeWidth = 2f)
    }
}

@Composable
private fun CardJackIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Blue Renaissance Brimless Cap (Beret) tilted back
        val capPath = Path().apply {
            moveTo(w * 0.35f, h * 0.28f)
            quadraticTo(w * 0.40f, h * 0.08f, w * 0.70f, h * 0.10f)
            quadraticTo(w * 0.88f, h * 0.18f, w * 0.82f, h * 0.36f)
            lineTo(w * 0.50f, h * 0.34f)
            close()
        }
        drawPath(capPath, color = Color(0xFF1D4ED8))
        drawPath(capPath, color = Color(0xFF1E3A8A), style = Stroke(width = 2f))

        // Cap headband
        drawLine(Color(0xFF0F172A), Offset(w * 0.40f, h * 0.30f), Offset(w * 0.78f, h * 0.32f), strokeWidth = 3f)

        // 2. Yellow/Blonde Wavy Curls cascading down nape
        val hairPath = Path().apply {
            moveTo(w * 0.58f, h * 0.34f)
            quadraticTo(w * 0.86f, h * 0.42f, w * 0.82f, h * 0.62f)
            quadraticTo(w * 0.72f, h * 0.68f, w * 0.64f, h * 0.58f)
            quadraticTo(w * 0.56f, h * 0.48f, w * 0.54f, h * 0.36f)
            close()
        }
        drawPath(hairPath, color = Color(0xFFFBBF24))
        drawPath(hairPath, color = Color(0xFFD97706), style = Stroke(width = 1.5f))

        // 3. Side Profile of Jack (Facing strictly LEFT!)
        val profilePath = Path().apply {
            moveTo(w * 0.44f, h * 0.30f) // Forehead
            lineTo(w * 0.34f, h * 0.38f) // Brow
            lineTo(w * 0.26f, h * 0.46f) // Nose tip!
            lineTo(w * 0.34f, h * 0.48f) // Under nose
            lineTo(w * 0.32f, h * 0.51f) // Upper lip
            lineTo(w * 0.30f, h * 0.53f) // Lower lip
            lineTo(w * 0.34f, h * 0.58f) // Chin
            quadraticTo(w * 0.44f, h * 0.62f, w * 0.54f, h * 0.56f) // Jawline to ear
            lineTo(w * 0.56f, h * 0.36f)
            close()
        }
        drawPath(profilePath, color = Color(0xFFFDE8D0))
        drawPath(profilePath, color = Color(0xFF78350F), style = Stroke(width = 1.8f))

        // Profile Eye
        val eyePath = Path().apply {
            moveTo(w * 0.34f, h * 0.40f)
            lineTo(w * 0.40f, h * 0.38f)
            lineTo(w * 0.38f, h * 0.42f)
            close()
        }
        drawPath(eyePath, color = Color(0xFF0F172A))

        // Eyebrow
        drawLine(Color(0xFF78350F), Offset(w * 0.32f, h * 0.36f), Offset(w * 0.42f, h * 0.34f), strokeWidth = 2f)

        // 4. White Standing Collar
        val collarPath = Path().apply {
            moveTo(w * 0.32f, h * 0.62f)
            lineTo(w * 0.44f, h * 0.56f)
            lineTo(w * 0.60f, h * 0.58f)
            lineTo(w * 0.66f, h * 0.72f)
            lineTo(w * 0.30f, h * 0.72f)
            close()
        }
        drawPath(collarPath, color = Color(0xFFFFFFFF))
        drawPath(collarPath, color = Color(0xFF94A3B8), style = Stroke(width = 1.5f))

        // 5. Royal Blue Tunic with White diagonal strap
        val tunicPath = Path().apply {
            moveTo(w * 0.16f, h * 0.98f)
            lineTo(w * 0.30f, h * 0.72f)
            lineTo(w * 0.66f, h * 0.72f)
            lineTo(w * 0.88f, h * 0.98f)
            close()
        }
        drawPath(tunicPath, color = Color(0xFF1D4ED8))
        drawPath(tunicPath, color = Color(0xFF1E3A8A), style = Stroke(width = 2f))

        // White shoulder strap
        drawLine(Color.White, Offset(w * 0.32f, h * 0.72f), Offset(w * 0.56f, h * 0.98f), strokeWidth = 4f)
    }
}

@Composable
private fun CardSpadeIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val spadePath = Path().apply {
            moveTo(w * 0.50f, h * 0.14f)
            cubicTo(w * 0.72f, h * 0.28f, w * 0.94f, h * 0.48f, w * 0.82f, h * 0.70f)
            cubicTo(w * 0.72f, h * 0.82f, w * 0.55f, h * 0.74f, w * 0.50f, h * 0.66f)
            cubicTo(w * 0.45f, h * 0.74f, w * 0.28f, h * 0.82f, w * 0.18f, h * 0.70f)
            cubicTo(w * 0.06f, h * 0.48f, w * 0.28f, h * 0.28f, w * 0.50f, h * 0.14f)
            close()
        }
        drawPath(
            spadePath,
            brush = Brush.verticalGradient(listOf(Color(0xFF2D3748), Color(0xFF0F172A)))
        )
        val basePath = Path().apply {
            moveTo(w * 0.46f, h * 0.64f)
            lineTo(w * 0.35f, h * 0.90f)
            lineTo(w * 0.65f, h * 0.90f)
            lineTo(w * 0.54f, h * 0.64f)
            close()
        }
        drawPath(basePath, color = Color(0xFF0F172A))
    }
}

@Composable
private fun CardWildIllustration(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize(0.85f)) {
            val w = size.width
            val h = size.height

            // Gold & Red JILI 3-Peak Crown
            val crownPath = Path().apply {
                moveTo(w * 0.15f, h * 0.55f)
                lineTo(w * 0.12f, h * 0.22f)
                lineTo(w * 0.35f, h * 0.38f)
                lineTo(w * 0.50f, h * 0.10f)
                lineTo(w * 0.65f, h * 0.38f)
                lineTo(w * 0.88f, h * 0.22f)
                lineTo(w * 0.85f, h * 0.55f)
                close()
            }
            drawPath(
                crownPath,
                brush = Brush.verticalGradient(listOf(Color(0xFFFFEE55), Color(0xFFF59E0B), Color(0xFFD97706)))
            )
            drawPath(crownPath, color = Color(0xFF78350F), style = Stroke(width = 2f))

            // Red velvet cushions
            drawCircle(Color(0xFFDC2626), radius = w * 0.08f, center = Offset(w * 0.50f, h * 0.38f))
            drawCircle(Color(0xFFDC2626), radius = w * 0.06f, center = Offset(w * 0.30f, h * 0.44f))
            drawCircle(Color(0xFFDC2626), radius = w * 0.06f, center = Offset(w * 0.70f, h * 0.44f))

            // Golden pearls
            drawCircle(Color(0xFFFFEE55), radius = 5f, center = Offset(w * 0.12f, h * 0.22f))
            drawCircle(Color(0xFFFFEE55), radius = 6f, center = Offset(w * 0.50f, h * 0.10f))
            drawCircle(Color(0xFFFFEE55), radius = 5f, center = Offset(w * 0.88f, h * 0.22f))
        }

        // Red 3D Banner across bottom with glowing "WILD"
        Surface(
            shape = RoundedCornerShape(3.dp),
            color = Color(0xFFD32F2F),
            border = BorderStroke(1.dp, Color(0xFFFFD54F)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-4).dp)
        ) {
            Text(
                text = "WILD",
                color = Color(0xFFFFEE55),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.5.dp)
            )
        }
    }
}

