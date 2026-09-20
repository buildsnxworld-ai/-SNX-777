package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * High-fidelity Casino Sections matching the user's uploaded screenshots:
 * 1. LIVE CASINO: Dealer models (Evolution Gaming, Playtech, W Casino)
 * 2. SPORTS: Cricket Batsmen in national/IPL jerseys with cricket bats and helmets (9Wickets, Lucky Sports, Saba)
 * 3. SLOTS: Wild Athena Rising, Fortune Gems 2, Clover Coins 3x3, Wild Bandito, Lucky Neko, Fortuna Do Garuda, FlyX, Lucky Jaguar, Poker Win, Money Coming, etc.
 */
@Composable
fun CasinoSectionsComponent(
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
        // ==========================================
        // 1. LIVE CASINO SECTION (Dealer Models)
        // ==========================================
        SectionHeaderBar(
            title = "LIVE CASINO",
            titleColor = Color(0xFF00F5B8)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                CrazyTimeCard(
                    onPlay = { onOpenGame?.invoke("live_evolution_gaming") }
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onOpenGame != null) { onOpenGame?.invoke("live_playtech") }
            ) {
                LiveDealerCard(
                    brand = "PLAYTECH",
                    sub = "LIVE CASINO",
                    hairColor = Color(0xFF111111),
                    dressColor = Color(0xFF881337), // Crimson red dress with bindi
                    jewelryColor = Color(0xFFFFE082),
                    hasBindi = true,
                    bgGradients = listOf(Color(0xFF581C2E), Color(0xFF240A12), Color(0xFF03191D))
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onOpenGame != null) { onOpenGame?.invoke("live_w_casino") }
            ) {
                LiveDealerCard(
                    brand = "W CASINO",
                    sub = "LIVE CASINO",
                    hairColor = Color(0xFF18100C),
                    dressColor = Color(0xFF0284C7), // Royal blue saree with jewelry
                    jewelryColor = Color(0xFFFFD700),
                    hasMaangTikka = true,
                    bgGradients = listOf(Color(0xFF0C4A6E), Color(0xFF08273B), Color(0xFF03191D))
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ==========================================
        // 2. SPORTS SECTION (Cricket Players with Bat & Helmet)
        // ==========================================
        SectionHeaderBar(
            title = "SPORTS",
            titleColor = Color(0xFF00F5B8)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Player 1: Indian Team Blue & Orange Jersey with Helmet & CEAT bat (Rohit Sharma style)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onOpenGame != null) { onOpenGame?.invoke("sports_9wickets") }
            ) {
                CricketPlayerCard(
                    name = "9WICKETS",
                    sub = "SPORTS",
                    jerseyColor = Color(0xFF0284C7), // Team Blue
                    shoulderColor = Color(0xFFFF6D00), // Orange shoulders
                    helmetColor = Color(0xFF0C4A6E), // Dark Navy Helmet
                    batBrand = "CEAT",
                    poseType = 1,
                    bgGradient = listOf(Color(0xFF004D40), Color(0xFF002720), Color(0xFF021316))
                )
            }

            // Player 2: Blue Jersey Striking Bat upward (Virat / Hardik style)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onOpenGame != null) { onOpenGame?.invoke("sports_lucky") }
            ) {
                CricketPlayerCard(
                    name = "LUCKY SPORTS",
                    sub = "SPORTS",
                    jerseyColor = Color(0xFF1D4ED8), // Royal blue
                    shoulderColor = Color(0xFFFF7043),
                    helmetColor = Color(0xFF1E3A8A),
                    batBrand = "MRF",
                    poseType = 2,
                    bgGradient = listOf(Color(0xFF01579B), Color(0xFF002744), Color(0xFF021316))
                )
            }

            // Player 3: KKR Purple Jersey with Gold & Bat held up (Rinku / Russell style)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onOpenGame != null) { onOpenGame?.invoke("sports_saba") }
            ) {
                CricketPlayerCard(
                    name = "SABA",
                    sub = "SPORTS",
                    jerseyColor = Color(0xFF581C87), // KKR Purple
                    shoulderColor = Color(0xFFFBBF24), // Gold accents
                    helmetColor = Color(0xFF3B0764),
                    batBrand = "TON",
                    poseType = 3,
                    bgGradient = listOf(Color(0xFF4A148C), Color(0xFF1E053A), Color(0xFF021316))
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 3. SLOTS SECTION (From Screenshot 2)
        // ==========================================
        SectionHeaderBar(
            title = "SLOTS",
            titleColor = Color(0xFFFBBF24)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Row 1: Anubis Wrath, Fortune Gems 3, 777 Rocket
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                AnubisWrathCard(
                    onPlay = { onOpenGame?.invoke("slot_anubis_wrath") }
                )
            }
            Box(
                modifier = Modifier.weight(1f)
            ) {
                FortuneGems3Card(
                    onPlay = { onOpenGame?.invoke("slot_fortune_gems_3") }
                )
            }
            Box(
                modifier = Modifier.weight(1f)
            ) {
                Rocket777Card(
                    onPlay = { onOpenGame?.invoke("slot_777_rocket") }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Wild Bandito (25,000x), Lucky Neko, Fortune Garuda 500
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onOpenGame != null) { onOpenGame?.invoke("slot_wild_bandito") }
            ) {
                SlotGameCardArt(
                    title = "WILD",
                    subtitle = "BANDITO",
                    provider = "PG SOFT",
                    multiplierBadge = "25,000x",
                    theme = SlotTheme.BANDITO_SKELETON,
                    gradient = listOf(Color(0xFF9333EA), Color(0xFF581C87), Color(0xFF1E053A))
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onOpenGame != null) { onOpenGame?.invoke("slot_lucky_neko") }
            ) {
                SlotGameCardArt(
                    title = "LUCKY",
                    subtitle = "NEKO",
                    provider = "PG SOFT",
                    theme = SlotTheme.LUCKY_NEKO_CAT,
                    gradient = listOf(Color(0xFFDB2777), Color(0xFF831843), Color(0xFF240713))
                )
            }
            Box(
                modifier = Modifier.weight(1f)
            ) {
                FortuneGarudaCard(
                    onPlay = { onOpenGame?.invoke("fortune_garuda") }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 3: FlyX Cash Turbo, Lucky Jaguar, FC Poker Win! (25000X)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onOpenGame != null) { onOpenGame?.invoke("flyx") }
            ) {
                SlotGameCardArt(
                    title = "FlyX",
                    subtitle = "CASH TURBO",
                    provider = "Microgaming",
                    theme = SlotTheme.FLYX_ROCKET_HERO,
                    gradient = listOf(Color(0xFFC026D3), Color(0xFF701A75), Color(0xFF260527))
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onOpenGame != null) { onOpenGame?.invoke("slot_lucky_jaguar") }
            ) {
                SlotGameCardArt(
                    title = "LUCKY",
                    subtitle = "JAGUAR",
                    provider = "JILI",
                    theme = SlotTheme.LUCKY_JAGUAR_WARRIOR,
                    gradient = listOf(Color(0xFF0D9488), Color(0xFF115E59), Color(0xFF042F2E))
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onOpenGame != null) { onOpenGame?.invoke("slot_poker_win") }
            ) {
                SlotGameCardArt(
                    title = "POKER",
                    subtitle = "WIN!",
                    provider = "FA CHAI",
                    multiplierBadge = "25000X",
                    theme = SlotTheme.JOKER_POKER_WIN,
                    gradient = listOf(Color(0xFFE11D48), Color(0xFF9F1239), Color(0xFF330517))
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 4: Money Coming, 3 Lucky Rainbow, Mighty Sevens (25000X)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onOpenGame != null) { onOpenGame?.invoke("slot_money_coming") }
            ) {
                SlotGameCardArt(
                    title = "MONEY",
                    subtitle = "COMING",
                    provider = "JILI",
                    theme = SlotTheme.MONEY_CASH_NOTES,
                    gradient = listOf(Color(0xFF059669), Color(0xFF064E3B), Color(0xFF022C22))
                )
            }
            Box(
                modifier = Modifier.weight(1f)
            ) {
                BoxingKingCard(
                    onPlay = { onOpenGame?.invoke("boxing_king") }
                )
            }
            Box(
                modifier = Modifier.weight(1f)
            ) {
                MightySevensCard(
                    onPlay = { onOpenGame?.invoke("slot_mighty_sevens") }
                )
            }
        }
    }
}

