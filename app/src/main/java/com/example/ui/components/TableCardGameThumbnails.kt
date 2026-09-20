package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// ========================================================================
// 1. LUDO QUICK (JILI) - Exact Match to IMG-20260921-WA0010.jpg
// ========================================================================
@Composable
fun LudoQuickCard(
    onPlay: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(154.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = onPlay != null) { onPlay?.invoke() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC2410C)),
        border = BorderStroke(1.2.dp, Color(0xFFF97316))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Canvas: Radiant orange sunburst & board art
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Warm sunset amber radiant background
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFED7AA), Color(0xFFFB923C), Color(0xFFEA580C), Color(0xFF9A3412)),
                        center = Offset(w * 0.7f, h * 0.3f),
                        radius = w * 1.1f
                    )
                )

                // Sunburst rays
                for (i in 0 until 12) {
                    val angle = (i * 30f) * (Math.PI / 180f).toFloat()
                    val rayPath = Path().apply {
                        moveTo(w * 0.7f, h * 0.3f)
                        lineTo(
                            w * 0.7f + cos(angle - 0.12f) * w * 1.3f,
                            h * 0.3f + sin(angle - 0.12f) * h * 1.3f
                        )
                        lineTo(
                            w * 0.7f + cos(angle + 0.12f) * w * 1.3f,
                            h * 0.3f + sin(angle + 0.12f) * h * 1.3f
                        )
                        close()
                    }
                    drawPath(rayPath, Color.White.copy(alpha = 0.10f))
                }

                // LUDO Board on right side
                val boardLeft = w * 0.38f
                val boardTop = h * 0.38f
                val boardW = w * 0.60f
                val boardH = h * 0.55f

                // Board background (white)
                drawRoundRect(
                    color = Color(0xFFFFFBEB),
                    topLeft = Offset(boardLeft, boardTop),
                    size = Size(boardW, boardH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(boardLeft, boardTop),
                    size = Size(boardW, boardH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                    style = Stroke(width = 3f)
                )

                // Red quadrant (top-left of board)
                drawRoundRect(
                    color = Color(0xFFDC2626),
                    topLeft = Offset(boardLeft + 4f, boardTop + 4f),
                    size = Size(boardW * 0.44f, boardH * 0.44f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                // 4 Red circles in home
                val rbx = boardLeft + 8f
                val rby = boardTop + 8f
                drawCircle(Color.White, radius = 5f, center = Offset(rbx + 10f, rby + 10f))
                drawCircle(Color.White, radius = 5f, center = Offset(rbx + 28f, rby + 10f))
                drawCircle(Color.White, radius = 5f, center = Offset(rbx + 10f, rby + 26f))
                drawCircle(Color.White, radius = 5f, center = Offset(rbx + 28f, rby + 26f))

                // Green quadrant (top-right of board)
                drawRoundRect(
                    color = Color(0xFF16A34A),
                    topLeft = Offset(boardLeft + boardW * 0.52f, boardTop + 4f),
                    size = Size(boardW * 0.44f, boardH * 0.44f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                // Blue quadrant (bottom-right of board)
                drawRoundRect(
                    color = Color(0xFF0284C7),
                    topLeft = Offset(boardLeft + boardW * 0.52f, boardTop + boardH * 0.52f),
                    size = Size(boardW * 0.44f, boardH * 0.44f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                // 4 Blue circles in home
                val bbx = boardLeft + boardW * 0.54f
                val bby = boardTop + boardH * 0.54f
                drawCircle(Color.White, radius = 5f, center = Offset(bbx + 10f, bby + 10f))
                drawCircle(Color.White, radius = 5f, center = Offset(bbx + 28f, bby + 10f))
                drawCircle(Color.White, radius = 5f, center = Offset(bbx + 10f, bby + 24f))
                drawCircle(Color.White, radius = 5f, center = Offset(bbx + 28f, bby + 24f))

                // 3D Flying Blue Die on top-right
                val dieX = w * 0.65f
                val dieY = h * 0.18f
                val dieSize = w * 0.30f

                // Isometric Die body
                val diePath = Path().apply {
                    moveTo(dieX + dieSize * 0.45f, dieY)
                    lineTo(dieX + dieSize * 0.90f, dieY + dieSize * 0.22f)
                    lineTo(dieX + dieSize * 0.90f, dieY + dieSize * 0.72f)
                    lineTo(dieX + dieSize * 0.45f, dieY + dieSize * 0.95f)
                    lineTo(dieX, dieY + dieSize * 0.72f)
                    lineTo(dieX, dieY + dieSize * 0.22f)
                    close()
                }
                drawPath(diePath, Color(0xFF1D4ED8))
                drawPath(diePath, Color(0xFF93C5FD), style = Stroke(width = 2f))

                // Top face (lighter blue)
                val topFace = Path().apply {
                    moveTo(dieX + dieSize * 0.45f, dieY)
                    lineTo(dieX + dieSize * 0.90f, dieY + dieSize * 0.22f)
                    lineTo(dieX + dieSize * 0.45f, dieY + dieSize * 0.44f)
                    lineTo(dieX, dieY + dieSize * 0.22f)
                    close()
                }
                drawPath(topFace, Color(0xFF3B82F6))

                // White pips on top face (3 dots diagonal)
                drawCircle(Color.White, radius = 3.5f, center = Offset(dieX + dieSize * 0.25f, dieY + dieSize * 0.22f))
                drawCircle(Color.White, radius = 3.5f, center = Offset(dieX + dieSize * 0.45f, dieY + dieSize * 0.22f))
                drawCircle(Color.White, radius = 3.5f, center = Offset(dieX + dieSize * 0.65f, dieY + dieSize * 0.22f))

                // Left face pips (5 dots)
                drawCircle(Color.White, radius = 3f, center = Offset(dieX + dieSize * 0.15f, dieY + dieSize * 0.40f))
                drawCircle(Color.White, radius = 3f, center = Offset(dieX + dieSize * 0.35f, dieY + dieSize * 0.48f))
                drawCircle(Color.White, radius = 3f, center = Offset(dieX + dieSize * 0.25f, dieY + dieSize * 0.58f))
                drawCircle(Color.White, radius = 3f, center = Offset(dieX + dieSize * 0.15f, dieY + dieSize * 0.66f))
                drawCircle(Color.White, radius = 3f, center = Offset(dieX + dieSize * 0.35f, dieY + dieSize * 0.74f))

                // Indian Lady Avatar (Left side of card)
                // Wavy dark hair behind
                drawOval(
                    color = Color(0xFF3E1F14),
                    topLeft = Offset(w * 0.02f, h * 0.02f),
                    size = Size(w * 0.52f, h * 0.75f)
                )

                // Face oval
                drawOval(
                    color = Color(0xFFF0BD93), // Warm golden wheatish skin tone
                    topLeft = Offset(w * 0.08f, h * 0.12f),
                    size = Size(w * 0.38f, h * 0.48f)
                )

                // Hair front curls framing face
                val hairCurlLeft = Path().apply {
                    moveTo(w * 0.08f, h * 0.16f)
                    cubicTo(w * 0.02f, h * 0.30f, w * 0.02f, h * 0.55f, w * 0.08f, h * 0.70f)
                    lineTo(w * 0.15f, h * 0.65f)
                    cubicTo(w * 0.10f, h * 0.45f, w * 0.12f, h * 0.30f, w * 0.18f, h * 0.20f)
                    close()
                }
                drawPath(hairCurlLeft, Color(0xFF26120B))

                val hairCurlRight = Path().apply {
                    moveTo(w * 0.36f, h * 0.16f)
                    cubicTo(w * 0.44f, h * 0.30f, w * 0.46f, h * 0.50f, w * 0.42f, h * 0.68f)
                    lineTo(w * 0.36f, h * 0.64f)
                    cubicTo(w * 0.38f, h * 0.45f, w * 0.36f, h * 0.30f, w * 0.30f, h * 0.20f)
                    close()
                }
                drawPath(hairCurlRight, Color(0xFF26120B))

                // Maang Tikka: Golden chain + Red ruby droplet on forehead
                drawLine(
                    color = Color(0xFFFBBF24),
                    start = Offset(w * 0.24f, h * 0.12f),
                    end = Offset(w * 0.24f, h * 0.24f),
                    strokeWidth = 2.5f
                )
                drawCircle(Color(0xFFFBBF24), radius = 6f, center = Offset(w * 0.24f, h * 0.24f))
                drawCircle(Color(0xFFDC2626), radius = 3.5f, center = Offset(w * 0.24f, h * 0.24f))

                // Almond eyes with liner
                drawArc(
                    color = Color(0xFF1C1917),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.14f, h * 0.28f),
                    size = Size(w * 0.08f, h * 0.04f),
                    style = Stroke(width = 2.5f)
                )
                drawArc(
                    color = Color(0xFF1C1917),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.27f, h * 0.28f),
                    size = Size(w * 0.08f, h * 0.04f),
                    style = Stroke(width = 2.5f)
                )

                // Beautiful smiling lips
                drawOval(
                    color = Color(0xFFE11D48),
                    topLeft = Offset(w * 0.19f, h * 0.44f),
                    size = Size(w * 0.11f, h * 0.045f)
                )

                // Gold Earring
                drawCircle(Color(0xFFFBBF24), radius = 5f, center = Offset(w * 0.06f, h * 0.38f))
                drawCircle(Color(0xFFFBBF24), radius = 5f, center = Offset(w * 0.44f, h * 0.38f))

                // Turquoise saree & jewelry
                drawOval(
                    color = Color(0xFF0D9488),
                    topLeft = Offset(w * 0.02f, h * 0.60f),
                    size = Size(w * 0.45f, h * 0.40f)
                )
                // Gold necklace
                drawArc(
                    color = Color(0xFFFBBF24),
                    startAngle = 10f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(w * 0.13f, h * 0.50f),
                    size = Size(w * 0.24f, h * 0.14f),
                    style = Stroke(width = 4f)
                )

                // Blue Pawn held in fingers
                val pawnX = w * 0.10f
                val pawnY = h * 0.39f
                // Pawn head
                drawCircle(Color(0xFF2563EB), radius = 8f, center = Offset(pawnX, pawnY))
                drawCircle(Color(0xFF93C5FD), radius = 2.5f, center = Offset(pawnX - 2f, pawnY - 2f))
                // Pawn body
                val pawnBody = Path().apply {
                    moveTo(pawnX - 4f, pawnY + 6f)
                    lineTo(pawnX + 4f, pawnY + 6f)
                    lineTo(pawnX + 9f, pawnY + 24f)
                    lineTo(pawnX - 9f, pawnY + 24f)
                    close()
                }
                drawPath(pawnBody, Color(0xFF1D4ED8))
                // Pawn base
                drawRoundRect(
                    color = Color(0xFF1E40AF),
                    topLeft = Offset(pawnX - 11f, pawnY + 23f),
                    size = Size(22f, 6f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
            }

            // Top-right Golden JL Badge with Heart
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                JlBrandHeartBadge()
            }

            // Bottom Title Banner: LUDO Quick + JILI
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "LUDO" in extruded 3D Gold + "Quick" in cursive green
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // LUDO Text with dual layered shadow
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "LUDO",
                            color = Color(0xFF78350F),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp,
                            modifier = Modifier.offset(x = 1.5.dp, y = 2.dp)
                        )
                        Text(
                            text = "LUDO",
                            color = Color(0xFFFEF08A),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp,
                            style = androidx.compose.ui.text.TextStyle(
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFFFFFBEB), Color(0xFFFDE047), Color(0xFFD97706))
                                ),
                                shadow = Shadow(Color(0xFF451A03), Offset(1f, 1f), 2f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // "Quick" cursive script
                    Text(
                        text = "Quick",
                        color = Color(0xFF10B981),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Cursive,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = Shadow(Color(0xFF064E3B), Offset(1.5f, 1.5f), 2f)
                        )
                    )
                }

                // JILI Silver Logo
                JiliSilverLogo(offsetY = (-2).dp)
            }
        }
    }
}

