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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// ========================================================================
// 1. FORTUNE GARUDA 500 (JILI) - Exact Match to IMG-20260921-WA0000.jpg
// ========================================================================
@Composable
fun FortuneGarudaCard(
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF78350F)),
        border = BorderStroke(1.2.dp, Color(0xFFD97706))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Canvas: Radiant Golden Divine Wings & Aura
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Warm golden aura background
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFFFEF3C7),
                            Color(0xFFFDE68A),
                            Color(0xFFF59E0B),
                            Color(0xFFB45309),
                            Color(0xFF78350F),
                            Color(0xFF1E2A22)
                        )
                    )
                )

                // Sunburst light beams
                for (i in 0 until 12) {
                    val angle = (i * 30f) * (Math.PI / 180f)
                    val x2 = (w * 0.65f) + (w * 1.2f * cos(angle)).toFloat()
                    val y2 = (h * 0.35f) + (h * 1.2f * sin(angle)).toFloat()
                    drawLine(
                        color = Color(0x33FFFFFF),
                        start = Offset(w * 0.65f, h * 0.35f),
                        end = Offset(x2, y2),
                        strokeWidth = 14f
                    )
                }

                // Golden Garuda Wings (Left and Right feather sweeps)
                val wingPath = Path().apply {
                    moveTo(w * 0.05f, h * 0.45f)
                    quadraticTo(w * 0.0f, h * 0.15f, w * 0.35f, h * 0.05f)
                    quadraticTo(w * 0.25f, h * 0.25f, w * 0.4f, h * 0.35f)
                    close()
                }
                drawPath(wingPath, Brush.verticalGradient(listOf(Color(0xFFFFD54F), Color(0xFFD97706))))

                // Muscular Golden Torso
                val torsoPath = Path().apply {
                    moveTo(w * 0.45f, h * 0.5f)
                    lineTo(w * 0.85f, h * 0.5f)
                    lineTo(w * 0.95f, h * 0.85f)
                    lineTo(w * 0.35f, h * 0.85f)
                    close()
                }
                drawPath(torsoPath, Brush.verticalGradient(listOf(Color(0xFFFBBF24), Color(0xFFB45309))))

                // Golden Royal Crown with jewels
                val crownPath = Path().apply {
                    moveTo(w * 0.58f, h * 0.28f)
                    lineTo(w * 0.72f, h * 0.10f) // tall pointed tip
                    lineTo(w * 0.86f, h * 0.28f)
                    close()
                }
                drawPath(crownPath, Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFF59E0B))))

                // Crown jewels (Ruby center, emerald sides)
                drawCircle(Color(0xFFE11D48), radius = 4f, center = Offset(w * 0.72f, h * 0.22f))
                drawCircle(Color(0xFF10B981), radius = 3f, center = Offset(w * 0.66f, h * 0.24f))
                drawCircle(Color(0xFF2563EB), radius = 3f, center = Offset(w * 0.78f, h * 0.24f))

                // Glowing Fiery Eyes
                drawCircle(Color(0xFFFDE047), radius = 5f, center = Offset(w * 0.67f, h * 0.32f))
                drawCircle(Color(0xFFEF4444), radius = 2.5f, center = Offset(w * 0.67f, h * 0.32f))
                drawCircle(Color(0xFFFDE047), radius = 5f, center = Offset(w * 0.77f, h * 0.32f))
                drawCircle(Color(0xFFEF4444), radius = 2.5f, center = Offset(w * 0.77f, h * 0.32f))

                // Golden Beak
                val beakPath = Path().apply {
                    moveTo(w * 0.69f, h * 0.35f)
                    lineTo(w * 0.72f, h * 0.44f)
                    lineTo(w * 0.75f, h * 0.35f)
                    close()
                }
                drawPath(beakPath, Color(0xFFD97706))

                // Ornate Jeweled Necklace & Ruby Pendant
                drawCircle(Color(0xFF7C3AED), radius = 5f, center = Offset(w * 0.72f, h * 0.52f))
                val rubyPath = Path().apply {
                    moveTo(w * 0.72f, h * 0.56f)
                    lineTo(w * 0.78f, h * 0.62f)
                    lineTo(w * 0.72f, h * 0.68f)
                    lineTo(w * 0.66f, h * 0.62f)
                    close()
                }
                drawPath(rubyPath, Color(0xFFDC2626))
                drawPath(rubyPath, Color(0xFFFDE047), style = Stroke(1.5f))
            }

            // Ex NUDGE 500 Badge on Center-Left
            Box(
                modifier = Modifier
                    .padding(start = 6.dp, top = 28.dp)
                    .width(62.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF581C87), Color(0xFF3B0764))
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(10.dp))
                    .padding(vertical = 3.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // "Ex NUDGE" Banner
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFDC2626), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "Ex NUDGE",
                            color = Color(0xFFFEF08A),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    // Golden Face Emblem
                    Text(text = "👑", fontSize = 11.sp)
                    // "500"
                    Text(
                        text = "500",
                        color = Color(0xFFFEF08A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = Shadow(Color(0xFF78350F), Offset(1f, 1f), 2f)
                        )
                    )
                }
            }

            // Top-Right JL + Heart Badge
            JlBrandHeartBadge(modifier = Modifier.align(Alignment.TopEnd))

            // Bottom Title: "FORTUNE GARUDA 500" + "JILI"
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FORTUNE GARUDA",
                    color = Color(0xFFFEF3C7),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(Color(0xFF451A03), Offset(1f, 2f), 2f)
                    )
                )
                Text(
                    text = "500",
                    color = Color(0xFF86EFAC),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(Color(0xFF064E3B), Offset(1f, 2f), 2f)
                    )
                )
                JiliSilverLogo(offsetY = (-2).dp)
            }
        }
    }
}

