package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.ElectricIndigo

@Composable
fun GlowingOrb(
    isSpeaking: Boolean,
    isListening: Boolean,
    rmsAmplitude: Float = 0f,
    size: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val baseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = if (isListening) 1.2f else if (isSpeaking) 1.12f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 500 else if (isSpeaking) 800 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseScale = if (isListening) baseScale + (rmsAmplitude * 0.35f) else baseScale

    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 800 else 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_alpha"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val baseRadius = (size.toPx() / 2f) * 0.75f

            // Outer pulsing ring
            drawCircle(
                color = if (isListening) CyanGlow.copy(alpha = waveAlpha) else ElectricIndigo.copy(alpha = waveAlpha * 0.5f),
                radius = baseRadius * pulseScale,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // Inner glowing ring
            drawCircle(
                color = CyanPrimary.copy(alpha = 0.4f),
                radius = baseRadius * 0.95f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Core radial gradient fill
            val orbBrush = Brush.radialGradient(
                colors = listOf(
                    if (isListening) CyanGlow else if (isSpeaking) CyanPrimary else ElectricIndigo,
                    DeepBackground
                ),
                center = center,
                radius = baseRadius
            )
            drawCircle(
                brush = orbBrush,
                radius = baseRadius * 0.85f,
                center = center
            )
        }

        // Center Monogram & Status
        Box(
            modifier = Modifier
                .size(size * 0.55f)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            CyanPrimary.copy(alpha = 0.35f),
                            ElectricIndigo.copy(alpha = 0.45f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isListening) "•••" else "TG",
                color = CyanLight,
                fontSize = if (size > 80.dp) 18.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
