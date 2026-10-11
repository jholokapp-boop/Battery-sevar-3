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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.batterysaver.model.BatteryInfo
import com.example.batterysaver.ui.theme.AccentCyan
import com.example.batterysaver.ui.theme.BgDark
import com.example.batterysaver.ui.theme.GoodGreen
import com.example.batterysaver.ui.theme.SurfaceDark
import com.example.batterysaver.ui.theme.Text2Dark
import com.example.batterysaver.ui.theme.TextDark
import com.example.batterysaver.ui.theme.WarnAmber
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ChargingScreen(
    batteryInfo: BatteryInfo,
    onClose: () -> Unit
) {
    var decimalOffset by remember { mutableFloatStateOf(0.4f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1800)
            val next = decimalOffset + 0.1f
            decimalOffset = if (next >= 1.0f) 0.1f else next
        }
    }

    val displayPercentage = (batteryInfo.level + decimalOffset).coerceAtMost(100f)

    val infiniteTransition = rememberInfiniteTransition(label = "charging")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .testTag("charging_animation_screen")
    ) {
        // Futuristic grid background with radial glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GoodGreen.copy(alpha = 0.22f),
                        AccentCyan.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * 0.7f
                )
            )

            // Dynamic electric particles
            val particleCount = 18
            for (i in 0 until particleCount) {
                val angle = Math.toRadians((i * (360.0 / particleCount) + waveOffset)).toFloat()
                val radius = (size.minDimension * 0.38f) * (0.8f + 0.2f * sin(angle * 2f))
                val px = center.x + radius * cos(angle)
                val py = center.y + radius * sin(angle)

                drawCircle(
                    color = if (i % 2 == 0) AccentCyan else GoodGreen,
                    radius = 3.5.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = GoodGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TURBO FLASH CHARGING",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoodGreen,
                    letterSpacing = 1.5.sp
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextDark
                )
            }
        }

        // Center Charging Core
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Outer rotating ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 3.dp.toPx()
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(GoodGreen, AccentCyan, GoodGreen.copy(alpha = 0.1f), GoodGreen)
                        ),
                        radius = size.minDimension / 2f - stroke,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }

                // Inner core with percentage
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = GoodGreen,
                        modifier = Modifier.size(38.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = String.format("%.1f", displayPercentage),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark,
                        letterSpacing = (-2).sp
                    )

                    Text(
                        text = "%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoodGreen
                    )

                    Text(
                        text = "Super Fast 65W Flow",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Text2Dark,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // 4 Telemetry Metrics Cards
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "HARDWARE TELEMETRY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ChargingMetric(label = "VOLTAGE", value = "${batteryInfo.voltageV} V")
                        ChargingMetric(label = "TEMPERATURE", value = "${batteryInfo.temperatureC} °C")
                        ChargingMetric(label = "HEALTH", value = batteryInfo.health)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ChargingMetric(label = "POWER SOURCE", value = batteryInfo.pluggedSource)
                        ChargingMetric(label = "TECHNOLOGY", value = batteryInfo.technology)
                        ChargingMetric(label = "EST. FULL", value = "${batteryInfo.estimatedChargeTimeMinutes}m")
                    }
                }
            }
        }

        // Bottom Tap to Return hint
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                    .clickable { onClose() }
                    .padding(horizontal = 22.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "TAP TO RETURN TO DASHBOARD",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Text2Dark,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}

@Composable
private fun ChargingMetric(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Text2Dark,
            letterSpacing = 0.6.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
    }
}
