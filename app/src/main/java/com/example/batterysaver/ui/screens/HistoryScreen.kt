package com.example.batterysaver.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.batterysaver.model.BatteryEvent
import com.example.batterysaver.model.HistoryPoint
import com.example.batterysaver.ui.theme.AccentCyan
import com.example.batterysaver.ui.theme.GoodGreen

@Composable
fun HistoryScreen(
    historyPoints: List<HistoryPoint>,
    events: List<BatteryEvent>
) {
    val minLevel = historyPoints.minOfOrNull { it.level } ?: 0
    val maxLevel = historyPoints.maxOfOrNull { it.level } ?: 100
    val avgLevel = if (historyPoints.isNotEmpty()) {
        historyPoints.sumOf { it.level } / historyPoints.size
    } else 50

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("history_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 24H Chart Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Last 24 Hours Battery Curve",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Canvas Chart
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val padLeft = 36.dp.toPx()
                            val padRight = 10.dp.toPx()
                            val padTop = 12.dp.toPx()
                            val padBottom = 22.dp.toPx()
                            val chartW = w - padLeft - padRight
                            val chartH = h - padTop - padBottom

                            // Grid lines
                            val gridColor = Color.White.copy(alpha = 0.06f)
                            for (i in 0..4) {
                                val y = padTop + (chartH / 4f) * i
                                drawLine(
                                    color = gridColor,
                                    start = Offset(padLeft, y),
                                    end = Offset(w - padRight, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            if (historyPoints.size > 1) {
                                val stepX = chartW / (historyPoints.size - 1)
                                val range = (maxLevel - minLevel).coerceAtLeast(1)

                                fun getY(lvl: Int): Float {
                                    val frac = (lvl - minLevel).toFloat() / range.toFloat()
                                    return padTop + (1f - frac) * chartH
                                }

                                // Filled path under curve
                                val fillPath = Path().apply {
                                    moveTo(padLeft, padTop + chartH)
                                    historyPoints.forEachIndexed { i, p ->
                                        val x = padLeft + i * stepX
                                        val y = getY(p.level)
                                        lineTo(x, y)
                                    }
                                    lineTo(padLeft + (historyPoints.size - 1) * stepX, padTop + chartH)
                                    close()
                                }

                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            AccentCyan.copy(alpha = 0.32f),
                                            AccentCyan.copy(alpha = 0.0f)
                                        ),
                                        startY = padTop,
                                        endY = padTop + chartH
                                    )
                                )

                                // Stroke curve
                                val strokePath = Path().apply {
                                    historyPoints.forEachIndexed { i, p ->
                                        val x = padLeft + i * stepX
                                        val y = getY(p.level)
                                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                                    }
                                }

                                drawPath(
                                    path = strokePath,
                                    color = AccentCyan,
                                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // Current level beacon dot
                                val lastX = padLeft + (historyPoints.size - 1) * stepX
                                val lastY = getY(historyPoints.last().level)
                                drawCircle(
                                    color = AccentCyan,
                                    radius = 5.dp.toPx(),
                                    center = Offset(lastX, lastY)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.5.dp.toPx(),
                                    center = Offset(lastX, lastY)
                                )
                            }
                        }
                    }

                    // Chart Legend
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ChartLegendItem(label = "MIN", value = "$minLevel%")
                        ChartLegendItem(label = "AVG", value = "$avgLevel%")
                        ChartLegendItem(label = "MAX", value = "$maxLevel%")
                    }
                }
            }
        }

        // Recent Events Title
        item {
            Text(
                text = "Recent Power Events",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Events list
        if (events.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No events logged yet",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(events, key = { it.id }) { event ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = event.icon, fontSize = 17.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.title,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${event.subtitle} • ${event.timeLabel}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartLegendItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
