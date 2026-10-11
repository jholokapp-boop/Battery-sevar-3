package com.example.batterysaver.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.batterysaver.ui.theme.AccentCyan
import com.example.batterysaver.ui.theme.BgDark
import com.example.batterysaver.ui.theme.GoodGreen
import com.example.batterysaver.ui.theme.Text2Dark
import com.example.batterysaver.ui.theme.TextDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onFinish: () -> Unit
) {
    val progressAnim = remember { Animatable(0f) }
    var statusText by remember { mutableStateOf("Initializing") }
    var currentPercent by remember { mutableIntStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        // Status text steps matching the web app
        launch {
            delay(400)
            statusText = "Detecting battery"
            delay(800)
            statusText = "Scanning background apps"
            delay(900)
            statusText = "Analyzing power usage"
            delay(900)
            statusText = "Calibrating Turbo Saver"
            delay(800)
            statusText = "Ready"
            delay(400)
            onFinish()
        }

        // Animate counter from 0 to 100
        progressAnim.animateTo(
            targetValue = 100f,
            animationSpec = tween(durationMillis = 3800, easing = FastOutSlowInEasing)
        ) {
            currentPercent = value.toInt()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Radial ambient glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AccentCyan.copy(alpha = 0.18f),
                        Color(0xFF8B5CF6).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * 0.7f
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Neon battery visual with aura and lightning
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Expanding wave rings
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f
                    drawCircle(
                        color = AccentCyan.copy(alpha = 0.25f),
                        radius = radius * 0.95f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = GoodGreen.copy(alpha = 0.15f),
                        radius = radius * 1.05f,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Battery capsule canvas
                Canvas(
                    modifier = Modifier
                        .size(140.dp, 190.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Terminal cap
                    drawRoundRect(
                        color = AccentCyan.copy(alpha = 0.5f),
                        topLeft = Offset(w * 0.35f, 0f),
                        size = Size(w * 0.3f, h * 0.08f),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )

                    // Outer glass border
                    drawRoundRect(
                        color = AccentCyan.copy(alpha = 0.4f),
                        topLeft = Offset(0f, h * 0.08f),
                        size = Size(w, h * 0.92f),
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        style = Stroke(width = 3.5.dp.toPx())
                    )

                    // Fill level
                    val fillFrac = (currentPercent / 100f).coerceIn(0f, 1f)
                    val fillHeight = (h * 0.84f) * fillFrac
                    val fillTop = (h * 0.08f + 8.dp.toPx()) + ((h * 0.84f) - fillHeight)

                    if (fillHeight > 0f) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(GoodGreen, AccentCyan)
                            ),
                            topLeft = Offset(8.dp.toPx(), fillTop),
                            size = Size(w - 16.dp.toPx(), fillHeight),
                            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                        )
                    }

                    // Lightning bolt path in center
                    val bolt = Path().apply {
                        moveTo(w * 0.54f, h * 0.32f)
                        lineTo(w * 0.38f, h * 0.55f)
                        lineTo(w * 0.50f, h * 0.55f)
                        lineTo(w * 0.42f, h * 0.78f)
                        lineTo(w * 0.64f, h * 0.48f)
                        lineTo(w * 0.52f, h * 0.48f)
                        close()
                    }
                    drawPath(
                        path = bolt,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                // Percentage overlay
                Box(
                    modifier = Modifier
                        .offset(y = 12.dp)
                        .background(BgDark.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$currentPercent%",
                        color = AccentCyan,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App title
            Text(
                text = "Battery Saver",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "POWER · TRACK · SAVE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AccentCyan,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Status text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = statusText.uppercase(),
                    color = Text2Dark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = " ...",
                    color = AccentCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Skip button at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .clickable { onFinish() }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
                    .testTag("splash_skip_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SKIP",
                    color = TextDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}
