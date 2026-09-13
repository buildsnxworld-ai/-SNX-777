package com.example.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppLanguage
import com.example.ui.theme.*
import com.example.util.StringRes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CricketBettingGame(
    isOpen: Boolean,
    currentBalance: Double,
    language: AppLanguage,
    onBalanceChange: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val coroutineScope = rememberCoroutineScope()
    var betStake by remember { mutableStateOf(100.0) }
    var selectedOption by remember { mutableStateOf("Dhaka") } // Dhaka / Sylhet / Six / Four
    var isDelivering by remember { mutableStateOf(false) }
    var matchScore by remember { mutableStateOf("ঢাকা: ১৪২/৩ (১৬.২ ওভার)") }
    var ballResult by remember { mutableStateOf<String?>(null) }
    var targetText by remember { mutableStateOf("সিলেটের দরকার ২২ বলে ৩৬ রান") }

    Dialog(onDismissRequest = { if (!isDelivering) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cricket_game_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CasinoSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(GoldPrimary, AccentCyan))
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
                        Text(text = "🏏", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = StringRes.t(language, "বিপিএল ক্রিকেট লাইভ প্রেডিকশন", "BPL Cricket Live Betting"),
                            color = GoldLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isDelivering,
                        modifier = Modifier.size(32.dp).testTag("close_cricket_game")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live Match Scoreboard Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CasinoSurfaceElevated),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CasinoCardBorder, Color.Transparent)))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "BPL T20 • LIVE 🔴", color = AccentCrimson, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "ব্যালেন্স: ${StringRes.formatBDT(currentBalance)}",
                                color = AccentEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "ঢাকা ডমিনেটর্স", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "vs", color = TextMuted, fontSize = 10.sp)
                                Text(text = "সিলেট স্ট্রাইকার্স", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = matchScore, color = GoldLight, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                Text(text = targetText, color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Odds Betting Selection
                Text(
                    text = StringRes.t(language, "লাইভ মার্কেট সিলেক্ট করুন:", "Select Live Market:"),
                    color = GoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dhaka Win
                    OddsCard(
                        title = "ঢাকা জিতবে",
                        odds = "1.85",
                        isSelected = selectedOption == "Dhaka",
                        onClick = { selectedOption = "Dhaka" },
                        modifier = Modifier.weight(1f)
                    )

                    // Sylhet Win
                    OddsCard(
                        title = "সিলেট জিতবে",
                        odds = "2.05",
                        isSelected = selectedOption == "Sylhet",
                        onClick = { selectedOption = "Sylhet" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Next ball boundary (4 or 6)
                    OddsCard(
                        title = "পরের বলে চার (4)",
                        odds = "3.50",
                        isSelected = selectedOption == "Four",
                        onClick = { selectedOption = "Four" },
                        modifier = Modifier.weight(1f)
                    )

                    // Next ball 6
                    OddsCard(
                        title = "পরের বলে ছক্কা (6)",
                        odds = "5.00",
                        isSelected = selectedOption == "Six",
                        onClick = { selectedOption = "Six" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Result announcement
                if (ballResult != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldPrimary.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = ballResult ?: "",
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stake Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(50.0, 100.0, 200.0, 500.0).forEach { s ->
                        FilterChip(
                            selected = betStake == s,
                            onClick = { if (!isDelivering) betStake = s },
                            label = { Text("৳${s.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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

                // Place Bet Button
                Button(
                    onClick = {
                        if (currentBalance < betStake) {
                            ballResult = "অপর্যাপ্ত ব্যালেন্স! ডিপোজিট করুন"
                            return@Button
                        }

                        isDelivering = true
                        ballResult = "বোলার দৌড়াচ্ছেন... বল করা হচ্ছে! 🏏"
                        onBalanceChange(-betStake)

                        coroutineScope.launch {
                            delay(1800)
                            val outcomes = listOf("DOT", "1", "2", "4", "6", "W")
                            val outcome = outcomes.random()

                            var won = false
                            var multiplier = 1.0

                            when (selectedOption) {
                                "Dhaka" -> {
                                    won = (1..10).random() <= 6
                                    multiplier = 1.85
                                }
                                "Sylhet" -> {
                                    won = (1..10).random() <= 5
                                    multiplier = 2.05
                                }
                                "Four" -> {
                                    won = outcome == "4"
                                    multiplier = 3.50
                                }
                                "Six" -> {
                                    won = outcome == "6"
                                    multiplier = 5.00
                                }
                            }

                            if (won) {
                                val payout = betStake * multiplier
                                ballResult = "🎉 বল আউটকাম: $outcome! আপনি জিতেছেন ৳%,.0f!".format(payout)
                                onBalanceChange(payout)
                            } else {
                                ballResult = "বল আউটকাম: $outcome! এই বলে জেতা সম্ভব হয়নি।"
                            }

                            matchScore = "ঢাকা: ১৪৬/৪ (১৭.১ ওভার)"
                            targetText = "সিলেটের দরকার ১৭ বলে ৩১ রান"
                            isDelivering = false
                        }
                    },
                    enabled = !isDelivering && currentBalance >= betStake,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("cricket_place_bet_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Text(
                        text = if (isDelivering)
                            StringRes.t(language, "বলিং চলছে...", "BOWLING...")
                        else
                            StringRes.t(language, "বেট কনফার্ম করুন (৳%,.0f)".format(betStake), "CONFIRM BET (৳%,.0f)".format(betStake)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun OddsCard(
    title: String,
    odds: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GoldPrimary.copy(alpha = 0.2f) else CasinoSurfaceElevated
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (isSelected) listOf(GoldPrimary, AccentEmerald)
                else listOf(CasinoCardBorder, Color.Transparent)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = odds, color = if (isSelected) GoldLight else AccentEmerald, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
    }
}