@Composable
private fun SectionHeaderBar(
    title: String,
    titleColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = titleColor,
            fontSize = 17.sp,
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

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF082E32),
                border = BorderStroke(1.dp, Color(0xFF0E4A51)),
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "‹", color = Color(0xFF9EBFBF), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF082E32),
                border = BorderStroke(1.dp, Color(0xFF0E4A51)),
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "›", color = Color(0xFF9EBFBF), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Custom High-Resolution Graphic: Live Casino Female Dealer Model
 * Matching the real Evolution/Playtech casino dealer portrait from the user's screenshot.
 */
@Composable
private fun LiveDealerCard(
    brand: String,
    sub: String,
    hairColor: Color,
    dressColor: Color,
    jewelryColor: Color,
    hasBindi: Boolean = false,
    hasMaangTikka: Boolean = false,
    bgGradients: List<Color>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(162.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF16474E))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(bgGradients))
        ) {
            // Background studio light bokeh
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    radius = size.width * 0.45f,
                    center = Offset(size.width * 0.5f, size.height * 0.45f)
                )
                // Sparkle dots
                drawCircle(Color.White.copy(alpha = 0.7f), radius = 2.5f, center = Offset(size.width * 0.2f, size.height * 0.25f))
                drawCircle(Color.White.copy(alpha = 0.8f), radius = 3f, center = Offset(size.width * 0.82f, size.height * 0.28f))
                drawCircle(Color.White.copy(alpha = 0.6f), radius = 2f, center = Offset(size.width * 0.15f, size.height * 0.55f))
            }

            // Dealer portrait vector composite
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 26.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(92.dp, 108.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Long Dark Silky Hair Behind
                    drawOval(
                        color = hairColor,
                        topLeft = Offset(w * 0.12f, h * 0.12f),
                        size = androidx.compose.ui.geometry.Size(w * 0.76f, h * 0.72f)
                    )

                    // 2. Neck & Shoulders (Skin Tone)
                    val skinTone = Color(0xFFF6C8A6)
                    val neckPath = Path().apply {
                        moveTo(w * 0.38f, h * 0.42f)
                        lineTo(w * 0.62f, h * 0.42f)
                        lineTo(w * 0.75f, h * 0.75f)
                        lineTo(w * 0.25f, h * 0.75f)
                        close()
                    }
                    drawPath(neckPath, skinTone)

                    // 3. Gorgeous Dress (Deep V-neck / Sleeveless Corset)
                    val dressPath = Path().apply {
                        moveTo(w * 0.15f, h * 0.68f)
                        lineTo(w * 0.35f, h * 0.58f)
                        lineTo(w * 0.5f, h * 0.70f) // cleavage V
                        lineTo(w * 0.65f, h * 0.58f)
                        lineTo(w * 0.85f, h * 0.68f)
                        lineTo(w * 0.90f, h * 1.0f)
                        lineTo(w * 0.10f, h * 1.0f)
                        close()
                    }
                    drawPath(dressPath, dressColor)

                    // Gold embroidery on dress
                    drawLine(
                        color = jewelryColor,
                        start = Offset(w * 0.35f, h * 0.58f),
                        end = Offset(w * 0.5f, h * 0.70f),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = jewelryColor,
                        start = Offset(w * 0.65f, h * 0.58f),
                        end = Offset(w * 0.5f, h * 0.70f),
                        strokeWidth = 3f
                    )

                    // 4. Gold Necklace
                    drawArc(
                        color = jewelryColor,
                        startAngle = 20f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(w * 0.34f, h * 0.44f),
                        size = androidx.compose.ui.geometry.Size(w * 0.32f, h * 0.16f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
                    )
                    // Necklace diamond pendant
                    drawCircle(
                        color = Color.White,
                        radius = 3.5f,
                        center = Offset(w * 0.5f, h * 0.56f)
                    )

                    // 5. Face Oval
                    drawOval(
                        color = skinTone,
                        topLeft = Offset(w * 0.28f, h * 0.14f),
                        size = androidx.compose.ui.geometry.Size(w * 0.44f, h * 0.44f)
                    )

                    // 6. Hair Front styling / Locks
                    val leftLock = Path().apply {
                        moveTo(w * 0.28f, h * 0.16f)
                        cubicTo(w * 0.20f, h * 0.28f, w * 0.18f, h * 0.45f, w * 0.22f, h * 0.62f)
                        lineTo(w * 0.30f, h * 0.58f)
                        cubicTo(w * 0.26f, h * 0.42f, w * 0.28f, h * 0.28f, w * 0.35f, h * 0.20f)
                        close()
                    }
                    drawPath(leftLock, hairColor)

                    val rightLock = Path().apply {
                        moveTo(w * 0.72f, h * 0.16f)
                        cubicTo(w * 0.80f, h * 0.28f, w * 0.82f, h * 0.45f, w * 0.78f, h * 0.62f)
                        lineTo(w * 0.70f, h * 0.58f)
                        cubicTo(w * 0.74f, h * 0.42f, w * 0.72f, h * 0.28f, w * 0.65f, h * 0.20f)
                        close()
                    }
                    drawPath(rightLock, hairColor)

                    // 7. Eyes & Eyebrows
                    drawArc(
                        color = Color(0xFF261811),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.35f, h * 0.28f),
                        size = androidx.compose.ui.geometry.Size(w * 0.10f, h * 0.05f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                    )
                    drawArc(
                        color = Color(0xFF261811),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.55f, h * 0.28f),
                        size = androidx.compose.ui.geometry.Size(w * 0.10f, h * 0.05f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                    )

                    // 8. Glamorous Red Lips
                    drawOval(
                        color = Color(0xFFDC2626),
                        topLeft = Offset(w * 0.44f, h * 0.46f),
                        size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.05f)
                    )

                    // 9. Bindi or Maang Tikka if enabled
                    if (hasBindi) {
                        drawCircle(
                            color = Color(0xFF991B1B),
                            radius = 2.5f,
                            center = Offset(w * 0.5f, h * 0.25f)
                        )
                    }
                    if (hasMaangTikka) {
                        drawLine(
                            color = jewelryColor,
                            start = Offset(w * 0.5f, h * 0.14f),
                            end = Offset(w * 0.5f, h * 0.24f),
                            strokeWidth = 2f
                        )
                        drawCircle(
                            color = jewelryColor,
                            radius = 3.5f,
                            center = Offset(w * 0.5f, h * 0.24f)
                        )
                    }

                    // Gold Earrings
                    drawCircle(color = jewelryColor, radius = 2.5f, center = Offset(w * 0.27f, h * 0.38f))
                    drawCircle(color = jewelryColor, radius = 2.5f, center = Offset(w * 0.73f, h * 0.38f))
                }
            }

            // Bottom Brand Name Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC001217), Color(0xF2000A0D))
                        )
                    )
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = brand,
                    color = Color(0xFFFFB74D),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 10.sp
                )
                Text(
                    text = sub,
                    color = Color(0xFF80CBC4),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Custom High-Resolution Graphic: Real Cricket Batsman with Team Jersey, Bat & Helmet
 * Matching 9Wickets, Lucky Sports and Saba sports cards from user's screenshots.
 */
@Composable
private fun CricketPlayerCard(
    name: String,
    sub: String,
    jerseyColor: Color,
    shoulderColor: Color,
    helmetColor: Color,
    batBrand: String,
    poseType: Int,
    bgGradient: List<Color>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(162.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF16474E))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(bgGradient))
        ) {
            // Stadium Lights in Background
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(Color(0xFF80CBC4).copy(alpha = 0.15f), radius = size.width * 0.5f, center = Offset(size.width * 0.5f, size.height * 0.35f))
                // Sparkles
                drawCircle(Color.White.copy(alpha = 0.9f), radius = 2.5f, center = Offset(size.width * 0.18f, size.height * 0.2f))
                drawCircle(Color.White.copy(alpha = 0.8f), radius = 2f, center = Offset(size.width * 0.82f, size.height * 0.22f))
                drawCircle(Color.White.copy(alpha = 0.7f), radius = 3f, center = Offset(size.width * 0.75f, size.height * 0.45f))
            }

            // Cricket Batsman Vector
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 26.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.size(95.dp, 110.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val skin = Color(0xFFE0A97E)

                    // 1. Cricket Bat (Rendered angled based on pose)
                    if (poseType == 1) {
                        // Horizontal CEAT bat held in front (Rohit Sharma style)
                        val batPath = Path().apply {
                            moveTo(w * 0.15f, h * 0.78f)
                            lineTo(w * 0.85f, h * 0.65f)
                            lineTo(w * 0.88f, h * 0.75f)
                            lineTo(w * 0.18f, h * 0.88f)
                            close()
                        }
                        drawPath(batPath, Color(0xFFE5D5B8)) // English Willow wood
                        // Bat handle
                        drawLine(
                            color = Color(0xFF263238),
                            start = Offset(w * 0.16f, h * 0.82f),
                            end = Offset(w * 0.05f, h * 0.86f),
                            strokeWidth = 6f
                        )
                        // Bat Grip / Label line
                        drawLine(
                            color = Color(0xFF0284C7),
                            start = Offset(w * 0.35f, h * 0.74f),
                            end = Offset(w * 0.65f, h * 0.68f),
                            strokeWidth = 5f
                        )
                    } else if (poseType == 2) {
                        // Bat raised up high backward (Batting stroke)
                        val batPath = Path().apply {
                            moveTo(w * 0.18f, h * 0.12f)
                            lineTo(w * 0.60f, h * 0.30f)
                            lineTo(w * 0.58f, h * 0.38f)
                            lineTo(w * 0.15f, h * 0.20f)
                            close()
                        }
                        drawPath(batPath, Color(0xFFF5E6CA))
                        // Handle held by glove
                        drawLine(
                            color = Color(0xFFE53935),
                            start = Offset(w * 0.58f, h * 0.34f),
                            end = Offset(w * 0.68f, h * 0.38f),
                            strokeWidth = 6f
                        )
                    } else {
                        // Bat raised vertically in celebration / stance (TON / KKR style)
                        val batPath = Path().apply {
                            moveTo(w * 0.22f, h * 0.10f)
                            lineTo(w * 0.35f, h * 0.12f)
                            lineTo(w * 0.40f, h * 0.55f)
                            lineTo(w * 0.27f, h * 0.53f)
                            close()
                        }
                        drawPath(batPath, Color(0xFFF5E6CA))
                        // Purple/Gold sticker
                        drawRect(
                            color = Color(0xFF7E22CE),
                            topLeft = Offset(w * 0.26f, h * 0.25f),
                            size = androidx.compose.ui.geometry.Size(w * 0.11f, h * 0.14f)
                        )
                    }

                    // 2. Jersey Torso (Muscular Athlete Body)
                    val bodyPath = Path().apply {
                        moveTo(w * 0.28f, h * 0.48f)
                        lineTo(w * 0.72f, h * 0.48f)
                        lineTo(w * 0.80f, h * 0.95f)
                        lineTo(w * 0.20f, h * 0.95f)
                        close()
                    }
                    drawPath(bodyPath, jerseyColor)

                    // 3. Orange / Gold Shoulder Straps & Sleeves
                    drawArc(
                        color = shoulderColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.18f, h * 0.46f),
                        size = androidx.compose.ui.geometry.Size(w * 0.22f, h * 0.24f)
                    )
                    drawArc(
                        color = shoulderColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.60f, h * 0.46f),
                        size = androidx.compose.ui.geometry.Size(w * 0.22f, h * 0.24f)
                    )

                    // "INDIA" or Jersey Front Stripe
                    drawLine(
                        color = Color.White.copy(alpha = 0.8f),
                        start = Offset(w * 0.35f, h * 0.65f),
                        end = Offset(w * 0.65f, h * 0.65f),
                        strokeWidth = 3f
                    )

                    // 4. Muscular Bare Arms (Tanned Skin)
                    drawOval(color = skin, topLeft = Offset(w * 0.14f, h * 0.56f), size = androidx.compose.ui.geometry.Size(w * 0.14f, h * 0.28f))
                    drawOval(color = skin, topLeft = Offset(w * 0.72f, h * 0.56f), size = androidx.compose.ui.geometry.Size(w * 0.14f, h * 0.28f))

                    // 5. White Batting Gloves
                    drawCircle(color = Color.White, radius = 6.5f, center = Offset(w * 0.22f, h * 0.78f))
                    drawCircle(color = Color.White, radius = 6.5f, center = Offset(w * 0.72f, h * 0.76f))

                    // 6. Cricket Helmet & Face with Steel Grill
                    // Helmet Dome
                    drawOval(
                        color = helmetColor,
                        topLeft = Offset(w * 0.32f, h * 0.15f),
                        size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.32f)
                    )
                    // Helmet Visor Brim
                    drawArc(
                        color = Color(0xFF0F172A),
                        startAngle = 10f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(w * 0.30f, h * 0.22f),
                        size = androidx.compose.ui.geometry.Size(w * 0.40f, h * 0.16f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                    )
                    // Steel Face Grill (Bars)
                    val grillColor = Color(0xFFCFD8DC)
                    drawLine(grillColor, Offset(w * 0.36f, h * 0.32f), Offset(w * 0.64f, h * 0.32f), strokeWidth = 2.5f)
                    drawLine(grillColor, Offset(w * 0.38f, h * 0.37f), Offset(w * 0.62f, h * 0.37f), strokeWidth = 2.5f)
                    drawLine(grillColor, Offset(w * 0.40f, h * 0.42f), Offset(w * 0.60f, h * 0.42f), strokeWidth = 2.5f)
                    // Vertical Grill bars
                    drawLine(grillColor, Offset(w * 0.45f, h * 0.30f), Offset(w * 0.45f, h * 0.44f), strokeWidth = 2f)
                    drawLine(grillColor, Offset(w * 0.55f, h * 0.30f), Offset(w * 0.55f, h * 0.44f), strokeWidth = 2f)

                    // Ears & Beard visible behind grill
                    drawArc(
                        color = Color(0xFF261811),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.40f, h * 0.38f),
                        size = androidx.compose.ui.geometry.Size(w * 0.20f, h * 0.08f)
                    )
                }
            }

            // Batsman Name & Badge
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC001217), Color(0xF2000A0D))
                        )
                    )
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = name,
                    color = Color(0xFFFFB74D),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = sub,
                    color = Color(0xFF80CBC4),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

