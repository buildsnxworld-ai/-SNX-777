package com.example.predictor

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.*

@Composable
fun PredictorFlightCanvas(
    state: PredictorState,
    currentMultiplier: Double,
    targetMultiplier: Double,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flight_infinite")
    
    // Engine exhaust oscillation
    val exhaustFlicker by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "exhaust"
    )

    // Radar scanning rotation
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing)
        ),
        label = "radar"
    )

    // Blast shockwave animation when crashed
    var blastProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(state) {
        if (state == PredictorState.CRASHED) {
            val anim = TargetBasedAnimation(
                animationSpec = tween(700, easing = FastOutSlowInEasing),
                typeConverter = Float.VectorConverter,
                initialValue = 0f,
                targetValue = 1f
            )
            val startTime = withFrameNanos { it }
            do {
                val frameTime = withFrameNanos { it }
                val playTime = frameTime - startTime
                blastProgress = anim.getValueFromNanos(playTime)
            } while (!anim.isFinishedFromNanos(playTime))
        } else {
            blastProgress = 0f
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Draw Cyberpunk Grid lines
        val gridLinesCount = 6
        val gridStepX = w / gridLinesCount
        val gridStepY = h / gridLinesCount

        for (i in 1..gridLinesCount) {
            drawLine(
                color = Color(0x1500E5FF),
                start = Offset(gridStepX * i, 0f),
                end = Offset(gridStepX * i, h),
                strokeWidth = 1f
            )
            drawLine(
                color = Color(0x1500E5FF),
                start = Offset(0f, gridStepY * i),
                end = Offset(w, gridStepY * i),
                strokeWidth = 1f
            )
        }

        // 2. Radar sweep effect in background
        drawCircle(
            color = Color(0x0C00E5FF),
            radius = min(w, h) * 0.42f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        )

        drawCircle(
            color = Color(0x0600E5FF),
            radius = min(w, h) * 0.25f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // 3. Flight Animation Calculation
        if (state == PredictorState.SIMULATING_FLIGHT || state == PredictorState.CRASHED) {
            // Normalized flight progress from 1.0x to targetMultiplier
            val progress = if (targetMultiplier > 1.0) {
                ((currentMultiplier - 1.0) / (targetMultiplier - 1.0)).coerceIn(0.0, 1.0).toFloat()
            } else 1f

            val startX = w * 0.10f
            val startY = h * 0.88f
            val endX = startX + (w * 0.76f) * progress
            // Exponential lift curve
            val endY = startY - (h * 0.68f) * progress.pow(1.3f)

            // Trajectory Curve
            val path = Path().apply {
                moveTo(startX, startY)
                cubicTo(
                    startX + (endX - startX) * 0.4f, startY,
                    startX + (endX - startX) * 0.7f, startY - (startY - endY) * 0.4f,
                    endX, endY
                )
            }

            // Glow under the curve
            val fillPath = Path().apply {
                addPath(path)
                lineTo(endX, startY)
                lineTo(startX, startY)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.35f),
                        primaryColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    startY = endY,
                    endY = startY
                )
            )

            // Glowing Trajectory Line
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFF8A80),
                        primaryColor,
                        Color(0xFFFFF176)
                    )
                ),
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )

            // 4. Draw Airplane or Blast
            if (state == PredictorState.SIMULATING_FLIGHT || (state == PredictorState.CRASHED && blastProgress < 0.25f)) {
                // Calculate flight angle
                val angle = (-32f * progress - 12f).coerceIn(-48f, -10f)

                rotate(degrees = angle, pivot = Offset(endX, endY)) {
                    // Engine exhaust plume
                    val flameLength = 34f * exhaustFlicker
                    val flamePath = Path().apply {
                        moveTo(endX - 30f, endY - 4f)
                        lineTo(endX - 30f - flameLength, endY)
                        lineTo(endX - 30f, endY + 4f)
                        close()
                    }
                    drawPath(
                        path = flamePath,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFFFD54F), Color(0xFFFF3D00), Color.Transparent)
                        )
                    )

                    // Aerodynamic Jet Body
                    val planePath = Path().apply {
                        moveTo(endX + 22f, endY) // Nose
                        lineTo(endX - 22f, endY - 14f) // Upper wing tip
                        lineTo(endX - 16f, endY - 4f)
                        lineTo(endX - 28f, endY - 10f) // Tail top
                        lineTo(endX - 26f, endY + 10f) // Tail bottom
                        lineTo(endX - 16f, endY + 4f)
                        lineTo(endX - 22f, endY + 14f) // Lower wing tip
                        close()
                    }

                    // Shadow/Glow
                    drawPath(
                        path = planePath,
                        color = Color(0x88FF1744)
                    )
                    // Solid Jet
                    drawPath(
                        path = planePath,
                        brush = Brush.linearGradient(
                            listOf(Color.White, primaryColor, Color(0xFFB71C1C)),
                            start = Offset(endX + 22f, endY),
                            end = Offset(endX - 28f, endY)
                        )
                    )

                    // Cockpit Glass
                    drawOval(
                        color = Color(0xFF00E5FF),
                        topLeft = Offset(endX + 2f, endY - 3f),
                        size = androidx.compose.ui.geometry.Size(10f, 6f)
                    )
                }
            }

            // 5. CRASH BLAST ANIMATION
            if (state == PredictorState.CRASHED && blastProgress > 0f) {
                // Shockwave expanding circle
                val maxRadius = w * 0.35f
                val currentRadius = maxRadius * blastProgress
                val alpha = (1f - blastProgress).coerceIn(0f, 1f)

                drawCircle(
                    color = Color(0xFFFF1744).copy(alpha = alpha * 0.7f),
                    radius = currentRadius,
                    center = Offset(endX, endY),
                    style = Stroke(width = 6f * (1f - blastProgress))
                )

                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = alpha * 0.5f),
                    radius = currentRadius * 0.6f,
                    center = Offset(endX, endY)
                )

                // Blast sparks
                val sparkCount = 12
                for (s in 0 until sparkCount) {
                    val sAngle = (s * (360f / sparkCount) + blastProgress * 40f) * (PI / 180f)
                    val sDist = currentRadius * 1.15f
                    val spkX = endX + (cos(sAngle) * sDist).toFloat()
                    val spkY = endY + (sin(sAngle) * sDist).toFloat()

                    drawCircle(
                        color = if (s % 2 == 0) Color(0xFFFF5252) else Color(0xFFFFEA00),
                        radius = 4f * (1f - blastProgress),
                        center = Offset(spkX, spkY)
                    )
                }
            }
        }
    }
}