// ========================================================================
// 2. ANDAR BAHAR (KINGMAKER / KINGMIDAS) - Exact Match to IMG-20260921-WA0009.jpg
// ========================================================================
@Composable
fun AndarBaharCard(
    onPlay: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(154.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = onPlay != null) { onPlay?.invoke() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
        border = BorderStroke(1.2.dp, Color(0xFF6366F1))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Cosmic starry dark indigo gradient with flying playing cards
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Deep indigo to galaxy purple gradient
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF4338CA), Color(0xFF312E81), Color(0xFF1E1B4B), Color(0xFF0F172A)),
                        center = Offset(w * 0.5f, h * 0.45f),
                        radius = w * 0.9f
                    )
                )

                // Background floating cards
                // Card 1: Top left (Ace of Hearts)
                val card1Path = Path().apply {
                    moveTo(w * 0.08f, h * 0.10f)
                    lineTo(w * 0.28f, h * 0.16f)
                    lineTo(w * 0.22f, h * 0.28f)
                    lineTo(w * 0.04f, h * 0.22f)
                    close()
                }
                drawPath(card1Path, Color.White.copy(alpha = 0.85f))
                drawCircle(Color(0xFFDC2626), radius = 3.5f, center = Offset(w * 0.16f, h * 0.18f))

                // Card 2: Top right
                val card2Path = Path().apply {
                    moveTo(w * 0.80f, h * 0.20f)
                    lineTo(w * 0.95f, h * 0.25f)
                    lineTo(w * 0.90f, h * 0.35f)
                    lineTo(w * 0.76f, h * 0.30f)
                    close()
                }
                drawPath(card2Path, Color(0xFFFDE68A).copy(alpha = 0.70f))

                // Card 3: Bottom left
                val card3Path = Path().apply {
                    moveTo(w * 0.02f, h * 0.75f)
                    lineTo(w * 0.18f, h * 0.82f)
                    lineTo(w * 0.14f, h * 0.92f)
                    lineTo(w * 0.00f, h * 0.85f)
                    close()
                }
                drawPath(card3Path, Color(0xFFFDE68A).copy(alpha = 0.65f))

                // Sparkles / Dust
                drawCircle(Color.White.copy(alpha = 0.8f), radius = 2f, center = Offset(w * 0.3f, h * 0.25f))
                drawCircle(Color.White.copy(alpha = 0.9f), radius = 2.5f, center = Offset(w * 0.78f, h * 0.70f))
                drawCircle(Color.White.copy(alpha = 0.7f), radius = 1.8f, center = Offset(w * 0.85f, h * 0.12f))

                // Center Ornate Golden Ring Medallion
                val centerX = w * 0.5f
                val centerY = h * 0.44f
                val ringRadius = w * 0.34f

                // Outer golden filigree ring
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(Color(0xFFFDE047), Color(0xFFCA8A04), Color(0xFFFEF08A), Color(0xFFCA8A04), Color(0xFFFDE047)),
                        center = Offset(centerX, centerY)
                    ),
                    radius = ringRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 6f)
                )
                // Inner dark blue void
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F172A)),
                        center = Offset(centerX, centerY),
                        radius = ringRadius * 0.85f
                    ),
                    radius = ringRadius * 0.85f,
                    center = Offset(centerX, centerY)
                )

                // Faceted Ruby Diamond on top-right of the title
                val diamondPath = Path().apply {
                    val dx = w * 0.82f
                    val dy = h * 0.38f
                    moveTo(dx, dy - 6f)
                    lineTo(dx + 6f, dy)
                    lineTo(dx, dy + 6f)
                    lineTo(dx - 6f, dy)
                    close()
                }
                drawPath(diamondPath, Color(0xFFF43F5E))
                drawPath(diamondPath, Color.White, style = Stroke(width = 1.2f))

                // Faceted Blue Diamond on bottom-left
                val blueDiamond = Path().apply {
                    val dx = w * 0.16f
                    val dy = h * 0.58f
                    moveTo(dx, dy - 5f)
                    lineTo(dx + 5f, dy)
                    lineTo(dx, dy + 5f)
                    lineTo(dx - 5f, dy)
                    close()
                }
                drawPath(blueDiamond, Color(0xFF38BDF8))
                drawPath(blueDiamond, Color.White, style = Stroke(width = 1.2f))
            }

            // Top-right white heart in translucent bubble
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0x55000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🤍", fontSize = 12.sp)
            }

            // Center Titles: 3D "Andar" & 3D "Bahar"
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-8).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3D Ice-Blue Cursive "Andar"
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Andar",
                        color = Color(0xFF0C4A6E),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Cursive,
                        modifier = Modifier.offset(x = 1.5.dp, y = 2.dp)
                    )
                    Text(
                        text = "Andar",
                        color = Color(0xFF38BDF8),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Cursive,
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0284C7))
                            ),
                            shadow = Shadow(Color(0xFF075985), Offset(1f, 1f), 3f)
                        )
                    )
                }

                // 3D Magenta/Rose-Pink "Bahar"
                Box(
                    modifier = Modifier.offset(y = (-6).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bahar",
                        color = Color(0xFF881337),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.offset(x = 1.5.dp, y = 2.dp)
                    )
                    Text(
                        text = "Bahar",
                        color = Color(0xFFF43F5E),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFFFFE4E6), Color(0xFFFB7185), Color(0xFFE11D48), Color(0xFF9F1239))
                            ),
                            shadow = Shadow(Color(0xFF4C0519), Offset(1f, 1f), 3f)
                        )
                    )
                }
            }

            // Bottom Dual Brand: KINGMAKER + KINGMIDAS
            KingmakerDualLogo(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            )
        }
    }
}