enum class SlotTheme {
    ATHENA_GODDESS,
    GOLDEN_GARUDA_MASK,
    POT_OF_GOLD,
    BANDITO_SKELETON,
    LUCKY_NEKO_CAT,
    GARUDA_GOLDEN_WINGS,
    FLYX_ROCKET_HERO,
    LUCKY_JAGUAR_WARRIOR,
    JOKER_POKER_WIN,
    MONEY_CASH_NOTES,
    JOKER_CROWN,
    GOLDEN_STAR_SEVENS
}

/**
 * Custom Slot Game Graphic Card accurately representing the games in user screenshot 2:
 * Wild Athena, Fortune Gems 2, Clover Coins, Wild Bandito, Lucky Neko, Fortuna Do Garuda, etc.
 */
@Composable
private fun SlotGameCardArt(
    title: String,
    subtitle: String,
    provider: String,
    multiplierBadge: String? = null,
    theme: SlotTheme,
    gradient: List<Color>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(162.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF264C35))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.radialGradient(gradient))
        ) {
            // Background rays effect
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width * 0.5f, size.height * 0.46f)
                val rayColor = Color.White.copy(alpha = 0.08f)
                for (i in 0 until 8) {
                    val angle = (i * 45).toDouble() * Math.PI / 180.0
                    val ex = center.x + Math.cos(angle).toFloat() * size.width
                    val ey = center.y + Math.sin(angle).toFloat() * size.height
                    drawLine(rayColor, center, Offset(ex, ey), strokeWidth = 14f)
                }
            }

            // Multiplier badge (e.g. 25,000x or 15X)
            if (multiplierBadge != null) {
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    color = Color(0xFFE53935),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = multiplierBadge,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            // Favorite Heart Icon
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.75f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(13.dp)
            )

            // Dynamic Custom Vector Art Character based on Slot Theme
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                when (theme) {
                    SlotTheme.ATHENA_GODDESS -> AthenaGoddessArt()
                    SlotTheme.GOLDEN_GARUDA_MASK -> GarudaMaskArt()
                    SlotTheme.POT_OF_GOLD -> CloverPotOfGoldArt()
                    SlotTheme.BANDITO_SKELETON -> WildBanditoArt()
                    SlotTheme.LUCKY_NEKO_CAT -> LuckyNekoCatArt()
                    SlotTheme.GARUDA_GOLDEN_WINGS -> FortunaGarudaArt()
                    SlotTheme.FLYX_ROCKET_HERO -> FlyXRocketArt()
                    SlotTheme.LUCKY_JAGUAR_WARRIOR -> LuckyJaguarArt()
                    SlotTheme.JOKER_POKER_WIN -> JokerPokerWinArt()
                    SlotTheme.MONEY_CASH_NOTES -> MoneyComingArt()
                    SlotTheme.JOKER_CROWN -> LuckyCrownAceArt()
                    SlotTheme.GOLDEN_STAR_SEVENS -> MightySevensArt()
                }
            }

            // Bottom Provider & Game Title Box
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xDD000000), Color(0xF8000000))
                        )
                    )
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = Color(0xFFFFD54F),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = provider,
                    color = Color(0xFF81D4FA),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/* =========================================================================
 * HIGH FIDELITY SLOT COMPOSITES MATCHING EXACT SCREENSHOT ARTWORK
 * ========================================================================= */

