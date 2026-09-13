package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppLanguage
import com.example.ui.theme.*
import com.example.util.StringRes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean, val time: String)

data class SupportFaq(val questionBn: String, val questionEn: String, val answerBn: String, val answerEn: String)

@Composable
fun CustomerSupportModal(
    isOpen: Boolean,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val safeDismiss = {
        focusManager.clearFocus()
        keyboardController?.hide()
        onDismiss()
    }

    var selectedSupportTab by remember { mutableIntStateOf(0) } // 0: Live Chat, 1: VIP Channels, 2: Callback, 3: FAQ

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    if (language == AppLanguage.BN)
                        "স্বাগতম SNX 777 লাইভ ২৪/৭ গ্রাহক সেবায়! আমি আরিফুল ইসলাম, আপনার ব্যক্তিগত সিনিয়র সাপোর্ট স্পেশালিস্ট। ডিপোজিট, উইথড্রয়াল বা যেকোনো প্রয়োজনে আমাকে জানান।"
                    else
                        "Welcome to SNX 777 24/7 Live Support! I am Ariful Islam, your dedicated senior specialist. How may I assist you with deposit, cashout, or games?",
                    isUser = false,
                    time = "12:00 PM"
                )
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isAgentTyping by remember { mutableStateOf(false) }

    // Callback Request form states
    var callbackPhone by remember { mutableStateOf("") }
    var callbackIssueCategory by remember { mutableStateOf("ডিপোজিট সমস্যা") }
    var callbackSubmitted by remember { mutableStateOf(false) }

    val faqs = remember {
        listOf(
            SupportFaq(
                questionBn = "বিকাশ বা নগদে ডিপোজিট কতক্ষণে একাউন্টে জমা হয়?",
                questionEn = "How long does bKash/Nagad deposit take to credit?",
                answerBn = "ডিপোজিট করার পর TrxID সাবমিট করলে আমাদের অটোমেটিক গেটওয়ে সাধারণত ১ থেকে ৩ মিনিটের মধ্যে ব্যালেন্সে টাকা ও ৫% নিশ্চিত বোনাস যোগ করে দেয়।",
                answerEn = "Once submitted with TrxID, our automated gateway credits your balance with 5% bonus within 1 to 3 minutes."
            ),
            SupportFaq(
                questionBn = "উত্তোলন (Cash Out) করতে কোনো টার্নওভার শর্ত আছে কি?",
                questionEn = "Are there any turnover requirements for withdrawals?",
                answerBn = "না! SNX 777 বাংলাদেশের একমাত্র প্ল্যাটফর্ম যেখানে কোনো প্রকার বাধ্যতামূলক টার্নওভারের শর্ত নেই। আপনি যেকোনো সময় সম্পূর্ণ উইনিং বিকাশ ও নগদে ক্যাশআউট করতে পারবেন।",
                answerEn = "No! SNX 777 is Bangladesh's top platform with ZERO mandatory turnover. You can cash out your entire winnings anytime to bKash & Nagad."
            ),
            SupportFaq(
                questionBn = "সর্বনিম্ন ও সর্বোচ্চ ডিপোজিট এবং উত্তোলনের সীমা কত?",
                questionEn = "What are the minimum and maximum limits for deposit & withdrawal?",
                answerBn = "সর্বনিম্ন ডিপোজিট ৳৩০০ এবং সর্বোচ্চ ৳২৫,০০০ পর্যন্ত। সর্বনিম্ন উত্তোলন ৳৫০০ এবং দৈনিক কোনো উইথড্রয়াল লিমিট বা চার্জ নেই।",
                answerEn = "Minimum deposit is ৳300 and max ৳25,000 per transaction. Minimum withdrawal is ৳500 with zero charges."
            ),
            SupportFaq(
                questionBn = "ভুল TrxID বা নম্বরে টাকা সেন্ড মানি হলে করণীয় কি?",
                questionEn = "What should I do if incorrect TrxID or wrong number was sent?",
                answerBn = "চিন্তার কোনো কারণ নেই! লাইভ চ্যাটে আপনার প্রেরক নম্বর ও সঠিক TrxID লিখে পাঠান অথবা আমাদের ২৪/৭ হোয়াটসঅ্যাপে মেসেজ দিন, সাপোর্ট টিম সঙ্গে সঙ্গে ম্যানুয়ালি ভেরিফাই করে দেবে।",
                answerEn = "No worries! Provide your sender mobile & TrxID in Live Chat or message our WhatsApp helpline for instant manual verification."
            ),
            SupportFaq(
                questionBn = "প্রথম ডিপোজিটে ৫০% ওয়েলকাম বোনাস কীভাবে পাব?",
                questionEn = "How do I claim the 50% First Deposit Bonus?",
                answerBn = "রেজিস্ট্রেশনের পর আপনার প্রথম ডিপোজিটের সাথে স্বয়ংক্রিয়ভাবে ৫০% অতিরিক্ত বোনাস ক্যাশ একাউন্টে যুক্ত হয়ে যাবে।",
                answerEn = "On your first deposit after registration, 50% extra welcome bonus cash is automatically added to your account."
            )
        )
    }

    var expandedFaqIndex by remember { mutableIntStateOf(-1) }

    Dialog(
        onDismissRequest = safeDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .imePadding()
                .testTag("support_dialog_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(GoldPrimary.copy(alpha = 0.6f), AccentCrimson.copy(alpha = 0.4f), CasinoBorderSubtle))
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar with Agent Details and Live Indicator
                Surface(
                    color = Slate800,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(listOf(GoldPrimary, Color(0xFF6B4E00)))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.HeadsetMic,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = StringRes.t(language, "আরিফুল ইসলাম (সিনিয়র সাপোর্ট)", "Ariful Islam (Senior Agent)"),
                                            color = GoldLight,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Verified",
                                            tint = AccentCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(AccentEmerald)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = StringRes.t(language, "অনলাইন | গড় রেসপন্স: < ৩০ সে.", "Online | Avg Reply: < 30s"),
                                            color = AccentEmerald,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = safeDismiss,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate300, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Multi-Feature Tab Navigation Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.45f))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val tabs = listOf(
                                StringRes.t(language, "💬 চ্যাট", "💬 Chat"),
                                StringRes.t(language, "⚡ চ্যানেল", "⚡ Direct"),
                                StringRes.t(language, "📞 কলব্যাক", "📞 Call"),
                                StringRes.t(language, "❓ FAQ", "❓ FAQ")
                            )
                            tabs.forEachIndexed { index, title ->
                                val isSelected = selectedSupportTab == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) GoldPrimary else Color.Transparent)
                                        .clickable { selectedSupportTab = index }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        color = if (isSelected) Color.Black else Slate300,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Tab Content Body
                when (selectedSupportTab) {
                    0 -> {
                        // --- TAB 1: LIVE CHAT & INSTANT ISSUE ROUTING ---
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Chat Messages
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(messages) { msg ->
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (msg.isUser) GoldPrimary else Slate800,
                                            border = if (msg.isUser) null else CardDefaults.outlinedCardBorder().copy(
                                                brush = Brush.linearGradient(listOf(CasinoBorderSubtle, CasinoBorderSubtle))
                                            ),
                                            modifier = Modifier.widthIn(max = 280.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = msg.text,
                                                    color = if (msg.isUser) Color.Black else Slate100,
                                                    fontSize = 12.sp,
                                                    lineHeight = 17.sp,
                                                    fontWeight = if (msg.isUser) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = msg.time,
                                                    color = if (msg.isUser) Color.Black.copy(alpha = 0.65f) else Slate400,
                                                    fontSize = 9.sp,
                                                    modifier = Modifier.align(Alignment.End)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (isAgentTyping) {
                                    item {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Slate800,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(12.dp),
                                                    strokeWidth = 2.dp,
                                                    color = GoldPrimary
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = StringRes.t(language, "আরিফুল ইসলাম লিখছেন...", "Agent is typing..."),
                                                    color = GoldLight,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Quick Category Action Chips (Like real betting site chatbots)
                            Text(
                                text = StringRes.t(language, "কুইক সমস্যা নির্বাচন করুন:", "Quick Select Issue:"),
                                color = Slate400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val quickIssues = listOf(
                                    Pair("💰 ডিপোজিট জমা হয়নি", "ডিপোজিট রিকোয়েস্ট পাঠিয়ে থাকলে আপনার প্রেরক নম্বর ও TrxID টি দিন। ১ মিনিটের মধ্যে চেক করে ব্যালেন্সে যোগ করা হবে।"),
                                    Pair("⚡ দ্রুত ক্যাশআউট", "আমাদের ক্যাশআউট সার্বক্ষণিক চালু আছে এবং কোনো টার্নওভার নেই। ক্যাশআউট মেনু থেকে বিকাশ বা নগদ নম্বর দিয়ে সাবমিট করলেই ২ মিনিটে টাকা পৌঁছে যাবে।"),
                                    Pair("🎁 ৫০% বোনাস তথ্য", "প্রথম ডিপোজিটে সরাসরি ৫০% বোনাস সক্রিয় হবে। ডিপোজিট করার পর স্বয়ংক্রিয়ভাবে একাউন্টে জমা হবে।")
                                )
                                quickIssues.forEach { (chipLabel, autoResponse) ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Slate800,
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.4f), Slate700))
                                        ),
                                        modifier = Modifier.clickable {
                                            messages = messages + ChatMessage(chipLabel, true, "Just now")
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(messages.size - 1)
                                                isAgentTyping = true
                                                delay(800L)
                                                isAgentTyping = false
                                                messages = messages + ChatMessage(autoResponse, false, "Just now")
                                                listState.animateScrollToItem(messages.size - 1)
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = chipLabel,
                                            color = GoldLight,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Chat Input Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Slate800)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    placeholder = {
                                        Text(
                                            StringRes.t(language, "আপনার সমস্যা বা প্রশ্ন লিখুন...", "Type your issue or query..."),
                                            fontSize = 12.sp,
                                            color = Slate400
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = CasinoBorderSubtle,
                                        focusedTextColor = Slate100,
                                        unfocusedTextColor = Slate100
                                    )
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = {
                                        if (inputText.isNotBlank()) {
                                            val userMsg = inputText.trim()
                                            inputText = ""
                                            messages = messages + ChatMessage(userMsg, true, "Just now")
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(messages.size - 1)
                                                isAgentTyping = true
                                                delay(1000L)
                                                isAgentTyping = false
                                                val reply = if (language == AppLanguage.BN)
                                                    "ধন্যবাদ! আপনার বার্তাটি প্রাপ্ত হয়েছে। আমি বিষয়টি সরাসরি চেক করে অবিলম্বে সমাধান দিচ্ছি।"
                                                else
                                                    "Thank you! Your message has been received. I am verifying this directly to assist you immediately."
                                                messages = messages + ChatMessage(reply, false, "Just now")
                                                listState.animateScrollToItem(messages.size - 1)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    1 -> {
                        // --- TAB 2: VIP OFFICIAL DIRECT CHANNELS ---
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Black.copy(alpha = 0.35f),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(GoldPrimary, AccentCrimson)))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = StringRes.t(
                                                language,
                                                "অফিসিয়াল ভেরিফাইড সাপোর্ট চ্যানেল। প্রতারণা এড়াতে শুধুমাত্র নিচের অফিসিয়াল মাধ্যমে যোগাযোগ করুন।",
                                                "Official verified support channels. Connect safely via our verified links below."
                                            ),
                                            color = Slate200,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }

                            // WhatsApp 24/7 VIP
                            item {
                                ChannelCard(
                                    title = "WhatsApp VIP সাপোর্ট (২৪/৭)",
                                    subtitle = "+880 1819-777000",
                                    description = "তাত্ক্ষণিক চ্যাট, স্ক্রিনশট ও দ্রুত ডিপোজিট/উইথড্রয়াল সহায়তা",
                                    badge = "সবচেয়ে দ্রুত",
                                    badgeColor = AccentEmerald,
                                    actionLabel = "কপি ও চ্যাট করুন",
                                    onAction = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("WhatsApp", "+8801819777000"))
                                    }
                                )
                            }

                            // Telegram Official Channel
                            item {
                                ChannelCard(
                                    title = "Telegram অফিশিয়াল চ্যানেল ও হেল্পলাইন",
                                    subtitle = "@SNX777_Support_BD",
                                    description = "লাইভ বট, অফার আপডেট এবং ২৪ ঘণ্টা ডেডিকেটেড এজেন্ট",
                                    badge = "ভেরিফাইড",
                                    badgeColor = AccentCyan,
                                    actionLabel = "কপি করুন",
                                    onAction = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Telegram", "@SNX777_Support_BD"))
                                    }
                                )
                            }

                            // Toll-Free Voice Hotline
                            item {
                                ChannelCard(
                                    title = "২৪/৭ অফিশিয়াল কাস্টমার কেয়ার হটলাইন",
                                    subtitle = "+880 9612-777777",
                                    description = "যেকোনো জরুরী প্রয়োজনে সরাসরি কল করুন",
                                    badge = "টোল ফ্রি",
                                    badgeColor = GoldPrimary,
                                    actionLabel = "নম্বর কপি করুন",
                                    onAction = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Hotline", "+8809612777777"))
                                    }
                                )
                            }

                            // Email Support
                            item {
                                ChannelCard(
                                    title = "অফিশিয়াল সাপোর্ট ইমেইল",
                                    subtitle = "support@snx777.com",
                                    description = "একাউন্ট ভেরিফিকেশন ও বিজনেস জিজ্ঞাসা",
                                    badge = "অফিসিয়াল",
                                    badgeColor = Slate400,
                                    actionLabel = "ইমেইল কপি করুন",
                                    onAction = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Email", "support@snx777.com"))
                                    }
                                )
                            }
                        }
                    }

                    2 -> {
                        // --- TAB 3: CALLBACK REQUEST SERVICE ---
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = StringRes.t(language, "📞 ইনস্ট্যান্ট কলব্যাক রিকোয়েস্ট", "📞 Instant Callback Request"),
                                color = GoldLight,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = StringRes.t(
                                    language,
                                    "আপনার মোবাইল নম্বর প্রদান করুন। আমাদের সিনিয়র সাপোর্ট স্পেশালিস্ট আগামী ৫ মিনিটের মধ্যে আপনার সাথে সরাসরি কথা বলবেন।",
                                    "Submit your phone number. Our senior specialist will call you directly within 5 minutes."
                                ),
                                color = Slate300,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            if (callbackSubmitted) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AccentEmerald.copy(alpha = 0.15f),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(AccentEmerald, GoldPrimary))),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = StringRes.t(language, "কলব্যাক রিকোয়েস্ট সফলভাবে গৃহীত হয়েছে! ✅", "Callback Request Submitted! ✅"),
                                            color = GoldLight,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = StringRes.t(
                                                language,
                                                "টিকেট আইডি: #CB${System.currentTimeMillis() % 10000} | আগামী ৫ মিনিটের মধ্যে আমাদের এজেন্ট আপনার $callbackPhone নম্বরে কল করবেন।",
                                                "Ticket #CB${System.currentTimeMillis() % 10000} | Agent will call $callbackPhone shortly."
                                            ),
                                            color = Slate300,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                OutlinedTextField(
                                    value = callbackPhone,
                                    onValueChange = { callbackPhone = it },
                                    label = { Text(StringRes.t(language, "আপনার মোবাইল নম্বর (যেমন: 017xxxxxxxx)", "Your Mobile Number"), color = Slate400) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = CasinoBorderSubtle,
                                        focusedTextColor = Slate100,
                                        unfocusedTextColor = Slate100
                                    )
                                )

                                Text(
                                    text = StringRes.t(language, "সমস্যার ধরন নির্বাচন করুন:", "Select Issue Category:"),
                                    color = Slate300,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                val issues = listOf("ডিপোজিট সমস্যা", "উইথড্রয়াল বিলম্ব", "বোনাস অনুসন্ধান", "একাউন্ট নিরাপত্তা")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    issues.forEach { issue ->
                                        val isSel = callbackIssueCategory == issue
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSel) GoldPrimary else Slate800,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { callbackIssueCategory = issue }
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = issue,
                                                color = if (isSel) Color.Black else Slate300,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        if (callbackPhone.trim().length >= 10) {
                                            callbackSubmitted = true
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = StringRes.t(language, "কলব্যাক রিকোয়েস্ট পাঠান ❯", "Submit Callback Request ❯"),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        // --- TAB 4: INTERACTIVE FAQ HUB ---
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Text(
                                    text = StringRes.t(language, "💡 সচরাচর জিজ্ঞাসিত প্রশ্ন ও দ্রুত সমাধান:", "💡 Frequently Asked Questions:"),
                                    color = GoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            items(faqs.indices.toList()) { index ->
                                val faq = faqs[index]
                                val isExpanded = expandedFaqIndex == index

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedFaqIndex = if (isExpanded) -1 else index
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Slate800),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.linearGradient(
                                            if (isExpanded) listOf(GoldPrimary, AccentCrimson)
                                            else listOf(CasinoBorderSubtle, CasinoBorderSubtle)
                                        )
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = StringRes.t(language, faq.questionBn, faq.questionEn),
                                                color = if (isExpanded) GoldLight else Slate100,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = GoldLight
                                            )
                                        }

                                        if (isExpanded) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Divider(color = Slate700, thickness = 0.8.dp)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = StringRes.t(language, faq.answerBn, faq.answerEn),
                                                color = Slate300,
                                                fontSize = 11.sp,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelCard(
    title: String,
    subtitle: String,
    description: String,
    badge: String,
    badgeColor: Color,
    actionLabel: String,
    onAction: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Slate800,
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CasinoBorderSubtle, Slate700))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.2f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(badgeColor, badgeColor)))
                ) {
                    Text(
                        text = badge,
                        color = badgeColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, color = Slate400, fontSize = 10.sp, lineHeight = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = actionLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