// ========================================================================
// 2. BOXING KING (JILI) - Exact Match to IMG-20260921-WA0003.jpg
// ========================================================================
@Composable
fun BoxingKingCard(
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
        border = BorderStroke(1.2.dp, Color(0xFF6B21A8))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Canvas: Arena Lights, Ropes & Muscular Boxer
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Purple stadium haze
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF4A154B),
                            Color(0xFF2E1065),
                            Color(0xFF0F172A),
                            Color(0xFF03191D)
                        )
                    )
                )

                // Arena Spotlight Beams
                drawLine(
                    color = Color(0x33A855F7),
                    start = Offset(w * 0.1f, 0f),
                    end = Offset(w * 0.6f, h),
                    strokeWidth = 22f
                )
                drawLine(
                    color = Color(0x33A855F7),
                    start = Offset(w * 0.9f, 0f),
                    end = Offset(w * 0.4f, h),
                    strokeWidth = 22f
                )

                // Ring Ropes in Background
                for (r in listOf(0.24f, 0.38f, 0.52f)) {
                    drawLine(
                        color = Color(0x5594A3B8),
                        start = Offset(0f, h * r),
                        end = Offset(w, h * r),
                        strokeWidth = 3f
                    )
                }

                // Boxer Head & Face
                drawCircle(Color(0xFFFED7AA), radius = 17f, center = Offset(w * 0.5f, h * 0.30f))

                // Fiery Red Mohawk Hair
                val hairPath = Path().apply {
                    moveTo(w * 0.44f, h * 0.25f)
                    lineTo(w * 0.5f, h * 0.11f) // high spike
                    lineTo(w * 0.56f, h * 0.25f)
                    close()
                }
                drawPath(hairPath, Color(0xFFDC2626))

                // Beard / Goatee
                val beardPath = Path().apply {
                    moveTo(w * 0.46f, h * 0.35f)
                    lineTo(w * 0.5f, h * 0.38f)
                    lineTo(w * 0.54f, h * 0.35f)
                    close()
                }
                drawPath(beardPath, Color(0xFF1E293B))

                // Fierce Eyes & Mouth
                drawLine(Color(0xFF0F172A), start = Offset(w * 0.46f, h * 0.29f), end = Offset(w * 0.48f, h * 0.30f), strokeWidth = 2f)
                drawLine(Color(0xFF0F172A), start = Offset(w * 0.52f, h * 0.30f), end = Offset(w * 0.54f, h * 0.29f), strokeWidth = 2f)
                drawRect(Color(0xFFFFFFFF), topLeft = Offset(w * 0.48f, h * 0.33f), size = Size(w * 0.04f, 3f))

                // Muscular Chest & Shoulders
                val bodyPath = Path().apply {
                    moveTo(w * 0.22f, h * 0.48f)
                    lineTo(w * 0.5f, h * 0.38f)
                    lineTo(w * 0.78f, h * 0.48f)
                    lineTo(w * 0.75f, h * 0.65f)
                    lineTo(w * 0.25f, h * 0.65f)
                    close()
                }
                drawPath(bodyPath, Brush.verticalGradient(listOf(Color(0xFFFED7AA), Color(0xFFFDBA74))))

                // Red Boxing Gloves Clenched Together
                drawCircle(Color(0xFFDC2626), radius = 13f, center = Offset(w * 0.44f, h * 0.58f))
                drawCircle(Color(0xFFDC2626), radius = 13f, center = Offset(w * 0.56f, h * 0.58f))
                // Wrist wraps & rivets
                drawRoundRect(
                    color = Color(0xFFE2E8F0),
                    topLeft = Offset(w * 0.38f, h * 0.57f),
                    size = Size(10f, 16f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
                drawRoundRect(
                    color = Color(0xFFE2E8F0),
                    topLeft = Offset(w * 0.59f, h * 0.57f),
                    size = Size(10f, 16f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )

                // Gold Championship Belt Plate
                val beltPath = Path().apply {
                    moveTo(w * 0.38f, h * 0.68f)
                    lineTo(w * 0.62f, h * 0.68f)
                    lineTo(w * 0.58f, h * 0.75f)
                    lineTo(w * 0.42f, h * 0.75f)
                    close()
                }
                drawPath(beltPath, Color(0xFFF59E0B))
                drawPath(beltPath, Color(0xFFFEF08A), style = Stroke(1.5f))
            }

            // Top-Right JL + Heart Badge
            JlBrandHeartBadge(modifier = Modifier.align(Alignment.TopEnd))

            // Bottom Title: "Boxing King" + "JILI"
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "Boxing King" 3D Gold Text with Red Border
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Boxing King",
                        color = Color(0xFF7F1D1D),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.offset(y = 1.5.dp)
                    )
                    Text(
                        text = "Boxing King",
                        color = Color(0xFFFEF08A),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = Shadow(Color(0xFFB45309), Offset(0f, 1f), 1f)
                        )
                    )
                }

                // JILI Silver 3D Text
                JiliSilverLogo(offsetY = (-1).dp)
            }
        }
    }
}