@Composable
private fun AthenaGoddessArt() {
    Canvas(modifier = Modifier.size(76.dp)) {
        val w = size.width
        val h = size.height

        // Golden Trident Spear in Hand
        drawLine(Color(0xFFFFD700), Offset(w * 0.15f, h * 0.15f), Offset(w * 0.25f, h * 0.90f), strokeWidth = 4f)
        drawCircle(Color(0xFFFFD700), radius = 5f, center = Offset(w * 0.14f, h * 0.14f))

        // Blonde Silky Flowing Hair
        drawOval(Color(0xFFFDE047), topLeft = Offset(w * 0.25f, h * 0.12f), size = androidx.compose.ui.geometry.Size(w * 0.60f, h * 0.70f))

        // Skin & Face
        drawOval(Color(0xFFFCD34D), topLeft = Offset(w * 0.35f, h * 0.24f), size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.40f))

        // Golden Crown / Tiara
        val crownPath = Path().apply {
            moveTo(w * 0.30f, h * 0.22f)
            lineTo(w * 0.53f, h * 0.10f)
            lineTo(w * 0.76f, h * 0.22f)
            close()
        }
        drawPath(crownPath, Color(0xFFF59E0B))
        drawCircle(Color(0xFF38BDF8), radius = 3.5f, center = Offset(w * 0.53f, h * 0.16f)) // Blue Gem

        // White & Gold Grecian Toga Dress
        val togaPath = Path().apply {
            moveTo(w * 0.30f, h * 0.62f)
            lineTo(w * 0.76f, h * 0.62f)
            lineTo(w * 0.85f, h * 0.98f)
            lineTo(w * 0.20f, h * 0.98f)
            close()
        }
        drawPath(togaPath, Color.White)
        drawArc(Color(0xFFFFD700), startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(w * 0.35f, h * 0.58f), size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.14f), style = androidx.compose.ui.graphics.drawscope.Stroke(3f))
    }
}

