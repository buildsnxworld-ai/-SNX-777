package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.GameCategory
import com.example.model.GameItem
import com.example.model.GameServerStatus
import com.example.model.UserProfile
import com.example.ui.components.CasinoPromotionsAndInviteHub
import com.example.ui.theme.*
import com.example.util.StringRes
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    games: List<GameItem>,
    selectedCategory: GameCategory,
    onSelectCategory: (GameCategory) -> Unit,
    jackpotPool: Double,
    language: AppLanguage,
    onOpenGame: (String) -> Unit,
    onOpenDeposit: () -> Unit,
    onOpenWithdraw: () -> Unit,
    onClaimDailyBonus: () -> Unit,
    onOpenWheel: () -> Unit,
    onShare: () -> Unit,
    userProfile: UserProfile? = null,
    onOpenAuth: ((Int) -> Unit)? = null,
    onShowToast: ((String) -> Unit)? = null,
    onClaimCommission: (() -> Unit)? = null,
    onApplyCoupon: ((String) -> Unit)? = null,
    onInviteShared: (() -> Unit)? = null,
    onOpenSecurityCenter: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val filteredGames = remember(selectedCategory, games) {
        val activeGames = games.filter { it.isActive }
        if (selectedCategory == GameCategory.ALL) activeGames
        else activeGames.filter { it.category == selectedCategory || (selectedCategory == GameCategory.HOT && it.badge != null) }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(CasinoBg)
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sophisticated Dark Balance Card
        item(span = { GridItemSpan(2) }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                IndigoCardBgStart,
                                IndigoCardBgEnd
                            )
                        )
                    )
                    .border(1.dp, CasinoCardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                // Top-right clean decorative badge instead of dark circle artifact
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                    shape = RoundedCornerShape(20.dp),
                    color = GoldPrimary.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.5f), GoldLight.copy(alpha = 0.3f))))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⚡", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = StringRes.t(language, "তাৎক্ষণিক ক্যাশইন", "Instant Cash In"),
                            color = GoldLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = StringRes.t(language, "বর্তমান ব্যালেন্স / BALANCE", "CURRENT BALANCE / BALANCE"),
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = StringRes.formatBDT(userProfile?.balanceBDT ?: 0.0, language),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Payment Method Badges: bKash & Nagad
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BKashPink,
                            modifier = Modifier
                                .clickable { onOpenDeposit() }
                                .testTag("home_deposit_bkash_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "bKash " + StringRes.t(language, "ডিপোজিট", "Deposit"),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NagadOrange,
                            modifier = Modifier
                                .clickable { onOpenWithdraw() }
                                .testTag("home_deposit_nagad_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Nagad " + StringRes.t(language, "উত্তোলন", "Withdraw"),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Feature Action Buttons (Positioned directly under balance card, next to Deposit, Withdraw & Website link)
        item(span = { GridItemSpan(2) }) {
            val isLoggedIn = userProfile?.isLoggedIn == true
            QuickActionsBar(
                language = language,
                onOpenDeposit = {
                    if (isLoggedIn) onOpenDeposit()
                    else {
                        onShowToast?.invoke(
                            if (language == AppLanguage.BN)
                                "ডিপোজিট করতে অনুগ্রহ করে প্রথমে বিনামূল্যে রেজিস্ট্রেশন অথবা লগইন করুন"
                            else
                                "Please register or login first to make a deposit"
                        )
                        onOpenAuth?.invoke(1)
                    }
                },
                onOpenWithdraw = {
                    if (isLoggedIn) onOpenWithdraw()
                    else {
                        onShowToast?.invoke(
                            if (language == AppLanguage.BN)
                                "টাকা উত্তোলন করতে অনুগ্রহ করে প্রথমে বিনামূল্যে রেজিস্ট্রেশন অথবা লগইন করুন"
                            else
                                "Please register or login first to withdraw funds"
                        )
                        onOpenAuth?.invoke(1)
                    }
                },
                onOpenChorki = {
                    if (isLoggedIn) {
                        onOpenWheel()
                    } else {
                        onShowToast?.invoke(
                            if (language == AppLanguage.BN)
                                "ফ্রি বোনাস নিতে অনুগ্রহ করে প্রথমে বিনামূল্যে রেজিস্ট্রেশন অথবা লগইন করুন"
                            else
                                "Please register or login first to claim free bonus"
                        )
                        onOpenAuth?.invoke(1)
                    }
                },
                onOpenSecurityCenter = {
                    if (isLoggedIn) {
                        onOpenSecurityCenter?.invoke()
                    } else {
                        onShowToast?.invoke(
                            if (language == AppLanguage.BN)
                                "সিকিউরিটি সেন্টার ব্যবহার করতে প্রথমে লগইন অথবা রেজিস্ট্রেশন করুন"
                            else
                                "Please login or register first to access Security Center"
                        )
                        onOpenAuth?.invoke(1)
                    }
                },
                onShare = onShare
            )
        }

        // Hero Promotional Banner
        item(span = { GridItemSpan(2) }) {
            val isLoggedIn = userProfile?.isLoggedIn == true
            HeroBannerCard(
                language = language,
                onDepositClick = {
                    if (isLoggedIn) {
                        onOpenDeposit()
                    } else {
                        onShowToast?.invoke(
                            if (language == AppLanguage.BN)
                                "ডিপোজিট করতে অনুগ্রহ করে প্রথমে বিনামূল্যে রেজিস্ট্রেশন করুন"
                            else
                                "Please register free to make a deposit"
                        )
                        onOpenAuth?.invoke(1)
                    }
                }
            )
        }

        // Live Mega Jackpot Display
        item(span = { GridItemSpan(2) }) {
            JackpotCard(
                jackpotBDT = jackpotPool,
                language = language
            )
        }

        // Casino Promotions, Invite & Discounts Hub (Positioned directly under Mega Jackpot)
        item(span = { GridItemSpan(2) }) {
            CasinoPromotionsAndInviteHub(
                userProfile = userProfile ?: UserProfile(),
                language = language,
                onClaimCommission = { onClaimCommission?.invoke() },
                onApplyCoupon = { onApplyCoupon?.invoke(it) },
                onShowToast = onShowToast,
                onInviteShared = onInviteShared,
                onOpenAuth = onOpenAuth
            )
        }

        // New HOT GAMES Section (Matching exact screenshot layout, replacing previous games listing)
        item(span = { GridItemSpan(2) }) {
            com.example.ui.components.HotGamesSection(
                onOpenAviator = {
                    if (userProfile?.isLoggedIn != true) {
                        onShowToast?.invoke(
                            if (language == AppLanguage.BN)
                                "গেমটি খেলতে অনুগ্রহ করে প্রথমে বিনামূল্যে একাউন্ট রেজিস্ট্রেশন করুন"
                            else
                                "Please register free to play Aviator"
                        )
                        onOpenAuth?.invoke(1)
                    } else {
                        onOpenGame("aviator_crash")
                    }
                },
                onOpenSuperAce = {
                    onOpenGame("super_ace")
                },
                onOpenGame = { gameId ->
                    onOpenGame(gameId)
                }
            )
        }

        // LIVE CASINO, SPORTS & SLOTS Section (From User Screenshots)
        item(span = { GridItemSpan(2) }) {
            com.example.ui.components.CasinoSectionsComponent(
                onOpenGame = { gameId -> onOpenGame(gameId) }
            )
        }

        // Recent Big Winners Ticker
        item(span = { GridItemSpan(2) }) {
            RecentWinnersSection(language = language)
        }
    }
}

