package com.example.ui.games

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppLanguage
import com.example.ui.theme.*
import com.example.util.StringRes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ChorkiSliceItem(
    val amount: Double,
    val labelBn: String,
    val labelEn: String,
    val sliceColor: Color,
    val textColor: Color = Color.White
)

@Composable
fun LuckyWheelGame(
    isOpen: Boolean,
    currentBalance: Double,
    language: AppLanguage,
    lastDailySpinTimestamp: Long = 0L,
    hasSpunToday: Boolean = false,
    onAwardBonus: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val cooldownMillis = 24 * 60 * 60 * 1000L // 24-Hour Cooldown
    var nowTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(isOpen) {
        while (isOpen) {
            nowTime = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val elapsedSinceLastSpin = if (lastDailySpinTimestamp > 0L) nowTime - lastDailySpinTimestamp else Long.MAX_VALUE
    val isCooldownActive = hasSpunToday || (lastDailySpinTimestamp > 0L && elapsedSinceLastSpin < cooldownMillis)
    val remainingMillis = if (isCooldownActive && lastDailySpinTimestamp > 0L) {
        (cooldownMillis - elapsedSinceLastSpin).coerceAtLeast(0L)
    } else 0L

    val remainingHours = remainingMillis / (1000 * 60 * 60)
    val remainingMinutes = (remainingMillis % (1000 * 60 * 60)) / (1000 * 60)
    val remainingSeconds = (remainingMillis % (1000 * 60)) / 1000
    val countdownFormatted = "%02d:%02d:%02d".format(remainingHours, remainingMinutes, remainingSeconds)

    // চরকি স্লাইস: সর্বনিম্ন ১ টাকা থেকে ১০০ টাকা পর্যন্ত
    val slices = remember {
        listOf(
            ChorkiSliceItem(1.0, "৳ ১", "৳ 1", Color(0xFFD32F2F)),      // Index 0: ৳1
            ChorkiSliceItem(2.0, "৳ ২", "৳ 2", Color(0xFF1976D2)),      // Index 1: ৳2
            ChorkiSliceItem(3.0, "৳ ৩", "৳ 3", Color(0xFF7B1FA2)),      // Index 2: ৳3
            ChorkiSliceItem(5.0, "৳ ৫", "৳ 5", Color(0xFF388E3C)),      // Index 3: ৳5
            ChorkiSliceItem(10.0, "৳ ১০", "৳ 10", Color(0xFFF57C00)),   // Index 4: ৳10
            ChorkiSliceItem(20.0, "৳ ২০", "৳ 20", Color(0xFF0097A7)),   // Index 5: ৳20
            ChorkiSliceItem(50.0, "৳ ৫০", "৳ 50", Color(0xFFC2185B)),   // Index 6: ৳50
            ChorkiSliceItem(100.0, "৳ ১০০", "৳ 100", Color(0xFFFFB300), textColor = Color.Black) // Index 7: ৳100 (জ্যাকপট)
        )
    }

    var isSpinning by remember { mutableStateOf(false) }
    var isTurbo by remember { mutableStateOf(false) }
    var wonPrize by remember { mutableStateOf<Double?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val animatedRotation = remember { Animatable(0f) }

    Dialog(onDismissRequest = { if (!isSpinning) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("chorki_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CasinoSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(GoldPrimary, AccentEmerald, GoldLight))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎁", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = StringRes.t(language, "ফ্রি বোনাস চরকি (১৳ - ১০০৳)", "Free Bonus Chorki (৳1 - ৳100)"),
                            color = GoldLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSpinning,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_chorki_game")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = StringRes.t(language, "চরকি ঘুরিয়ে গুল্লি দিয়ে জিতুন ১৳ - ১০০৳!", "Spin Chorki & Win ৳1 - ৳100!"),
                        color = Slate300,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Turbo toggle
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isTurbo) AccentCrimson.copy(alpha = 0.2f) else Slate800,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(if (isTurbo) AccentCrimson else Slate700, Slate700))
                        ),
                        modifier = Modifier
                            .clickable { if (!isSpinning) isTurbo = !isTurbo }
                            .testTag("chorki_speed_toggle")
                    ) {
                        Text(
                            text = if (isTurbo) "⚡ " + StringRes.t(language, "ফাস্ট", "Fast")
                            else "⏱️ " + StringRes.t(language, "নরমাল", "Normal"),
                            color = if (isTurbo) GoldLight else Slate300,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Chorki Container with Gullee (Golden Pegs) and Pointer
                Box(
                    modifier = Modifier.size(246.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Rotating Canvas Chorki Wheel
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(animatedRotation.value)
                    ) {
                        val sweepAngle = 360f / slices.size
                        val radius = size.minDimension / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)

                        // 1. Draw Slice Arcs
                        for (i in slices.indices) {
                            val slice = slices[i]
                            val startAngle = i * sweepAngle
                            drawArc(
                                color = slice.sliceColor,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                size = size
                            )

                            // Slice border separator
                            val borderAngleRad = Math.toRadians(startAngle.toDouble())
                            val endX = center.x + radius * cos(borderAngleRad).toFloat()
                            val endY = center.y + radius * sin(borderAngleRad).toFloat()
                            drawLine(
                                color = Color(0xFF0F172A),
                                start = center,
                                end = Offset(endX, endY),
                                strokeWidth = 3.5f
                            )
                        }

                        // 2. Draw Slice Text Labels
                        drawIntoCanvas { canvas ->
                            val textPaint = Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 32f
                                typeface = Typeface.DEFAULT_BOLD
                                textAlign = Paint.Align.CENTER
                                isAntiAlias = true
                                setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK)
                            }

                            for (i in slices.indices) {
                                val slice = slices[i]
                                val midAngle = i * sweepAngle + (sweepAngle / 2f)
                                val rad = Math.toRadians(midAngle.toDouble())
                                val textDist = radius * 0.65f
                                val tx = center.x + textDist * cos(rad).toFloat()
                                val ty = center.y + textDist * sin(rad).toFloat() + 10f

                                textPaint.color = if (slice.textColor == Color.Black) android.graphics.Color.BLACK else android.graphics.Color.WHITE

                                canvas.nativeCanvas.save()
                                canvas.nativeCanvas.rotate(midAngle + 90f, tx, ty - 10f)
                                canvas.nativeCanvas.drawText(
                                    if (language == AppLanguage.BN) slice.labelBn else slice.labelEn,
                                    tx,
                                    ty,
                                    textPaint
                                )
                                canvas.nativeCanvas.restore()
                            }
                        }

                        // 3. Draw Outer Golden Ring with Gullee (Golden Metallic Pegs / গুল্লি)
                        drawCircle(
                            color = Color(0xFF1E293B),
                            radius = radius,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 12.dp.toPx())
                        )
                        drawCircle(
                            brush = Brush.sweepGradient(listOf(GoldPrimary, AccentEmerald, GoldLight, GoldPrimary)),
                            radius = radius - 2.dp.toPx(),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5.dp.toPx())
                        )

                        // 4. Draw Gullee (১৬ টি উজ্জ্বল গোল্ডেন গুল্লি পিন)
                        val totalGullee = 16
                        val gulleeAngleStep = 360f / totalGullee
                        val gulleeDist = radius - 6.dp.toPx()
                        for (g in 0 until totalGullee) {
                            val gAngleRad = Math.toRadians((g * gulleeAngleStep).toDouble())
                            val gx = center.x + gulleeDist * cos(gAngleRad).toFloat()
                            val gy = center.y + gulleeDist * sin(gAngleRad).toFloat()

                            // Outer shadow
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.6f),
                                radius = 4.5.dp.toPx(),
                                center = Offset(gx + 1f, gy + 1f)
                            )
                            // Gullee pearl body
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color.White, GoldLight, GoldPrimary, GoldDark),
                                    center = Offset(gx - 1.5f, gy - 1.5f),
                                    radius = 4.dp.toPx()
                                ),
                                radius = 4.dp.toPx(),
                                center = Offset(gx, gy)
                            )
                        }
                    }

                    // Center Chorki Hub
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(GoldLight, GoldPrimary, GoldDark))
                            )
                            .border(2.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🎯", fontSize = 14.sp)
                            Text(
                                text = StringRes.t(language, "চরকি", "SPIN"),
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Top Needle Pointer Indicator with Gullee stopper
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-10).dp)
                    ) {
                        Text(text = "🔻", fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Prize Result Card
                if (wonPrize != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AccentEmerald.copy(alpha = 0.2f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(AccentEmerald, GoldPrimary))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = StringRes.t(
                                    language,
                                    "🎉 অভিনন্দন! চরকি স্পিনে জিতেছেন ৳%,.0f বোনাস!".format(wonPrize ?: 0.0),
                                    "🎉 Congratulations! You won ৳%,.0f Chorki Bonus!".format(wonPrize ?: 0.0)
                                ),
                                color = GoldLight,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                text = StringRes.t(
                                    language,
                                    "টাকাটি সরাসরি আপনার মেইন ব্যালেন্সে যোগ হয়েছে",
                                    "Bonus added directly to your main balance"
                                ),
                                color = Slate300,
                                fontSize = 10.sp
                            )
                        }
                    }
                } else if (isCooldownActive) {
                    // 24-Hour Cooldown Timer Box
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(AccentCrimson.copy(alpha = 0.8f), GoldPrimary.copy(alpha = 0.5f)))
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chorki_cooldown_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "⏳", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = StringRes.t(
                                        language,
                                        "আজকের ফ্রি চরকি স্পিন সমাপ্ত!",
                                        "Today's Free Chorki Spin Completed!"
                                    ),
                                    color = AccentCrimson,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = StringRes.t(
                                    language,
                                    "প্রতি ২৪ ঘণ্টায় ১ বার ফ্রি চরকি স্পিন করতে পারবেন। পরবর্তী স্পিন আনলক হতে বাকি:",
                                    "1 free spin allowed every 24 hours. Next spin unlocks in:"
                                ),
                                color = Slate300,
                                fontSize = 10.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.6f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(listOf(GoldPrimary, AccentEmerald))
                                )
                            ) {
                                Text(
                                    text = "⏱️ $countdownFormatted",
                                    color = GoldLight,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Explanatory note
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate800,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(AccentEmerald.copy(alpha = 0.4f), GoldPrimary.copy(alpha = 0.4f)))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🎁", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = StringRes.t(
                                        language,
                                        "দৈনিক ফ্রি চরকি উপহার (১৳ - ১০০৳)",
                                        "Daily Free Chorki Gift (৳1 - ৳100)"
                                    ),
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = StringRes.t(
                                    language,
                                    "চরকি ঘুরিয়ে গুল্লি দিয়ে জিতে নিন সর্বনিম্ন ১ টাকা থেকে ১০০ টাকা পর্যন্ত নগদ বোনাস!",
                                    "Spin the chorki to win instant cash bonus from ৳1 up to ৳100!"
                                ),
                                color = Slate300,
                                fontSize = 10.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Spin Button with User Requested Probability:
                // Lowest ৳1 up to ৳100, where majority get ৳1 to ৳5 (98% chance)
                Button(
                    onClick = {
                        if (isCooldownActive) return@Button
                        isSpinning = true
                        wonPrize = null

                        // Mathematical Distribution:
                        // ৳1 to ৳5: 98% (Majority)
                        // ৳10 to ৳100: 2%
                        val roll = Random.nextDouble(100.0)
                        val winningIndex = when {
                            roll < 38.0 -> 0 // ৳1 (38%)
                            roll < 70.0 -> 1 // ৳2 (32%)
                            roll < 88.0 -> 2 // ৳3 (18%)
                            roll < 98.0 -> 3 // ৳5 (10%)  -> Total ৳1-৳5 = 98%
                            roll < 99.2 -> 4 // ৳10 (1.2%)
                            roll < 99.7 -> 5 // ৳20 (0.5%)
                            roll < 99.9 -> 6 // ৳50 (0.2%)
                            else -> 7        // ৳100 (0.1%)
                        }

                        val prize = slices[winningIndex].amount
                        val sweepAngle = 360f / slices.size

                        val sliceCenter = winningIndex * sweepAngle + (sweepAngle / 2f)
                        val desiredAngleMod = ((270f - sliceCenter) % 360f + 360f) % 360f
                        val currentAngleMod = (animatedRotation.value % 360f + 360f) % 360f
                        var delta = desiredAngleMod - currentAngleMod
                        if (delta < 0) delta += 360f
                        val totalExtraSpins = 360f * 6 // 6 full rotations
                        val targetAngle = animatedRotation.value + totalExtraSpins + delta

                        coroutineScope.launch {
                            val duration = if (isTurbo) 1600 else 3400
                            animatedRotation.animateTo(
                                targetValue = targetAngle,
                                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
                            )
                            wonPrize = prize
                            onAwardBonus(prize)
                            isSpinning = false
                        }
                    },
                    enabled = !isSpinning && !isCooldownActive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("spin_chorki_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCooldownActive) Slate700 else GoldPrimary,
                        contentColor = if (isCooldownActive) Slate400 else Color.Black,
                        disabledContainerColor = Slate700,
                        disabledContentColor = Slate400
                    )
                ) {
                    Text(
                        text = if (isSpinning)
                            StringRes.t(language, "চরকি ঘুরছে...", "CHORKI SPINNING...")
                        else if (isCooldownActive)
                            StringRes.t(
                                language,
                                "২৪ ঘণ্টার লিমিট: পরবর্তী স্পিন $countdownFormatted পরে",
                                "24H LIMIT: NEXT SPIN IN $countdownFormatted"
                            )
                        else
                            StringRes.t(language, "🎯 চরকি ঘুরান! (১৳ - ১০০৳ বোনাস)", "🎯 SPIN CHORKI! (৳1 - ৳100 BONUS)"),
                        fontSize = if (isCooldownActive) 11.sp else 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