// ========================================================================
// 3. MIGHTY SEVENS 25000x (FASTSPIN) - Exact Match to IMG-20260921-WA0005.jpg
// ========================================================================
@Composable
fun MightySevensCard(
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
        border = BorderStroke(1.2.dp, Color(0xFFB91C1C))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Canvas: Crimson Aura, Glowing Golden Star & Ruby Crystal
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Crimson red base gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF7F1D1D),
                            Color(0xFF991B1B),
                            Color(0xFF450A0A),
                            Color(0xFF1C0505)
                        )
                    )
                )

                // Floating gold coins
                drawCircle(Color(0xFFF59E0B), radius = 10f, center = Offset(w * 0.75f, h * 0.22f))
                drawCircle(Color(0xFFFDE68A), radius = 8f, center = Offset(w * 0.75f, h * 0.22f))
                drawCircle(Color(0xFFF59E0B), radius = 11f, center = Offset(w * 0.88f, h * 0.58f))
                drawCircle(Color(0xFFFDE68A), radius = 9f, center = Offset(w * 0.88f, h * 0.58f))

                // Central Golden Star Frame
                val cx = w * 0.5f
                val cy = h * 0.44f
                val outerR = w * 0.34f
                val innerR = w * 0.17f

                val starPath = Path().apply {
                    for (i in 0 until 10) {
                        val r = if (i % 2 == 0) outerR else innerR
                        val angle = (i * 36 - 90) * (Math.PI / 180)
                        val x = (cx + r * cos(angle)).toFloat()
                        val y = (cy + r * sin(angle)).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                // Golden Outer Glow & Star
                drawPath(starPath, Brush.radialGradient(listOf(Color(0xFFFFFBEB), Color(0xFFF59E0B), Color(0xFFB45309))))
                drawPath(starPath, Color(0xFFFEF08A), style = Stroke(2.5f))

                // Inner Ruby Red Star
                val rubyStarPath = Path().apply {
                    val rOut = outerR * 0.65f
                    val rIn = innerR * 0.65f
                    for (i in 0 until 10) {
                        val r = if (i % 2 == 0) rOut else rIn
                        val angle = (i * 36 - 90) * (Math.PI / 180)
                        val x = (cx + r * cos(angle)).toFloat()
                        val y = (cy + r * sin(angle)).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(rubyStarPath, Brush.radialGradient(listOf(Color(0xFFF87171), Color(0xFFDC2626), Color(0xFF991B1B))))
            }

            // Top-Left "25000x" Crimson Banner Ribbon
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, start = 6.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFDC2626), Color(0xFFB91C1C))
                        ),
                        shape = RoundedCornerShape(topStart = 4.dp, bottomEnd = 8.dp)
                    )
                    .border(1.dp, Color(0xFFFDE047), RoundedCornerShape(topStart = 4.dp, bottomEnd = 8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "25000x",
                    color = Color(0xFFFEF08A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(Color(0xFF450A0A), Offset(1f, 1f), 2f)
                    )
                )
            }

            // Top-Right Pure White Heart
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .background(Color(0x55000000), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🤍", fontSize = 11.sp)
            }

            // Bottom Title: "MIGHTY SEVENS" + "FASTSPIN"
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MIGHTY",
                    color = Color(0xFFFEF08A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(Color(0xFF450A0A), Offset(1f, 1.5f), 2f)
                    )
                )
                Text(
                    text = "SEVENS",
                    color = Color(0xFFF87171),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.offset(y = (-3).dp),
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(Color(0xFF000000), Offset(1f, 1.5f), 2f)
                    )
                )

                // "FASTSPIN" Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.offset(y = (-2).dp)
                ) {
                    Text(
                        text = "≡ FASTSPIN",
                        color = Color.White,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ========================================================================
// 4. ANUBIS WRATH (PG SOFT) - Exact Match to IMG-20260921-WA0007.jpg
// ========================================================================
@Composable
fun AnubisWrathCard(
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1917)),
        border = BorderStroke(1.2.dp, Color(0xFF78350F))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Canvas: Egyptian Hall & Throned Anubis
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Ancient temple background
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF78350F),
                            Color(0xFF451A03),
                            Color(0xFF1C1917),
                            Color(0xFF0F172A)
                        )
                    )
                )

                // Sandstone Temple Pillars
                drawRect(Color(0xFF451A03), topLeft = Offset(w * 0.08f, 0f), size = Size(w * 0.15f, h * 0.6f))
                drawRect(Color(0xFF451A03), topLeft = Offset(w * 0.77f, 0f), size = Size(w * 0.15f, h * 0.6f))

                // Floating gold coins
                drawCircle(Color(0xFFF59E0B), radius = 8f, center = Offset(w * 0.22f, h * 0.18f))
                drawCircle(Color(0xFFF59E0B), radius = 9f, center = Offset(w * 0.85f, h * 0.25f))

                // Golden Throne Backing
                drawRoundRect(
                    color = Color(0xFFB45309),
                    topLeft = Offset(w * 0.26f, h * 0.15f),
                    size = Size(w * 0.48f, h * 0.5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )

                // Anubis Muscular Slate-Black Body
                val body = Path().apply {
                    moveTo(w * 0.32f, h * 0.45f)
                    lineTo(w * 0.5f, h * 0.38f)
                    lineTo(w * 0.68f, h * 0.45f)
                    lineTo(w * 0.65f, h * 0.68f)
                    lineTo(w * 0.35f, h * 0.68f)
                    close()
                }
                drawPath(body, Color(0xFF334155))

                // Anubis Jackal Head & Long Pointed Ears
                val leftEar = Path().apply {
                    moveTo(w * 0.44f, h * 0.28f)
                    lineTo(w * 0.41f, h * 0.12f)
                    lineTo(w * 0.47f, h * 0.24f)
                    close()
                }
                drawPath(leftEar, Color(0xFF1E293B))
                val rightEar = Path().apply {
                    moveTo(w * 0.56f, h * 0.28f)
                    lineTo(w * 0.59f, h * 0.12f)
                    lineTo(w * 0.53f, h * 0.24f)
                    close()
                }
                drawPath(rightEar, Color(0xFF1E293B))

                // Striped Egyptian Nemes Headdress
                val nemes = Path().apply {
                    moveTo(w * 0.42f, h * 0.22f)
                    lineTo(w * 0.5f, h * 0.18f)
                    lineTo(w * 0.58f, h * 0.22f)
                    lineTo(w * 0.62f, h * 0.38f)
                    lineTo(w * 0.38f, h * 0.38f)
                    close()
                }
                drawPath(nemes, Brush.verticalGradient(listOf(Color(0xFFF59E0B), Color(0xFF1E3A8A))))

                // Jackal Snout & Glowing Amber Eyes
                drawCircle(Color(0xFFFEF08A), radius = 3.5f, center = Offset(w * 0.46f, h * 0.26f))
                drawCircle(Color(0xFFFEF08A), radius = 3.5f, center = Offset(w * 0.54f, h * 0.26f))

                // Broad Royal Egyptian Collar (Gold & Cyan)
                drawArc(
                    color = Color(0xFFF59E0B),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.39f, h * 0.36f),
                    size = Size(w * 0.22f, h * 0.12f)
                )
                drawArc(
                    color = Color(0xFF06B6D4),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.42f, h * 0.38f),
                    size = Size(w * 0.16f, h * 0.08f)
                )
            }

            // Top-Right Pure White Heart
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .background(Color(0x55000000), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🤍", fontSize = 11.sp)
            }

            // Bottom Title: "ANUBIS WRATH" + "PG POCKET GAMES SOFT"
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ANUBIS WRATH",
                    color = Color(0xFFFEF3C7),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(Color(0xFF000000), Offset(1f, 1.5f), 2f)
                    )
                )

                // Winged Underline Bar
                Box(
                    modifier = Modifier
                        .padding(vertical = 1.dp)
                        .width(70.dp)
                        .height(1.5.dp)
                        .background(Color(0xFFD97706))
                )

                // PG Logo + Pocket Games Soft
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "PG",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "POCKET GAMES SOFT",
                        color = Color(0xFFCBD5E1),
                        fontSize = 5.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ========================================================================