private data class PromoBannerData(
    val badgeBn: String,
    val badgeEn: String,
    val titleBn: String,
    val titleEn: String,
    val subtitleBn: String,
    val subtitleEn: String,
    val imageResId: Int,
    val badgeColor: Color
)

@Composable
private fun HeroBannerCard(
    language: AppLanguage,
    onDepositClick: () -> Unit
) {
    val banners = remember {
        listOf(
            PromoBannerData(
                badgeBn = "ইনস্ট্যান্ট ক্যাশ ডিপোজিট",
                badgeEn = "INSTANT DEPOSIT",
                titleBn = "বিকাশ ও নগদে মাত্র ২ মিনিটে ক্যাশ ইন!",
                titleEn = "Fast & Secure Deposit via bKash & Nagad!",
                subtitleBn = "নিরাপদ লেনদেন ও ১০০% নির্ভরযোগ্য প্ল্যাটফর্ম",
                subtitleEn = "Safe transactions & 100% reliable platform",
                imageResId = R.drawable.img_casino_hero_banner,
                badgeColor = AccentEmerald
            ),
            PromoBannerData(
                badgeBn = "মেগা স্লট জ্যাকপট",
                badgeEn = "MEGA SLOTS JACKPOT",
                titleBn = "প্রতিদিন লক্ষ টাকার মেগা জ্যাকপট অফার!",
                titleEn = "Daily Lakh Taka Mega Jackpot Prize Pools!",
                subtitleBn = "আজই স্পিন করুন এবং জিতে নিন জ্যাকপট",
                subtitleEn = "Spin today and hit the big jackpot prize",
                imageResId = R.drawable.img_promo_banner_2,
                badgeColor = GoldPrimary
            ),
            PromoBannerData(
                badgeBn = "ভিআইপি ক্যাশব্যাক ১০%",
                badgeEn = "VIP 10% CASHBACK",
                titleBn = "প্রতি শুক্রবার বিশেষ ডিপোজিট ক্যাশব্যাক!",
                titleEn = "Special Weekly Deposit Cashback Every Friday!",
                subtitleBn = "বিকাশ ও নগদ অ্যাকাউন্টে সরাসরি গ্রহণ করুন",
                subtitleEn = "Direct cashback to your bKash & Nagad",
                imageResId = R.drawable.img_promo_banner_3,
                badgeColor = AccentCyan
            )
        )
    }

    var currentBannerIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000L)
            currentBannerIndex = (currentBannerIndex + 1) % banners.size
        }
    }

    val banner = banners[currentBannerIndex]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .testTag("hero_banner_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CasinoBorderSubtle, CasinoBorderSubtle)))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
        ) {
            // Promotional Banner image
            Image(
                painter = painterResource(id = banner.imageResId),
                contentDescription = "Casino Hero Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dark gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xF00A0E17),
                                Color(0xCC0F172A),
                                Color(0x66000000)
                            )
                        )
                    )
            )

            // Content on top of banner
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = banner.badgeColor
                ) {
                    Text(
                        text = StringRes.t(language, banner.badgeBn, banner.badgeEn),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = StringRes.t(language, banner.titleBn, banner.titleEn),
                    color = GoldLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = StringRes.t(language, banner.subtitleBn, banner.subtitleEn),
                    color = Slate300,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDepositClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = StringRes.t(language, "এখনই ডিপোজিট করুন", "Deposit Now"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Dot indicators for banners
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        banners.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == currentBannerIndex) 8.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == currentBannerIndex) GoldPrimary
                                        else Color.White.copy(alpha = 0.3f)
                                    )
                                    .clickable { currentBannerIndex = index }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsBar(
    language: AppLanguage,
    onOpenDeposit: () -> Unit,
    onOpenWithdraw: () -> Unit,
    onOpenChorki: () -> Unit,
    onOpenSecurityCenter: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Slate800)
            .border(1.dp, CasinoBorderSubtle, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        QuickActionButton(
            emoji = "💳",
            title = StringRes.t(language, "ডিপোজিট", "Deposit"),
            color = BKashPink,
            onClick = onOpenDeposit,
            testTag = "quick_action_deposit"
        )
        QuickActionButton(
            emoji = "💸",
            title = StringRes.t(language, "উত্তোলন", "Withdraw"),
            color = NagadOrange,
            onClick = onOpenWithdraw,
            testTag = "quick_action_withdraw"
        )
        QuickActionButton(
            emoji = "🎁",
            title = StringRes.t(language, "ফ্রি বোনাস", "Free Bonus"),
            color = GoldPrimary,
            onClick = onOpenChorki,
            testTag = "quick_action_bonus"
        )
        QuickActionButton(
            emoji = "🛡️",
            title = StringRes.t(language, "সিকিউরিটি", "Security"),
            color = AccentEmerald,
            onClick = onOpenSecurityCenter,
            testTag = "quick_action_security"
        )
        QuickActionButton(
            emoji = "🌐",
            title = StringRes.t(language, "ওয়েবসাইট লিংক", "Website Link"),
            color = AccentCyan,
            onClick = onShare,
            testTag = "quick_action_share"
        )
    }
}