// ========================================================================
// 3. THAI FISH PRAWN CRAB (น้ำเต้าปูปลา) - Exact Match to IMG-20260921-WA0011.jpg
// ========================================================================
@Composable
fun ThaiFishPrawnCrabCard(
    onPlay: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(154.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = onPlay != null) { onPlay?.invoke() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF134E4A)),
        border = BorderStroke(1.2.dp, Color(0xFF2DD4BF))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Canvas: Traditional dice game board & 3D red dice
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Board background grid (blue-slate parchment)
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )

                // Grid lines of Thai game board
                drawRect(
                    color = Color(0xFF475569).copy(alpha = 0.4f),
                    topLeft = Offset(w * 0.05f, h * 0.05f),
                    size = Size(w * 0.90f, h * 0.85f),
                    style = Stroke(width = 2f)
                )
                drawLine(
                    color = Color(0xFF475569).copy(alpha = 0.4f),
                    start = Offset(w * 0.45f, h * 0.05f),
                    end = Offset(w * 0.45f, h * 0.90f),
                    strokeWidth = 2f
                )

                // Crab silhouette drawing on left
                drawOval(
                    color = Color(0xFF64748B).copy(alpha = 0.45f),
                    topLeft = Offset(w * 0.06f, h * 0.12f),
                    size = Size(w * 0.32f, h * 0.15f)
                )

                // Golden Fish (Gourami) drawing on top-right
                val fishX = w * 0.52f
                val fishY = h * 0.16f
                val fishPath = Path().apply {
                    moveTo(fishX, fishY + 12f)
                    cubicTo(fishX + 20f, fishY, fishX + 45f, fishY + 5f, fishX + 65f, fishY + 15f)
                    lineTo(fishX + 75f, fishY + 5f)
                    lineTo(fishX + 70f, fishY + 22f)
                    lineTo(fishX + 76f, fishY + 38f)
                    lineTo(fishX + 65f, fishY + 28f)
                    cubicTo(fishX + 45f, fishY + 38f, fishX + 20f, fishY + 35f, fishX, fishY + 20f)
                    close()
                }
                drawPath(fishPath, Color(0xFFD97706).copy(alpha = 0.75f))
                drawPath(fishPath, Color(0xFFFDE68A).copy(alpha = 0.85f), style = Stroke(width = 1.5f))

                // Three 3D Red Dice angled dynamically on right
                // Die 1: Upper die
                val d1x = w * 0.55f
                val d1y = h * 0.36f
                drawRoundRect(
                    color = Color(0xFFDC2626),
                    topLeft = Offset(d1x, d1y),
                    size = Size(w * 0.24f, h * 0.20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                drawRoundRect(
                    color = Color(0xFFFFFBEB),
                    topLeft = Offset(d1x + 4f, d1y + 4f),
                    size = Size(w * 0.20f, h * 0.16f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                // Fish emblem inside Die 1
                drawCircle(Color(0xFFD97706), radius = 5f, center = Offset(d1x + w * 0.10f, d1y + h * 0.08f))

                // Die 2: Front main die (Tiger 5)
                val d2x = w * 0.60f
                val d2y = h * 0.48f
                drawRoundRect(
                    color = Color(0xFFB91C1C),
                    topLeft = Offset(d2x, d2y),
                    size = Size(w * 0.28f, h * 0.24f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )
                drawRoundRect(
                    color = Color(0xFFFFFBEB),
                    topLeft = Offset(d2x + 5f, d2y + 5f),
                    size = Size(w * 0.23f, h * 0.19f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                // Tiger inside Die 2
                drawOval(
                    color = Color(0xFFEA580C),
                    topLeft = Offset(d2x + 12f, d2y + 10f),
                    size = Size(w * 0.14f, h * 0.10f)
                )
                drawCircle(Color(0xFF1C1917), radius = 4f, center = Offset(d2x + w * 0.18f, d2y + 12f))

                // Die 3: Left lower die (Crab)
                val d3x = w * 0.46f
                val d3y = h * 0.54f
                drawRoundRect(
                    color = Color(0xFF991B1B),
                    topLeft = Offset(d3x, d3y),
                    size = Size(w * 0.20f, h * 0.18f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                drawRoundRect(
                    color = Color(0xFFFFFBEB),
                    topLeft = Offset(d3x + 3f, d3y + 3f),
                    size = Size(w * 0.17f, h * 0.15f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }

            // Top Right: Red "2X BONUS" ribbon badge with heart
            BonusBadge2X(
                modifier = Modifier
                    .align(Alignment.TopEnd)
            )

            // Title: Bold Thai "น้ำเต้า" (Gold) + "ปูปลา" (Pink/Green) + Subtitle
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp)
                    .offset(y = (-6).dp)
            ) {
                // Top Thai word: น้ำเต้า (Nam Tao) in Gold
                Text(
                    text = "น้ำเต้า",
                    color = Color(0xFFFEF08A),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFFFFBEB), Color(0xFFFACC15), Color(0xFFB45309))
                        ),
                        shadow = Shadow(Color(0xFF451A03), Offset(1.5f, 1.5f), 3f)
                    )
                )

                // Bottom Thai word: ปูปลา (Poo Pla) in Pink to Turquoise gradient
                Text(
                    text = "ปูปลา",
                    color = Color(0xFFF43F5E),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.offset(y = (-4).dp),
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFF43F5E), Color(0xFFFB7185), Color(0xFF2DD4BF), Color(0xFF14B8A6))
                        ),
                        shadow = Shadow(Color(0xFF881337), Offset(1.5f, 1.5f), 3f)
                    )
                )

                // English Subtitle: "Thai Fish Prawn Crab"
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xCC000000),
                    modifier = Modifier.offset(y = (-2).dp)
                ) {
                    Text(
                        text = "Thai Fish Prawn Crab",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Bottom Dual Brand: KINGMAKER + KINGMIDAS
            KingmakerDualLogo(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            )
        }
    }
}