@Composable
private fun GarudaMaskArt() {
    Canvas(modifier = Modifier.size(76.dp)) {
        val w = size.width
        val h = size.height

        // Outer Multi-tiered Wheel Ring
        drawCircle(Color(0xFFD97706), radius = w * 0.44f, center = Offset(w * 0.5f, h * 0.5f))
        drawCircle(Color(0xFFF59E0B), radius = w * 0.36f, center = Offset(w * 0.5f, h * 0.5f))

        // Mayan / Golden Temple Mask
        val maskPath = Path().apply {
            moveTo(w * 0.30f, h * 0.28f)
            lineTo(w * 0.70f, h * 0.28f)
            lineTo(w * 0.78f, h * 0.65f)
            lineTo(w * 0.50f, h * 0.82f)
            lineTo(w * 0.22f, h * 0.65f)
            close()
        }
        drawPath(maskPath, Color(0xFFFEF08A))

        // Mask Eyes & Teeth (Ruby Gems)
        drawRect(Color(0xFFEF4444), topLeft = Offset(w * 0.35f, h * 0.42f), size = androidx.compose.ui.geometry.Size(w * 0.10f, h * 0.08f))
        drawRect(Color(0xFFEF4444), topLeft = Offset(w * 0.55f, h * 0.42f), size = androidx.compose.ui.geometry.Size(w * 0.10f, h * 0.08f))

        // Golden Crown Feathers on Top
        drawLine(Color(0xFFFBBF24), Offset(w * 0.5f, h * 0.10f), Offset(w * 0.5f, h * 0.28f), strokeWidth = 5f)
        drawLine(Color(0xFFFBBF24), Offset(w * 0.38f, h * 0.14f), Offset(w * 0.44f, h * 0.28f), strokeWidth = 4f)
        drawLine(Color(0xFFFBBF24), Offset(w * 0.62f, h * 0.14f), Offset(w * 0.56f, h * 0.28f), strokeWidth = 4f)
    }
}

