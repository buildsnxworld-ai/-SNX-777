package com.example.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppLanguage
import com.example.ui.theme.*
import com.example.util.StringRes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TeenPattiGame(
    isOpen: Boolean,
    currentBalance: Double,
    language: AppLanguage,
    onBalanceChange: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val coroutineScope = rememberCoroutineScope()
    var betAmount by remember { mutableStateOf(50.0) }
    var playerCards by remember { mutableStateOf(listOf("🂡", "🂮", "🂭")) }
    var dealerCards by remember { mutableStateOf(listOf("🂪", "🂫", "🂩")) }
    var isDealing by remember { mutableStateOf(false) }
    var isTurboDeal by remember { mutableStateOf(false) }
    var gameOutcome by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { if (!isDealing) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("teen_patti_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CasinoSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(GoldPrimary, AccentCrimson))
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
                        Text(text = "🃏", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = StringRes.t(language, "তিন পাত্তি রয়েল লাইভ", "Teen Patti Royal Live"),
                            color = GoldLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isDealing,
                        modifier = Modifier.size(32.dp).testTag("close_teen_patti_game")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Balance & Speed Controller
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
                            Text(text = StringRes.t(language, "ব্যালেন্স: ", "Balance: "), color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = StringRes.formatBDT(currentBalance),
                                color = AccentEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Speed toggle: স্লো / ফাস্ট
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isTurboDeal) AccentCrimson.copy(alpha = 0.2f) else Slate700)
                                .border(1.dp, if (isTurboDeal) AccentCrimson else Slate500, RoundedCornerShape(6.dp))
                                .clickable { if (!isDealing) isTurboDeal = !isTurboDeal }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("teen_patti_speed_toggle")
                        ) {
                            Text(
                                text = if (isTurboDeal) "⚡ " + StringRes.t(language, "ফাস্ট", "Fast")
                                else "⏱️ " + StringRes.t(language, "স্লো", "Slow"),
                                color = if (isTurboDeal) GoldLight else Slate200,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Table Green Felt Canvas Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF0F472B), Color(0xFF062415))
                            )
                        )
                        .border(2.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Dealer Cards
                        Text(text = "ডিলার (Dealer)", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            dealerCards.forEach { card ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White,
                                    modifier = Modifier.size(width = 46.dp, height = 62.dp),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = card, fontSize = 28.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Box(modifier = Modifier.width(120.dp).height(1.dp).background(GoldPrimary.copy(alpha = 0.3f)))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Player Cards
                        Text(text = "আপনার কার্ড (Player)", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            playerCards.forEach { card ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White,
                                    modifier = Modifier.size(width = 46.dp, height = 62.dp),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = card, fontSize = 28.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Outcome
                if (gameOutcome != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldPrimary.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = gameOutcome ?: "",
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bet chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(20.0, 50.0, 100.0, 500.0).forEach { amt ->
                        FilterChip(
                            selected = betAmount == amt,
                            onClick = { if (!isDealing) betAmount = amt },
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

                // Deal Button
                Button(
                    onClick = {
                        if (currentBalance < betAmount) {
                            gameOutcome = "অপর্যাপ্ত ব্যালেন্স! ডিপোজিট করুন"
                            return@Button
                        }

                        isDealing = true
                        gameOutcome = "কার্ড ডিল করা হচ্ছে..."
                        onBalanceChange(-betAmount)

                        coroutineScope.launch {
                            val deck = listOf("🂡", "🂮", "🂭", "🂫", "🂪", "🂩", "🂨", "🂧", "🂦", "🂥", "🂤", "🂣", "🂢").shuffled()
                            val dealDelay = if (isTurboDeal) 400L else 1100L
                            delay(dealDelay)

                            val player = deck.take(3)
                            val dealer = deck.drop(3).take(3)
                            playerCards = player
                            dealerCards = dealer

                            // 55% chance player wins
                            val playerWins = (1..100).random() <= 55
                            if (playerWins) {
                                val payout = betAmount * 2.0
                                gameOutcome = "🎉 অভিনন্দন! আপনি জিতেছেন ৳%,.0f!".format(payout)
                                onBalanceChange(payout)
                            } else {
                                gameOutcome = "ডিলার জিতেছেন! পরবর্তী হাতে আবার চেষ্টা করুন।"
                            }

                            isDealing = false
                        }
                    },
                    enabled = !isDealing && currentBalance >= betAmount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("deal_cards_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Text(
                        text = if (isDealing)
                            StringRes.t(language, "ডিলিং হচ্ছে...", "DEALING...")
                        else
                            StringRes.t(language, "কার্ড ডিল করুন (৳%,.0f)".format(betAmount), "DEAL HAND (৳%,.0f)".format(betAmount)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
