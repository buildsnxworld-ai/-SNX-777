package com.example.ui.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppLanguage
import com.example.ui.theme.*
import com.example.util.StringRes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SlotMachineGame(
    isOpen: Boolean,
    currentBalance: Double,
    language: AppLanguage,
    onBalanceChange: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val symbols = listOf("7️⃣", "👑", "💎", "🍒", "🔔", "⭐", "🍇")
    var betAmount by remember { mutableStateOf(50.0) }
    var reel1 by remember { mutableStateOf("7️⃣") }
    var reel2 by remember { mutableStateOf("7️⃣") }
    var reel3 by remember { mutableStateOf("7️⃣") }
    var isSpinning by remember { mutableStateOf(false) }
    var isTurboSpeed by remember { mutableStateOf(false) }
    var winAmount by remember { mutableStateOf(0.0) }
    var winMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = { if (!isSpinning) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("slot_machine_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CasinoSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(GoldPrimary, AccentCrimson, GoldLight))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎰", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = StringRes.t(language, "মেগা জ্যাকপট ৭৭৭ স্লট", "Mega Jackpot 777"),
                            color = GoldLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSpinning,
                        modifier = Modifier.size(32.dp).testTag("close_slot_game")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Balance display & Speed Toggle
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CasinoSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = StringRes.t(language, "ব্যালেন্স: ", "Bal: "), color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = StringRes.formatBDT(currentBalance),
                                color = AccentEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Speed Controller: স্লো / ফাস্ট
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isTurboSpeed) AccentCrimson.copy(alpha = 0.2f) else Slate700)
                                .border(1.dp, if (isTurboSpeed) AccentCrimson else Slate500, RoundedCornerShape(6.dp))
                                .clickable { if (!isSpinning) isTurboSpeed = !isTurboSpeed }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("slot_speed_toggle")
                        ) {
                            Text(
                                text = if (isTurboSpeed) "⚡ " + StringRes.t(language, "ফাস্ট স্পিন", "Fast Spin")
                                else "⏱️ " + StringRes.t(language, "নরমাল স্পিন", "Normal Spin"),
                                color = if (isTurboSpeed) GoldLight else Slate200,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The 3 Reels Display Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF070A10), Color(0xFF141926), Color(0xFF070A10))
                            )
                        )
                        .border(3.dp, Brush.linearGradient(listOf(GoldPrimary, GoldDark)), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SlotReelBox(symbol = reel1, isSpinning = isSpinning)
                        SlotReelBox(symbol = reel2, isSpinning = isSpinning)
                        SlotReelBox(symbol = reel3, isSpinning = isSpinning)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Win Result announcement
                if (winAmount > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AccentEmerald.copy(alpha = 0.2f),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(AccentEmerald, GoldPrimary))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = winMessage ?: "🎉 অভিনন্দন! জিতেছেন ৳%,.0f!".format(winAmount),
                            color = GoldLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    Text(
                        text = StringRes.t(language, "৩টি 7️⃣ মিললে পাবেন ৫০ গুণ জ্যাকপট!", "Match 3x 7️⃣ for 50x Mega Jackpot!"),
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bet selector chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(10.0, 50.0, 100.0, 500.0).forEach { amt ->
                        FilterChip(
                            selected = betAmount == amt,
                            onClick = { if (!isSpinning) betAmount = amt },
                            label = { Text("৳${amt.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = Color.Black,
                                containerColor = CasinoSurfaceElevated,
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Spin Button
                Button(
                    onClick = {
                        if (currentBalance < betAmount) {
                            winMessage = StringRes.t(language, "অপর্যাপ্ত ব্যালেন্স! ডিপোজিট করুন", "Insufficient balance! Please deposit")
                            return@Button
                        }

                        isSpinning = true
                        winAmount = 0.0
                        winMessage = null
                        onBalanceChange(-betAmount)

                        coroutineScope.launch {
                            // Dynamic Spin animation loop: Fast (quick spins) vs Normal/Slow (longer suspense)
                            val totalCycles = if (isTurboSpeed) 8 else 16
                            for (i in 0 until totalCycles) {
                                reel1 = symbols.random()
                                reel2 = symbols.random()
                                reel3 = symbols.random()
                                val stepDelay = if (isTurboSpeed) (30 + (i * 5L)) else (60 + (i * 10L))
                                delay(stepDelay)
                            }

                            // Determine final outcome
                            val rng = (1..100).random()
                            if (rng <= 12) {
                                // Big 777 Win (12% chance)
                                reel1 = "7️⃣"; reel2 = "7️⃣"; reel3 = "7️⃣"
                                val win = betAmount * 25.0
                                winAmount = win
                                winMessage = "🔥 MEGA 777 JACKPOT! +৳%,.0f".format(win)
                                onBalanceChange(win)
                            } else if (rng <= 35) {
                                // 3 matching symbols
                                val sym = listOf("👑", "💎", "🍒", "⭐").random()
                                reel1 = sym; reel2 = sym; reel3 = sym
                                val win = betAmount * 5.0
                                winAmount = win
                                winMessage = "🎉 BIG WIN! +৳%,.0f".format(win)
                                onBalanceChange(win)
                            } else if (rng <= 60) {
                                // 2 matching symbols
                                val sym = symbols.random()
                                reel1 = sym; reel2 = sym; reel3 = symbols.filter { it != sym }.random()
                                val win = betAmount * 1.8
                                winAmount = win
                                winMessage = "✨ WIN! +৳%,.0f".format(win)
                                onBalanceChange(win)
                            } else {
                                // No match
                                reel1 = symbols.random()
                                reel2 = symbols.filter { it != reel1 }.random()
                                reel3 = symbols.filter { it != reel1 && it != reel2 }.random()
                                winMessage = StringRes.t(language, "আবার চেষ্টা করুন!", "Try again next spin!")
                            }

                            isSpinning = false
                        }
                    },
                    enabled = !isSpinning && currentBalance >= betAmount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("slot_spin_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Text(
                        text = if (isSpinning)
                            StringRes.t(language, "ঘুরছে...", "SPINNING...")
                        else
                            StringRes.t(language, "স্পিন করুন (৳%,.0f)".format(betAmount), "SPIN (৳%,.0f)".format(betAmount)),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun SlotReelBox(symbol: String, isSpinning: Boolean) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CasinoSurfaceElevated)
            .border(1.dp, CasinoCardBorder, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = 38.sp
        )
    }
}
