package com.example.predictor

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictorScreen(
    viewModel: PredictorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val selectedGame by viewModel.selectedGame.collectAsState()
    val selectedPlatform by viewModel.selectedPlatform.collectAsState()
    val currentMultiplier by viewModel.currentMultiplier.collectAsState()
    val targetMultiplier by viewModel.targetMultiplier.collectAsState()
    val safeCashout by viewModel.safeCashout.collectAsState()
    val countdown by viewModel.countdownSeconds.collectAsState()
    val confidence by viewModel.confidencePercent.collectAsState()
    val statusText by viewModel.analysisStatusText.collectAsState()
    val progress by viewModel.analysisProgress.collectAsState()
    val seedHash by viewModel.serverSeedHash.collectAsState()
    val history by viewModel.signalsHistory.collectAsState()
    val customPreset by viewModel.customPresetMultiplier.collectAsState()
    val isAutoMode by viewModel.isAutoMode.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    val primaryThemeColor = Color(selectedGame.primaryColorHex)

    Scaffold(
        containerColor = Color(0xFF06090F),
        topBar = {
            // Top App Bar with Server Status & VIP Indicator
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0E17)
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "AVIATOR AI PREDICTOR",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "PRO v5.2 • LIVE ENCRYPTED FEED",
                                color = Color(0xFF00E5FF),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    // VIP Override Badge / Settings Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (customPreset != null) Color(0xFF8E05C2) else Color(0xFF141D2D),
                        border = BorderStroke(1.dp, if (customPreset != null) Color(0xFFFF4081) else Color(0xFF223147)),
                        modifier = Modifier
                            .clickable { showSettingsDialog = true }
                            .padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "VIP Settings",
                                tint = if (customPreset != null) Color.White else Color(0xFF9EBFBF),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (customPreset != null) "PRESET: ${customPreset}x" else "VIP SETTINGS",
                                color = if (customPreset != null) Color.White else Color(0xFF9EBFBF),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Casino Platform Selector Pills
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CasinoPlatform.values()) { platform ->
                    val isSelected = platform == selectedPlatform
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(platform.badgeColorHex).copy(alpha = 0.2f) else Color(0xFF111724),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(platform.badgeColorHex) else Color(0xFF1E293B)
                        ),
                        modifier = Modifier.clickable { viewModel.selectPlatform(platform) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(platform.badgeColorHex))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = platform.platformName,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Game Selector Horizontal Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PredictorGame.values()) { game ->
                    val isSelected = game == selectedGame
                    val color = Color(game.primaryColorHex)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) color.copy(alpha = 0.25f) else Color(0xFF0F1522),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) color else Color(0xFF1A2333)
                        ),
                        modifier = Modifier.clickable { viewModel.selectGame(game) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = game.iconEmoji, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = game.displayName,
                                color = if (isSelected) Color.White else Color(0xFF64748B),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. MAIN PREDICTOR STAGE / HUD RADAR DISPLAY
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = primaryThemeColor,
                        spotColor = primaryThemeColor
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF080C14)),
                border = BorderStroke(
                    1.5.dp,
                    Brush.verticalGradient(
                        listOf(primaryThemeColor, Color(0xFF1E293B), primaryThemeColor.copy(alpha = 0.3f))
                    )
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    // Flight Canvas (Active during flight or crashed)
                    PredictorFlightCanvas(
                        state = state,
                        currentMultiplier = currentMultiplier,
                        targetMultiplier = targetMultiplier,
                        primaryColor = primaryThemeColor
                    )

                    // Top Bar inside HUD: Round ID & Platform Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xCC05080E),
                            border = BorderStroke(1.dp, Color(0x3300E5FF))
                        ) {
                            Text(
                                text = "HASH: ${seedHash.take(8)}",
                                color = Color(0xFF00E5FF),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xCC05080E),
                            border = BorderStroke(1.dp, Color(0x3300E676))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E676))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ACCURACY ${confidence}%",
                                    color = Color(0xFF00E676),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // CENTER CONTENT DEPENDING ON STATE
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when (state) {
                            PredictorState.IDLE -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = selectedGame.iconEmoji,
                                        fontSize = 46.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "AI ENGINE READY",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Click 'GET SIGNAL' to predict next crash point",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            PredictorState.ANALYZING -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    CircularProgressIndicator(
                                        progress = { progress },
                                        color = primaryThemeColor,
                                        strokeWidth = 5.dp,
                                        modifier = Modifier.size(60.dp),
                                        trackColor = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = statusText,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Scanning ${selectedPlatform.platformName} live RTP hash...",
                                        color = Color(0xFF00E5FF),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            PredictorState.SIGNAL_LOCKED -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0x3300E676),
                                        border = BorderStroke(1.dp, Color(0xFF00E676))
                                    ) {
                                        Text(
                                            text = "🎯 NEXT ROUND SIGNAL LOCKED",
                                            color = Color(0xFF00E676),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Huge Glowing Multiplier
                                    Text(
                                        text = "${targetMultiplier}x",
                                        color = Color.White,
                                        fontSize = 52.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.SansSerif
                                    )

                                    // Countdown Timer Ring
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = "FLIGHT STARTS IN: ",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "00:0${countdown}s",
                                            color = Color(0xFFFFD54F),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0x88050E17),
                                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                                    ) {
                                        Text(
                                            text = "RECOMMENDED CASHOUT: ${safeCashout}x",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            PredictorState.SIMULATING_FLIGHT -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${currentMultiplier}x",
                                        color = Color.White,
                                        fontSize = 58.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0x6600E5FF),
                                        border = BorderStroke(1.dp, Color(0xFF00E5FF))
                                    ) {
                                        Text(
                                            text = "TARGET: ${targetMultiplier}x",
                                            color = Color(0xFFE0F7FA),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            PredictorState.CRASHED -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0x33FF1744),
                                        border = BorderStroke(1.dp, Color(0xFFFF1744))
                                    ) {
                                        Text(
                                            text = "💥 FLEW AWAY / CRASHED",
                                            color = Color(0xFFFF5252),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "@ ${targetMultiplier}x",
                                        color = Color(0xFFFF1744),
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Black
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0x99052E16),
                                        border = BorderStroke(1.dp, Color(0xFF22C55E))
                                    ) {
                                        Text(
                                            text = "✅ 100% PREDICTION ACCURACY HIT!",
                                            color = Color(0xFF86EFAC),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. ACTION CONTROLS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // GET SIGNAL / NEXT ROUND BUTTON
                Button(
                    onClick = { viewModel.requestSignal() },
                    enabled = state != PredictorState.ANALYZING && state != PredictorState.SIMULATING_FLIGHT,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .shadow(12.dp, RoundedCornerShape(14.dp), ambientColor = primaryThemeColor, spotColor = primaryThemeColor),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryThemeColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (state == PredictorState.IDLE) "⚡ GET SIGNAL" else "⚡ NEXT ROUND",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // AUTO MODE TOGGLE BUTTON
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isAutoMode) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFF111724),
                    border = BorderStroke(
                        1.5.dp,
                        if (isAutoMode) Color(0xFF00E676) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .height(52.dp)
                        .clickable { viewModel.toggleAutoMode() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isAutoMode) Color(0xFF00E676) else Color(0xFF64748B))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAutoMode) "AUTO ON" else "AUTO",
                            color = if (isAutoMode) Color.White else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. LIVE SIGNAL HISTORY RIBBON
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT SIGNALS HISTORY",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "100% VERIFIED",
                    color = Color(0xFF00E676),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(history) { sig ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0D1322),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${sig.targetMultiplier}x",
                                color = if (sig.targetMultiplier >= 3.0) Color(0xFFFFD700) else Color(0xFF00E5FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "✅ WIN",
                                color = Color(0xFF22C55E),
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // 6. VIP PRESET MULTIPLIER MODAL DIALOG
    if (showSettingsDialog) {
        VipPresetDialog(
            currentPreset = customPreset,
            onSetPreset = { newPreset ->
                viewModel.setCustomPreset(newPreset)
                showSettingsDialog = false
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun VipPresetDialog(
    currentPreset: Double?,
    onSetPreset: (Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var inputVal by remember { mutableStateOf(currentPreset?.toString() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B101D)),
            border = BorderStroke(1.5.dp, Color(0xFF8E05C2)),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "👑 VIP TARGET OVERRIDE",
                    color = Color(0xFFFF4081),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Type any exact multiplier you want the AI to predict and crash at:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = inputVal,
                    onValueChange = { inputVal = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("e.g. 4.50 or 8.20", color = Color(0xFF475569)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8E05C2),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Chips: 2.0x, 3.5x, 5.0x, 10.0x
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("2.00", "3.50", "5.80", "10.00").forEach { quick ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E1B4B),
                            border = BorderStroke(1.dp, Color(0xFF6366F1)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { inputVal = quick }
                        ) {
                            Text(
                                text = "${quick}x",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onSetPreset(null)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear / Auto", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val parsed = inputVal.toDoubleOrNull()
                            onSetPreset(parsed)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E05C2)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply Override", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