@Composable
private fun CloverPotOfGoldArt() {
    Canvas(modifier = Modifier.size(76.dp)) {
        val w = size.width
        val h = size.height

        // Green Celtic Cauldron / Pot
        drawOval(Color(0xFF166534), topLeft = Offset(w * 0.20f, h * 0.38f), size = androidx.compose.ui.geometry.Size(w * 0.60f, h * 0.52f))
        // Rim
        drawOval(Color(0xFF14532D), topLeft = Offset(w * 0.22f, h * 0.34f), size = androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.14f))

        // Overflowing Golden Coins
        drawCircle(Color(0xFFFACC15), radius = 10f, center = Offset(w * 0.42f, h * 0.30f))
        drawCircle(Color(0xFFFDE047), radius = 12f, center = Offset(w * 0.55f, h * 0.26f))
        drawCircle(Color(0xFFEAB308), radius = 9f, center = Offset(w * 0.68f, h * 0.32f))
        drawCircle(Color(0xFFFEF08A), radius = 8f, center = Offset(w * 0.32f, h * 0.34f))

        // 4-Leaf Clover Emblem on Pot
        drawCircle(Color(0xFF22C55E), radius = 4f, center = Offset(w * 0.46f, h * 0.58f))
        drawCircle(Color(0xFF22C55E), radius = 4f, center = Offset(w * 0.54f, h * 0.58f))
        drawCircle(Color(0xFF22C55E), radius = 4f, center = Offset(w * 0.46f, h * 0.66f))
        drawCircle(Color(0xFF22C55E), radius = 4f, center = Offset(w * 0.54f, h * 0.66f))
    }
}

@Composable
private fun WildBanditoArt() {
    Canvas(modifier = Modifier.size(76.dp)) {
        val w = size.width
        val h = size.height

        // Mexican Sombrero Hat
        drawOval(Color(0xFFFBBF24), topLeft = Offset(w * 0.08f, h * 0.16f), size = androidx.compose.ui.geometry.Size(w * 0.84f, h * 0.22f))
        drawOval(Color(0xFFD97706), topLeft = Offset(w * 0.32f, h * 0.06f), size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.20f))

        // Calavera / Skeleton Face
        drawOval(Color.White, topLeft = Offset(w * 0.32f, h * 0.30f), size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.38f))

        // Black Eye Sockets & Nose
        drawCircle(Color(0xFF1E1B4B), radius = 5f, center = Offset(w * 0.42f, h * 0.44f))
        drawCircle(Color(0xFF1E1B4B), radius = 5f, center = Offset(w * 0.58f, h * 0.44f))
        // Mustache
        drawArc(Color(0xFF18181B), startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(w * 0.40f, h * 0.54f), size = androidx.compose.ui.geometry.Size(w * 0.20f, h * 0.06f), style = androidx.compose.ui.graphics.drawscope.Stroke(3f))

        // Red Mariachi Bowtie / Scarf
        val bowtie = Path().apply {
            moveTo(w * 0.38f, h * 0.68f)
            lineTo(w * 0.62f, h * 0.68f)
            lineTo(w * 0.50f, h * 0.74f)
            close()
        }
        drawPath(bowtie, Color(0xFFDC2626))

        // Guitar / Revolver held
        drawLine(Color(0xFFB45309), Offset(w * 0.20f, h * 0.55f), Offset(w * 0.40f, h * 0.90f), strokeWidth = 6f)
    }
}

