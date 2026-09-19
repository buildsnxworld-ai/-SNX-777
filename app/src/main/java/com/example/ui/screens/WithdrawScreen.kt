package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*
import com.example.ui.theme.*
import com.example.util.StringRes
import kotlinx.coroutines.delay

@Composable
fun WithdrawScreen(
    userProfile: UserProfile,
    transactions: List<TransactionRecord>,
    language: AppLanguage,
    onSubmitWithdrawal: (PaymentMethod, Double, String) -> Boolean,
    onOpenAuth: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var selectedMethod by remember { mutableStateOf(PaymentMethod.BKASH) }
    var receiverPhone by remember { mutableStateOf("") }
    var withdrawAmount by remember { mutableStateOf("1000") }
    var accountType by remember { mutableStateOf("Personal") } // Personal / Agent

    val quickAmounts = listOf(500, 1000, 2000, 5000, 10000)
    val pastWithdrawals = remember(transactions, userProfile.username, userProfile.phone, userProfile.isLoggedIn) {
        if (!userProfile.isLoggedIn) {
            emptyList()
        } else {
            val currentName = userProfile.username.trim()
            val currentPhone = userProfile.phone.trim()
            transactions.filter {
                it.type == TransactionType.WITHDRAW &&
                ((currentName.isNotBlank() && it.username.isNotBlank() && it.username.trim().equals(currentName, ignoreCase = true)) ||
                 (currentPhone.isNotBlank() && it.userPhone.isNotBlank() && it.userPhone.trim() == currentPhone))
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CasinoBg)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(bottom = 90.dp)
    ) {
        // Balance Banner
        Card(
            modifier = Modifier.fillMaxWidth().testTag("withdraw_balance_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.5f), CasinoBorderSubtle)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = StringRes.t(language, "উত্তোলনযোগ্য ব্যালেন্স", "Withdrawable Balance"),
                    color = Slate400,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = StringRes.formatBDT(if (userProfile.isLoggedIn) userProfile.balanceBDT else 0.0, language),
                    color = AccentEmerald,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Guest Registration Banner if not logged in
        if (!userProfile.isLoggedIn) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth().testTag("withdraw_guest_banner"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(GoldPrimary, NagadOrange))
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(text = "🔒", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = StringRes.t(language, "উত্তোলনের জন্য একাউন্ট প্রয়োজন", "Account Required to Withdraw"),
                                color = GoldLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = StringRes.t(
                                    language,
                                    "টাকা উত্তোলন করতে এখনই সম্পূর্ণ বিনামূল্যে রেজিস্ট্রেশন অথবা লগইন করুন।",
                                    "Please register 100% free or login to withdraw your winnings."
                                ),
                                color = Slate300,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onOpenAuth?.invoke(1) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("withdraw_guest_register_btn")
                    ) {
                        Text(
                            text = StringRes.t(language, "রেজিস্ট্রেশন", "Register"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Method Selection
        Text(
            text = StringRes.t(language, "১. উত্তোলন মেথড সিলেক্ট করুন", "1. Select Withdrawal Method"),
            color = GoldLight,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(PaymentMethod.BKASH, PaymentMethod.NAGAD).forEach { method ->
                val isSelected = method == selectedMethod
                val brandColor = Color(method.colorHex)
                val methodTitle = if (method == PaymentMethod.BKASH) {
                    StringRes.t(language, "বিকাশ", "bKash")
                } else {
                    StringRes.t(language, "নগদ", "Nagad")
                }
                val methodSubtitle = StringRes.t(language, "দ্রুত ক্যাশইন", "Fast Cashin")

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedMethod = method }
                        .testTag("withdraw_method_${method.name}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) brandColor.copy(alpha = 0.22f) else Slate800
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            if (isSelected) listOf(brandColor, GoldPrimary)
                            else listOf(CasinoBorderSubtle, CasinoBorderSubtle)
                        ),
                        width = if (isSelected) 2.dp else 1.dp
                    )
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color.White else Slate900.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, if (isSelected) brandColor.copy(alpha = 0.5f) else CasinoBorderSubtle)
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = if (method == PaymentMethod.BKASH) R.drawable.img_bkash_pure else R.drawable.img_nagad_pure
                                    ),
                                    contentDescription = method.displayName,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = methodTitle,
                                    color = if (isSelected) Color.White else Slate100,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.3.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = methodSubtitle,
                                    color = if (isSelected) brandColor else Slate400,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(16.dp)
                                    .background(brandColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Account Number Input (Personal Account Requirement)
        Text(
            text = if (selectedMethod == PaymentMethod.BKASH)
                StringRes.t(language, "২. আপনার বিকাশ পার্সোনাল নম্বর", "2. Your bKash Personal Number")
            else
                StringRes.t(language, "২. আপনার নগদ পার্সোনাল নম্বর", "2. Your Nagad Personal Number"),
            color = GoldLight,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = GoldLight.copy(alpha = 0.12f),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(GoldPrimary, GoldLight))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "⚠️", fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedMethod == PaymentMethod.BKASH)
                        StringRes.t(language, "উত্তোলনের জন্য অবশ্যই আপনার বিকাশ পার্সোনাল নম্বর দিন।", "Please provide your bKash Personal number for withdrawal.")
                    else
                        StringRes.t(language, "উত্তোলনের জন্য অবশ্যই আপনার নগদ পার্সোনাল নম্বর দিন।", "Please provide your Nagad Personal number for withdrawal."),
                    color = GoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = receiverPhone,
            onValueChange = { receiverPhone = it },
            label = {
                Text(
                    if (selectedMethod == PaymentMethod.BKASH)
                        StringRes.t(language, "বিকাশ পার্সোনাল নম্বর (01XXXXXXXXX)", "bKash Personal (01XXXXXXXXX)")
                    else
                        StringRes.t(language, "নগদ পার্সোনাল নম্বর (01XXXXXXXXX)", "Nagad Personal (01XXXXXXXXX)")
                )
            },
            leadingIcon = { Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = GoldLight) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_withdraw_phone"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = CasinoBorderSubtle,
                focusedTextColor = Slate100,
                unfocusedTextColor = Slate100
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Amount Selection
        Text(
            text = StringRes.t(language, "৩. উত্তোলনের পরিমাণ (মিনিমাম ৳৫০০)", "3. Withdrawal Amount (Min ৳500)"),
            color = GoldLight,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick amount chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickAmounts.forEach { amt ->
                val isSelected = withdrawAmount == amt.toString()
                FilterChip(
                    selected = isSelected,
                    onClick = { withdrawAmount = amt.toString() },
                    label = { Text(StringRes.formatBDTShort(amt.toDouble(), language), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f).testTag("withdraw_chip_$amt"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = Color.Black,
                        containerColor = Slate800,
                        labelColor = Slate300
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = withdrawAmount,
            onValueChange = { withdrawAmount = it },
            label = { Text(StringRes.t(language, "টাকার পরিমাণ লিখুন", "Enter Amount")) },
            leadingIcon = { Text("৳", color = GoldLight, fontSize = 18.sp, fontWeight = FontWeight.Black) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_withdraw_amount"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = CasinoBorderSubtle,
                focusedTextColor = Slate100,
                unfocusedTextColor = Slate100
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Submit Button
        Button(
            onClick = {
                if (!userProfile.isLoggedIn) {
                    onOpenAuth?.invoke(1)
                } else {
                    val amt = withdrawAmount.toDoubleOrNull() ?: 0.0
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onSubmitWithdrawal(selectedMethod, amt, receiverPhone)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_withdraw_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
        ) {
            Icon(Icons.Default.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (userProfile.isLoggedIn)
                    StringRes.t(language, "উত্তোলন নিশ্চিত করুন", "Confirm Withdrawal")
                else
                    StringRes.t(language, "উত্তোলনের জন্য রেজিস্ট্রেশন করুন", "Register to Withdraw"),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Live Withdrawal Status Section
        LiveWithdrawalStatusSection(
            language = language,
            pastWithdrawals = pastWithdrawals
        )
    }
}

@Composable
private fun LiveWithdrawalStatusSection(
    language: AppLanguage,
    pastWithdrawals: List<TransactionRecord>
) {
    val liveWithdrawalFeeds = remember {
        listOf(
            Triple("017****9941", "৳১,৫০০ (bKash)", "কমপ্লিট"),
            Triple("019****3201", "৳২,০০০ (Nagad)", "প্রসেসিং"),
            Triple("018****4429", "৳৫,০০০ (bKash)", "কমপ্লিট"),
            Triple("016****7182", "৳৮০০ (bKash)", "কমপ্লিট"),
            Triple("013****6610", "৳৩,০০০ (Nagad)", "প্রসেসিং"),
            Triple("017****2233", "৳১০,০০০ (bKash)", "কমপ্লিট"),
            Triple("019****8514", "৳১,২০০ (Nagad)", "প্রসেসিং")
        )
    }

    var liveFeedOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3500L)
            liveFeedOffset = (liveFeedOffset + 1) % liveWithdrawalFeeds.size
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CasinoBorderSubtle, CasinoBorderSubtle)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⚡", fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = StringRes.t(language, "লাইভ উত্তোলন অনুমোদন হিস্ট্রি", "Live Withdrawal Approvals"),
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AccentCyan.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(AccentCyan.copy(alpha = 0.4f), AccentCyan.copy(alpha = 0.4f))))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AccentCyan))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = StringRes.t(language, "লাইভ", "LIVE"),
                            color = AccentCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Display user's own submissions if any
            if (pastWithdrawals.isNotEmpty()) {
                Text(
                    text = StringRes.t(language, "আপনার সাম্প্রতিক আবেদন:", "Your Recent Requests:"),
                    color = Slate300,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                pastWithdrawals.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate700,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "${item.method.displayName} • ${item.accountNo}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = item.timeFormatted, color = Slate400, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "৳%,.0f".format(item.amount), color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(item.status.colorHex).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.BN) item.status.bn else item.status.en,
                                        color = Color(item.status.colorHex),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Live scrolling feeds of community withdrawals
            for (i in 0 until 4) {
                val feed = liveWithdrawalFeeds[(liveFeedOffset + i) % liveWithdrawalFeeds.size]
                val isDone = feed.third == "কমপ্লিট"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isDone) AccentEmerald else Color(0xFFFFB300))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = feed.first, color = Slate200, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = feed.second, color = Slate400, fontSize = 11.sp)
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDone) AccentEmerald.copy(alpha = 0.15f) else Color(0x22FFB300)
                    ) {
                        Text(
                            text = if (isDone)
                                StringRes.t(language, "কমপ্লিট", "Complete")
                            else
                                StringRes.t(language, "প্রসেস এ আছে", "Processing"),
                            color = if (isDone) AccentEmerald else Color(0xFFFFB300),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
