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
    onOpenGame: ((String) -> Unit)? = null,
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
        // Row 1: Super Ace, Fortune Gems 3, FlyX
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SuperAceCard(onPlay = onOpenSuperAce)
            }
            Box(modifier = Modifier.weight(1f)) {
                FortuneGems3Card(onPlay = { onOpenGame?.invoke("slot_fortune_gems_3") })
            }
            Box(modifier = Modifier.weight(1f)) {
                FlyXCard(onPlay = { onOpenGame?.invoke("flyx") })
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Aviator (FUNCTIONAL), Boxing King, Mighty Sevens
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AviatorHotCard(onPlay = onOpenAviator)
            }
            Box(modifier = Modifier.weight(1f)) {
                BoxingKingCard(onPlay = { onOpenGame?.invoke("boxing_king") })
            }
            Box(modifier = Modifier.weight(1f)) {
                MightySevensCard(onPlay = { onOpenGame?.invoke("mighty_sevens") })
            }
        }
    }
}

// -------------------------------------------------------------
// GAME 1: Super Ace (Interactive - Exact Match to IMG-20260920-WA0005.jpg)
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4A7228)),
        border = BorderStroke(1.5.dp, Color(0xFF28562A))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Background Artwork: Glowing Lime-Olive Gradient with Radiant Aura
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Radial olive/lime green base
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF92BB35),
                            Color(0xFF779E26),
                            Color(0xFF4D721A),
                            Color(0xFF1B432C),
                            Color(0xFF0C2B1D)
                        )
                    )
                )

                // Central golden glow behind crown
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0x99FFF9C4),
                            Color(0x55E6EE9C),
                            Color(0x00000000)
                        ),
                        center = Offset(w * 0.5f, h * 0.44f),
                        radius = w * 0.55f
                    ),
                    center = Offset(w * 0.5f, h * 0.44f),
                    radius = w * 0.55f
                )

                // Sparkling 4-point star lens flares
                fun drawSparkle(center: Offset, radius: Float, color: Color = Color.White) {
                    val p = Path().apply {
                        moveTo(center.x, center.y - radius)
                        quadraticTo(center.x, center.y, center.x + radius, center.y)
                        quadraticTo(center.x, center.y, center.x, center.y + radius)
                        quadraticTo(center.x, center.y, center.x - radius, center.y)
                        quadraticTo(center.x, center.y, center.x, center.y - radius)
                        close()
                    }
                    drawPath(p, color)
                    drawCircle(color = color.copy(alpha = 0.85f), radius = radius * 0.35f, center = center)
                }

                // Sparkles matching screenshot positions
                drawSparkle(Offset(w * 0.28f, h * 0.16f), 9f, Color(0xFFFFFDE7))
                drawSparkle(Offset(w * 0.42f, h * 0.10f), 6f, Color(0xFFFFF9C4))
                drawSparkle(Offset(w * 0.62f, h * 0.20f), 8f, Color(0xFFFFFDE7))
                drawSparkle(Offset(w * 0.16f, h * 0.68f), 12f, Color(0xFFFFFFFF)) // Bright flare near base left
                drawSparkle(Offset(w * 0.84f, h * 0.60f), 9f, Color(0xFFFFFFFF)) // Flare near base right

                // -------------------------------------------------------------
                // 2. 3D ROYAL JESTER CROWN (IMG-20260920-WA0005.jpg)
                // -------------------------------------------------------------
                val cx = w * 0.50f
                val baseBandTopY = h * 0.63f
                val baseBandBottomY = h * 0.74f
                val baseHalfW = w * 0.30f

                // Key Peak Coordinates
                val centerTip = Offset(cx, h * 0.21f)
                val leftTip = Offset(w * 0.13f, h * 0.32f)
                val rightTip = Offset(w * 0.87f, h * 0.32f)

                // Valleys between horns
                val leftValley = Offset(cx - w * 0.17f, h * 0.44f)
                val rightValley = Offset(cx + w * 0.17f, h * 0.44f)

                // Base connection points
                val baseLeft = Offset(cx - baseHalfW, baseBandTopY)
                val baseRight = Offset(cx + baseHalfW, baseBandTopY)
                val baseCenterTop = Offset(cx, baseBandTopY + 4f)

                // A. HEAVY 3D GOLD CASING OUTLINE BEHIND CROWN
                val casingPath = Path().apply {
                    moveTo(baseLeft.x, baseLeft.y)
                    // Left outer curve
                    cubicTo(w * 0.08f, h * 0.48f, w * 0.07f, h * 0.36f, leftTip.x, leftTip.y)
                    // Left inner curve to valley
                    quadraticTo(w * 0.22f, h * 0.37f, leftValley.x, leftValley.y)
                    // Valley to center tip
                    quadraticTo(cx - w * 0.10f, h * 0.30f, centerTip.x, centerTip.y)
                    // Center tip to right valley
                    quadraticTo(cx + w * 0.10f, h * 0.30f, rightValley.x, rightValley.y)
                    // Right valley to right tip
                    quadraticTo(w * 0.78f, h * 0.37f, rightTip.x, rightTip.y)
                    // Right outer curve to base
                    cubicTo(w * 0.92f, h * 0.36f, w * 0.92f, h * 0.48f, baseRight.x, baseRight.y)
                    // Curve along base band
                    quadraticTo(cx, baseBandTopY + 8f, baseLeft.x, baseLeft.y)
                    close()
                }

                // Dark drop shadow behind crown
                drawPath(
                    path = casingPath,
                    color = Color(0x66071E11)
                )

                // Thick golden rim casing
                drawPath(
                    path = casingPath,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFFEE55), Color(0xFFF59E0B), Color(0xFFB45309), Color(0xFF78350F))
                    ),
                    style = Stroke(width = 10f)
                )

                // Specular gold line on top of casing
                drawPath(
                    path = casingPath,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFFFDE7), Color(0xFFFFEE55), Color(0xFFD97706))
                    ),
                    style = Stroke(width = 3.5f)
                )

                // B. ALTERNATING 3D FACETS (RED & GOLDEN YELLOW)

                // Facet 1: Left Horn Outer (Brilliant Gold)
                val f1 = Path().apply {
                    moveTo(baseLeft.x, baseLeft.y)
                    cubicTo(w * 0.08f, h * 0.48f, w * 0.07f, h * 0.36f, leftTip.x, leftTip.y)
                    quadraticTo(w * 0.18f, h * 0.46f, baseLeft.x + 10f, baseLeft.y)
                    close()
                }
                drawPath(
                    f1,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F), Color(0xFFF59E0B), Color(0xFFB45309)),
                        start = leftTip,
                        end = baseLeft
                    )
                )

                // Facet 2: Left Horn Inner (Crimson Red)
                val f2 = Path().apply {
                    moveTo(leftTip.x, leftTip.y)
                    quadraticTo(w * 0.22f, h * 0.37f, leftValley.x, leftValley.y)
                    quadraticTo(w * 0.26f, h * 0.54f, baseLeft.x + 10f, baseLeft.y)
                    quadraticTo(w * 0.18f, h * 0.46f, leftTip.x, leftTip.y)
                    close()
                }
                drawPath(
                    f2,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFEF4444), Color(0xFFDC2626), Color(0xFF991B1B), Color(0xFF7F1D1D)),
                        start = leftTip,
                        end = leftValley
                    )
                )

                // Facet 3: Center Horn Left Half (Crimson Red)
                val f3 = Path().apply {
                    moveTo(centerTip.x, centerTip.y)
                    quadraticTo(cx - w * 0.10f, h * 0.30f, leftValley.x, leftValley.y)
                    quadraticTo(cx - w * 0.08f, h * 0.54f, baseCenterTop.x - 2f, baseCenterTop.y)
                    lineTo(centerTip.x, centerTip.y)
                    close()
                }
                drawPath(
                    f3,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFF87171), Color(0xFFDC2626), Color(0xFF991B1B)),
                        start = centerTip,
                        end = leftValley
                    )
                )

                // Facet 4: Center Horn Right Half (Brilliant Gold)
                val f4 = Path().apply {
                    moveTo(centerTip.x, centerTip.y)
                    lineTo(baseCenterTop.x - 2f, baseCenterTop.y)
                    quadraticTo(cx + w * 0.08f, h * 0.54f, rightValley.x, rightValley.y)
                    quadraticTo(cx + w * 0.10f, h * 0.30f, centerTip.x, centerTip.y)
                    close()
                }
                drawPath(
                    f4,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFFF9C4), Color(0xFFFFEE55), Color(0xFFF59E0B)),
                        start = centerTip,
                        end = rightValley
                    )
                )

                // Facet 5: Right Horn Inner (Crimson Red)
                val f5 = Path().apply {
                    moveTo(rightValley.x, rightValley.y)
                    quadraticTo(w * 0.78f, h * 0.37f, rightTip.x, rightTip.y)
                    quadraticTo(w * 0.82f, h * 0.46f, baseRight.x - 10f, baseRight.y)
                    quadraticTo(w * 0.74f, h * 0.54f, rightValley.x, rightValley.y)
                    close()
                }
                drawPath(
                    f5,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFEF4444), Color(0xFFDC2626), Color(0xFF991B1B), Color(0xFF7F1D1D)),
                        start = rightTip,
                        end = rightValley
                    )
                )

                // Facet 6: Right Horn Outer (Brilliant Gold)
                val f6 = Path().apply {
                    moveTo(rightTip.x, rightTip.y)
                    cubicTo(w * 0.92f, h * 0.36f, w * 0.92f, h * 0.48f, baseRight.x, baseRight.y)
                    quadraticTo(w * 0.82f, h * 0.46f, rightTip.x, rightTip.y)
                    close()
                }
                drawPath(
                    f6,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F), Color(0xFFF59E0B), Color(0xFFB45309)),
                        start = rightTip,
                        end = baseRight
                    )
                )

                // Center Ridge Specular Highlight Line
                drawLine(
                    color = Color(0xFFFFFDE7),
                    start = centerTip,
                    end = baseCenterTop,
                    strokeWidth = 2.5f
                )

                // C. BASE BAND: CURVED 3D POLISHED GOLDEN CYLINDER
                val baseBandPath = Path().apply {
                    moveTo(baseLeft.x - 2f, baseBandTopY)
                    quadraticTo(cx, baseBandTopY + 7f, baseRight.x + 2f, baseBandTopY)
                    lineTo(baseRight.x, baseBandBottomY)
                    quadraticTo(cx, baseBandBottomY + 7f, baseLeft.x - 4f, baseBandBottomY)
                    close()
                }

                // Base band drop shadow
                drawPath(baseBandPath, color = Color(0x66000000))

                // Base band gold metallic surface
                drawPath(
                    path = baseBandPath,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFFB45309),
                            Color(0xFFFFD54F),
                            Color(0xFFFFFDE7),
                            Color(0xFFFFD54F),
                            Color(0xFF92400E)
                        )
                    )
                )

                // Base band bevel lips
                drawPath(
                    path = baseBandPath,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFFFDE7), Color(0x00000000), Color(0xFF78350F))
                    ),
                    style = Stroke(width = 2.5f)
                )

                // D. 3D GOLDEN JINGLE BELLS (AT ALL 3 PEAKS)
                fun drawJingleBell(center: Offset, radius: Float) {
                    // Drop shadow
                    drawCircle(
                        color = Color(0x55000000),
                        radius = radius + 1.5f,
                        center = Offset(center.x + 1.5f, center.y + 2f)
                    )
                    // Outer gold rim
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFFFFFDE7), Color(0xFFFFD54F), Color(0xFFD97706), Color(0xFF78350F)),
                            center = Offset(center.x - radius * 0.3f, center.y - radius * 0.35f),
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                    // Metallic gold stroke
                    drawCircle(
                        color = Color(0xFF78350F),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )
                    // Bell slit opening: vertical dark line with center dot
                    drawLine(
                        color = Color(0xFF451A03),
                        start = Offset(center.x, center.y - radius * 0.1f),
                        end = Offset(center.x, center.y + radius * 0.65f),
                        strokeWidth = 2.5f
                    )
                    drawCircle(
                        color = Color(0xFF451A03),
                        radius = radius * 0.22f,
                        center = Offset(center.x, center.y + radius * 0.1f)
                    )
                    // Specular highlight gleam
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = radius * 0.25f,
                        center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f)
                    )
                }

                // Left Bell
                drawJingleBell(leftTip, w * 0.08f)
                // Center Bell (Largest)
                drawJingleBell(centerTip, w * 0.09f)
                // Right Bell
                drawJingleBell(rightTip, w * 0.08f)
            }

            // -------------------------------------------------------------
            // 3. TOP RIGHT: GOLDEN JL BADGE WITH WHITE HEART
            // -------------------------------------------------------------
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 8.dp, bottomEnd = 6.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, Color(0xFFFFE082)),
                    modifier = Modifier
                        .size(width = 38.dp, height = 24.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFD54F), Color(0xFFD97706), Color(0xFF78350F))
                            ),
                            shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 8.dp, bottomEnd = 6.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Embossed JL
                        Text(
                            text = "JL",
                            color = Color(0xFFFFF9C4),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color(0xFF5C2C06),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            )
                        )

                        // Pure white heart superimposed
                        Text(
                            text = "🤍",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.offset(y = 1.dp)
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // 4. BOTTOM TITLE: 3D "SuperAce" + "JILI" OVERLAY
            // -------------------------------------------------------------
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                // Layer A: "SuperAce" 3D Block Text
                Box(contentAlignment = Alignment.Center) {
                    // Dark 3D Drop Shadow / Extrusion
                    Text(
                        text = "SuperAce",
                        color = Color(0xFF0F1B12),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.offset(y = 2.dp)
                    )
                    // Dark stroke outline
                    Text(
                        text = "SuperAce",
                        color = Color(0xFF192A1A),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.offset(y = 1.dp)
                    )
                    // Front Olive-Silver Metallic Face
                    Text(
                        text = "SuperAce",
                        color = Color(0xFFB5C9A4),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF1B301D),
                                offset = Offset(0f, 1.5f),
                                blurRadius = 1.5f
                            )
                        )
                    )
                }

                // Layer B: Embossed Chrome White "JILI" Overlay (Positioned across the bottom of SuperAce)
                Box(
                    modifier = Modifier.offset(y = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Dark shadow for JILI
                    Text(
                        text = "JILI",
                        color = Color(0xCC000000),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.offset(y = 1.dp)
                    )
                    // Gleaming White Chrome JILI
                    Text(
                        text = "JILI",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF64748B),
                                offset = Offset(0.5f, 0.5f),
                                blurRadius = 1f
                            )
                        )
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// GAME 2: Wild Athena Rising (Static)
// -------------------------------------------------------------
@Composable
private fun WildAthenaRisingCard(onPlay: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp)
            .clickable(enabled = onPlay != null) { onPlay?.invoke() },
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
private fun FlyXCard(onPlay: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp)
            .clickable(enabled = onPlay != null) { onPlay?.invoke() },
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
// GAME 4: Aviator (FUNCTIONAL - Exact Match to IMG-20260920-WA0006.jpg)
// -------------------------------------------------------------
@Composable
private fun AviatorHotCard(
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onPlay)
            .testTag("hot_game_aviator"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B2421)),
        // Dark Teal-Green outer card border (matching user's green theme request)
        border = BorderStroke(1.5.dp, Color(0xFF103D36))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // -------------------------------------------------------------
            // 1. CANVAS: Vintage Aviation Sky with Green Gradient, Rays & Red Plane
            // -------------------------------------------------------------
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w * 0.5f

                // Base Background: Charcoal/Slate-Grey top fading into Deep Dark Teal-Green bottom
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF282B2D),
                            Color(0xFF1C2022),
                            Color(0xFF14191A),
                            Color(0xFF0E2824),
                            Color(0xFF071F1C)
                        )
                    )
                )

                // Vintage Altitude / Compass Rose at Top Center
                val compassCenter = Offset(cx, h * 0.04f)
                val compassR = w * 0.26f
                drawCircle(
                    color = Color(0xFF4A5568).copy(alpha = 0.25f),
                    radius = compassR,
                    center = compassCenter,
                    style = Stroke(width = 1.5f)
                )
                drawCircle(
                    color = Color(0xFF4A5568).copy(alpha = 0.15f),
                    radius = compassR * 0.7f,
                    center = compassCenter,
                    style = Stroke(width = 1f)
                )
                // Compass tick marks
                for (i in 0 until 16) {
                    val angle = (i * 22.5) * (Math.PI / 180f)
                    val r1 = compassR * 0.85f
                    val r2 = compassR
                    val x1 = compassCenter.x + (r1 * Math.cos(angle)).toFloat()
                    val y1 = compassCenter.y + (r1 * Math.sin(angle)).toFloat()
                    val x2 = compassCenter.x + (r2 * Math.cos(angle)).toFloat()
                    val y2 = compassCenter.y + (r2 * Math.sin(angle)).toFloat()
                    if (y2 > 0f) {
                        drawLine(
                            color = Color(0xFF64748B).copy(alpha = 0.25f),
                            start = Offset(x1, y1),
                            end = Offset(x2, y2),
                            strokeWidth = 1.5f
                        )
                    }
                }

                // Radiating Sunburst Rays centered behind the plane
                val rayCenter = Offset(w * 0.52f, h * 0.36f)
                val numRays = 18
                for (i in 0 until numRays) {
                    val angle = (i * (360f / numRays)) * (Math.PI / 180f)
                    val rayLength = w * 0.9f
                    val x = rayCenter.x + (rayLength * Math.cos(angle)).toFloat()
                    val y = rayCenter.y + (rayLength * Math.sin(angle)).toFloat()
                    drawLine(
                        color = Color.White.copy(alpha = 0.035f),
                        start = rayCenter,
                        end = Offset(x, y),
                        strokeWidth = 14f
                    )
                }

                // Red Aura Behind Plane
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0x66E50914), Color(0x00E50914)),
                        center = rayCenter,
                        radius = w * 0.45f
                    ),
                    center = rayCenter,
                    radius = w * 0.45f
                )

                // -------------------------------------------------------------
                // 2. INSET RED WEATHERED FRAME (IMG-20260920-WA0006.jpg)
                // -------------------------------------------------------------
                val inset = 12f
                val framePath = Path().apply {
                    moveTo(inset + 6f, inset)
                    lineTo(w - inset - 6f, inset)
                    lineTo(w - inset, inset + 6f)
                    lineTo(w - inset, h - inset - 6f)
                    lineTo(w - inset - 6f, h - inset)
                    lineTo(inset + 6f, h - inset)
                    lineTo(inset, h - inset - 6f)
                    lineTo(inset, inset + 6f)
                    close()
                }
                drawPath(
                    path = framePath,
                    color = Color(0xFFE50914).copy(alpha = 0.85f),
                    style = Stroke(width = 2f)
                )

                // -------------------------------------------------------------
                // 3. ICONIC RED AVIATOR PROPELLER PLANE
                // -------------------------------------------------------------
                // The plane climbs at ~30 degrees towards top-right
                val planeNose = Offset(w * 0.72f, h * 0.28f)
                val planeTail = Offset(w * 0.24f, h * 0.46f)
                val planeCenter = Offset(w * 0.50f, h * 0.35f)

                // Main Aerodynamic Fuselage
                val fuselage = Path().apply {
                    // Start at propeller spinner hub
                    moveTo(planeNose.x, planeNose.y)
                    // Top fuselage to cockpit
                    quadraticTo(w * 0.62f, h * 0.29f, w * 0.54f, h * 0.31f)
                    // Cockpit bubble
                    quadraticTo(w * 0.46f, h * 0.33f, w * 0.40f, h * 0.36f)
                    // Spine to tail fin
                    lineTo(w * 0.26f, h * 0.44f)
                    // Rudder fin pointing up
                    lineTo(w * 0.20f, h * 0.40f)
                    // Rudder back
                    lineTo(w * 0.18f, h * 0.44f)
                    // Lower tail
                    lineTo(w * 0.25f, h * 0.47f)
                    // Bottom belly forward
                    quadraticTo(w * 0.42f, h * 0.44f, w * 0.58f, h * 0.38f)
                    // Nose chin
                    lineTo(w * 0.70f, h * 0.31f)
                    close()
                }

                // Fuselage drop shadow
                drawPath(
                    path = fuselage,
                    color = Color(0x66000000)
                )

                // Fuselage vibrant neon red fill
                drawPath(
                    path = fuselage,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFFFF3355),
                            Color(0xFFE50914),
                            Color(0xFFB3000B)
                        ),
                        start = planeNose,
                        end = planeTail
                    )
                )

                // Neon red fuselage outer highlight stroke
                drawPath(
                    path = fuselage,
                    color = Color(0xFFFF5E7E),
                    style = Stroke(width = 1.5f)
                )

                // Large Aerodynamic Swept Wings
                val wingPath = Path().apply {
                    moveTo(w * 0.60f, h * 0.34f) // Wing root top
                    lineTo(w * 0.34f, h * 0.35f) // Wing tip left
                    lineTo(w * 0.38f, h * 0.40f) // Wing trailing tip
                    lineTo(w * 0.52f, h * 0.38f) // Wing root bottom
                    close()
                }
                drawPath(
                    path = wingPath,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFF2247), Color(0xFFCC000E)),
                        start = Offset(w * 0.60f, h * 0.34f),
                        end = Offset(w * 0.34f, h * 0.35f)
                    )
                )
                drawPath(
                    path = wingPath,
                    color = Color(0xFFFF5E7E),
                    style = Stroke(width = 1.5f)
                )

                // Lower small landing gear / wing detail
                drawLine(
                    color = Color(0xFFE50914),
                    start = Offset(w * 0.48f, h * 0.40f),
                    end = Offset(w * 0.44f, h * 0.44f),
                    strokeWidth = 2.5f
                )

                // Two Spinning Propeller Blades
                // Blade 1: Upper blade sweeping up-left
                val propBlade1 = Path().apply {
                    moveTo(planeNose.x, planeNose.y)
                    quadraticTo(w * 0.70f, h * 0.23f, w * 0.68f, h * 0.19f)
                    quadraticTo(w * 0.72f, h * 0.22f, planeNose.x, planeNose.y)
                    close()
                }
                drawPath(
                    path = propBlade1,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFF5E7E), Color(0xFFE50914))
                    )
                )
                drawPath(path = propBlade1, color = Color(0xFFFF8A9E), style = Stroke(width = 1.2f))

                // Blade 2: Lower blade sweeping down-right
                val propBlade2 = Path().apply {
                    moveTo(planeNose.x, planeNose.y)
                    quadraticTo(w * 0.77f, h * 0.35f, w * 0.81f, h * 0.40f)
                    quadraticTo(w * 0.75f, h * 0.35f, planeNose.x, planeNose.y)
                    close()
                }
                drawPath(
                    path = propBlade2,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFE50914), Color(0xFFFF2247))
                    )
                )
                drawPath(path = propBlade2, color = Color(0xFFFF8A9E), style = Stroke(width = 1.2f))

                // Propeller Spinner Center Cone
                drawCircle(
                    color = Color(0xFFFF5E7E),
                    radius = 4.5f,
                    center = planeNose
                )
                drawCircle(
                    color = Color(0xFF8B0000),
                    radius = 4.5f,
                    center = planeNose,
                    style = Stroke(width = 1.5f)
                )
            }

            // -------------------------------------------------------------
            // 4. TOP RIGHT: TRANSLUCENT CIRCLE WITH WHITE HEART
            // -------------------------------------------------------------
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0x77000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🤍",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.offset(y = (-0.5).dp)
                )
            }

            // -------------------------------------------------------------
            // 5. BOTTOM SECTION: "Aviator" Red Script + SPRIBE Underlined
            // -------------------------------------------------------------
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "Aviator" Iconic Neon Red Script
                Text(
                    text = "Aviator",
                    color = Color(0xFFE50914),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = (-0.5).sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xCCFF1744),
                            offset = Offset(0f, 0f),
                            blurRadius = 10f
                        )
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Two Red Speed Accent Bars (IMG-20260920-WA0006.jpg)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color(0xFFE50914))
                    )
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color(0xFFE50914))
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // "SPRIBE" Crisp Bold White Typography with Sharp Underline
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SPRIBE",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black,
                                offset = Offset(1f, 1f),
                                blurRadius = 2f
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(62.dp)
                            .height(2.dp)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// GAME 5: Wild Bounty Showdown (Static)
// -------------------------------------------------------------
@Composable
private fun WildBountyShowdownCard(onPlay: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp)
            .clickable(enabled = onPlay != null) { onPlay?.invoke() },
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
private fun PirateLegendsCard(onPlay: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp)
            .clickable(enabled = onPlay != null) { onPlay?.invoke() },
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
