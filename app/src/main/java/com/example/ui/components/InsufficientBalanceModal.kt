package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun InsufficientBalanceModal(
    isOpen: Boolean,
    language: AppLanguage,
    gameName: String = "",
    currentBalance: Double,
    onGoToDeposit: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("insufficient_balance_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFFEF4444),
                        GoldPrimary,
                        Color(0xFFEF4444)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_insufficient_modal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate400
                        )
                    }
                }

                // Lock & Wallet Icon with Pulsing Effect
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                        .border(2.dp, Color(0xFFEF4444).copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Game Locked",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title: আপনার একাউন্টে ব্যালেন্স নেই
                Text(
                    text = StringRes.t(
                        language,
                        "আপনার একাউন্টে ব্যালেন্স নেই!",
                        "Insufficient Account Balance!"
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = StringRes.t(
                        language,
                        "গেম খেলতে অনুগ্রহ করে ডিপোজিট করুন। বিকাশ ও নগদে সর্বনিম্ন ৩০০ টাকা ডিপোজিটে পাবেন ৫% ক্যাশ বোনাস!",
                        "Please deposit to play games. Get 5% instant bonus on minimum ৳300 deposit via bKash & Nagad!"
                    ),
                    color = Slate300,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Balance status card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate800,
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CasinoBorderSubtle, CasinoBorderSubtle))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = StringRes.t(language, "বর্তমান ব্যালেন্স:", "Current Balance:"),
                                color = Slate400,
                                fontSize = 12.sp
                            )
                            Text(
                                text = StringRes.formatBDT(currentBalance, language),
                                color = if (currentBalance <= 0) Color(0xFFEF4444) else AccentEmerald,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Slate700, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = StringRes.t(language, "প্রথম ডিপোজিট অফার:", "1st Deposit Offer:"),
                                color = GoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentCrimson
                            ) {
                                Text(
                                    text = StringRes.t(language, "৫% বোনাস অফার", "5% Bonus Offer"),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Action Button: ডিপোজিট করতে ক্লিক করুন
                Button(
                    onClick = onGoToDeposit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("insufficient_balance_deposit_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = StringRes.t(
                            language,
                            "ডিপোজিট করতে ক্লিক করুন",
                            "Click to Deposit"
                        ),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Cancel / Back Button: ফিরে যান
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("insufficient_balance_cancel_btn")
                ) {
                    Text(
                        text = StringRes.t(language, "ফিরে যান", "Go Back"),
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
