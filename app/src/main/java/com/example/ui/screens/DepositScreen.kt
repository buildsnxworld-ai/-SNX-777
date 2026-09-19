package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AdminManager
import com.example.data.SharedDataStore
import com.example.data.SnxCloudSyncService
import com.example.model.AppLanguage
import com.example.model.PaymentMethod
import com.example.model.UserProfile
import com.example.ui.theme.*
import com.example.util.StringRes
import kotlinx.coroutines.delay

@Composable
fun DepositScreen(
    userProfile: UserProfile,
    language: AppLanguage,
    onSubmitDeposit: (PaymentMethod, Double, String, String) -> Boolean,
    onShowToast: (String) -> Unit,
    onOpenAuth: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val adminManager = remember { AdminManager.getInstance(context) }
    val paymentNumbers by adminManager.paymentNumbers.collectAsState()
    var selectedMethod by remember { mutableStateOf(PaymentMethod.BKASH) }
    var currentDepositNumber by remember { mutableStateOf("") }

    // Pick a random number once when user enters the page or switches method.
    // It will NEVER change or fluctuate while the user remains on the deposit page.
    fun pickRandomNumberForMethod(method: PaymentMethod, numbers: List<com.example.data.AdminPaymentNumber>) {
        val activeMatches = numbers.filter { it.method == method && it.isActive && it.number.isNotBlank() }
        currentDepositNumber = if (activeMatches.isNotEmpty()) {
            activeMatches.random().number
        } else {
            adminManager.getRandomActiveDepositNumber(method)
        }
    }

    // Only pick when entering the page / initial composition, and keep active cloud sync running
    LaunchedEffect(Unit) {
        adminManager.reloadFromStorage()
        SharedDataStore.pullFromOtherApp(context)
        SnxCloudSyncService.pullFromCloud(context)
        adminManager.reloadFromStorage()
        if (currentDepositNumber.isBlank()) {
            pickRandomNumberForMethod(selectedMethod, adminManager.paymentNumbers.value)
        }
        while (true) {
            kotlinx.coroutines.delay(1500)
            SnxCloudSyncService.pullFromCloud(context)
        }
    }

    // When user explicitly switches method (bKash <-> Nagad), pick a random number for that method
    LaunchedEffect(selectedMethod) {
        pickRandomNumberForMethod(selectedMethod, adminManager.paymentNumbers.value)
    }

    // When payment numbers update from Admin or Cloud, ensure current deposit number is active and valid
    LaunchedEffect(paymentNumbers) {
        val activeMatches = paymentNumbers.filter { it.method == selectedMethod && it.isActive && it.number.isNotBlank() }
        if (currentDepositNumber.isBlank() || (activeMatches.isNotEmpty() && activeMatches.none { it.number == currentDepositNumber })) {
            pickRandomNumberForMethod(selectedMethod, paymentNumbers)
        }
    }
    var selectedAmount by remember { mutableStateOf(300.0) }
    var customAmountText by remember { mutableStateOf("") }
    var senderPhoneInput by remember { mutableStateOf(userProfile.phone) }
    var trxIdInput by remember { mutableStateOf("") }
    var selectedBonus by remember { mutableStateOf("5% Bonus on Deposit (৳300+)") }

    val quickAmounts = listOf(300.0, 500.0, 1000.0, 2000.0, 5000.0, 10000.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CasinoBg)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(bottom = 90.dp)
    ) {
        // Step 1: Select Payment Gateway (bKash & Nagad)
        Text(
            text = StringRes.t(language, "১. পেমেন্ট মেথড নির্বাচন", "1. Select Payment Method"),
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
                val methodSubtitle = StringRes.t(language, "স্বয়ংক্রিয় ইনস্ট্যান্ট", "Auto Instant")

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedMethod = method }
                        .testTag("method_${method.name}"),
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

        // Step 2: Agent/Personal Number & Send Money Copy (Numbers temporarily removed)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("agent_number_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(selectedMethod.colorHex).copy(alpha = 0.7f), CasinoBorderSubtle)))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                val activeNumber = if (currentDepositNumber.isNotBlank()) currentDepositNumber else adminManager.getActiveDepositNumber(selectedMethod)
                if (activeNumber.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (selectedMethod == PaymentMethod.BKASH)
                                        "এই নাম্বারে শুধুমাত্র বিকাশ থেকে সেন্ড মানি গ্রহণ করা হয়"
                                    else
                                        "এই নাম্বারে শুধুমাত্র নগদ থেকে সেন্ড মানি গ্রহণ করা হয়",
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeNumber,
                                    color = GoldLight,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Deposit Number", activeNumber)
                                clipboard.setPrimaryClip(clip)
                                onShowToast(
                                    if (language == AppLanguage.BN) "সেন্ড মানি নম্বরটি কপি করা হয়েছে: $activeNumber"
                                    else "Send Money number copied: $activeNumber"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp).testTag("copy_agent_number_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = StringRes.t(language, "কপি", "Copy"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = StringRes.t(
                                    language,
                                    if (selectedMethod == PaymentMethod.BKASH)
                                        "বিকাশ ডিপোজিট নম্বর সাময়িকভাবে আপডেট হচ্ছে"
                                    else
                                        "নগদ ডিপোজিট নম্বর সাময়িকভাবে আপডেট হচ্ছে",
                                    if (selectedMethod == PaymentMethod.BKASH)
                                        "bKash deposit number is temporarily being updated"
                                    else
                                        "Nagad deposit number is temporarily being updated"
                                ),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = StringRes.t(
                                    language,
                                    "সঠিক ডিপোজিট নম্বরের জন্য লাইভ চ্যাট অথবা সাপোর্টে যোগাযোগ করুন।",
                                    "Please contact Live Chat or Support for the updated deposit number."
                                ),
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5% Bonus Highlight Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Slate800,
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(AccentEmerald.copy(alpha = 0.6f), GoldPrimary))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentEmerald.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎁", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = StringRes.t(language, "৫% ইনস্ট্যান্ট ডিপোজিট বোনাস অফার", "5% Instant Deposit Bonus Offer"),
                        color = GoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = StringRes.t(
                            language,
                            "সর্বনিম্ন ৳৩০০ টাকা ডিপোজিটে ৫% এক্সট্রা বোনাস সরাসরি একাউন্টে যোগ হবে!",
                            "Min ৳300 deposit gets extra 5% bonus credited directly to your balance!"
                        ),
                        color = Slate300,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step 3: Amount Selection
        Text(
            text = StringRes.t(language, "২. টাকার পরিমাণ নির্ধারণ করুন (BDT)", "2. Select Deposit Amount (BDT)"),
            color = GoldLight,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick amount chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickAmounts.take(3).forEach { amt ->
                val isSelected = selectedAmount == amt && customAmountText.isEmpty()
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedAmount = amt
                        customAmountText = ""
                    },
                    label = { Text(StringRes.formatBDTShort(amt, language), fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f).testTag("chip_amount_${amt.toInt()}"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = Color.Black,
                        containerColor = Slate800,
                        labelColor = Slate300
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickAmounts.drop(3).forEach { amt ->
                val isSelected = selectedAmount == amt && customAmountText.isEmpty()
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedAmount = amt
                        customAmountText = ""
                    },
                    label = { Text(StringRes.formatBDTShort(amt, language), fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f).testTag("chip_amount_${amt.toInt()}"),
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

        // Custom Amount Field
        OutlinedTextField(
            value = customAmountText,
            onValueChange = {
                customAmountText = it
                val parsed = it.toDoubleOrNull()
                if (parsed != null && parsed > 0) {
                    selectedAmount = parsed
                }
            },
            label = { Text(StringRes.t(language, "অথবা অন্যান্য পরিমাণ (মিনিমাম ৳৩০০)", "Or enter custom amount (Min ৳300)")) },
            leadingIcon = { Text("৳", color = GoldLight, fontSize = 18.sp, fontWeight = FontWeight.Black) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_deposit_amount"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = CasinoBorderSubtle,
                focusedTextColor = Slate100,
                unfocusedTextColor = Slate100
            )
        )

        // Bonus calculation breakdown preview
        val currentDepositAmt = if (customAmountText.isNotBlank()) customAmountText.toDoubleOrNull() ?: selectedAmount else selectedAmount
        val bonusAmt = if (currentDepositAmt >= 300) currentDepositAmt * 0.05 else 0.0
        val totalToReceive = currentDepositAmt + bonusAmt

        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Slate800,
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CasinoBorderSubtle, CasinoBorderSubtle))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = StringRes.t(language, "জমা + ৫% বোনাস:", "Deposit + 5% Bonus:"),
                        color = Slate400,
                        fontSize = 11.sp
                    )
                    Text(
                        text = StringRes.t(
                            language,
                            "${StringRes.formatBDTShort(currentDepositAmt, language)} + ${StringRes.formatBDTShort(bonusAmt, language)} (বোনাস)",
                            "${StringRes.formatBDTShort(currentDepositAmt, language)} + ${StringRes.formatBDTShort(bonusAmt, language)} (Bonus)"
                        ),
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = StringRes.t(language, "মোট জমা হবে", "Total Credited"),
                        color = Slate400,
                        fontSize = 10.sp
                    )
                    Text(
                        text = StringRes.formatBDTShort(totalToReceive, language),
                        color = AccentEmerald,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 3: Sender Account Number & TrxID
        Text(
            text = StringRes.t(language, "৩. আপনার প্রেরক নম্বর ও ট্রানজেকশন আইডি (TrxID)", "3. Your Sender Number & Transaction ID"),
            color = GoldLight,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = senderPhoneInput,
            onValueChange = { senderPhoneInput = it },
            label = {
                Text(
                    if (selectedMethod == PaymentMethod.BKASH)
                        StringRes.t(language, "আপনার বিকাশ নম্বর (যেখান থেকে টাকা পাঠিয়েছেন)", "Your bKash Number (Sender)")
                    else
                        StringRes.t(language, "আপনার নগদ নম্বর (যেখান থেকে টাকা পাঠিয়েছেন)", "Your Nagad Number (Sender)")
                )
            },
            placeholder = { Text("01XXXXXXXXX", color = Slate500) },
            leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = GoldLight) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_deposit_sender_phone"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = CasinoBorderSubtle,
                focusedTextColor = Slate100,
                unfocusedTextColor = Slate100
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = trxIdInput,
            onValueChange = { trxIdInput = it },
            label = { Text(StringRes.t(language, "ট্রানজেকশন আইডি (TrxID)", "Transaction ID (TrxID)")) },
            placeholder = { Text("যেমন: 9J8K2LA1", color = Slate500) },
            leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = GoldLight) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_trx_id"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = CasinoBorderSubtle,
                focusedTextColor = Slate100,
                unfocusedTextColor = Slate100
            )
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Confirm Deposit Button
        Button(
            onClick = {
                val amt = if (customAmountText.isNotBlank()) customAmountText.toDoubleOrNull() ?: selectedAmount else selectedAmount
                if (amt < 300) {
                    onShowToast(if (language == AppLanguage.BN) "সর্বনিম্ন ডিপোজিট ৩০০ টাকা" else "Minimum deposit is ৳300")
                    return@Button
                }
                val phone = senderPhoneInput.trim().ifEmpty { userProfile.phone }
                if (trxIdInput.trim().length < 5) {
                    onShowToast(if (language == AppLanguage.BN) "অনুগ্রহ করে সঠিক ট্রানজেকশন আইডি (TrxID) দিন" else "Please enter valid TrxID")
                    return@Button
                }

                if (onSubmitDeposit(selectedMethod, amt, phone, trxIdInput.trim())) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    trxIdInput = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_deposit_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = StringRes.t(
                    language,
                    "ডিপোজিট কনফার্ম করুন (৳%,.0f)".format(currentDepositAmt),
                    "Confirm Deposit (৳%,.0f)".format(currentDepositAmt)
                ),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Live Deposit Approval Feed / History
        LiveDepositApprovalSection(language = language)
    }
}

@Composable
private fun LiveDepositApprovalSection(language: AppLanguage) {
    val liveApprovals = remember {
        listOf(
            Triple("017****3291", "৳১,০০০ (bKash)", "অনুমোদিত"),
            Triple("019****8824", "৳৩০০ (Nagad)", "প্রক্রিয়াধীন"),
            Triple("018****5610", "৳৫,০০০ (bKash)", "অনুমোদিত"),
            Triple("016****9941", "৳২,০০০ (Nagad)", "অনুমোদিত"),
            Triple("013****1209", "৳৫০০ (bKash)", "প্রক্রিয়াধীন"),
            Triple("017****7732", "৳১০,০০০ (bKash)", "অনুমোদিত"),
            Triple("019****4021", "৳১,৫০০ (Nagad)", "অনুমোদিত")
        )
    }

    var startIdx by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000L)
            startIdx = (startIdx + 1) % liveApprovals.size
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
                    Text(text = "📡", fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = StringRes.t(language, "লাইভ ডিপোজিট অনুমোদন হিস্ট্রি", "Live Deposit Approvals"),
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AccentEmerald.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(AccentEmerald.copy(alpha = 0.4f), AccentEmerald.copy(alpha = 0.4f))))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AccentEmerald))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = StringRes.t(language, "লাইভ ফিড", "LIVE FEED"),
                            color = AccentEmerald,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            for (i in 0 until 4) {
                val item = liveApprovals[(startIdx + i) % liveApprovals.size]
                val isComplete = item.third == "অনুমোদিত"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isComplete) AccentEmerald else Color(0xFFFFB300))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item.first, color = Slate200, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = item.second, color = Slate400, fontSize = 11.sp)
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isComplete) AccentEmerald.copy(alpha = 0.15f) else Color(0x22FFB300)
                    ) {
                        Text(
                            text = if (isComplete)
                                StringRes.t(language, "অনুমোদিত", "Approved")
                            else
                                StringRes.t(language, "প্রসেসিং", "Processing"),
                            color = if (isComplete) AccentEmerald else Color(0xFFFFB300),
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

