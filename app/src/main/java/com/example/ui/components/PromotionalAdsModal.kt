package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.AppLanguage
import com.example.ui.theme.*
import com.example.util.StringRes
import kotlinx.coroutines.delay

data class PromotionalAd(
    val id: Int,
    val imageResId: Int,
    val badge: String,
    val titleBn: String,
    val titleEn: String,
    val subtitleBn: String,
    val subtitleEn: String,
    val highlight1Bn: String,
    val highlight1En: String,
    val highlight2Bn: String,
    val highlight2En: String,
    val gradientColors: List<Color>,
    val ctaBn: String,
    val ctaEn: String,
    val isDepositAction: Boolean = false
)

@Composable
fun PromotionalAdsModal(
    isOpen: Boolean,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onOpenRegister: () -> Unit,
    onGoToDeposit: () -> Unit = onDismiss
) {
    if (!isOpen) return

    val ads = remember {
        listOf(
            // Ad 1: First Deposit 50% Bonus
            PromotionalAd(
                id = 1,
                imageResId = R.drawable.img_casino_hero_banner,
                badge = "স্পেশাল ওয়েলকাম বোনাস",
                titleBn = "প্রথম ডিপোজিট করলেই ৫০% এক্সট্রা বোনাস!",
                titleEn = "Get 50% Extra Bonus on 1st Deposit!",
                subtitleBn = "বিকাশ ও নগদে যেকোনো ডিপোজিটে সরাসরি একাউন্টে ৫০% বোনাস ক্যাশ যোগ হবে! কোনো হিডেন চার্জ নেই।",
                subtitleEn = "Deposit instantly via bKash & Nagad and get 50% extra bonus cash added directly to your balance!",
                highlight1Bn = "🔥 প্রথম ডিপোজিটে সরাসরি ৫০% নিশ্চিত বোনাস",
                highlight1En = "🔥 Direct 50% Guaranteed Bonus on 1st Deposit",
                highlight2Bn = "⚡ বিকাশ ও নগদে ২ মিনিটে ইনস্ট্যান্ট ডিপোজিট",
                highlight2En = "⚡ Instant 2-Minute Deposit via bKash & Nagad",
                gradientColors = listOf(Color(0xFF660A18), Color(0xFF1E1035), Color(0xFF0F0B1E)),
                ctaBn = "ডিপোজিট করুন ও ৫০% বোনাস নিন ❯",
                ctaEn = "Deposit & Claim 50% Bonus ❯",
                isDepositAction = true
            ),
            // Ad 2: #1 Betting Site in Bangladesh with Zero Turnover
            PromotionalAd(
                id = 2,
                imageResId = R.drawable.img_promo_banner_2,
                badge = "বাংলাদেশের ১ নম্বর প্ল্যাটফর্ম",
                titleBn = "বাংলাদেশের নাম্বার ওয়ান বেটিং সাইট - কোনো প্রকার টার্নওভার নাই!",
                titleEn = "Bangladesh #1 Betting Platform - ZERO Turnover!",
                subtitleBn = "এখানে কোনো প্রকার টার্নওভারের শর্ত নেই! আপনার যেকোনো উইনিং টাকা সরাসরি বিকাশ ও নগদে দ্রুত ক্যাশআউট করুন।",
                subtitleEn = "Zero turnover requirements! Cash out all your winnings directly to bKash & Nagad anytime.",
                highlight1Bn = "🚫 কোনো প্রকার টার্নওভার নাই",
                highlight1En = "🚫 Absolutely Zero Turnover Required",
                highlight2Bn = "💰 আনলিমিটেড উইনিং সরাসরি ক্যাশআউট",
                highlight2En = "💰 Instant Unlimited Cashout Anytime",
                gradientColors = listOf(Color(0xFF0D253F), Color(0xFF1E1035), Color(0xFF0F0B1E)),
                ctaBn = "এখনই খেলা শুরু করুন ❯",
                ctaEn = "Play Now ❯",
                isDepositAction = false
            )
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val currentAd = ads[currentIndex]

    // 5-second automatic progression and auto-dismiss timer
    var secondsRemaining by remember(currentIndex) { mutableIntStateOf(5) }

    LaunchedEffect(currentIndex) {
        secondsRemaining = 5
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        }
        if (currentIndex < ads.size - 1) {
            currentIndex++
        } else {
            onDismiss() // Automatically closes after the 2nd ad finishes its 5 seconds
        }
    }

    // Animated progress bar representing the 5 seconds countdown
    var progressTarget by remember(currentIndex) { mutableFloatStateOf(1f) }
    LaunchedEffect(currentIndex) {
        progressTarget = 0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 5000, easing = LinearEasing),
        label = "ad_timer_progress"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp)
                .testTag("promotional_ads_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(
                    listOf(GoldPrimary, AccentCrimson, CasinoBorderSubtle)
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(currentAd.gradientColors)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Control Bar: Step Indicator, 5-second Countdown, and Prominent Close Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ad Step Indicator
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(GoldPrimary, Slate700))
                        )
                    ) {
                        Text(
                            text = StringRes.t(language, "বিজ্ঞাপন ${currentIndex + 1}/${ads.size}", "Ad ${currentIndex + 1}/${ads.size}"),
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // 5-Second Countdown Timer Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AccentCrimson.copy(alpha = 0.35f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.5f), AccentCrimson))
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏱️ ${secondsRemaining}s",
                                color = GoldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Prominent Close Button (Instant Dismiss)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .border(1.2.dp, GoldPrimary.copy(alpha = 0.8f), CircleShape)
                            .testTag("close_promotional_ads_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Ad",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // Smooth 5-Second Depleting Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .padding(horizontal = 14.dp),
                    color = GoldPrimary,
                    trackColor = Color.White.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Premium Header Banner Image (Without any cut-off text)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Image(
                        painter = painterResource(id = currentAd.imageResId),
                        contentDescription = "Promotion Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(165.dp),
                        contentScale = ContentScale.FillWidth,
                        alignment = Alignment.TopCenter
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Badge Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AccentCrimson,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(GoldPrimary, AccentCrimson))
                    )
                ) {
                    Text(
                        text = "🔥 " + currentAd.badge,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Ad Content Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = StringRes.t(language, currentAd.titleBn, currentAd.titleEn),
                        color = GoldLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Subtitle
                    Text(
                        text = StringRes.t(language, currentAd.subtitleBn, currentAd.subtitleEn),
                        color = Slate300,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Highlights
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.45f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.4f), Slate700))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AccentEmerald,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = StringRes.t(language, currentAd.highlight1Bn, currentAd.highlight1En),
                                    color = Slate200,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AccentEmerald,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = StringRes.t(language, currentAd.highlight2Bn, currentAd.highlight2En),
                                    color = Slate200,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Step Dots Indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ads.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == currentIndex) 22.dp else 8.dp, 7.dp)
                                    .clip(CircleShape)
                                    .background(if (index == currentIndex) GoldPrimary else Slate600)
                                    .clickable { currentIndex = index }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Primary Action Button
                    Button(
                        onClick = {
                            if (currentAd.isDepositAction) {
                                onDismiss()
                                onGoToDeposit()
                            } else if (currentIndex < ads.size - 1) {
                                currentIndex++
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("ad_primary_action_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = StringRes.t(language, currentAd.ctaBn, currentAd.ctaEn),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Previous / Next Nav Control Row (without "সাইট ভিজিট করুন" as requested)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentIndex > 0) {
                            TextButton(
                                onClick = { currentIndex-- }
                            ) {
                                Text(
                                    text = StringRes.t(language, "❮ আগের বিজ্ঞাপন", "❮ Previous Ad"),
                                    color = Slate300,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                        }

                        if (currentIndex < ads.size - 1) {
                            TextButton(
                                onClick = { currentIndex = 1 }
                            ) {
                                Text(
                                    text = StringRes.t(language, "পরবর্তী বিজ্ঞাপন ❯", "Next Ad ❯"),
                                    color = GoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
