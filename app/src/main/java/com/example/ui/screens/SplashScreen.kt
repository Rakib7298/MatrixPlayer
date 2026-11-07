package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AnimatedWaveformBadge
import com.example.ui.components.GlowingProgressRing
import com.example.ui.components.MatrixBackground
import com.example.ui.theme.LocalMatrixColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val colors = LocalMatrixColors.current
    val scale = remember { Animatable(0.7f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.05f,
            animationSpec = tween(1200)
        )
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(400)
        )
        delay(600)
        onSplashFinished()
    }

    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    MatrixBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("splash_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Futuristic glowing logo badge
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale.value),
                contentAlignment = Alignment.Center
            ) {
                GlowingProgressRing(
                    progress = 0.75f,
                    modifier = Modifier
                        .size(150.dp)
                        .rotate(rotation.value),
                    strokeWidth = 4.dp,
                    color = colors.accentGlow
                )
                GlowingProgressRing(
                    progress = 0.45f,
                    modifier = Modifier
                        .size(120.dp)
                        .rotate(-rotation.value * 1.5f),
                    strokeWidth = 2.dp,
                    color = colors.progressRingGlow
                )

                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(colors.primaryGradient)
                        .border(1.5.dp, colors.glassBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Matrix Logo",
                        tint = colors.backgroundDeep,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "MATRIX PLAYER",
                style = MaterialTheme.typography.displayLarge.copy(
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "SUPER CRYSTAL LIQUID GLASS",
                style = MaterialTheme.typography.bodySmall.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = colors.accentGlow
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedWaveformBadge(
                isPlaying = true,
                modifier = Modifier.size(48.dp, 24.dp),
                barCount = 7,
                activeColor = colors.accentGlow
            )
        }
    }
}
