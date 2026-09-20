package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * HOT GAMES Section matching the exact screenshot design:
 * - 3-column grid featuring the 6 games:
 *   1. Super Ace (JILI) [Static]
 *   2. Wild Athena Rising (2VA8 Victory Ark) [Static]
 *   3. FlyX (Microgaming) [Static]
 *   4. Aviator (SPRIBE) [FUNCTIONAL - launches real Aviator game]
 *   5. Wild Bounty Showdown (PG Soft) [Static]
 *   6. Pirate Legends (Yellow Bat) [Static]
 */
@Composable
fun HotGamesSection(
    onOpenAviator: () -> Unit,
    onOpenSuperAce: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF03191D))
            .padding(12.dp)
    ) {
        // 1. Top Category Tabs (HOT GAMES, FAVORITES, SLOTS)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tab 1: HOT GAMES (Active)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0C383C),
                border = BorderStroke(1.dp, Color(0xFF145258)),
                modifier = Modifier.weight(1.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "🔥", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "HOT GAMES",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Tab 2: FAVORITES
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF062326),
                border = BorderStroke(1.dp, Color(0xFF0D373C)),
                modifier = Modifier.weight(1.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "👝", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "FAVORITES",
                        color = Color(0xFF7FA1A4),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Tab 3: SLOTS
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF062326),
                border = BorderStroke(1.dp, Color(0xFF0D373C)),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "🎰", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SLOTS",
                        color = Color(0xFF7FA1A4),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Section Header: "HOT GAMES" + "See All" + [<] [>]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HOT GAMES",
                color = Color(0xFF00F5B8),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "See All",
                    color = Color(0xFFFBBF24),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                // Arrow buttons
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF082E32),
                    border = BorderStroke(1.dp, Color(0xFF0E4A51)),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "‹", color = Color(0xFF9EBFBF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF082E32),
                    border = BorderStroke(1.dp, Color(0xFF0E4A51)),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "›", color = Color(0xFF9EBFBF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Grid of 6 Games (3 columns x 2 rows)
        // Row 1: Super Ace, Wild Athena Rising, FlyX
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SuperAceCard(onPlay = onOpenSuperAce)
            }
            Box(modifier = Modifier.weight(1f)) {
                WildAthenaRisingCard()
            }
            Box(modifier = Modifier.weight(1f)) {
                FlyXCard()
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Aviator (FUNCTIONAL), Wild Bounty Showdown, Pirate Legends
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AviatorHotCard(onPlay = onOpenAviator)
            }
            Box(modifier = Modifier.weight(1f)) {
                WildBountyShowdownCard()
            }
            Box(modifier = Modifier.weight(1f)) {
                PirateLegendsCard()
            }
        }
    }
}