@Composable
private fun LuckyNekoCatArt() {
    Canvas(modifier = Modifier.size(76.dp)) {
        val w = size.width
        val h = size.height

        // White Maneki Neko Body & Head
        drawCircle(Color.White, radius = w * 0.32f, center = Offset(w * 0.5f, h * 0.52f))

        // Pointy Pink Ears
        val leftEar = Path().apply {
            moveTo(w * 0.24f, h * 0.32f)
            lineTo(w * 0.34f, h * 0.14f)
            lineTo(w * 0.44f, h * 0.26f)
            close()
        }
        drawPath(leftEar, Color(0xFFF472B6))

        val rightEar = Path().apply {
            moveTo(w * 0.56f, h * 0.26f)
            lineTo(w * 0.66f, h * 0.14f)
            lineTo(w * 0.76f, h * 0.32f)
            close()
        }
        drawPath(rightEar, Color(0xFFF472B6))

        // Waving Paw raised with Gold Coin
        drawOval(Color.White, topLeft = Offset(w * 0.66f, h * 0.28f), size = androidx.compose.ui.geometry.Size(w * 0.18f, h * 0.28f))
        drawCircle(Color(0xFFFBBF24), radius = 8f, center = Offset(w * 0.50f, h * 0.68f)) // Koban coin

        // Cute Cat Eyes & Whiskers
        drawArc(Color(0xFF1E293B), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(w * 0.36f, h * 0.45f), size = androidx.compose.ui.geometry.Size(w * 0.10f, h * 0.05f), style = androidx.compose.ui.graphics.drawscope.Stroke(2.5f))
        drawArc(Color(0xFF1E293B), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(w * 0.54f, h * 0.45f), size = androidx.compose.ui.geometry.Size(w * 0.10f, h * 0.05f), style = androidx.compose.ui.graphics.drawscope.Stroke(2.5f))
        // Red Collar with Bell
        drawLine(Color(0xFFEF4444), Offset(w * 0.34f, h * 0.62f), Offset(w * 0.66f, h * 0.62f), strokeWidth = 5f)
        drawCircle(Color(0xFFFACC15), radius = 4f, center = Offset(w * 0.5f, h * 0.62f))
    }
}

@Composable
private fun FortunaGarudaArt() {
    Canvas(modifier = Modifier.size(76.dp)) {
        val w = size.width
        val h = size.height

        // Golden Wings expanded
        drawArc(Color(0xFFF59E0B), startAngle = 160f, sweepAngle = 100f, useCenter = true, topLeft = Offset(w * 0.05f, h * 0.20f), size = androidx.compose.ui.geometry.Size(w * 0.45f, h * 0.50f))
        drawArc(Color(0xFFF59E0B), startAngle = 280f, sweepAngle = 100f, useCenter = true, topLeft = Offset(w * 0.50f, h * 0.20f), size = androidx.compose.ui.geometry.Size(w * 0.45f, h * 0.50f))

        // Divine Beaked Face
        drawCircle(Color(0xFFFDE047), radius = w * 0.24f, center = Offset(w * 0.5f, h * 0.44f))
        // Beak
        val beak = Path().apply {
            moveTo(w * 0.42f, h * 0.46f)
            lineTo(w * 0.58f, h * 0.46f)
            lineTo(w * 0.50f, h * 0.62f)
            close()
        }
        drawPath(beak, Color(0xFFEA580C))

        // 1000x Sunburst Mandala
        drawCircle(Color(0xFFFFD700).copy(alpha = 0.3f), radius = w * 0.42f, center = Offset(w * 0.5f, h * 0.44f))
    }
}

@Composable
private fun FlyXRocketArt() {
    Canvas(modifier = Modifier.size(76.dp)) {
        val w = size.width
        val h = size.height

        // Orange & White Rocket / Jetpack Hero flying upwards
        val rocketPath = Path().apply {
            moveTo(w * 0.70f, h * 0.20f)
            lineTo(w * 0.82f, h * 0.45f)
            lineTo(w * 0.45f, h * 0.78f)
            lineTo(w * 0.32f, h * 0.52f)
            close()
        }
        drawPath(rocketPath, Color(0xFFF97316))

        // Flame Trail
        val flame = Path().apply {
            moveTo(w * 0.42f, h * 0.72f)
            lineTo(w * 0.15f, h * 0.95f)
            lineTo(w * 0.28f, h * 0.60f)
            close()
        }
        drawPath(flame, Color(0xFFFACC15))

        // Pilot Helmet Visor
        drawCircle(Color(0xFF38BDF8), radius = 8f, center = Offset(w * 0.65f, h * 0.34f))
    }
}

@Composable
private fun LuckyJaguarArt() {
    Canvas(modifier = Modifier.size(76.dp)) {
        val w = size.width
        val h = size.height

        // Aztec / Mayan Jaguar God Face
        drawCircle(Color(0xFFEAB308), radius = w * 0.30f, center = Offset(w * 0.5f, h * 0.50f))

        // Jaguar Spots
        drawCircle(Color(0xFF713F12), radius = 3.5f, center = Offset(w * 0.38f, h * 0.42f))
        drawCircle(Color(0xFF713F12), radius = 3.5f, center = Offset(w * 0.62f, h * 0.42f))
        drawCircle(Color(0xFF713F12), radius = 4f, center = Offset(w * 0.50f, h * 0.36f))

        // Feathered Head-dress (Teal & Gold)
        for (i in -3..3) {
            val angle = 270 + i * 20
            val rad = angle * Math.PI / 180.0
            val x = (w * 0.5f + Math.cos(rad) * w * 0.38f).toFloat()
            val y = (h * 0.45f + Math.sin(rad) * h * 0.38f).toFloat()
            drawLine(if (i % 2 == 0) Color(0xFF14B8A6) else Color(0xFFFBBF24), Offset(w * 0.5f, h * 0.40f), Offset(x, y), strokeWidth = 5f)
        }

        // Orb of Power in Hand
        drawCircle(Color(0xFFC084FC), radius = 10f, center = Offset(w * 0.25f, h * 0.55f))
    }
}

