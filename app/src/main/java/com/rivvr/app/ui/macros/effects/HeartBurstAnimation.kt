package com.rivvr.app.ui.macros.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Heart burst animation for <love> macro
 * Creates floating hearts that radiate outward from center
 */
@Composable
fun HeartBurstAnimation(
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(true) }
    
    // Create multiple heart particles
    val heartCount = 12
    val hearts = remember {
        List(heartCount) { index ->
            HeartParticle(
                emoji = listOf("💖", "💕", "💗", "❤️", "💘").random(),
                angle = (index * 30f) + Random.nextFloat() * 30f, // Spread around circle
                distance = 60f + Random.nextFloat() * 40f,
                animationDelay = Random.nextLong(0, 500)
            )
        }
    }
    
    // Animation duration
    LaunchedEffect(Unit) {
        delay(3000)
        isVisible = false
        onAnimationEnd()
    }
    
    if (isVisible) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            hearts.forEach { heart ->
                FloatingHeart(heart = heart)
            }
        }
    }
}

@Composable
private fun FloatingHeart(
    heart: HeartParticle,
    modifier: Modifier = Modifier
) {
    var targetProgress by remember { mutableStateOf(0f) }
    
    // Start animation immediately when composed
    LaunchedEffect(Unit) {
        targetProgress = 1f
    }
    
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween<Float>(
            durationMillis = 3000,
            easing = LinearEasing
        ),
        label = "heart_animation"
    )
    
    // Simple fade effect
    val alpha = when {
        progress < 0.2f -> progress / 0.2f // Fade in
        progress < 0.8f -> 1f // Stay visible
        else -> 1f - ((progress - 0.8f) / 0.2f) // Fade out
    }
    
    // Calculate position based on angle and distance
    val offsetX = cos(Math.toRadians(heart.angle.toDouble())).toFloat() * heart.distance * progress
    val offsetY = sin(Math.toRadians(heart.angle.toDouble())).toFloat() * heart.distance * progress
    
    Text(
        text = heart.emoji,
        fontSize = 28.sp,
        modifier = modifier
            .offset(x = offsetX.dp, y = (-offsetY).dp)
            .graphicsLayer {
                this.alpha = alpha
            }
    )
}

/**
 * Data class representing a single heart particle
 */
private data class HeartParticle(
    val emoji: String,
    val angle: Float, // Direction in degrees
    val distance: Float, // How far to travel in pixels
    val animationDelay: Long // Delay before starting animation
)