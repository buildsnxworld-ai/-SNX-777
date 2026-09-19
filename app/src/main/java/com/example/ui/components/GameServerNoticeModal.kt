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

    val isUpdate = status == GameServerStatus.SERVER_UPDATE
    val isError = status == GameServerStatus.SERVER_ERROR

    val themeColor = when (status) {
        GameServerStatus.SERVER_UPDATE -> Color(0xFFF59E0B) // Amber
        GameServerStatus.SERVER_ERROR -> Color(0xFFEF4444)  // Crimson Red
        else -> Color(0xFF64748B)                           // Slate
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
                            text = if (isUpdate) "🛠️" else if (isError) "🚫" else "🔒",
                            fontSize = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = when (status) {
                        GameServerStatus.SERVER_UPDATE -> if (language == AppLanguage.BN) "সার্ভার আপডেট চলছে!" else "Server Update in Progress!"
                        GameServerStatus.SERVER_ERROR -> if (language == AppLanguage.BN) "সার্ভার এরর / সংযোগ ত্রুটি!" else "Server Connection Error!"
                        else -> if (language == AppLanguage.BN) "গেমটি বর্তমানে সাময়িক বন্ধ" else "Game Temporarily Inactive"
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
                        text = when (status) {
                            GameServerStatus.SERVER_UPDATE -> "⚠️ SERVER UNDER MAINTENANCE"
                            GameServerStatus.SERVER_ERROR -> "🚫 SERVER OFFLINE / 503 ERROR"
                            else -> "🔒 GAME DISABLED BY ADMIN"
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
                    text = when (status) {
                        GameServerStatus.SERVER_UPDATE -> if (language == AppLanguage.BN)
                            "প্রিয় গ্রাহক, বর্তমানে এই গেমটির সার্ভারে আপগ্রেডেশন ও সিস্টেম মেইনটেন্যান্স কাজ চলছে। খুব শীঘ্রই গেমটি পুনরায় সবার জন্য চালু হবে। সাময়িক অসুবিধার জন্য আমরা আন্তরিকভাবে দুঃখিত।"
                        else
                            "Dear player, server upgrade and system maintenance is currently ongoing for this game. It will be restored very shortly. Thank you for your patience."
                        GameServerStatus.SERVER_ERROR -> if (language == AppLanguage.BN)
                            "দুঃখিত! এই গেমটির সার্ভার থেকে সাময়িক সংযোগ বিচ্ছিন্ন হয়েছে (Connection Timeout)। আমাদের টেকনিক্যাল টিম সমস্যা সমাধানে কাজ করছে। অনুগ্রহ করে কিছুক্ষণ পর আবার চেষ্টা করুন।"
                        else
                            "Sorry! There is a temporary connection issue with this game server. Our technical team is actively investigating. Please retry in a few moments."
                        else -> if (language == AppLanguage.BN)
                            "এই গেমটি অ্যাডমিন প্যানেল কর্তৃক সাময়িকভাবে বন্ধ রাখা হয়েছে। অন্যান্য আকর্ষণীয় গেম খেলতে হোমপেজে ফিরে যান।"
                        else
                            "This game has been temporarily disabled by administration. Please enjoy our other active games."
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