// 5. 777 ROCKET (JILI) - Exact Match to IMG-20260921-WA0002.jpg
// ========================================================================
@Composable
fun Rocket777Card(
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF9A3412)),
        border = BorderStroke(1.2.dp, Color(0xFFEA580C))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Canvas: Amber Floor, Giant Red 7 & Blasting Rocket
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Warm Orange Casino Backdrop
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF9A3412),
                            Color(0xFFEA580C),
                            Color(0xFFC2410C),
                            Color(0xFF7C2D12)
                        )
                    )
                )

                // Perspective Checkered Floor at Bottom
                val checkH = h * 0.28f
                val startY = h - checkH
                val cols = 8
                val rows = 4
                for (r in 0 until rows) {
                    val y1 = startY + (r * checkH / rows)
                    val y2 = startY + ((r + 1) * checkH / rows)
                    for (c in 0 until cols) {
                        val isEven = (r + c) % 2 == 0
                        val color = if (isEven) Color(0xFF15803D) else Color(0xFF166534)
                        drawRect(
                            color = color.copy(alpha = 0.85f),
                            topLeft = Offset(c * w / cols, y1),
                            size = Size(w / cols, y2 - y1)
                        )
                    }
                }

                // Giant Red Number "7" with Gold Bevel Edge
                val sevenPath = Path().apply {
                    moveTo(w * 0.25f, h * 0.28f)
                    lineTo(w * 0.78f, h * 0.28f)
                    lineTo(w * 0.78f, h * 0.36f)
                    lineTo(w * 0.48f, h * 0.65f)
                    lineTo(w * 0.35f, h * 0.65f)
                    lineTo(w * 0.62f, h * 0.36f)
                    lineTo(w * 0.25f, h * 0.36f)
                    close()
                }
                drawPath(sevenPath, Color(0xFFDC2626))
                drawPath(sevenPath, Color(0xFFFDE047), style = Stroke(3.5f))

                // Rocket Blasting Diagonally Upwards
                val rx = w * 0.28f
                val ry = h * 0.62f
                val rocket = Path().apply {
                    moveTo(rx, ry - 18f)
                    lineTo(rx + 12f, ry + 10f)
                    lineTo(rx - 12f, ry + 10f)
                    close()
                }
                drawPath(rocket, Color(0xFFEF4444))
                // Rocket Flame Tail
                val flame = Path().apply {
                    moveTo(rx - 8f, ry + 10f)
                    lineTo(rx, ry + 26f)
                    lineTo(rx + 8f, ry + 10f)
                    close()
                }
                drawPath(flame, Color(0xFFFACC15))
            }

            // Top-Right JL + Heart Badge
            JlBrandHeartBadge(modifier = Modifier.align(Alignment.TopEnd))

            // Front Graphic: Metallic Gold "777" + "JILI"
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "777" with 3D shadow
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "777",
                        color = Color(0xFF7F1D1D),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.offset(y = 2.dp)
                    )
                    Text(
                        text = "777",
                        color = Color(0xFFFEF08A),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = Shadow(Color(0xFFB45309), Offset(0f, 1f), 1f)
                        )
                    )
                }

                // JILI Silver 3D Text
                JiliSilverLogo(offsetY = (-2).dp)
            }
        }
    }
}

