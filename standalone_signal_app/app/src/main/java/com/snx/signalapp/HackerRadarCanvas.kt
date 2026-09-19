package com.snx.signalapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HackerRadarDisplay(
    modifier: Modifier = Modifier,
    size: Dp = 280.dp,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTechAnim")

    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    val outerRingAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OuterRing"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = (this.size.minDimension / 2f) - 6.dp.toPx()

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00384D).copy(alpha = 0.65f),
                        Color(0xFF061524).copy(alpha = 0.85f),
                        Color(0xFF040810)
                    ),
                    center = center,
                    radius = maxRadius
                ),
                radius = maxRadius,
                center = center
            )

            val ringCount = 4
            for (i in 1..ringCount) {
                val r = (maxRadius / ringCount) * i
                val isOuter = i == ringCount
                val strokeW = if (isOuter) 2.5.dp.toPx() else 1.2.dp.toPx()
                val color = if (isOuter) Color(0xFF00F0FF).copy(alpha = 0.85f) else Color(0xFF00A3FF).copy(alpha = 0.25f)
                
                val pathEffect = if (i % 2 == 1) {
                    PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                } else {
                    null
                }

                drawCircle(
                    color = color,
                    radius = r,
                    center = center,
                    style = Stroke(width = strokeW, pathEffect = pathEffect)
                )
            }

            drawCircle(
                color = Color(0xFF00F0FF).copy(alpha = (1f - pulseScale) * glowAlpha),
                radius = maxRadius * pulseScale,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            val crosshairLen = maxRadius * 0.95f
            val crosshairColor = Color(0xFF00E5FF).copy(alpha = 0.22f)
            drawLine(
                color = crosshairColor,
                start = Offset(center.x - crosshairLen, center.y),
                end = Offset(center.x + crosshairLen, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = crosshairColor,
                start = Offset(center.x, center.y - crosshairLen),
                end = Offset(center.x, center.y + crosshairLen),
                strokeWidth = 1.dp.toPx()
            )

            rotate(outerRingAngle, pivot = center) {
                val outerSegments = 12
                val segAngle = 360f / outerSegments
                for (s in 0 until outerSegments) {
                    val startA = s * segAngle
                    if (s % 2 == 0) {
                        drawArc(
                            color = Color(0xFF00F0FF).copy(alpha = 0.6f * glowAlpha),
                            startAngle = startA,
                            sweepAngle = segAngle * 0.5f,
                            useCenter = false,
                            topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
                            size = androidx.compose.ui.geometry.Size(maxRadius * 2, maxRadius * 2),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            for (angleDeg in 0 until 360 step 10) {
                val rad = Math.toRadians(angleDeg.toDouble())
                val isMajor = angleDeg % 30 == 0
                val tickLen = if (isMajor) 9.dp.toPx() else 4.dp.toPx()
                val tickAlpha = if (isMajor) 0.8f else 0.35f
                val tickColor = if (isMajor) Color(0xFF00FFCC) else Color(0xFF00A3FF)

                val startX = center.x + (maxRadius - tickLen) * cos(rad).toFloat()
                val startY = center.y + (maxRadius - tickLen) * sin(rad).toFloat()
                val endX = center.x + maxRadius * cos(rad).toFloat()
                val endY = center.y + maxRadius * sin(rad).toFloat()

                drawLine(
                    color = tickColor.copy(alpha = tickAlpha),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            rotate(sweepAngle, pivot = center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to Color(0x00000000),
                        0.85f to Color(0x1000F0FF),
                        1.0f to Color(0x5500F0FF),
                        center = center
                    ),
                    startAngle = 0f,
                    sweepAngle = 75f,
                    useCenter = true,
                    topLeft = Offset(center.x - maxRadius * 0.96f, center.y - maxRadius * 0.96f),
                    size = androidx.compose.ui.geometry.Size(maxRadius * 1.92f, maxRadius * 1.92f)
                )

                val beamRad = Math.toRadians(75.0)
                val beamEndX = center.x + maxRadius * 0.96f * cos(beamRad).toFloat()
                val beamEndY = center.y + maxRadius * 0.96f * sin(beamRad).toFloat()

                drawLine(
                    color = Color(0xFF00FFFF),
                    start = center,
                    end = Offset(beamEndX, beamEndY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        content()
    }
}