// ========================================================================
// 4. THAI HI-LO (KINGMAKER / KINGMIDAS) - Exact Match to IMG-20260921-WA0008.jpg
// ========================================================================
@Composable
fun ThaiHiLoCard(
    onPlay: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(154.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = onPlay != null) { onPlay?.invoke() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F262B)),
        border = BorderStroke(1.2.dp, Color(0xFF38BDF8))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Canvas: Thai Hi-Lo dice mat + Chrome Thai flag disk
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // White / light grey felt mat background with grid
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                    )
                )

                // Grid table lines
                drawRect(
                    color = Color(0xFF94A3B8).copy(alpha = 0.7f),
                    topLeft = Offset(w * 0.05f, h * 0.05f),
                    size = Size(w * 0.90f, h * 0.85f),
                    style = Stroke(width = 1.5f)
                )

                // Thai Hi-Lo mat marks: Red target bullseye top-left
                drawCircle(Color(0xFFDC2626), radius = 10f, center = Offset(w * 0.18f, h * 0.28f), style = Stroke(width = 2.5f))
                drawCircle(Color(0xFFDC2626), radius = 3.5f, center = Offset(w * 0.18f, h * 0.28f))

                // Dice pips (5 black dots) on left
                drawCircle(Color(0xFF1E293B), radius = 2.5f, center = Offset(w * 0.08f, h * 0.48f))
                drawCircle(Color(0xFF1E293B), radius = 2.5f, center = Offset(w * 0.14f, h * 0.52f))
                drawCircle(Color(0xFF1E293B), radius = 2.5f, center = Offset(w * 0.08f, h * 0.56f))

                // Dice pips on right (6 black dots)
                drawCircle(Color(0xFF1E293B), radius = 2.5f, center = Offset(w * 0.88f, h * 0.48f))
                drawCircle(Color(0xFF1E293B), radius = 2.5f, center = Offset(w * 0.94f, h * 0.50f))
                drawCircle(Color(0xFF1E293B), radius = 2.5f, center = Offset(w * 0.88f, h * 0.56f))
                drawCircle(Color(0xFF1E293B), radius = 2.5f, center = Offset(w * 0.94f, h * 0.58f))

                // Numbers: 4.2 in red, 5.3 in black
                // Center circular metallic disc
                val cx = w * 0.5f
                val cy = h * 0.44f
                val outerR = w * 0.35f

                // Outer silver metallic chrome bevel ring
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(Color(0xFFF8FAFC), Color(0xFF94A3B8), Color(0xFFFFFFFF), Color(0xFF64748B), Color(0xFFF8FAFC)),
                        center = Offset(cx, cy)
                    ),
                    radius = outerR,
                    center = Offset(cx, cy),
                    style = Stroke(width = 8f)
                )

                // Inner Thai Flag Disk (Red / White / Navy Blue / White / Red)
                val flagR = outerR * 0.82f
                drawCircle(Color(0xFFDC2626), radius = flagR, center = Offset(cx, cy)) // Base red

                // White stripes
                drawRect(
                    color = Color.White,
                    topLeft = Offset(cx - flagR, cy - flagR * 0.60f),
                    size = Size(flagR * 2, flagR * 1.20f)
                )

                // Navy Blue center stripe
                drawRect(
                    color = Color(0xFF1E3A8A),
                    topLeft = Offset(cx - flagR, cy - flagR * 0.30f),
                    size = Size(flagR * 2, flagR * 0.60f)
                )

                // Glossy glass specular reflection across top half
                val gloss = Path().apply {
                    moveTo(cx - flagR * 0.8f, cy)
                    cubicTo(cx - flagR * 0.6f, cy - flagR * 0.8f, cx + flagR * 0.6f, cy - flagR * 0.8f, cx + flagR * 0.8f, cy)
                    close()
                }
                drawPath(gloss, Color.White.copy(alpha = 0.35f))
            }

            // Top Right: Red "2X BONUS" ribbon banner
            BonusBadge2X(
                modifier = Modifier
                    .align(Alignment.TopEnd)
            )

            // Centerpiece: Giant 3D Golden "THAI" + "HI-LO" Pill
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-8).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3D Metallic Golden "THAI" with sharp gothic cutouts
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "THAI",
                        color = Color(0xFF78350F),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.offset(x = 1.5.dp, y = 2.dp)
                    )
                    Text(
                        text = "THAI",
                        color = Color(0xFFFEF08A),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFFFFFBEB), Color(0xFFFDE047), Color(0xFFEAB308), Color(0xFF92400E))
                            ),
                            shadow = Shadow(Color(0xFF451A03), Offset(1f, 1f), 2f)
                        )
                    )
                }

                // "HI-LO" pill plaque: Black HI, blue dash, red LO
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.offset(y = (-4).dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HI",
                            color = Color(0xFF0F172A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "-",
                            color = Color(0xFF2563EB),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                        Text(
                            text = "LO",
                            color = Color(0xFFDC2626),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Bottom Dual Brand: KINGMAKER + KINGMIDAS
            KingmakerDualLogo(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            )
        }
    }
}