// -------------------------------------------------------------
// GAME 1: Super Ace (Interactive)
// -------------------------------------------------------------
@Composable
private fun SuperAceCard(onPlay: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = onPlay != null) { onPlay?.invoke() }
            .testTag("super_ace_hot_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6B8E23)),
        border = BorderStroke(1.dp, Color(0xFF264C35))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Artwork: Lime/Golden gradient glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFD4E157),
                                Color(0xFF827717),
                                Color(0xFF1B382B)
                            )
                        )
                    )
            )

            // Crown Illustration with bells
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val crownBaseY = h * 0.58f

                // Golden radial aura
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0x99FFF176), Color(0x00FFF176)),
                        center = Offset(w * 0.5f, h * 0.42f),
                        radius = w * 0.45f
                    ),
                    center = Offset(w * 0.5f, h * 0.42f),
                    radius = w * 0.45f
                )

                // Crown Path
                val crownPath = Path().apply {
                    moveTo(w * 0.18f, crownBaseY)
                    // Left spike
                    lineTo(w * 0.12f, h * 0.32f)
                    lineTo(w * 0.32f, h * 0.42f)
                    // Center spike
                    lineTo(w * 0.5f, h * 0.25f)
                    lineTo(w * 0.68f, h * 0.42f)
                    // Right spike
                    lineTo(w * 0.88f, h * 0.32f)
                    lineTo(w * 0.82f, crownBaseY)
                    close()
                }

                // Crown Red Velvet Base Fill
                drawPath(
                    path = crownPath,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFE53935), Color(0xFFB71C1C))
                    )
                )

                // Crown Gold Borders
                drawPath(
                    path = crownPath,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                    ),
                    style = Stroke(width = 4f)
                )

                // Gold Bells on peaks
                drawCircle(Color(0xFFFFD54F), radius = 6f, center = Offset(w * 0.12f, h * 0.32f))
                drawCircle(Color(0xFFFFD54F), radius = 8f, center = Offset(w * 0.5f, h * 0.25f))
                drawCircle(Color(0xFFFFD54F), radius = 6f, center = Offset(w * 0.88f, h * 0.32f))

                // Bottom Gold Band
                drawRoundRect(
                    brush = Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFF8F00))),
                    topLeft = Offset(w * 0.15f, crownBaseY - 4f),
                    size = androidx.compose.ui.geometry.Size(w * 0.7f, 14f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }

            // Top Provider & Favorite Icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Top Right: JILI Badge + Heart
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xB33E2723),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = "JILI",
                        color = Color(0xFFFFD700),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0x55000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🤍", fontSize = 9.sp)
                }
            }

            // Bottom Labels: Super Ace + JILI
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC051810), Color(0xF0030E09))
                        )
                    )
                    .padding(horizontal = 4.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Super Ace",
                    color = Color(0xFFE8F5E9),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "JILI",
                    color = Color(0xFFC8E6C9),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// GAME 2: Wild Athena Rising (Static)
// -------------------------------------------------------------
@Composable
private fun WildAthenaRisingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2744)),
        border = BorderStroke(1.dp, Color(0xFF1E3A5F))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Artwork: Celestial Blue & Gold
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2C3E6B), Color(0xFF141F36), Color(0xFF0A101C))
                        )
                    )
            )

            // Canvas drawing for Athena & Divine Staff
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Sunburst Behind Goddess
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0x88FFD54F), Color(0x00FFD54F)),
                        center = Offset(w * 0.5f, h * 0.35f),
                        radius = w * 0.5f
                    ),
                    center = Offset(w * 0.5f, h * 0.35f),
                    radius = w * 0.5f
                )

                // Golden Spear/Staff (Left side)
                drawLine(
                    color = Color(0xFFFFD54F),
                    start = Offset(w * 0.3f, h * 0.7f),
                    end = Offset(w * 0.35f, h * 0.15f),
                    strokeWidth = 3f
                )
                // Staff Trident/Ornament
                drawCircle(Color(0xFFFFE082), radius = 6f, center = Offset(w * 0.35f, h * 0.15f))

                // Athena Figure Silhouette & Helmet
                drawCircle(
                    brush = Brush.linearGradient(listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))),
                    radius = w * 0.16f,
                    center = Offset(w * 0.55f, h * 0.35f)
                )

                // Golden Armor/Diadem
                drawArc(
                    brush = Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFB300))),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.40f, h * 0.22f),
                    size = androidx.compose.ui.geometry.Size(w * 0.30f, h * 0.16f)
                )
            }

            // Top Heart Icon
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0x55000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🤍", fontSize = 9.sp)
            }

            // Bottom Labels: WILD ATHENA RISING + 2VA8 VICTORY ARK
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC050E1D), Color(0xF2020710))
                        )
                    )
                    .padding(horizontal = 4.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "WILD",
                    color = Color(0xFFFFD700),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ATHENA RISING",
                    color = Color.White,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "2VA8 VICTORY ARK",
                    color = Color(0xFF64B5F6),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// GAME 3: FlyX (Static)
// -------------------------------------------------------------
@Composable
private fun FlyXCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E202B)),
        border = BorderStroke(1.dp, Color(0xFF2E3447))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Artwork: Deep night with clouds & supersonic streaks
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2A2E3D), Color(0xFF1A1C24), Color(0xFF0F1015))
                        )
                    )
            )

            // Rocket / Jet Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Jet Trail (Glowing Red/Orange diagonal laser)
                drawLine(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFF1744), Color(0xFFFF8A80), Color.White)
                    ),
                    start = Offset(w * 0.15f, h * 0.65f),
                    end = Offset(w * 0.85f, h * 0.25f),
                    strokeWidth = 9f
                )

                // Supersonic Plane Body
                val planePath = Path().apply {
                    moveTo(w * 0.85f, h * 0.25f) // Nose
                    lineTo(w * 0.60f, h * 0.32f) // Top wing
                    lineTo(w * 0.40f, h * 0.52f) // Left wing
                    lineTo(w * 0.50f, h * 0.42f)
                    close()
                }

                drawPath(
                    path = planePath,
                    brush = Brush.linearGradient(listOf(Color.White, Color(0xFFFFCDD2)))
                )
            }

            // Top Heart Icon
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0x55000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🤍", fontSize = 9.sp)
            }

            // Bottom Labels: FlyX + Microgaming
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC0D0E14), Color(0xF208090C))
                        )
                    )
                    .padding(horizontal = 4.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Fly",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "X™",
                        color = Color(0xFFFF1744),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Ⓜ️ ", fontSize = 7.sp)
                    Text(
                        text = "Microgaming",
                        color = Color(0xFFB0BEC5),
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// GAME 4: Aviator (FUNCTIONAL - Launches real Aviator game!)
// -------------------------------------------------------------
@Composable
private fun AviatorHotCard(
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp)
            .shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = Color(0xFFFF1744), spotColor = Color(0xFFFF1744))
            .clickable(onClick = onPlay)
            .testTag("hot_game_aviator"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0407)),
        // Vibrant Glowing Red Border as seen in the screenshot!
        border = BorderStroke(2.dp, Color(0xFFFF1A38))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Crimson Vignette Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF38080E),
                                Color(0xFF1E0407),
                                Color(0xFF0F0103)
                            )
                        )
                    )
            )

            // Propeller Airplane Illustration
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Red Aura Behind Plane
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0x88FF1744), Color(0x00FF1744)),
                        center = Offset(w * 0.5f, h * 0.42f),
                        radius = w * 0.45f
                    ),
                    center = Offset(w * 0.5f, h * 0.42f),
                    radius = w * 0.45f
                )

                // Aerodynamic Propeller Plane Path
                val planeBody = Path().apply {
                    moveTo(w * 0.78f, h * 0.32f) // Nose propeller point
                    lineTo(w * 0.22f, h * 0.50f) // Tail end
                    lineTo(w * 0.20f, h * 0.40f) // Tail fin tip
                    lineTo(w * 0.27f, h * 0.48f)
                    close()
                }

                drawPath(
                    path = planeBody,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFF5252), Color(0xFFD50000))
                    )
                )

                // Wings
                drawLine(
                    color = Color(0xFFFF1744),
                    start = Offset(w * 0.45f, h * 0.30f),
                    end = Offset(w * 0.60f, h * 0.55f),
                    strokeWidth = 6f
                )

                // Front Propeller blur
                drawLine(
                    color = Color(0xFFFF8A80),
                    start = Offset(w * 0.78f, h * 0.25f),
                    end = Offset(w * 0.78f, h * 0.39f),
                    strokeWidth = 3f
                )
            }

            // Top Heart Icon
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🤍", fontSize = 9.sp)
            }

            // Bottom Labels: Aviator (Red Script) + SPRIBE (White)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC180204), Color(0xF2100102))
                        )
                    )
                    .padding(horizontal = 4.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Aviator",
                    color = Color(0xFFFF1A38),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.SansSerif
                )
                // Red underline accent
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(1.5.dp)
                        .background(Color(0xFFFF1A38))
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "SPRIBE",
                    color = Color.White,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// -------------------------------------------------------------
