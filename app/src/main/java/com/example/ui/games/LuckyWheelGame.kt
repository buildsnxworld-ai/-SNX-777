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
import kotlin.random.Random

data class WheelSliceItem(
    val amount: Double,
    val labelBn: String,
    val labelEn: String,
    val color: Color,
    val isTeaser: Boolean = false
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

    val cooldownMillis = 24 * 60 * 60 * 1000L // Strict 24-Hour Limit
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

    // Wheel slices: lowest ৳1 up to ৳200 (চরকি সিস্টেম)
    val slices = remember {
        listOf(
            WheelSliceItem(1.0, "৳১", "৳1", Color(0xFFE53935)),       // Index 0: ৳1
            WheelSliceItem(2.0, "৳২", "৳2", Color(0xFF1E88E5)),       // Index 1: ৳2
            WheelSliceItem(3.0, "৳৩", "৳3", Color(0xFF8E24AA)),       // Index 2: ৳3
            WheelSliceItem(5.0, "৳৫", "৳5", Color(0xFF43A047)),       // Index 3: ৳5
            WheelSliceItem(7.0, "৳৭", "৳7", Color(0xFFFB8C00)),       // Index 4: ৳7
            WheelSliceItem(9.0, "৳৯", "৳9", Color(0xFF00ACC1)),       // Index 5: ৳9 (99% range ৳1-৳9)
            WheelSliceItem(100.0, "৳১০০", "৳100", Color(0xFFD81B60), isTeaser = true), // Index 6: ৳100
            WheelSliceItem(200.0, "৳২০০", "৳200", Color(0xFFFFB300), isTeaser = true)  // Index 7: ৳200 (1% up to ৳200)
        )
    }

    var isSpinning by remember { mutableStateOf(false) }
    var isTurboWheel by remember { mutableStateOf(false) }
    var wonPrize by remember { mutableStateOf<Double?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val animatedRotation = remember { Animatable(0f) }

    Dialog(onDismissRequest = { if (!isSpinning) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lucky_wheel_dialog"),
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
                        Text(text = "🎡", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = StringRes.t(language, "দৈনিক ফ্রি উপহার চরকি (১৳ - ২০০৳)", "Daily Free Gift Spin (৳1 - ৳200)"),
                            color = GoldLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSpinning,
                        modifier = Modifier.size(32.dp).testTag("close_wheel_game")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = StringRes.t(language, "সর্বনিম্ন ৳১ থেকে সর্বোচ্চ ৳১০০ পর্যন্ত বোনাস!", "Win ৳1 up to ৳100 Free Bonus!"),
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Turbo toggle
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isTurboWheel) AccentCrimson.copy(alpha = 0.2f) else Slate800,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(if (isTurboWheel) AccentCrimson else Slate700, Slate700))
                        ),
                        modifier = Modifier
                            .clickable { if (!isSpinning) isTurboWheel = !isTurboWheel }
                            .testTag("wheel_speed_toggle")
                    ) {
                        Text(
                            text = if (isTurboWheel) "⚡ " + StringRes.t(language, "ফাস্ট", "Fast")
                            else "⏱️ " + StringRes.t(language, "নরমাল", "Normal"),
                            color = if (isTurboWheel) GoldLight else Slate300,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Wheel Container with Needle Pointer
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Rotating Canvas Wheel
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(animatedRotation.value)
                    ) {
                        val sweepAngle = 360f / slices.size
                        val radius = size.minDimension / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)

                        // Draw Slices
                        for (i in slices.indices) {
                            val slice = slices[i]
                            val startAngle = i * sweepAngle
                            drawArc(
                                color = slice.color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                size = size
                            )

                            // Slice border separator
                            val borderAngleRad = Math.toRadians(startAngle.toDouble())
                            val endX = center.x + radius * Math.cos(borderAngleRad).toFloat()
                            val endY = center.y + radius * Math.sin(borderAngleRad).toFloat()
                            drawLine(
                                color = Color(0xFF131926),
                                start = center,
                                end = Offset(endX, endY),
                                strokeWidth = 3f
                            )
                        }

                        // Draw Slice Text Labels
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
                                val tx = center.x + textDist * Math.cos(rad).toFloat()
                                val ty = center.y + textDist * Math.sin(rad).toFloat() + 10f

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

                        // Outer Casino Gold Ring with glowing studs
                        drawCircle(
                            color = Color(0xFF131926),
                            radius = radius,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
                        )
                        drawCircle(
                            brush = Brush.sweepGradient(listOf(GoldPrimary, AccentEmerald, GoldPrimary)),
                            radius = radius - 4.dp.toPx(),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                        )
                    }

                    // Center Gold Crown Hub
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(GoldLight, GoldPrimary, GoldDark))
                            )
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "👑", fontSize = 14.sp)
                            Text(
                                text = "SNX",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Top Needle Pointer Indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-8).dp)
                    ) {
                        Text(text = "🔻", fontSize = 26.sp)
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
                                    "🎉 অভিনন্দন! আপনি ফ্রি স্পিনে জিতেছেন ৳%,.0f ক্যাশ বোনাস!".format(wonPrize ?: 0.0),
                                    "🎉 Congratulations! You won ৳%,.0f Free Cash Bonus!".format(wonPrize ?: 0.0)
                                ),
                                color = GoldLight,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                text = StringRes.t(
                                    language,
                                    "বোনাস সরাসরি আপনার ব্যালেন্সে যোগ হয়েছে",
                                    "Bonus credited directly to your balance"
                                ),
                                color = Slate300,
                                fontSize = 10.sp
                            )
                        }
                    }
                } else if (isCooldownActive) {
                    // Real Casino 24-Hour Cooldown Timer Box
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(AccentCrimson.copy(alpha = 0.8f), GoldPrimary.copy(alpha = 0.5f)))
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("wheel_cooldown_banner")
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
                                        "আজকের ২৪ ঘণ্টার ফ্রি স্পিন সমাপ্ত!",
                                        "Today's 24-Hour Free Spin Completed!"
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
                                    "প্ল্যাটফর্ম নিয়মানুযায়ী প্রতি ২৪ ঘণ্টায় শুধুমাত্র ১ বার ফ্রি স্পিন অনুমোদিত। পরবর্তী স্পিন আনলক হতে বাকি:",
                                    "As per platform rules, 1 free spin is allowed every 24 hours. Next spin unlocks in:"
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
                                        "দৈনিক ফ্রি উপহার চরকি (২৪ ঘণ্টায় ১ বার)",
                                        "Daily Free Gift Spin (Once per 24H)"
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
                                    "সম্পূর্ণ ফ্রিতে স্পিন করে জিতে নিন ১ টাকা থেকে ২০০ টাকা পর্যন্ত সরাসরি ক্যাশ বোনাস!",
                                    "Spin 100% free to win instant cash bonus from ৳1 up to ৳200!"
                                ),
                                color = Slate300,
                                fontSize = 10.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Free Spin Button with Exact Mathematical Distribution & 24-Hour limit
                Button(
                    onClick = {
                        if (isCooldownActive) return@Button
                        isSpinning = true
                        wonPrize = null

                        // Exact Mathematical Distribution:
                        // 99% probability: ৳1 to ৳9; 1% probability: up to ৳200
                        val roll = Random.nextDouble(100.0)
                        val winningIndex = if (roll < 99.0) {
                            listOf(0, 1, 2, 3, 4, 5).random()
                        } else {
                            listOf(6, 7).random()
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
                            val duration = if (isTurboWheel) 1600 else 3400
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
                        .testTag("spin_wheel_button"),
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
                            StringRes.t(language, "হুইল ঘুরছে...", "SPINNING...")
                        else if (isCooldownActive)
                            StringRes.t(
                                language,
                                "২৪ ঘণ্টার লিমিট: পরবর্তী স্পিন $countdownFormatted পরে",
                                "24H LIMIT: NEXT SPIN IN $countdownFormatted"
                            )
                        else
                            StringRes.t(language, "ফ্রি স্পিন করুন! (১৳ - ২০০৳ বোনাস)", "FREE SPIN NOW! (৳1 - ৳200 BONUS)"),
                        fontSize = if (isCooldownActive) 11.sp else 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
