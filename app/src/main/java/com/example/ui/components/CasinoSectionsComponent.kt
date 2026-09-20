package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

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
                    imageResId = R.drawable.img_dealer_foreign_1,
                    badgeText = "LIVE VIP"
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
                    imageResId = R.drawable.img_dealer_foreign_2,
                    badgeText = "DEALER HD"
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ==========================================
        // 2. SPORTS SECTION (Realistic Cricket Players)
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
            // Player 1: Shakib Al Hasan (Real photo in Bangladesh National Team jersey)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onOpenGame != null) { onOpenGame?.invoke("sports_9wickets") }
            ) {
                CricketPlayerCard(
                    brand = "9WICKETS",
                    playerName = "SHAKIB AL HASAN",
                    jerseyTag = "BANGLADESH #75",
                    imageResId = R.drawable.img_cricket_shakib,
                    badgeColor = Color(0xFF15803D),
                    accentColor = Color(0xFF22C55E)
                )
            }

            // Player 2: Virat Kohli (Real photo in Team Blue jersey)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onOpenGame != null) { onOpenGame?.invoke("sports_lucky") }
            ) {
                CricketPlayerCard(
                    brand = "LUCKY SPORTS",
                    playerName = "VIRAT KOHLI",
                    jerseyTag = "INDIA #18",
                    imageResId = R.drawable.img_cricket_virat,
                    badgeColor = Color(0xFF1D4ED8),
                    accentColor = Color(0xFF38BDF8)
                )
            }

            // Player 3: Rohit Sharma (Real photo in World Cup champion jersey)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onOpenGame != null) { onOpenGame?.invoke("sports_saba") }
            ) {
                CricketPlayerCard(
                    brand = "SABA SPORTS",
                    playerName = "ROHIT SHARMA",
                    jerseyTag = "CAPTAIN #45",
                    imageResId = R.drawable.img_cricket_rohit,
                    badgeColor = Color(0xFFB45309),
                    accentColor = Color(0xFFFBBF24)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 3. SLOTS & TABLE GAMES SECTION (3 Rows x 3 Games = 9 Games)
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
            Box(modifier = Modifier.weight(1f)) {
                AnubisWrathCard(
                    onPlay = { onOpenGame?.invoke("slot_anubis_wrath") }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                FortuneGems3Card(
                    onPlay = { onOpenGame?.invoke("slot_fortune_gems_3") }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                Rocket777Card(
                    onPlay = { onOpenGame?.invoke("slot_777_rocket") }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Fortune Garuda 500, Ludo Quick (JILI), Andar Bahar (Kingmaker)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                FortuneGarudaCard(
                    onPlay = { onOpenGame?.invoke("fortune_garuda") }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                LudoQuickCard(
                    onPlay = { onOpenGame?.invoke("ludo_quick") }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                AndarBaharCard(
                    onPlay = { onOpenGame?.invoke("andar_bahar") }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 3: 32 Cards (Kingmaker), Thai Hi-Lo (Kingmaker), Thai Fish Prawn Crab (Kingmaker)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Cards32Card(
                    onPlay = { onOpenGame?.invoke("32_cards") }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                ThaiHiLoCard(
                    onPlay = { onOpenGame?.invoke("thai_hi_lo") }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                ThaiFishPrawnCrabCard(
                    onPlay = { onOpenGame?.invoke("thai_fish_prawn_crab") }
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
/**
 * Realistic Live Casino Foreign Dealer Model Card
 * Displays high-definition realistic foreign dealer photo with VIP casino overlays.
 */
@Composable
private fun LiveDealerCard(
    brand: String,
    sub: String,
    imageResId: Int,
    badgeText: String = "LIVE VIP"
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(162.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.2.dp, Color(0xFF00F5B8).copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF021316))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Real Dealer Model Photo
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = brand,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Ambient dark vignette scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x33000000),
                                Color.Transparent,
                                Color(0x66000000),
                                Color(0xF0021316)
                            )
                        )
                    )
            )

            // Top-left "LIVE VIP" badge with red dot
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(5.dp),
                shape = RoundedCornerShape(4.dp),
                color = Color(0xDDDC2626)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Top-right Casino chip / heart icon
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0x88000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🤍", fontSize = 9.sp)
            }

            // Bottom Brand Name Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = brand,
                    color = Color(0xFFFFD54F),
                    fontSize = 9.5.sp,
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

/**
 * Realistic Sports Card: Real Cricket Stars (Shakib Al Hasan, Virat Kohli, Rohit Sharma)
 * Displays realistic cricket photo with high-contrast stadium gradient, team badge & clean name tag.
 */
@Composable
private fun CricketPlayerCard(
    brand: String,
    playerName: String,
    jerseyTag: String,
    imageResId: Int,
    badgeColor: Color,
    accentColor: Color = Color(0xFF00F5B8)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(162.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF021316))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Real Cricket Player Photo
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = playerName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Stadium lighting & bottom vignette overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x33000000),
                                Color.Transparent,
                                Color(0x66000000),
                                Color(0xF0021316)
                            )
                        )
                    )
            )

            // Top Badge (Team / Country / Jersey Number)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(5.dp),
                shape = RoundedCornerShape(4.dp),
                color = badgeColor
            ) {
                Text(
                    text = jerseyTag,
                    color = Color.White,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Top Right: Cricket ball icon
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0x88000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🏏", fontSize = 10.sp)
            }

            // Bottom Player Name & Brand Banner
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = brand,
                    color = Color(0xFFFFD54F),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = playerName,
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
