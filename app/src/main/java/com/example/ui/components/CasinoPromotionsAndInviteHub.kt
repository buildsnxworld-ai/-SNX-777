package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.UserProfile
import com.example.ui.theme.*
import com.example.util.StringRes

@Composable
fun CasinoPromotionsAndInviteHub(
    userProfile: UserProfile,
    language: AppLanguage,
    onClaimCommission: () -> Unit,
    onApplyCoupon: (String) -> Unit,
    onShowToast: ((String) -> Unit)? = null,
    onInviteShared: (() -> Unit)? = null,
    onOpenAuth: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Invite & Commission, 1: Promotions, 2: Discounts

    val referralCode = if (userProfile.referralCode.isNotBlank()) userProfile.referralCode else "SNX777VIP"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("promo_and_invite_hub_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.65f), CasinoBorderSubtle))
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Promotional Banner Header Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_invite_promo),
                    contentDescription = "Invite & Promotion Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Slate800.copy(alpha = 0.95f))
                            )
                        )
                )

                // Top Badge
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 10.dp),
                    color = GoldPrimary,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "🔥 " + StringRes.t(language, "রিওয়ার্ডস ও কমিশন হাব", "Rewards & Commission Hub"),
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Header title over banner
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = StringRes.t(language, "ইনভাইট কমিশন ও প্রমোশন অফার", "Invite Commission & Promotions"),
                        color = GoldLight,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = StringRes.t(
                            language,
                            "বন্ধু জিতলে আপনি পাবেন উইনিং কমিশনের ৫% ক্যাশ বোনাস!",
                            "Earn 5% instant cash bonus whenever your invited friend wins!"
                        ),
                        color = Slate200,
                        fontSize = 11.sp
                    )
                }
            }

            // Tab Selector: ইনভাইট ও কমিশন | প্রমোশন | ডিসকাউন্ট
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate900)
                    .padding(3.dp)
            ) {
                listOf(
                    Triple(0, "👥 " + StringRes.t(language, "ইনভাইট বোনাস", "Invite & Win"), "tab_invite"),
                    Triple(1, "🎁 " + StringRes.t(language, "প্রমোশন", "Promotions"), "tab_promo"),
                    Triple(2, "🏷️ " + StringRes.t(language, "ডিসকাউন্ট", "Discounts"), "tab_discount")
                ).forEach { (index, title, tag) ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) GoldPrimary else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 7.dp)
                            .testTag(tag),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color.Black else Slate300,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Content per Tab
            when (selectedTab) {
                0 -> {
                    // TAB 0: INVITATION & WINNING COMMISSION
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        // Explanation Box
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AccentEmerald.copy(alpha = 0.12f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(listOf(AccentEmerald.copy(alpha = 0.5f), AccentEmerald.copy(alpha = 0.2f)))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "💎", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = StringRes.t(language, "৫% উইনিং কমিশন নিয়ম:", "5% Winnings Commission Rule:"),
                                        color = AccentEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = StringRes.t(
                                            language,
                                            "আপনার ইনভাইট করা বন্ধু যে গেমেই জয়লাভ করুক না কেন, তার উইনিং পরিমাণের ৫% কমিশন তাৎক্ষণিকভাবে আপনার মূল ওয়ালেটে জমা হবে।",
                                            "Whenever your invited friend wins any game, you automatically receive 5% of their winnings directly into your wallet."
                                        ),
                                        color = Slate300,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Referral Code & Copy Bar
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate900,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = StringRes.t(language, "আপনার রেফারেল কোড", "Your Referral Code"),
                                        color = Slate400,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = referralCode,
                                        color = GoldLight,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Copy Code
                                    Button(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Referral Code", referralCode)
                                            clipboard.setPrimaryClip(clip)
                                            onShowToast?.invoke(
                                                if (language == AppLanguage.BN) "রেফারেল কোড কপি করা হয়েছে: $referralCode"
                                                else "Referral code copied: $referralCode"
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate700, contentColor = Slate100),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp).testTag("copy_referral_code_btn")
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(StringRes.t(language, "কপি কোড", "Copy"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Share Link / Invite
                                    Button(
                                        onClick = {
                                            val shareText = if (language == AppLanguage.BN)
                                                "🔥 SNX 777 অনলাইন ক্যাসিনোতে যোগ দিন! আমার রেফারেল কোড: $referralCode ব্যবহার করে রেজিস্ট্রেশন করুন এবং আকর্ষণীয় রিওয়ার্ড ও ডেইলি ফ্রি স্পিন উপভোগ করুন!"
                                            else
                                                "🔥 Join SNX 777 Casino! Use my referral code: $referralCode to register and enjoy exciting rewards & daily free spins!"
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, shareText)
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "বন্ধুদের ইনভাইট করুন"))
                                            onInviteShared?.invoke()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp).testTag("share_invite_btn")
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(StringRes.t(language, "ইনভাইট করুন", "Invite"), fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stats Dashboard: 0 by default until actually earned/invited
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate900,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "${userProfile.totalInvitedCount} জন", color = GoldLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(text = StringRes.t(language, "মোট ইনভাইট", "Total Invites"), color = Slate400, fontSize = 9.sp)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate900,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "৳${userProfile.friendsTotalWonBDT.toLong()}", color = AccentCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(text = StringRes.t(language, "বন্ধুর মোট জয়", "Friends Won"), color = Slate400, fontSize = 9.sp)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate900,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "৳${userProfile.earnedCommissionBDT.toLong()}", color = AccentEmerald, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    Text(text = StringRes.t(language, "অর্জিত কমিশন", "Earned 5%"), color = Slate400, fontSize = 9.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Claim Commission Button
                        val hasCommission = userProfile.earnedCommissionBDT > 0.0
                        Button(
                            onClick = {
                                if (!userProfile.isLoggedIn) {
                                    onShowToast?.invoke(
                                        if (language == AppLanguage.BN)
                                            "কমিশন পেতে অনুগ্রহ করে প্রথমে বিনামূল্যে রেজিস্ট্রেশন অথবা লগইন করুন"
                                        else
                                            "Please register or login first to claim commission"
                                    )
                                    onOpenAuth?.invoke(1)
                                } else if (hasCommission) {
                                    onClaimCommission()
                                } else {
                                    onShowToast?.invoke(
                                        if (language == AppLanguage.BN)
                                            "বর্তমানে কোনো অর্জিত কমিশন নেই। বন্ধুদের ইনভাইট করুন!"
                                        else
                                            "No commission earned yet. Invite friends to start earning!"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("claim_commission_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasCommission) AccentEmerald else Slate700,
                                contentColor = if (hasCommission) Color.Black else Slate300
                            )
                        ) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasCommission)
                                    StringRes.t(language, "🎉 অর্জিত কমিশন ৳%,.0f ওয়ালেটে নিন".format(userProfile.earnedCommissionBDT), "Claim ৳%,.0f Commission".format(userProfile.earnedCommissionBDT))
                                else
                                    StringRes.t(language, "অর্জিত কমিশন ৳০ (ইনভাইট করে আয় করুন)", "Commission ৳0 (Invite to Earn)"),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                1 -> {
                    // TAB 1: PROMOTIONS
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Promo 1: Instant Deposit
                        PromoCardItem(
                            emoji = "⚡",
                            title = StringRes.t(language, "ইনস্ট্যান্ট ডিপোজিট সুবিধা", "Instant Deposit Advantage"),
                            subtitle = StringRes.t(language, "বিকাশ ও নগদ গেটওয়ে দিয়ে মাত্র ২ মিনিটে যেকোনো ডিপোজিট নিরাপদে সম্পন্ন করুন।", "Fast, safe and reliable instant deposit via bKash & Nagad."),
                            tag = StringRes.t(language, "সবার জন্য প্রযোজ্য", "Instant Active"),
                            accentColor = GoldPrimary
                        )

                        // Promo 2: Daily Turnover Rebate
                        PromoCardItem(
                            emoji = "🔄",
                            title = StringRes.t(language, "১.২% দৈনিক আনলিমিটেড রিবেট", "1.2% Daily Unlimited Rebate"),
                            subtitle = StringRes.t(language, "প্রতিদিনের স্লট ও ক্যাসিনো গেমের টার্নওভারের ১.২% ক্যাশ প্রতিদিন রাত ১২টায় জমা হবে।", "1.2% turnover rebate automatically credited to your wallet at midnight."),
                            tag = StringRes.t(language, "অটো জমা", "Auto Credit"),
                            accentColor = AccentCyan
                        )

                        // Promo 3: Weekly Cashback
                        PromoCardItem(
                            emoji = "🛡️",
                            title = StringRes.t(language, "সাপ্তাহিক ১০% ক্যাশব্যাক রিফান্ড", "Weekly 10% VIP Cashback"),
                            subtitle = StringRes.t(language, "প্রতি শুক্রবার পুরো সপ্তাহের নেট লসের ১০% সরাসরি ক্যাশব্যাক হিসেবে ফেরত পান।", "Get 10% of weekly net losses refunded every Friday."),
                            tag = StringRes.t(language, "প্রতি শুক্রবার", "Every Friday"),
                            accentColor = AccentEmerald
                        )
                    }
                }

                2 -> {
                    // TAB 2: DISCOUNTS & VOUCHERS (Manual claiming only when user has real activity)
                    val hasRealActivity = userProfile.isLoggedIn && userProfile.totalDeposited >= 1000.0

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Discount 1: 150 BDT Off on 1000 Deposit
                        DiscountCardItem(
                            couponCode = "SNX150OFF",
                            title = StringRes.t(language, "৳১৫০ ডিপোজিট ক্যাশ ডিসকাউন্ট", "৳150 Deposit Cash Discount"),
                            description = StringRes.t(
                                language,
                                "শর্ত: একাউন্টে ন্যূনতম ৳১,০০০ ডিপোজিট বা গেম খেলার সক্রিয়তা সম্পন্ন করার পর এই ভাউচারটি সংরক্ষণ করা যাবে।",
                                "Requires deposit of ৳1,000+ before this voucher can be claimed."
                            ),
                            isApplied = hasRealActivity && userProfile.pendingCouponCode == "SNX150OFF",
                            isEligible = hasRealActivity,
                            onApply = { onApplyCoupon("SNX150OFF") },
                            language = language
                        )

                        // Discount 2: Loss Recovery Discount
                        DiscountCardItem(
                            couponCode = "RECOVER10",
                            title = StringRes.t(language, "১০% লস রিকভারি ভাউচার", "10% Loss Recovery Voucher"),
                            description = StringRes.t(
                                language,
                                "শর্ত: একাউন্টে ন্যূনতম ৳১,০০০ ডিপোজিট বা সক্রিয়তার পরেই কেবল ভাউচার সংরক্ষণ করে রিবেট উপভোগ করা যাবে।",
                                "Requires activity with ৳1,000+ deposit before this rebate voucher can be claimed."
                            ),
                            isApplied = hasRealActivity && userProfile.pendingCouponCode == "RECOVER10",
                            isEligible = hasRealActivity,
                            onApply = { onApplyCoupon("RECOVER10") },
                            language = language
                        )

                        // Discount 3: Zero Fee Cashout
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate900,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "⚡", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = StringRes.t(language, "০% ক্যাশআউট ফি ডিসকাউন্ট", "0% Cashout Processing Fee"),
                                        color = GoldLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = StringRes.t(language, "বিকাশ ও নগদে যেকোনো পরিমাণের উত্তোলনে কোনো ফি কাটা হবে না।", "Zero deduction fee on all withdrawals to bKash and Nagad."),
                                        color = Slate400,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PromoCardItem(
    emoji: String,
    title: String,
    subtitle: String,
    tag: String,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Slate900,
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(accentColor.copy(alpha = 0.4f), Slate800))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = accentColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = tag,
                            color = accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, color = Slate400, fontSize = 10.sp, lineHeight = 14.sp)
            }
        }
    }
}

@Composable
private fun DiscountCardItem(
    couponCode: String,
    title: String,
    description: String,
    isApplied: Boolean = false,
    isEligible: Boolean = false,
    onApply: () -> Unit,
    language: AppLanguage
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Slate900,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (isApplied) listOf(AccentEmerald, GoldPrimary)
                else if (isEligible) listOf(GoldPrimary.copy(alpha = 0.6f), Slate800)
                else listOf(Slate700.copy(alpha = 0.5f), Slate800)
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = title, color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        if (!isEligible) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Slate800
                            ) {
                                Text(
                                    text = StringRes.t(language, "🔒 শর্ত প্রযোজ্য", "🔒 Req. Locked"),
                                    color = Slate400,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = description, color = Slate400, fontSize = 10.sp, lineHeight = 13.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onApply,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isApplied -> AccentEmerald
                            isEligible -> GoldPrimary
                            else -> Slate800
                        },
                        contentColor = when {
                            isApplied -> Color.Black
                            isEligible -> Color.Black
                            else -> GoldLight
                        }
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("apply_coupon_$couponCode")
                ) {
                    Text(
                        text = when {
                            isApplied -> StringRes.t(language, "✓ সংরক্ষিত", "✓ Saved")
                            isEligible -> StringRes.t(language, "সংরক্ষণ করুন", "Save Voucher")
                            else -> StringRes.t(language, "🔒 শর্ত দেখুন", "🔒 Locked")
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Slate800
                ) {
                    Text(
                        text = "🎟️ " + StringRes.t(language, "কুপন কোড:", "Promo Code:") + " $couponCode",
                        color = Slate300,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (isApplied) {
                    Text(
                        text = StringRes.t(language, "পরবর্তী ৳১,০০০+ ডিপোজিটে ক্যাশ পাবেন", "Applies on next ৳1,000+ deposit"),
                        color = AccentEmerald,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (!isEligible) {
                    Text(
                        text = StringRes.t(language, "ন্যূনতম ৳১,০০০ ডিপোজিটে সক্রিয় হবে", "Unlocks on min ৳1,000 deposit"),
                        color = Slate500,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}
