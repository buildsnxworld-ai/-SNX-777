package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.UserProfile
import com.example.ui.theme.*
import com.example.util.StringRes
import kotlinx.coroutines.delay

@Composable
fun AppHeader(
    userProfile: UserProfile,
    language: AppLanguage,
    onToggleLanguage: (() -> Unit)? = null,
    onOpenAuth: (Int) -> Unit,
    onOpenDeposit: () -> Unit,
    onOpenSupport: () -> Unit,
    siteConfig: com.example.data.SiteCustomization? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CasinoSurface)
            .statusBarsPadding()
            .border(width = 1.dp, color = CasinoBorderSubtle, shape = RoundedCornerShape(0.dp))
    ) {
        // Main Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo "SNX 777" with royal emblem logo image
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { /* Reset or home */ }
                    .padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, GoldPrimary.copy(alpha = 0.8f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_snx777_logo),
                        contentDescription = "SNX 777 Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SNX",
                        color = Slate100,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "777",
                        color = GoldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                }
            }

            // Right side: Balance display when logged in, or Quick Login / Register buttons for guests
            if (userProfile.isLoggedIn) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_cartoon_avatar),
                            contentDescription = "User Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Balance pill with instant deposit shortcut
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Slate800,
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0x1AFFFFFF), Color(0x0DFFFFFF)))),
                        modifier = Modifier
                            .testTag("balance_display_card")
                            .clickable { onOpenDeposit() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = StringRes.t(language, "ব্যালেন্স", "Balance"),
                                    fontSize = 8.sp,
                                    color = Slate400,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = StringRes.formatBDT(userProfile.balanceBDT, language),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(Color(0xFFEAB308), Color(0xFFF59E0B)))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add funds",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Quick Login & Free Register buttons in top header for visitors
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate800,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(Color(0x40FFFFFF), Color(0x20FFFFFF)))
                        ),
                        modifier = Modifier
                            .clickable { onOpenAuth(0) }
                            .testTag("header_login_btn")
                    ) {
                        Text(
                            text = StringRes.t(language, "লগইন", "Login"),
                            color = Slate100,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldPrimary,
                        modifier = Modifier
                            .clickable { onOpenAuth(1) }
                            .testTag("header_register_btn")
                    ) {
                        Text(
                            text = StringRes.t(language, "রেজিস্ট্রেশন", "Register"),
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Live Ticker / Smooth Scrolling Marquee bar
        val marqueeScrollState = rememberScrollState()
        LaunchedEffect(Unit) {
            while (true) {
                val max = marqueeScrollState.maxValue
                if (max > 0) {
                    val target = if (marqueeScrollState.value >= max) 0 else max
                    if (target == 0) {
                        marqueeScrollState.scrollTo(0)
                    } else {
                        val duration = (max - marqueeScrollState.value) * 25
                        marqueeScrollState.animateScrollTo(
                            value = max,
                            animationSpec = tween(
                                durationMillis = duration.coerceAtLeast(1000),
                                easing = LinearEasing
                            )
                        )
                        delay(500)
                        marqueeScrollState.scrollTo(0)
                    }
                }
                delay(50)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141414))
                .border(width = 1.dp, color = CasinoBorderSubtle, shape = RoundedCornerShape(0.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = "Announcement",
                tint = GoldLight,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(marqueeScrollState, enabled = false)
            ) {
                val tickerText = if (!siteConfig?.marqueeTicker.isNullOrBlank()) {
                    siteConfig!!.marqueeTicker + "         "
                } else {
                    StringRes.t(
                        language,
                        "🔥 স্বাগতম SNX 777 এ! নতুন একাউন্ট খুললেই ১০০% ওয়েলকাম বোনাস! ★ সর্বনিম্ন ৩০০ টাকা ডিপোজিটে ৫% ইনস্ট্যান্ট ক্যাশ বোনাস! ★ সাপ্তাহিক ধামাকা রিওয়ার্ড ও ক্যাশব্যাক! ★ দৈনিক সম্পূর্ণ ফ্রি লাকি স্পিন! ★ কোনো প্রকার টার্নওভার নাই! ★ বিকাশ ও নগদে ২৪/৭ দ্রুত সেন্ড মানি করুন।         ",
                        "🔥 Welcome to SNX 777! 100% Welcome Bonus on register! ★ 5% instant cash bonus on min ৳300 deposit! ★ Weekly rewards & cashback! ★ Daily 100% Free Lucky Spin! ★ ZERO turnover requirement! ★ Fast Send Money via bKash & Nagad 24/7.         "
                    )
                }
                Text(
                    text = tickerText,
                    color = GoldLight,
                    fontSize = 11.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