// GAME 5: Wild Bounty Showdown (Static)
// -------------------------------------------------------------
@Composable
private fun WildBountyShowdownCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1808)),
        border = BorderStroke(1.dp, Color(0xFF4E2A0E))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Artwork: Western Sunset & Desert saloon glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF5D3110), Color(0xFF331804), Color(0xFF1A0A02))
                        )
                    )
            )

            // Western Cowgirl Silhouette & Sunset
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Sunset glow
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0x88FFB74D), Color(0x00FFB74D)),
                        center = Offset(w * 0.5f, h * 0.38f),
                        radius = w * 0.45f
                    ),
                    center = Offset(w * 0.5f, h * 0.38f),
                    radius = w * 0.45f
                )

                // Stetson Hat
                drawOval(
                    brush = Brush.linearGradient(listOf(Color(0xFFD7CCC8), Color(0xFF8D6E63))),
                    topLeft = Offset(w * 0.30f, h * 0.22f),
                    size = androidx.compose.ui.geometry.Size(w * 0.45f, h * 0.12f)
                )

                // Revolver / Gun (Left side)
                drawLine(
                    color = Color(0xFFCFD8DC),
                    start = Offset(w * 0.25f, h * 0.48f),
                    end = Offset(w * 0.30f, h * 0.35f),
                    strokeWidth = 4f
                )
            }

            // Top Heart Icon
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0x55000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🤍", fontSize = 9.sp)
            }

            // Bottom Labels: WILD BOUNTY SHOWDOWN + PG SOFT
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC1A0801), Color(0xF2100501))
                        )
                    )
                    .padding(horizontal = 4.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "WILD BOUNTY",
                    color = Color(0xFFFFB74D),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SHOWDOWN",
                    color = Color(0xFFFFE0B2),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "PG SOFT",
                    color = Color(0xFFECEFF1),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// GAME 6: Pirate Legends (Static, with '$ BUY' badge)
// -------------------------------------------------------------
@Composable
private fun PirateLegendsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1018)),
        border = BorderStroke(1.dp, Color(0xFF4A1828))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Artwork: Red Pirate Seas & Gold
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF6B1B34), Color(0xFF3B0C1B), Color(0xFF19040A))
                        )
                    )
            )

            // Pirate Hat & Flintlock Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Blood-red pirate aura
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0x88FF4081), Color(0x00FF4081)),
                        center = Offset(w * 0.5f, h * 0.40f),
                        radius = w * 0.45f
                    ),
                    center = Offset(w * 0.5f, h * 0.40f),
                    radius = w * 0.45f
                )

                // Pirate Captain Hat (Bicorne shape)
                val hatPath = Path().apply {
                    moveTo(w * 0.20f, h * 0.32f)
                    lineTo(w * 0.50f, h * 0.18f)
                    lineTo(w * 0.80f, h * 0.32f)
                    lineTo(w * 0.50f, h * 0.26f)
                    close()
                }

                drawPath(
                    path = hatPath,
                    brush = Brush.linearGradient(listOf(Color(0xFFD50000), Color(0xFF880E4F)))
                )

                // Skull / Emblem on hat
                drawCircle(Color.White, radius = 4f, center = Offset(w * 0.5f, h * 0.23f))
            }

            // Top Badges: "$ BUY" Badge (Magenta) + Heart Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Vibrant "$ BUY" Badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF8E05C2),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = "$ BUY",
                        color = Color.White,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0x55000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🤍", fontSize = 9.sp)
                }
            }

            // Bottom Labels: PIRATE LEGENDS + YELLOW BAT
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC1A0309), Color(0xF20F0105))
                        )
                    )
                    .padding(horizontal = 4.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PIRATE",
                    color = Color(0xFFFFD54F),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "LEGENDS",
                    color = Color(0xFFFFCDD2),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "YELLOW BAT",
                    color = Color(0xFFFFCA28),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