// ========================================================================
// 5. 32 CARDS (KINGMAKER / KINGMIDAS) - Exact Match to IMG-20260921-WA0012.jpg
// ========================================================================
@Composable
fun Cards32Card(
    onPlay: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(154.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = onPlay != null) { onPlay?.invoke() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF451A03)),
        border = BorderStroke(1.2.dp, Color(0xFFF59E0B))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Canvas: Warm golden amber haze + Marquee Diamond + Lights
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Radial warm golden amber background
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFD97706), Color(0xFF78350F), Color(0xFF451A03), Color(0xFF1C1917)),
                        center = Offset(w * 0.5f, h * 0.45f),
                        radius = w * 0.9f
                    )
                )

                // Fanned golden cards flying around
                // Left card
                val c1 = Path().apply {
                    moveTo(w * 0.04f, h * 0.38f)
                    lineTo(w * 0.24f, h * 0.28f)
                    lineTo(w * 0.18f, h * 0.42f)
                    lineTo(w * 0.02f, h * 0.48f)
                    close()
                }
                drawPath(c1, Color(0xFFFDE68A).copy(alpha = 0.8f))

                // Right card
                val c2 = Path().apply {
                    moveTo(w * 0.76f, h * 0.35f)
                    lineTo(w * 0.94f, h * 0.40f)
                    lineTo(w * 0.88f, h * 0.52f)
                    lineTo(w * 0.72f, h * 0.46f)
                    close()
                }
                drawPath(c2, Color(0xFFFDE68A).copy(alpha = 0.8f))

                // Central Diamond Marquee Sign
                val cx = w * 0.5f
                val cy = h * 0.45f
                val dw = w * 0.44f
                val dh = h * 0.32f

                // Outer golden marquee diamond
                val diamondOuter = Path().apply {
                    moveTo(cx, cy - dh)
                    lineTo(cx + dw, cy)
                    lineTo(cx, cy + dh)
                    lineTo(cx - dw, cy)
                    close()
                }
                drawPath(diamondOuter, Color(0xFF92400E))
                drawPath(diamondOuter, Color(0xFFFDE047), style = Stroke(width = 6f))

                // Inner emerald green velvet felt
                val diamondInner = Path().apply {
                    moveTo(cx, cy - dh * 0.88f)
                    lineTo(cx + dw * 0.88f, cy)
                    lineTo(cx, cy + dh * 0.88f)
                    lineTo(cx - dw * 0.88f, cy)
                    close()
                }
                drawPath(diamondInner, Color(0xFF065F46))

                // Glowing Marquee Incandescent Lightbulbs along perimeter
                for (i in 0 until 12) {
                    val t = i / 12f
                    val px: Float
                    val py: Float
                    when {
                        t < 0.25f -> {
                            val f = t / 0.25f
                            px = cx + f * dw
                            py = (cy - dh) + f * dh
                        }
                        t < 0.50f -> {
                            val f = (t - 0.25f) / 0.25f
                            px = (cx + dw) - f * dw
                            py = cy + f * dh
                        }
                        t < 0.75f -> {
                            val f = (t - 0.50f) / 0.25f
                            px = cx - f * dw
                            py = (cy + dh) - f * dh
                        }
                        else -> {
                            val f = (t - 0.75f) / 0.25f
                            px = (cx - dw) + f * dw
                            py = cy - f * dh
                        }
                    }
                    // Outer glow
                    drawCircle(Color(0xFFFEF08A).copy(alpha = 0.4f), radius = 5f, center = Offset(px, py))
                    // Bulb center
                    drawCircle(Color.White, radius = 2.5f, center = Offset(px, py))
                }
            }

            // Top Right: Orange "BONUS" with lightning bolt & heart
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = 2.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(bottomStart = 8.dp),
                    color = Color(0xFFEA580C)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ BONUS",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "🤍", fontSize = 8.sp)
                    }
                }
            }

            // Center: Giant Sparkling 3D "32" + Neon Magenta "CARDS" Ribbon
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-8).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Giant 3D Gold "32"
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "32",
                        color = Color(0xFF78350F),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp,
                        modifier = Modifier.offset(x = 2.dp, y = 3.dp)
                    )
                    Text(
                        text = "32",
                        color = Color(0xFFFEF08A),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp,
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFFFFFBEB), Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFB45309))
                            ),
                            shadow = Shadow(Color(0xFF451A03), Offset(1.5f, 1.5f), 3f)
                        )
                    )
                }

                // Neon Magenta Glowing Ribbon "CARDS"
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFBE185D),
                    border = BorderStroke(1.5.dp, Color(0xFFF472B6)),
                    modifier = Modifier.offset(y = (-10).dp)
                ) {
                    Text(
                        text = " CARDS ",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = Shadow(Color(0xFFFF1493), Offset(0f, 0f), 8f)
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Bottom Dual Brand: KINGMAKER + KINGMIDAS
            KingmakerDualLogo(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            )
        }
    }
}

// ========================================================================
// REUSABLE BRANDING HELPERS
// ========================================================================

/**
 * Top-Right "2X BONUS" ribbon badge with heart
 */
@Composable
fun BonusBadge2X(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .offset(x = 2.dp, y = 2.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(bottomStart = 8.dp),
            color = Color(0xFFDC2626)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "2X BONUS",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = "🤍", fontSize = 8.sp)
            }
        }
    }
}

/**
 * KINGMAKER (Gold with crown on M) + KINGMIDAS (White with crown on M)
 */
@Composable
fun KingmakerDualLogo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // KINGMAKER in gold with crown
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "KING",
                color = Color(0xFFEAB308),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "👑",
                fontSize = 7.sp,
                modifier = Modifier.offset(y = (-1).dp)
            )
            Text(
                text = "MAKER",
                color = Color(0xFFEAB308),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }

        // KINGMIDAS in clean white with crown
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "KING",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "👑",
                fontSize = 6.sp,
                modifier = Modifier.offset(y = (-1).dp)
            )
            Text(
                text = "MIDAS",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }
    }
}