@Composable
private fun JokerPokerWinArt() {
    Canvas(modifier = Modifier.size(76.dp)) {
        val w = size.width
        val h = size.height

        // Harlequin Joker Hat
        val leftHorn = Path().apply {
            moveTo(w * 0.30f, h * 0.35f)
            lineTo(w * 0.15f, h * 0.16f)
            lineTo(w * 0.45f, h * 0.24f)
            close()
        }
        drawPath(leftHorn, Color(0xFFDC2626)) // Red side

        val rightHorn = Path().apply {
            moveTo(w * 0.55f, h * 0.24f)
            lineTo(w * 0.85f, h * 0.16f)
            lineTo(w * 0.70f, h * 0.35f)
            close()
        }
        drawPath(rightHorn, Color(0xFF2563EB)) // Blue side

        // Bells on hat tips
        drawCircle(Color(0xFFFACC15), radius = 4.5f, center = Offset(w * 0.14f, h * 0.15f))
        drawCircle(Color(0xFFFACC15), radius = 4.5f, center = Offset(w * 0.86f, h * 0.15f))

        // Clown / Joker Smiling Face
        drawCircle(Color(0xFFFFFBEB), radius = w * 0.22f, center = Offset(w * 0.5f, h * 0.50f))
        // Red Round Nose
        drawCircle(Color(0xFFEF4444), radius = 5f, center = Offset(w * 0.5f, h * 0.50f))
        // Big Smile
        drawArc(Color(0xFFDC2626), startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(w * 0.40f, h * 0.54f), size = androidx.compose.ui.geometry.Size(w * 0.20f, h * 0.12f), style = androidx.compose.ui.graphics.drawscope.Stroke(3.5f))
    }
}

@Composable
private fun MoneyComingArt() {
    Canvas(modifier = Modifier.size(76.dp)) {
        val w = size.width
        val h = size.height

        // Green Banknotes Stack
        for (i in 0..2) {
            val offset = i * 8f
            drawRoundRect(
                color = if (i == 2) Color(0xFF22C55E) else Color(0xFF15803D),
                topLeft = Offset(w * 0.20f + offset, h * 0.30f - offset),
                size = androidx.compose.ui.geometry.Size(w * 0.55f, h * 0.40f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
        }
        // $ Symbol in Center
        drawCircle(Color(0xFFFEF08A), radius = 10f, center = Offset(w * 0.55f, h * 0.42f))
    }
}

@Composable
private fun LuckyCrownAceArt() {
    Canvas(modifier = Modifier.size(76.dp)) {
        val w = size.width
        val h = size.height

        // 3 Crown Ace Royal Crown
        val crown = Path().apply {
            moveTo(w * 0.22f, h * 0.65f)
            lineTo(w * 0.18f, h * 0.32f) // left peak
            lineTo(w * 0.36f, h * 0.48f)
            lineTo(w * 0.50f, h * 0.24f) // center peak
            lineTo(w * 0.64f, h * 0.48f)
            lineTo(w * 0.82f, h * 0.32f) // right peak
            lineTo(w * 0.78f, h * 0.65f)
            close()
        }
        drawPath(crown, Color(0xFFFBBF24))
        // Jewels on peaks
        drawCircle(Color(0xFFEF4444), radius = 4f, center = Offset(w * 0.18f, h * 0.32f))
        drawCircle(Color(0xFF3B82F6), radius = 5f, center = Offset(w * 0.50f, h * 0.24f))
        drawCircle(Color(0xFFEF4444), radius = 4f, center = Offset(w * 0.82f, h * 0.32f))
    }
}

@Composable
private fun MightySevensArt() {
    Canvas(modifier = Modifier.size(76.dp)) {
        val w = size.width
        val h = size.height

        // Golden Shimmering Star
        val star = Path().apply {
            moveTo(w * 0.50f, h * 0.12f)
            lineTo(w * 0.60f, h * 0.38f)
            lineTo(w * 0.88f, h * 0.38f)
            lineTo(w * 0.66f, h * 0.54f)
            lineTo(w * 0.74f, h * 0.82f)
            lineTo(w * 0.50f, h * 0.66f)
            lineTo(w * 0.26f, h * 0.82f)
            lineTo(w * 0.34f, h * 0.54f)
            lineTo(w * 0.12f, h * 0.38f)
            lineTo(w * 0.40f, h * 0.38f)
            close()
        }
        drawPath(star, Color(0xFFF59E0B))

        // Red Flaming 7 in Front
        val seven = Path().apply {
            moveTo(w * 0.38f, h * 0.32f)
            lineTo(w * 0.64f, h * 0.32f)
            lineTo(w * 0.46f, h * 0.70f)
            lineTo(w * 0.54f, h * 0.70f)
            lineTo(w * 0.36f, h * 0.32f)
            close()
        }
        drawPath(seven, Color(0xFFEF4444))
    }
}
