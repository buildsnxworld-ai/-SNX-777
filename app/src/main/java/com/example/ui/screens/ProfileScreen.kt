package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*
import com.example.ui.components.CasinoPromotionsAndInviteHub
import com.example.ui.theme.*
import com.example.util.StringRes

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    transactions: List<TransactionRecord>,
    language: AppLanguage,
    onToggleLanguage: (() -> Unit)? = null,
    onOpenAuth: (Int) -> Unit,
    onLogout: () -> Unit,
    onClaimDailyBonus: () -> Unit,
    onOpenSupport: () -> Unit,
    onShowToast: (String) -> Unit,
    onOpenApkDownload: (() -> Unit)? = null,
    onClaimCommission: (() -> Unit)? = null,
    onApplyCoupon: ((String) -> Unit)? = null,
    onOpenSecurityCenter: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CasinoBg)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // User Profile Header
        Card(
            modifier = Modifier.fillMaxWidth().testTag("profile_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.5f), CasinoBorderSubtle)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Brush.linearGradient(listOf(GoldPrimary, GoldLight)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_cartoon_avatar),
                                contentDescription = "User Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = userProfile.username,
                                color = Slate100,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = userProfile.phone,
                                color = Slate400,
                                fontSize = 12.sp
                            )
                            if (userProfile.isLoggedIn) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(AccentEmerald, RoundedCornerShape(3.dp))
                                    )
                                    Text(
                                        text = StringRes.t(language, "সেশন সংরক্ষিত ও সক্রিয়", "Session Active & Saved"),
                                        color = AccentEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    if (userProfile.isLoggedIn) {
                        IconButton(onClick = onLogout, modifier = Modifier.testTag("btn_logout")) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = AccentCrimson)
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { onOpenAuth(0) },
                                colors = ButtonDefaults.buttonColors(containerColor = Slate700, contentColor = Slate100),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("profile_login_btn")
                            ) {
                                Text(StringRes.t(language, "লগইন", "Login"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onOpenAuth(1) },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("profile_register_btn")
                            ) {
                                Text(StringRes.t(language, "রেজিস্ট্রেশন", "Register"), fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Wallet Statistics Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate900)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = StringRes.t(language, "ব্যালেন্স", "Balance"), color = Slate400, fontSize = 10.sp)
                        Text(
                            text = StringRes.formatBDT(userProfile.balanceBDT, language),
                            color = AccentEmerald,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(CasinoBorderSubtle))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = StringRes.t(language, "মোট জমা", "Total Deposit"), color = Slate400, fontSize = 10.sp)
                        Text(
                            text = StringRes.formatBDT(userProfile.totalDeposited, language),
                            color = GoldLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(CasinoBorderSubtle))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = StringRes.t(language, "মোট উত্তোলন", "Total Withdrawn"), color = Slate400, fontSize = 10.sp)
                        Text(
                            text = StringRes.formatBDT(userProfile.totalWithdrawn, language),
                            color = AccentCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Casino Promotions, Invite & Discounts Hub
        CasinoPromotionsAndInviteHub(
            userProfile = userProfile,
            language = language,
            onClaimCommission = { onClaimCommission?.invoke() },
            onApplyCoupon = { onApplyCoupon?.invoke(it) },
            onShowToast = onShowToast,
            onOpenAuth = onOpenAuth
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Settings & Features
        Text(
            text = StringRes.t(language, "সেটিংস ও সুযোগ-সুবিধা", "Settings & Perks"),
            color = GoldLight,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CasinoBorderSubtle, CasinoBorderSubtle)))
        ) {
            Column {
                // Claim Daily Bonus (চরকি ১৳ - ১০০৳)
                ProfileOptionRow(
                    icon = Icons.Default.CardGiftcard,
                    title = StringRes.t(language, "দৈনিক ফ্রি চরকি স্পিন", "Daily Free Chorki Spin"),
                    subtitle = StringRes.t(language, "২৪ ঘণ্টায় ১ বার স্পিন করে জিতে নিন ১৳ থেকে ১০০৳ ক্যাশ", "Spin once per 24H for ৳1 to ৳100 cash bonus"),
                    onClick = {
                        if (!userProfile.isLoggedIn) {
                            onShowToast(
                                if (language == AppLanguage.BN)
                                    "ডেইলি বোনাস নিতে অনুগ্রহ করে প্রথমে বিনামূল্যে রেজিস্ট্রেশন অথবা লগইন করুন"
                                else
                                    "Please register or login first to claim daily bonus"
                            )
                            onOpenAuth(1)
                        } else {
                            onClaimDailyBonus()
                        }
                    },
                    testTag = "option_daily_bonus"
                )

                HorizontalDivider(color = CasinoBorderSubtle)

                // 24/7 Support
                ProfileOptionRow(
                    icon = Icons.Default.Headphones,
                    title = StringRes.t(language, "২৪/৭ লাইভ গ্রাহক সেবা", "24/7 Live Customer Chat"),
                    subtitle = StringRes.t(language, "যেকোনো সমস্যায় সহায়তা পেতে যোগাযোগ করুন", "Contact us for help anytime"),
                    onClick = onOpenSupport,
                    testTag = "option_support"
                )

                HorizontalDivider(color = CasinoBorderSubtle)

                // Security Center (নাম্বার ও পাসওয়ার্ড পরিবর্তন)
                ProfileOptionRow(
                    icon = Icons.Default.Security,
                    title = StringRes.t(language, "সিকিউরিটি সেন্টার", "Security Center"),
                    subtitle = StringRes.t(language, "মোবাইল নম্বর ও একাউন্ট পাসওয়ার্ড পরিবর্তন করুন", "Change mobile number & account password"),
                    onClick = {
                        if (!userProfile.isLoggedIn) {
                            onShowToast(
                                if (language == AppLanguage.BN)
                                    "সিকিউরিটি সেন্টার ব্যবহার করতে প্রথমে লগইন অথবা রেজিস্ট্রেশন করুন"
                                else
                                    "Please login or register first to access Security Center"
                            )
                            onOpenAuth(1)
                        } else {
                            onOpenSecurityCenter?.invoke()
                        }
                    },
                    testTag = "option_security_center"
                )
            }
        }

        // Real Transaction History for Current User Only:
        // Only show transactions belonging strictly to the currently logged in user (matching username or phone).
        // A new account or any other user will NEVER see anyone else's transactions.
        val myTransactions = remember(transactions, userProfile.username, userProfile.phone, userProfile.isLoggedIn) {
            if (!userProfile.isLoggedIn) {
                emptyList()
            } else {
                val currentName = userProfile.username.trim()
                val currentPhone = userProfile.phone.trim()
                transactions.filter { record ->
                    val matchName = currentName.isNotBlank() && record.username.isNotBlank() && record.username.trim().equals(currentName, ignoreCase = true)
                    val matchPhone = currentPhone.isNotBlank() && record.userPhone.isNotBlank() && record.userPhone.trim() == currentPhone
                    matchName || matchPhone
                }
            }
        }

        if (userProfile.isLoggedIn) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = StringRes.t(language, "লেনদেন হিস্ট্রি", "Transaction History"),
                color = GoldLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (myTransactions.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CasinoBorderSubtle, CasinoBorderSubtle)))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = StringRes.t(language, "আপনার একাউন্টে এখনো কোনো লেনদেন সম্পন্ন হয়নি", "No transactions yet on your account"),
                            color = Slate400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    myTransactions.take(15).forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CasinoBorderSubtle, CasinoBorderSubtle)))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (item.type == TransactionType.DEPOSIT) "⬇️ " else "⬆️ ",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = if (language == AppLanguage.BN) item.type.bn else item.type.en,
                                        color = Slate100,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = item.method.displayName.substringBefore(" "), color = Slate400, fontSize = 11.sp)
                                }
                                Text(
                                    text = "TrxID: ${item.trxId} • ${item.timeFormatted}",
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = (if (item.type == TransactionType.DEPOSIT) "+ " else "- ") + StringRes.formatBDT(item.amount, language),
                                    color = if (item.type == TransactionType.DEPOSIT) AccentEmerald else Slate100,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (language == AppLanguage.BN) item.status.bn else item.status.en,
                                    color = Color(item.status.colorHex),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
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

@Composable
private fun ProfileOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = GoldLight, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, color = Slate100, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, color = Slate400, fontSize = 10.sp)
            }
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
    }
}