// ========================================================================
// 6. CRAZY TIME (EVOLUTION) - Exact Match to IMG-20260921-WA0004.jpg
// ========================================================================
@Composable
fun CrazyTimeCard(
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF042F2E)),
        border = BorderStroke(1.2.dp, Color(0xFF0D9488))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Canvas: Game Show Studio & Giant Crazy Time Money Wheel
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Studio dark emerald / purple gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF4C0519),
                            Color(0xFF1E1B4B),
                            Color(0xFF042F2E),
                            Color(0xFF022C22)
                        )
                    )
                )

                val cx = w * 0.5f
                val cy = h * 0.44f
                val radius = w * 0.42f

                // Outer Gold Ring with indicator pegs
                drawCircle(Color(0xFFF59E0B), radius = radius + 3f, center = Offset(cx, cy))
                drawCircle(Color(0xFF1E293B), radius = radius, center = Offset(cx, cy))

                // 24 Multi-Colored Wheel Wedges
                val colors = listOf(
                    Color(0xFF06B6D4), // Cyan
                    Color(0xFFF43F5E), // Pink
                    Color(0xFFFBBF24), // Yellow
                    Color(0xFF10B981), // Emerald
                    Color(0xFF8B5CF6)  // Purple
                )
                for (i in 0 until 24) {
                    val startAngle = i * (360f / 24f)
                    drawArc(
                        color = colors[i % colors.size],
                        startAngle = startAngle,
                        sweepAngle = 360f / 24f,
                        useCenter = true,
                        topLeft = Offset(cx - radius, cy - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                }

                // Inner Wheel Divider
                drawCircle(Color(0xFF0F172A), radius = radius * 0.48f, center = Offset(cx, cy))

                // Center Red Hub: "CRAZY TIME"
                drawCircle(Color(0xFFDC2626), radius = radius * 0.38f, center = Offset(cx, cy))
                drawCircle(Color(0xFFFDE047), radius = radius * 0.38f, center = Offset(cx, cy), style = Stroke(2f))

                // Presenter Hand/Arm on Left
                drawRoundRect(
                    color = Color(0xFFDC2626),
                    topLeft = Offset(w * 0.04f, cy - 8f),
                    size = Size(w * 0.18f, 16f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                drawCircle(Color(0xFFFED7AA), radius = 6f, center = Offset(w * 0.22f, cy))
            }

            // Central "CRAZY TIME" Text on Wheel Hub
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-9).dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CRAZY",
                        color = Color(0xFFFEF08A),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "TIME",
                        color = Color.White,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Top-Right Pure White Heart
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .background(Color(0x55000000), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🤍", fontSize = 11.sp)
            }

            // Bottom Logo: "Evolution" (Signature leaf mark + text)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(text = "🍃", fontSize = 9.sp)
                Text(
                    text = "Evolution",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ========================================================================
// 7. FORTUNE GEMS 3 (JILI) - Exact Match to IMG-20260921-WA0006.jpg
// ========================================================================
@Composable
fun FortuneGems3Card(
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF012E40)),
        border = BorderStroke(1.2.dp, Color(0xFF00E5FF))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Canvas: Mystic Cyan Sunburst, Tiered Altars & Two Aztec Masks
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 1. Radiant deep cyan cosmos backdrop
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF026E8A),
                            Color(0xFF014E66),
                            Color(0xFF012E40),
                            Color(0xFF001724)
                        )
                    )
                )

                // 2. Cyan sunburst light rays emanating from upper center
                for (i in 0 until 18) {
                    val angle = (i * 20f - 10f) * (Math.PI / 180f)
                    val x2 = (w * 0.5f) + (w * 1.3f * cos(angle)).toFloat()
                    val y2 = (h * 0.35f) + (h * 1.3f * sin(angle)).toFloat()
                    drawLine(
                        brush = Brush.radialGradient(
                            listOf(Color(0x6600E5FF), Color(0x2200B4D8), Color(0x00000000)),
                            center = Offset(w * 0.5f, h * 0.35f),
                            radius = w * 0.85f
                        ),
                        start = Offset(w * 0.5f, h * 0.35f),
                        end = Offset(x2, y2),
                        strokeWidth = 10f
                    )
                }

                // 3. Soft twinkling celestial stars
                val stars = listOf(
                    Offset(w * 0.16f, h * 0.08f),
                    Offset(w * 0.30f, h * 0.05f),
                    Offset(w * 0.68f, h * 0.06f),
                    Offset(w * 0.84f, h * 0.12f),
                    Offset(w * 0.10f, h * 0.22f),
                    Offset(w * 0.90f, h * 0.26f)
                )
                for (st in stars) {
                    drawCircle(Color(0xFFFFFFFF), radius = 1.8f, center = st)
                    drawCircle(Color(0x6600E5FF), radius = 4.5f, center = st)
                }

                // 4. Golden volumetric halo behind temple altars
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0x99FBBF24), Color(0x44F59E0B), Color(0x00000000)),
                        center = Offset(w * 0.5f, h * 0.40f),
                        radius = w * 0.52f
                    ),
                    radius = w * 0.52f,
                    center = Offset(w * 0.5f, h * 0.40f)
                )

                // 5. Upper Golden Temple Altar (Tier 1 Slab)
                val upperAltar = Path().apply {
                    moveTo(w * 0.21f, h * 0.18f)
                    lineTo(w * 0.79f, h * 0.18f)
                    quadraticTo(w * 0.83f, h * 0.18f, w * 0.84f, h * 0.21f)
                    lineTo(w * 0.91f, h * 0.51f)
                    quadraticTo(w * 0.92f, h * 0.53f, w * 0.89f, h * 0.53f)
                    lineTo(w * 0.11f, h * 0.53f)
                    quadraticTo(w * 0.08f, h * 0.53f, w * 0.09f, h * 0.51f)
                    lineTo(w * 0.16f, h * 0.21f)
                    quadraticTo(w * 0.17f, h * 0.18f, w * 0.21f, h * 0.18f)
                    close()
                }
                drawPath(
                    upperAltar,
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFFBEB),
                            Color(0xFFFDE68A),
                            Color(0xFFF59E0B),
                            Color(0xFFD97706),
                            Color(0xFF92400E)
                        )
                    )
                )
                drawPath(upperAltar, Color(0xFFFEF08A), style = Stroke(1.8f))

                // Engraved Mayan swirl reliefs on upper altar sides
                val leftSwirl = Path().apply {
                    moveTo(w * 0.18f, h * 0.28f)
                    cubicTo(w * 0.25f, h * 0.25f, w * 0.20f, h * 0.36f, w * 0.27f, h * 0.42f)
                    moveTo(w * 0.22f, h * 0.23f)
                    cubicTo(w * 0.28f, h * 0.27f, w * 0.24f, h * 0.32f, w * 0.28f, h * 0.36f)
                }
                drawPath(leftSwirl, Color(0x6678350F), style = Stroke(2f))
                val rightSwirl = Path().apply {
                    moveTo(w * 0.82f, h * 0.28f)
                    cubicTo(w * 0.75f, h * 0.25f, w * 0.80f, h * 0.36f, w * 0.73f, h * 0.42f)
                    moveTo(w * 0.78f, h * 0.23f)
                    cubicTo(w * 0.72f, h * 0.27f, w * 0.76f, h * 0.32f, w * 0.72f, h * 0.36f)
                }
                drawPath(rightSwirl, Color(0x6678350F), style = Stroke(2f))

                // 6. Lower Golden Temple Altar (Tier 2 Slab)
                val lowerAltar = Path().apply {
                    moveTo(w * 0.08f, h * 0.54f)
                    lineTo(w * 0.92f, h * 0.54f)
                    quadraticTo(w * 0.95f, h * 0.54f, w * 0.95f, h * 0.57f)
                    lineTo(w * 0.94f, h * 0.72f)
                    quadraticTo(w * 0.93f, h * 0.74f, w * 0.90f, h * 0.74f)
                    lineTo(w * 0.10f, h * 0.74f)
                    quadraticTo(w * 0.07f, h * 0.74f, w * 0.06f, h * 0.72f)
                    lineTo(w * 0.05f, h * 0.57f)
                    quadraticTo(w * 0.05f, h * 0.54f, w * 0.08f, h * 0.54f)
                    close()
                }
                drawPath(
                    lowerAltar,
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFEF3C7),
                            Color(0xFFFBBF24),
                            Color(0xFFD97706),
                            Color(0xFF78350F)
                        )
                    )
                )
                drawPath(lowerAltar, Color(0xFFFEF08A), style = Stroke(1.8f))
                // Dark divider line between the two altars
                drawLine(
                    Color(0xAA451A03),
                    start = Offset(w * 0.08f, h * 0.535f),
                    end = Offset(w * 0.92f, h * 0.535f),
                    strokeWidth = 3f
                )

                // 7. UPPER MAIN AZTEC GOLDEN MASK (Centerpiece)
                // Crown arch & center horn
                val crownPath = Path().apply {
                    moveTo(w * 0.33f, h * 0.28f)
                    lineTo(w * 0.44f, h * 0.23f)
                    lineTo(w * 0.50f, h * 0.19f)
                    lineTo(w * 0.56f, h * 0.23f)
                    lineTo(w * 0.67f, h * 0.28f)
                    lineTo(w * 0.64f, h * 0.31f)
                    lineTo(w * 0.36f, h * 0.31f)
                    close()
                }
                drawPath(crownPath, Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFF59E0B), Color(0xFFB45309))))
                drawPath(crownPath, Color(0xFFFEF08A), style = Stroke(1.4f))

                // Forehead Red Diamond Ruby
                val rubyPath = Path().apply {
                    moveTo(w * 0.50f, h * 0.23f)
                    lineTo(w * 0.535f, h * 0.265f)
                    lineTo(w * 0.50f, h * 0.30f)
                    lineTo(w * 0.465f, h * 0.265f)
                    close()
                }
                drawPath(rubyPath, Brush.radialGradient(listOf(Color(0xFFFF5252), Color(0xFFDC2626), Color(0xFF7F1D1D))))
                drawPath(rubyPath, Color(0xFFFFF9C4), style = Stroke(1.2f))
                drawCircle(Color.White, radius = 1.5f, center = Offset(w * 0.495f, h * 0.255f))
                // Side ruby accents on crown
                drawCircle(Color(0xFFDC2626), radius = 2.4f, center = Offset(w * 0.42f, h * 0.275f))
                drawCircle(Color(0xFFDC2626), radius = 2.4f, center = Offset(w * 0.58f, h * 0.275f))

                // Flaming Horned Eyebrows
                val leftBrow = Path().apply {
                    moveTo(w * 0.46f, h * 0.31f)
                    cubicTo(w * 0.42f, h * 0.28f, w * 0.33f, h * 0.26f, w * 0.30f, h * 0.32f)
                    cubicTo(w * 0.34f, h * 0.34f, w * 0.40f, h * 0.33f, w * 0.46f, h * 0.33f)
                    close()
                }
                drawPath(leftBrow, Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFD97706))))
                val rightBrow = Path().apply {
                    moveTo(w * 0.54f, h * 0.31f)
                    cubicTo(w * 0.58f, h * 0.28f, w * 0.67f, h * 0.26f, w * 0.70f, h * 0.32f)
                    cubicTo(w * 0.66f, h * 0.34f, w * 0.60f, h * 0.33f, w * 0.54f, h * 0.33f)
                    close()
                }
                drawPath(rightBrow, Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFD97706))))

                // Bulging Golden Deity Eyes with Pupils
                drawCircle(Color(0xFFFEF08A), radius = 7.5f, center = Offset(w * 0.40f, h * 0.37f))
                drawCircle(Color(0xFF78350F), radius = 6f, center = Offset(w * 0.40f, h * 0.37f))
                drawCircle(Color(0xFFFEF08A), radius = 4f, center = Offset(w * 0.40f, h * 0.37f))
                drawCircle(Color(0xFF0F172A), radius = 2.5f, center = Offset(w * 0.40f, h * 0.37f))
                drawCircle(Color.White, radius = 1.2f, center = Offset(w * 0.39f, h * 0.36f))

                drawCircle(Color(0xFFFEF08A), radius = 7.5f, center = Offset(w * 0.60f, h * 0.37f))
                drawCircle(Color(0xFF78350F), radius = 6f, center = Offset(w * 0.60f, h * 0.37f))
                drawCircle(Color(0xFFFEF08A), radius = 4f, center = Offset(w * 0.60f, h * 0.37f))
                drawCircle(Color(0xFF0F172A), radius = 2.5f, center = Offset(w * 0.60f, h * 0.37f))
                drawCircle(Color.White, radius = 1.2f, center = Offset(w * 0.59f, h * 0.36f))

                // Beaked Nose
                val nosePath = Path().apply {
                    moveTo(w * 0.48f, h * 0.33f)
                    lineTo(w * 0.52f, h * 0.33f)
                    lineTo(w * 0.53f, h * 0.41f)
                    lineTo(w * 0.50f, h * 0.44f)
                    lineTo(w * 0.47f, h * 0.41f)
                    close()
                }
                drawPath(nosePath, Brush.verticalGradient(listOf(Color(0xFFFEF08A), Color(0xFFD97706), Color(0xFF92400E))))

                // Menacing Open Mouth & Fangs
                val mouthPath = Path().apply {
                    moveTo(w * 0.37f, h * 0.44f)
                    cubicTo(w * 0.43f, h * 0.42f, w * 0.57f, h * 0.42f, w * 0.63f, h * 0.44f)
                    cubicTo(w * 0.65f, h * 0.48f, w * 0.60f, h * 0.51f, w * 0.50f, h * 0.51f)
                    cubicTo(w * 0.40f, h * 0.51f, w * 0.35f, h * 0.48f, w * 0.37f, h * 0.44f)
                    close()
                }
                drawPath(mouthPath, Brush.verticalGradient(listOf(Color(0xFF451A03), Color(0xFF1C1917))))
                drawPath(mouthPath, Color(0xFFFEF08A), style = Stroke(1.8f))
                drawCircle(Color(0xFFFFFBEB), radius = 2f, center = Offset(w * 0.41f, h * 0.455f))
                drawCircle(Color(0xFFFFFBEB), radius = 2f, center = Offset(w * 0.59f, h * 0.455f))

                // Winged Golden Ear Shields
                val leftEar = Path().apply {
                    moveTo(w * 0.32f, h * 0.33f)
                    cubicTo(w * 0.21f, h * 0.28f, w * 0.16f, h * 0.35f, w * 0.19f, h * 0.43f)
                    cubicTo(w * 0.21f, h * 0.47f, w * 0.26f, h * 0.47f, w * 0.32f, h * 0.45f)
                    close()
                }
                drawPath(leftEar, Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFFBBF24))))
                drawPath(leftEar, Color(0xFFFEF08A), style = Stroke(1.5f))

                val rightEar = Path().apply {
                    moveTo(w * 0.68f, h * 0.33f)
                    cubicTo(w * 0.79f, h * 0.28f, w * 0.84f, h * 0.35f, w * 0.81f, h * 0.43f)
                    cubicTo(w * 0.79f, h * 0.47f, w * 0.74f, h * 0.47f, w * 0.68f, h * 0.45f)
                    close()
                }
                drawPath(rightEar, Brush.horizontalGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706))))
                drawPath(rightEar, Color(0xFFFEF08A), style = Stroke(1.5f))

                // 8. LOWER MATCHING AZTEC GOLDEN MASK (Mirroring the top idol on the lower altar)
                val botScale = 0.72f
                val botCy = h * 0.64f

                // Crown
                val botCrown = Path().apply {
                    moveTo(w * 0.5f - (w * 0.13f * botScale), botCy - 15f)
                    lineTo(w * 0.5f, botCy - 24f)
                    lineTo(w * 0.5f + (w * 0.13f * botScale), botCy - 15f)
                    close()
                }
                drawPath(botCrown, Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFF59E0B))))
                drawCircle(Color(0xFFDC2626), radius = 2.5f, center = Offset(w * 0.5f, botCy - 16f))

                // Brow & Face
                val botFace = Path().apply {
                    moveTo(w * 0.36f, botCy - 10f)
                    lineTo(w * 0.64f, botCy - 10f)
                    lineTo(w * 0.61f, botCy + 10f)
                    lineTo(w * 0.39f, botCy + 10f)
                    close()
                }
                drawPath(botFace, Brush.verticalGradient(listOf(Color(0xFFFEF3C7), Color(0xFFD97706))))
                drawPath(botFace, Color(0xFFFEF08A), style = Stroke(1.2f))

                // Eyes
                drawCircle(Color(0xFFFEF08A), radius = 4.5f, center = Offset(w * 0.43f, botCy - 2f))
                drawCircle(Color(0xFF78350F), radius = 2.5f, center = Offset(w * 0.43f, botCy - 2f))
                drawCircle(Color(0xFFFEF08A), radius = 4.5f, center = Offset(w * 0.57f, botCy - 2f))
                drawCircle(Color(0xFF78350F), radius = 2.5f, center = Offset(w * 0.57f, botCy - 2f))

                // Mouth
                val botMouth = Path().apply {
                    moveTo(w * 0.42f, botCy + 3f)
                    lineTo(w * 0.58f, botCy + 3f)
                    lineTo(w * 0.55f, botCy + 7f)
                    lineTo(w * 0.45f, botCy + 7f)
                    close()
                }
                drawPath(botMouth, Color(0xFF451A03))
                drawPath(botMouth, Color(0xFFFEF08A), style = Stroke(1f))

                // Lower ear wings
                drawCircle(Color(0xFFF59E0B), radius = 5f, center = Offset(w * 0.34f, botCy))
                drawCircle(Color(0xFFF59E0B), radius = 5f, center = Offset(w * 0.66f, botCy))
            }

            // Top-Right JL + Heart Badge
            JlBrandHeartBadge(modifier = Modifier.align(Alignment.TopEnd))

            // Bottom Title: "FORTUNE GEMS 3" + "JILI"
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "FORTUNE GEMS 3" with embedded Emerald, Sapphire, and Giant Red 3D "3"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // "FORTUNE" with embedded Emerald gem
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "FORTUNE",
                            color = Color(0xFF1E3A1A),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.offset(y = 1.5.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "F",
                                color = Color(0xFFE2D5A8),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black
                            )
                            // Emerald Cut Gemstone in "O"
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 0.5.dp)
                                    .size(7.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            listOf(Color(0xFF6EE7B7), Color(0xFF10B981), Color(0xFF047857))
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(0.8.dp, Color(0xFFA7F3D0), CircleShape)
                            )
                            Text(
                                text = "RTUNE",
                                color = Color(0xFFE2D5A8),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    // "GEMS" with embedded Sapphire gem
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "GEMS",
                            color = Color(0xFF1E3A1A),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.offset(y = 1.5.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "GE",
                                color = Color(0xFFE2D5A8),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black
                            )
                            // Sapphire Cut Gemstone in "M"
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 0.5.dp)
                                    .size(7.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            listOf(Color(0xFF93C5FD), Color(0xFF3B82F6), Color(0xFF1D4ED8))
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(0.8.dp, Color(0xFFBFDBFE), CircleShape)
                            )
                            Text(
                                text = "S",
                                color = Color(0xFFE2D5A8),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Giant 3D Red Number "3"
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "3",
                            color = Color(0xFF450A0A),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.offset(y = 2.dp)
                        )
                        Text(
                            text = "3",
                            color = Color(0xFFEF4444),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = Shadow(Color(0xFFFDE047), Offset(0.5f, 0.5f), 1f)
                            )
                        )
                    }
                }

                // JILI Silver 3D Text
                JiliSilverLogo(offsetY = (-2).dp)
            }
        }
    }
}