@Composable
private fun QuickActionButton(
    emoji: String,
    title: String,
    color: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Slate100
        )
    }
}

@Composable
private fun JackpotCard(
    jackpotBDT: Double,
    language: AppLanguage
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("jackpot_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(GoldPrimary.copy(alpha = 0.6f), Color(0x1AFFFFFF))
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFEAB308), Color(0xFFF59E0B)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👑", fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = StringRes.t(language, "মেগা জ্যাকপট প্রাইজপুল", "MEGA JACKPOT POOL"),
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = StringRes.formatBDT(jackpotBDT, language),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AccentCrimson.copy(alpha = 0.2f),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(AccentCrimson, Color.Transparent)))
            ) {
                Text(
                    text = "LIVE 🔴",
                    color = AccentCrimson,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterTabs(
    selectedCategory: GameCategory,
    onSelectCategory: (GameCategory) -> Unit,
    language: AppLanguage
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(GameCategory.values()) { cat ->
            val isSelected = cat == selectedCategory
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) GoldPrimary else Slate800,
                border = if (isSelected) null else CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CasinoBorderSubtle, CasinoBorderSubtle))),
                modifier = Modifier
                    .clickable { onSelectCategory(cat) }
                    .testTag("category_tab_${cat.name}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = cat.icon, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.BN) cat.bn else cat.en,
                        color = if (isSelected) Color.Black else Slate400,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun GameGridCard(
    game: GameItem,
    language: AppLanguage,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onPlay)
            .testTag("game_card_${game.id}"),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CasinoBorderSubtle, CasinoBorderSubtle)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Icon & Badge Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF243046),
                                Color(0xFF161E2E)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (game.imageUrl.isNotBlank()) {
                    coil.compose.AsyncImage(
                        model = game.imageUrl,
                        contentDescription = game.titleEn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = game.iconEmoji,
                        fontSize = 42.sp
                    )
                }

                // Server Status indicator if not ACTIVE
                if (game.serverStatus != GameServerStatus.ACTIVE) {
                    Surface(
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        color = Color(game.serverStatus.colorHex),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = when (game.serverStatus) {
                                GameServerStatus.SERVER_UPDATE -> "🛠️ UPDATE"
                                GameServerStatus.SERVER_ERROR -> "🚫 ERROR"
                                else -> "🔒 OFFLINE"
                            },
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // Optional badge (HOT, NEW, LIVE)
                if (game.badge != null) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 8.dp, bottomEnd = 0.dp, topEnd = 8.dp),
                        color = when (game.badge) {
                            "HOT" -> AccentCrimson
                            "LIVE" -> AccentEmerald
                            "JACKPOT" -> GoldPrimary
                            else -> AccentCyan
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = game.badge,
                            color = if (game.badge == "JACKPOT") Color.Black else Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Game Title
            Text(
                text = if (language == AppLanguage.BN) game.titleBn else game.titleEn,
                color = Slate100,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Players active & min bet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🟢 ${game.playersCount}",
                    color = Slate500,
                    fontSize = 10.sp
                )
                Text(
                    text = if (language == AppLanguage.BN) "মিনিমাম: ৳${game.minBet.toInt()}" else "Min: ৳${game.minBet.toInt()}",
                    color = GoldLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Play Button
            Button(
                onClick = onPlay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = StringRes.t(language, "খেলুন", "PLAY"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentWinnersSection(language: AppLanguage) {
    val allWinners = remember {
        listOf(
            Triple("017****8892", "মেগা জ্যাকপট ৭৭৭", "৳২৫,০০০"),
            Triple("019****4410", "SPRIBE AVIATOR", "৳১৮,৫০০"),
            Triple("018****1123", "লাকি স্পিন হুইল", "৳১০,০০০"),
            Triple("016****7731", "সুপার ৭ মেগা জ্যাকপট", "৳৩৫,২০০"),
            Triple("013****9024", "রুলেট রয়্যাল", "৳১২,৪০০"),
            Triple("017****5520", "ক্রেজি ডাইস", "৳৮,০০০"),
            Triple("019****3199", "ফরচুন ড্রাগন", "৳২২,০০০"),
            Triple("018****6488", "সোনার বাংলা স্লট", "৳১৫,০০০")
        )
    }

    var startIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2500L)
            startIndex = (startIndex + 1) % allWinners.size
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CasinoBorderSubtle, CasinoBorderSubtle)))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏆", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = StringRes.t(language, "সাম্প্রতিক বড় বিজয়ীগণ", "Recent Big Winners"),
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Live status indicator
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentEmerald.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(AccentEmerald.copy(alpha = 0.4f), AccentEmerald.copy(alpha = 0.4f))))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AccentEmerald)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = StringRes.t(language, "লাইভ", "LIVE"),
                            color = AccentEmerald,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Display 3 rolling items
            for (i in 0 until 3) {
                val winner = allWinners[(startIndex + i) % allWinners.size]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎉", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = winner.first, color = Slate300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "• ${winner.second}", color = Slate400, fontSize = 10.sp)
                    }
                    Text(text = "+${winner.third}", color = AccentEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}




