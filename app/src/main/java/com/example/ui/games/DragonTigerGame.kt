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
import androidx.compose.material.icons.filled.FlashOn
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
fun DragonTigerGame(
    isOpen: Boolean,
    currentBalance: Double,
    language: AppLanguage,
    onBalanceChange: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val coroutineScope = rememberCoroutineScope()
    var betAmount by remember { mutableStateOf(50.0) }
    var selectedSide by remember { mutableStateOf<String?>("DRAGON") } // "DRAGON", "TIGER", "TIE"

    var dragonCard by remember { mutableStateOf("🂮") }
    var tigerCard by remember { mutableStateOf("🂪") }
    var dragonScore by remember { mutableStateOf(13) }
    var tigerScore by remember { mutableStateOf(10) }

    var isBattling by remember { mutableStateOf(false) }
    var isTurboDealing by remember { mutableStateOf(false) }
    var battleResultText by remember { mutableStateOf<String?>(null) }
    var winStreak by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = { if (!isBattling) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dragon_tiger_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(Color(0xFFEF4444), GoldPrimary, Color(0xFF3B82F6)))
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
                        Text(text = "🐉", fontSize = 22.sp)
                        Text(text = "⚡", fontSize = 16.sp, color = GoldLight)
                        Text(text = "🐅", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = StringRes.t(language, "ড্রাগন ভার্সেস টাইগার", "Dragon vs Tiger Live"),
                            color = GoldLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isBattling,
                        modifier = Modifier.size(32.dp).testTag("close_dragon_tiger_game")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Balance & Win streak & Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate800,
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = StringRes.t(language, "ব্যালেন্স:", "Balance:"), color = Slate400, fontSize = 11.sp)
                            Text(
                                text = StringRes.formatBDT(currentBalance),
                                color = AccentEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Speed Toggle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isTurboDealing) AccentCrimson.copy(alpha = 0.2f) else Slate800,
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(if (isTurboDealing) AccentCrimson else Slate700, Slate700))),
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable { if (!isBattling) isTurboDealing = !isTurboDealing }
                            .testTag("dt_speed_toggle")
                    ) {
                        Text(
                            text = if (isTurboDealing) "⚡ " + StringRes.t(language, "ফাস্ট", "Fast")
                            else "⏱️ " + StringRes.t(language, "স্লো", "Slow"),
                            color = if (isTurboDealing) GoldLight else Slate300,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate800,
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Text(
                            text = "🔥 Streak: $winStreak",
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Battle Arena
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                            )
                        )
                        .border(1.5.dp, Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF3B82F6))), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dragon Side
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = StringRes.t(language, "🐉 ড্রাগন", "🐉 DRAGON"),
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                modifier = Modifier
                                    .size(width = 54.dp, height = 76.dp)
                                    .border(2.dp, if (selectedSide == "DRAGON") Color(0xFFEF4444) else Color.Transparent, RoundedCornerShape(8.dp)),
                                shadowElevation = 4.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = dragonCard, fontSize = 34.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "পয়েন্ট: $dragonScore",
                                color = Slate400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // VS Indicator
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "VS", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 12.sp)
                        }

                        // Tiger Side
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = StringRes.t(language, "🐅 টাইগার", "🐅 TIGER"),
                                color = Color(0xFF3B82F6),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                modifier = Modifier
                                    .size(width = 54.dp, height = 76.dp)
                                    .border(2.dp, if (selectedSide == "TIGER") Color(0xFF3B82F6) else Color.Transparent, RoundedCornerShape(8.dp)),
                                shadowElevation = 4.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = tigerCard, fontSize = 34.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "পয়েন্ট: $tigerScore",
                                color = Slate400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Outcome Notification
                if (battleResultText != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldPrimary.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = battleResultText ?: "",
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Choose Side (Dragon 1:1, Tie 1:8, Tiger 1:1)
                Text(
                    text = StringRes.t(language, "আপনার বাজি নির্বাচন করুন:", "Select Your Bet:"),
                    color = Slate300,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Dragon button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedSide == "DRAGON") Color(0xFFEF4444) else Slate800,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (!isBattling) selectedSide = "DRAGON" }
                            .testTag("bet_dragon_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🐉 ড্রাগন",
                                color = if (selectedSide == "DRAGON") Color.White else Slate300,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(text = "১ : ১", color = GoldLight, fontSize = 9.sp)
                        }
                    }

                    // Tie button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedSide == "TIE") AccentEmerald else Slate800,
                        modifier = Modifier
                            .weight(0.9f)
                            .clickable { if (!isBattling) selectedSide = "TIE" }
                            .testTag("bet_tie_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🤝 টাই",
                                color = if (selectedSide == "TIE") Color.White else Slate300,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(text = "১ : ৮", color = GoldLight, fontSize = 9.sp)
                        }
                    }

                    // Tiger button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedSide == "TIGER") Color(0xFF3B82F6) else Slate800,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (!isBattling) selectedSide = "TIGER" }
                            .testTag("bet_tiger_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🐅 টাইগার",
                                color = if (selectedSide == "TIGER") Color.White else Slate300,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(text = "১ : ১", color = GoldLight, fontSize = 9.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bet chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(20.0, 50.0, 100.0, 500.0).forEach { amt ->
                        FilterChip(
                            selected = betAmount == amt,
                            onClick = { if (!isBattling) betAmount = amt },
                            label = { Text("৳${amt.toInt()}", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = Color.Black,
                                containerColor = Slate800,
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fight / Bet Button
                Button(
                    onClick = {
                        if (currentBalance < betAmount) {
                            battleResultText = "অপর্যাপ্ত ব্যালেন্স! ডিপোজিট করুন"
                            return@Button
                        }
                        val chosen = selectedSide ?: "DRAGON"
                        isBattling = true
                        battleResultText = "যুদ্ধ শুরু হচ্ছে..."
                        onBalanceChange(-betAmount)

                        coroutineScope.launch {
                            val cardPool = listOf(
                                "🂡" to 1, "🂢" to 2, "🂣" to 3, "🂤" to 4, "🂥" to 5,
                                "🂦" to 6, "🂧" to 7, "🂨" to 8, "🂩" to 9, "🂪" to 10,
                                "🂫" to 11, "🂭" to 12, "🂮" to 13
                            )

                            // Quick suspense animation
                            val suspenseSteps = if (isTurboDealing) 2 else 4
                            val stepDelay = if (isTurboDealing) 60L else 150L
                            for (i in 1..suspenseSteps) {
                                val tempD = cardPool.random()
                                val tempT = cardPool.random()
                                dragonCard = tempD.first
                                tigerCard = tempT.first
                                dragonScore = tempD.second
                                tigerScore = tempT.second
                                delay(stepDelay)
                            }

                            val dCard = cardPool.random()
                            val tCard = cardPool.random()
                            dragonCard = dCard.first
                            dragonScore = dCard.second
                            tigerCard = tCard.first
                            tigerScore = tCard.second

                            val winner = when {
                                dragonScore > tigerScore -> "DRAGON"
                                tigerScore > dragonScore -> "TIGER"
                                else -> "TIE"
                            }

                            if (chosen == winner) {
                                val multiplier = if (winner == "TIE") 8.0 else 2.0
                                val winAmt = betAmount * multiplier
                                winStreak += 1
                                battleResultText = "🎉 দারুণ জয়! $winner জিতেছে! আপনি পেয়েছেন ৳%,.0f!".format(winAmt)
                                onBalanceChange(winAmt)
                            } else {
                                winStreak = 0
                                battleResultText = "$winner জিতেছে! আপনার বাজি সফল হয়নি। আবার চেষ্টা করুন!"
                            }

                            isBattling = false
                        }
                    },
                    enabled = !isBattling && currentBalance >= betAmount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("fight_dragon_tiger_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBattling)
                            StringRes.t(language, "কার্ড ওপেন হচ্ছে...", "REVEALING...")
                        else
                            StringRes.t(language, "বাজি ধরুন (৳%,.0f)".format(betAmount), "PLACE BET (৳%,.0f)".format(betAmount)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