// ========================================================================
// SHARED REUSABLE COMPONENTS
// ========================================================================

/**
 * The iconic golden "JL" top-right brand badge with superimposed white heart
 */
@Composable
fun JlBrandHeartBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .size(width = 28.dp, height = 24.dp)
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFBBF24), Color(0xFFD97706), Color(0xFF92400E))
                ),
                shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 8.dp)
            )
            .border(1.dp, Color(0xFFFEF08A), RoundedCornerShape(bottomStart = 8.dp, topEnd = 8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "JL",
            color = Color(0xFFFEF08A),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            style = androidx.compose.ui.text.TextStyle(
                shadow = Shadow(Color(0xFF78350F), Offset(1f, 1f), 1f)
            )
        )
        Text(
            text = "🤍",
            fontSize = 9.sp,
            modifier = Modifier.offset(y = 1.dp)
        )
    }
}

/**
 * The signature embossed chrome-white 3D "JILI" brand mark
 */
@Composable
fun JiliSilverLogo(offsetY: androidx.compose.ui.unit.Dp = 0.dp) {
    Box(
        modifier = Modifier.offset(y = offsetY),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "JILI",
            color = Color(0xAA000000),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.offset(y = 1.dp)
        )
        Text(
            text = "JILI",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            style = androidx.compose.ui.text.TextStyle(
                shadow = Shadow(Color(0xFF64748B), Offset(0.5f, 0.5f), 1f)
            )
        )
    }
}
