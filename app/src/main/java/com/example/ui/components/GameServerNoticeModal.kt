package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.GameNoticeInfo
import com.example.model.AppLanguage
import com.example.model.GameServerStatus

@Composable
fun GameServerNoticeModal(
    notice: GameNoticeInfo?,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onOpenSupport: () -> Unit
) {
    if (notice == null) return

    val game = notice.game
    val status = notice.status

    val isError = status == GameServerStatus.SERVER_ERROR
    val isMaintenance = status == GameServerStatus.SERVER_MAINTENANCE || status == GameServerStatus.SERVER_UPDATE
    val isUpgrade = status == GameServerStatus.SERVER_UPGRADE
    val isOffline = status == GameServerStatus.SERVER_OFF || status == GameServerStatus.OFFLINE

    val themeColor = when {
        isError -> Color(0xFFEF4444)        // Crimson Red
        isMaintenance -> Color(0xFFF59E0B)  // Amber Orange
        isUpgrade -> Color(0xFF38BDF8)      // Sky / Electric Blue
        else -> Color(0xFF94A3B8)           // Slate
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.5.dp, themeColor.copy(alpha = 0.8f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("game_server_status_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Glowing Alert Icon Circle
                Surface(
                    shape = CircleShape,
                    color = themeColor.copy(alpha = 0.15f),
                    border = BorderStroke(2.dp, themeColor),
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = when {
                                isError -> "⚠️"
                                isMaintenance -> "🛠️"
                                isUpgrade -> "🚀"
                                else -> "🔌"
                            },
                            fontSize = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = when {
                        isError -> if (language == AppLanguage.BN) "সার্ভার এরর (SERVER ERROR)!" else "Server Error 503!"
                        isMaintenance -> if (language == AppLanguage.BN) "সার্ভার মেইনটেন্যান্স (MAINTENANCE)!" else "Server Under Maintenance!"
                        isUpgrade -> if (language == AppLanguage.BN) "সার্ভার আপগ্রেড (SERVER UPGRADE)!" else "Server System Upgrade!"
                        else -> if (language == AppLanguage.BN) "সার্ভার অফ (SERVER OFFLINE)!" else "Provider Server Offline!"
                    },
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                // Subtitle / Status Tag
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = themeColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        text = when {
                            isError -> "🚫 503 SERVICE UNAVAILABLE • TIMEOUT"
                            isMaintenance -> "🛠️ ROUTINE MAINTENANCE IN PROGRESS"
                            isUpgrade -> "🔄 ENGINE UPGRADE v4.8.2 ACTIVE"
                            else -> "🔴 502 BAD GATEWAY • SERVER DISCONNECTED"
                        },
                        color = themeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Game preview pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (game.imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = game.imageUrl,
                                        contentDescription = game.titleEn,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(text = game.iconEmoji, fontSize = 22.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.BN) game.titleBn else game.titleEn,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${game.category.bn} • ${game.titleEn}",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detailed message
                Text(
                    text = when {
                        isError -> if (language == AppLanguage.BN)
                            "দুঃখিত! গেম প্রোভাইডারের সার্ভার থেকে সাময়িক সংযোগ বিচ্ছিন্ন হয়েছে (HTTP 503 Service Unavailable / Gateway Timeout)। প্রোভাইডার গেটওয়েতে টেকনিক্যাল টিম কাজ করছে। অনুগ্রহ করে কিছুক্ষণ পর পুনরায় চেষ্টা করুন। এরর কোড: ERR_PROV_503_GATEWAY_TIMEOUT"
                        else
                            "The game provider gateway server is currently unresponsive (HTTP 503 Service Unavailable). Our technical operations team is investigating. Please retry in a few moments. Error Code: ERR_PROV_503_GATEWAY_TIMEOUT"
                        isMaintenance -> if (language == AppLanguage.BN)
                            "প্রিয় গ্রাহক, উন্নত গেমিং অভিজ্ঞতা ও ডেটা সুরক্ষার স্বার্থে এই গেম প্রোভাইডারের সার্ভারে রুটিন মেইনটেন্যান্স কাজ চলছে। মেইনটেন্যান্স চলাকালীন এই গেমটি সাময়িকভাবে বন্ধ থাকবে। আনুমানিক সময়: ১৫-২০ মিনিট। মেইনটেন্যান্স আইডি: #MNT-8821"
                        else
                            "Scheduled routine maintenance is currently underway for this provider to improve performance and security. Service will be restored shortly. Estimated duration: 15-20 mins. Maintenance ID: #MNT-8821"
                        isUpgrade -> if (language == AppLanguage.BN)
                            "এই গেম সার্ভারটি বর্তমানে নতুন হাই-স্পিড ইঞ্জিন ভার্সনে (v4.8.2) আপগ্রেড করা হচ্ছে। গেমিং ইঞ্জিন আপডেট এবং ডেটাবেস সিঙ্ক্রোনাইজেশন সম্পন্ন না হওয়া পর্যন্ত সাময়িক পরিষেবা স্থগিত রাখা হয়েছে। আপগ্রেড রেফারেন্স: #UPG-4820"
                        else
                            "Game engine is currently being upgraded to version 4.8.2 for enhanced gameplay stability. Access is temporarily restricted during database sync. Upgrade Ref: #UPG-4820"
                        else -> if (language == AppLanguage.BN)
                            "এই গেমটির প্রোভাইডার সার্ভার বর্তমানে সম্পূর্ণ অফলাইনে রয়েছে অথবা আপনার অঞ্চলে সাময়িকভাবে সংযোগ বন্ধ রয়েছে (Status: 502 Bad Gateway / Disconnected)। অনুগ্রহ করে আমাদের সক্রিয় অন্যান্য গেম খেলুন।"
                        else
                            "The game provider server is currently offline or unreachable in this region (Status: 502 Bad Gateway / Disconnected). Please try our other active games."
                    },
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("dismiss_server_notice_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColor,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "অন্যান্য গেম খেলুন" else "Play Other Games",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenSupport()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF475569))
                    ) {
                        Icon(
                            Icons.Default.Headphones,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.BN) "লাইভ সাপোর্ট যোগাযোগ" else "Contact Support",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
