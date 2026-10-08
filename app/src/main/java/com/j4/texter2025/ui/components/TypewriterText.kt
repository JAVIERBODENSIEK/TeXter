package com.j4.texter2025.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

object TypewriterDefaults {
    // Function to calculate dynamic duration based on content length
    fun calculateDuration(length: Int): Long {
        return when {
            length <= 10 -> 200L    // Very short: 200ms
            length <= 30 -> 400L    // Short: 400ms
            length <= 100 -> 600L   // Medium: 600ms
            length <= 300 -> 800L   // Long: 800ms
            else -> 1000L           // Very long: 1s
        }
    }
}

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    fontFamily: FontFamily? = null,
    color: Color = Color.Unspecified,
    autoStart: Boolean = true
) {
    var isAnimating by remember { mutableStateOf(false) }
    var animatedContent by remember { mutableStateOf("") }
    val animationProgress = remember { Animatable(0f) }
    
    val textAlpha by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0.95f,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "textAlpha"
    )

    // Start animation when text changes or when autoStart is true
    LaunchedEffect(text) {
        if (!isAnimating && (autoStart || animatedContent.isNotEmpty())) {
            isAnimating = true
            try {
                animatedContent = ""
                val contentLength = text.length
                if (contentLength == 0) {
                    return@LaunchedEffect
                }

                val totalDuration = TypewriterDefaults.calculateDuration(contentLength)
                animationProgress.snapTo(0f)
                
                launch {
                    // Animate progress for smooth text expansion
                    animationProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = totalDuration.toInt(),
                            easing = FastOutSlowInEasing
                        )
                    )
                }

                val startTime = System.nanoTime()
                val durationNanos = totalDuration * 1_000_000L
                val frameTime = 16_666_667L // 60 FPS in nanoseconds
                var lastUpdateTime = startTime

                while (true) {
                    val currentTime = System.nanoTime()
                    val elapsedNanos = currentTime - startTime
                    val progress = (elapsedNanos.toFloat() / durationNanos).coerceIn(0f, 1f)
                    
                    // Synchronized text update with expansion
                    val easedProgress = FastOutSlowInEasing.transform(progress)
                    val targetLength = (text.length * easedProgress).toInt()
                    
                    if (targetLength >= text.length) {
                        animatedContent = text
                        break
                    }
                    
                    // Update content with proper timing
                    if (currentTime - lastUpdateTime >= frameTime) {
                        animatedContent = text.take(targetLength)
                        lastUpdateTime = currentTime
                        
                        // Ensure smooth frame timing
                        val nextFrameTime = currentTime + frameTime
                        val delayNanos = (nextFrameTime - System.nanoTime()).coerceAtLeast(0)
                        delay(delayNanos / 1_000_000) // Convert to milliseconds
                    } else {
                        yield()
                    }
                }
            } finally {
                animatedContent = text
                animationProgress.snapTo(1f)
                isAnimating = false
            }
        }
    }

    Box(modifier = modifier) {
        Text(
            text = animatedContent,
            style = style,
            fontFamily = fontFamily,
            color = color
        )
    }
}
