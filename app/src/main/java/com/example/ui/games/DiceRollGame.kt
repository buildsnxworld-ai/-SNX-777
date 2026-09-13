package com.example.ui.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
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
fun DiceRollGame(
    isOpen: Boolean,
    currentBalance: Double,
    language: AppLanguage,
    onBalanceChange: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val coroutineScope = rememberCoroutineScope()
    var betAmount by remember { mutableStateOf(50.0) }
    var selectedPrediction by remember { mutableStateOf("HIGH") } // "LOW" (2-6), "SEVEN" (7), "HIGH" (8-12)

    var die1 by remember { mutableStateOf(4) }
    var die2 by remember { mutableStateOf(5) }
    var isRolling by remember { mutableStateOf(false) }
    var isFastDice by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    val diceEmojis = listOf("⚀", "⚁", "⚂", "⚃", "⚄", "⚅")

    Dialog(onDismissRequest = { if (!isRolling) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dice_roll_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
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
                        Text(text = "🎲", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = StringRes.t(language, "লাকি ডাইস হাই-লো", "Lucky Dice Hi-Lo"),
                            color = GoldLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isRolling,
                        modifier = Modifier.size(32.dp).testTag("close_dice_roll_game")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Balance display & Speed Toggle
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate800,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = StringRes.t(language, "ব্যালেন্স: ", "Bal: "), color = Slate400, fontSize = 11.sp)
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
                                .background(if (isFastDice) AccentCyan.copy(alpha = 0.2f) else Slate700)
                                .border(1.dp, if (isFastDice) AccentCyan else Slate500, RoundedCornerShape(6.dp))
                                .clickable { if (!isRolling) isFastDice = !isFastDice }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("dice_speed_toggle")
                        ) {
                            Text(
                                text = if (isFastDice) "⚡ " + StringRes.t(language, "ফাস্ট স্পিড", "Fast")
                                else "⏱️ " + StringRes.t(language, "নরমাল স্পিড", "Normal"),
                                color = if (isFastDice) AccentCyan else Slate200,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dice Rolling Stage
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                            )
                        )
                        .border(1.5.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Die 1
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier.size(64.dp),
                                shadowElevation = 6.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = diceEmojis.getOrElse(die1 - 1) { "⚀" }, fontSize = 42.sp)
                                }
                            }

                            // Die 2
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier.size(64.dp),
                                shadowElevation = 6.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = diceEmojis.getOrElse(die2 - 1) { "⚀" }, fontSize = 42.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        val totalSum = die1 + die2
                        Text(
                            text = "মোট যোগফল: $totalSum (${if (totalSum < 7) "Low" else if (totalSum > 7) "High" else "Lucky 7"})",
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Result message
                if (resultMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldPrimary.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = resultMessage ?: "",
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Choose prediction (Low 2-6 (2x), Lucky 7 (5x), High 8-12 (2x))
                Text(
                    text = StringRes.t(language, "বাজি বাছাই করুন:", "Select Prediction:"),
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
                    // LOW
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedPrediction == "LOW") AccentCyan else Slate800,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (!isRolling) selectedPrediction = "LOW" }
                            .testTag("bet_low_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "লো (২ - ৬)",
                                color = if (selectedPrediction == "LOW") Color.Black else Slate200,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "২ গুণ",
                                color = if (selectedPrediction == "LOW") Color.Black else GoldLight,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // LUCKY 7
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedPrediction == "SEVEN") AccentCrimson else Slate800,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (!isRolling) selectedPrediction = "SEVEN" }
                            .testTag("bet_seven_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "লাকি ৭",
                                color = if (selectedPrediction == "SEVEN") Color.White else Slate200,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "৫ গুণ 🔥",
                                color = if (selectedPrediction == "SEVEN") Color.White else GoldLight,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // HIGH
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedPrediction == "HIGH") GoldPrimary else Slate800,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (!isRolling) selectedPrediction = "HIGH" }
                            .testTag("bet_high_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "হাই (৮ - ১২)",
                                color = if (selectedPrediction == "HIGH") Color.Black else Slate200,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "২ গুণ",
                                color = if (selectedPrediction == "HIGH") Color.Black else GoldLight,
                                fontSize = 9.sp
                            )
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
                            onClick = { if (!isRolling) betAmount = amt },
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

                // Roll Button
                Button(
                    onClick = {
                        if (currentBalance < betAmount) {
                            resultMessage = "অপর্যাপ্ত ব্যালেন্স! ডিপোজিট করুন"
                            return@Button
                        }
                        isRolling = true
                        resultMessage = "ডাইস ঘুরছে..."
                        onBalanceChange(-betAmount)

                        coroutineScope.launch {
                            val rollSteps = if (isFastDice) 3 else 6
                            val stepDelay = if (isFastDice) 50L else 120L
                            for (i in 1..rollSteps) {
                                die1 = (1..6).random()
                                die2 = (1..6).random()
                                delay(stepDelay)
                            }
                            val final1 = (1..6).random()
                            val final2 = (1..6).random()
                            die1 = final1
                            die2 = final2
                            val sum = final1 + final2

                            val actualOutcome = when {
                                sum < 7 -> "LOW"
                                sum > 7 -> "HIGH"
                                else -> "SEVEN"
                            }

                            if (selectedPrediction == actualOutcome) {
                                val mult = if (actualOutcome == "SEVEN") 5.0 else 2.0
                                val prize = betAmount * mult
                                resultMessage = "🎉 চমৎকার জয়! মোট $sum হয়েছে! আপনি পেয়েছেন ৳%,.0f!".format(prize)
                                onBalanceChange(prize)
                            } else {
                                resultMessage = "মোট যোগফল $sum! আপনার অনুমান মেলেনি, আবার চেষ্টা করুন।"
                            }

                            isRolling = false
                        }
                    },
                    enabled = !isRolling && currentBalance >= betAmount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("roll_dice_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRolling)
                            StringRes.t(language, "রোল হচ্ছে...", "ROLLING...")
                        else
                            StringRes.t(language, "ডাইস রোল করুন (৳%,.0f)".format(betAmount), "ROLL DICE (৳%,.0f)".format(betAmount)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
