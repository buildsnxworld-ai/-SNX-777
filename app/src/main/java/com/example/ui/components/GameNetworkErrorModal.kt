package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.AppLanguage
import com.example.model.GameItem
import kotlinx.coroutines.delay

@Composable
fun GameNetworkErrorModal(
    game: GameItem?,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    if (game == null) return

    var isLoading by remember(game.id) { mutableStateOf(true) }
    var loadingStep by remember(game.id) { mutableIntStateOf(0) }
    var retryCount by remember(game.id) { mutableIntStateOf(0) }

    LaunchedEffect(game.id, retryCount) {
        isLoading = true
        loadingStep = 0
        delay(600)
        loadingStep = 1
        delay(800)
        loadingStep = 2
        delay(1100)
        loadingStep = 3
        delay(500)
        isLoading = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE6050B14))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(
                    1.5.dp,
                    if (isLoading) Color(0xFF38BDF8).copy(alpha = 0.6f) else Color(0xFFEF4444).copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .testTag("game_network_error_modal")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top header: Close button
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

                    // Game info header
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
                                modifier = Modifier.size(46.dp)
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
                                        Text(text = game.iconEmoji, fontSize = 24.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (language == AppLanguage.BN) game.titleBn else game.titleEn,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${game.category.bn} • ${game.titleEn}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedContent(
                        targetState = isLoading,
                        label = "network_state"
                    ) { loading ->
                        if (loading) {
                            // -------------------------------------------------------------
                            // STATE 1: CONNECTING / LOADING INTO GAME
                            // -------------------------------------------------------------
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(54.dp),
                                    color = Color(0xFF38BDF8),
                                    strokeWidth = 4.dp
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = if (language == AppLanguage.BN) "গেম সার্ভারে প্রবেশ করা হচ্ছে..." else "Connecting to Game Server...",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = when (loadingStep) {
                                        0 -> if (language == AppLanguage.BN) "সিকিউর গেটওয়ে রিকোয়েস্ট পাঠানো হচ্ছে..." else "Requesting secure gateway token..."
                                        1 -> if (language == AppLanguage.BN) "অ্যাসেট ভেরিফাই ও লোড হচ্ছে (68%)..." else "Loading gaming assets (68%)..."
                                        2 -> if (language == AppLanguage.BN) "ওয়েবসকেট হ্যান্ডশেক কানেক্ট করা হচ্ছে..." else "Establishing secure WebSocket handshake..."
                                        else -> if (language == AppLanguage.BN) "হোস্ট রেসপন্সের অপেক্ষা করা হচ্ছে..." else "Waiting for host response..."
                                    },
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                LinearProgressIndicator(
                                    progress = {
                                        when (loadingStep) {
                                            0 -> 0.25f
                                            1 -> 0.65f
                                            2 -> 0.88f
                                            else -> 0.95f
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF38BDF8),
                                    trackColor = Color(0xFF1E293B)
                                )
                            }
                        } else {
                            // -------------------------------------------------------------
                            // STATE 2: NETWORK CONNECTION ERROR (As requested by user!)
                            // -------------------------------------------------------------
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                    border = BorderStroke(2.dp, Color(0xFFEF4444)),
                                    modifier = Modifier.size(68.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.WifiOff,
                                            contentDescription = "Network Error",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = if (language == AppLanguage.BN) "নেটওয়ার্ক কানেকশন সমস্যা!" else "Network Connection Error!",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                                    modifier = Modifier.padding(top = 6.dp)
                                ) {
                                    Text(
                                        text = "📡 CONNECTION TIMEOUT • ERR_CONNECTION_TIMED_OUT",
                                        color = Color(0xFFFCA5A5),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = if (language == AppLanguage.BN)
                                        "গেম সার্ভারের সাথে নিরাপদ সংযোগ স্থাপন করা সম্ভব হয়নি। আপনার ইন্টারনেট সংযোগ ধীরগতির হতে পারে অথবা প্রোভাইডার হোস্টের সাথে কানেকশন টাইমআউট হয়েছে (ERR_CONNECTION_TIMED_OUT)। অনুগ্রহ করে আপনার ইন্টারনেট চেক করুন এবং পুনরায় চেষ্টা করুন।"
                                    else
                                        "Unable to establish a secure connection with the game server host. The request timed out (ERR_CONNECTION_TIMED_OUT). Please check your internet connection and try again.",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Action Buttons: Retry and Back to Lobby
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { retryCount++ },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .testTag("network_error_retry_btn"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFEF4444),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (language == AppLanguage.BN) "পুনরায় চেষ্টা করুন" else "Retry Connection",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = onDismiss,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFF475569))
                                    ) {
                                        Text(
                                            text = if (language == AppLanguage.BN) "লবিতে ফিরে যান" else "Back to Lobby",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
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
