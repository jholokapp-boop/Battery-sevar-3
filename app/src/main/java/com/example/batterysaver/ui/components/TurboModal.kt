package com.example.batterysaver.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.batterysaver.model.AppConsumption
import com.example.batterysaver.ui.theme.AccentCyan
import com.example.batterysaver.ui.theme.GoodGreen
import com.example.batterysaver.ui.theme.TurboOrange
import com.example.batterysaver.ui.theme.TurboRose
import com.example.batterysaver.ui.theme.WarnAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TurboModal(
    appsList: List<AppConsumption>,
    onDismiss: () -> Unit,
    onActivate: () -> Unit
) {
    val stoppableApps = appsList.filter { it.isStoppable }
    var isOptimizing by remember { mutableStateOf(false) }
    var currentStepIdx by remember { mutableIntStateOf(0) }
    var showSuccessCheck by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "rocket")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val steps = listOf(
        "Background processes সম্পূর্ণ বন্ধ করা হচ্ছে…",
        "RAM মেমোরি খালি ও ব্যাকগ্রাউন্ড টাস্ক কিল করা হচ্ছে…",
        "Sync, GPS ও অপ্রয়োজনীয় ওয়েক-লক ব্লক করা হচ্ছে…",
        "CPU throttle ও আল্ট্রা পাওয়ার প্রোফাইল সক্রিয় করা হচ্ছে…"
    )

    fun startOptimization() {
        isOptimizing = true
    }

    LaunchedEffect(isOptimizing) {
        if (isOptimizing) {
            for (i in steps.indices) {
                currentStepIdx = i
                delay(650)
            }
            showSuccessCheck = true
            delay(1000)
            onActivate()
        }
    }

    Dialog(
        onDismissRequest = { if (!isOptimizing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("turbo_modal"),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TURBO SAVER MODE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.2.sp
                        )

                        if (!isOptimizing) {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (!isOptimizing) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                // Hero Badge & Copy
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .scale(pulseScale)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(listOf(TurboOrange, TurboRose))
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RocketLaunch,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(46.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Turbo Battery Saver",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "সব background apps বন্ধ হবে। শুধু এই app চলবে ~2MB RAM এ। ব্যাকগ্রাউন্ড ড্রেন একদম শূন্যে নেমে আসবে।",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }

                            item {
                                // 4 Impact Cards Grid
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        ImpactBox(
                                            metric = "+1h 15m",
                                            label = "Extra Time",
                                            isGradient = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        ImpactBox(
                                            metric = "+35%",
                                            label = "Saved Power",
                                            isGradient = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        ImpactBox(
                                            metric = "${stoppableApps.size}",
                                            label = "Apps Paused",
                                            isGradient = false,
                                            modifier = Modifier.weight(1f)
                                        )
                                        ImpactBox(
                                            metric = "~2 MB",
                                            label = "This App RAM",
                                            isGradient = false,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            item {
                                // What will happen explanation box
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = TurboOrange.copy(alpha = 0.09f)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        TurboOrange.copy(alpha = 0.28f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "⚡ Turbo Mode এ যা হবে:",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TurboOrange
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        BulletPoint("সব background apps সম্পূর্ণ কিল এবং সাসপেন্ড হবে")
                                        BulletPoint("ব্যাকগ্রাউন্ড ড্রেন একদম 0% এ রাখা হবে")
                                        BulletPoint("Background sync, GPS ও অটো আপডেট বন্ধ থাকবে")
                                        BulletPoint("CPU throttle ও আল্ট্রা পাওয়ার প্রোফাইল সেট হবে")
                                        BulletPoint("শুধু এই app চলবে অত্যন্ত কম (~2MB) RAM রিসোর্সে")
                                        BulletPoint("ম্যানুয়ালি যে app এ ক্লিক করবে সেটাই শুধু স্বাভাবিকভাবে চলবে")
                                    }
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "যে Apps কিল ও বন্ধ হবে",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(100.dp))
                                            .background(WarnAmber.copy(alpha = 0.15f))
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "${stoppableApps.size} apps",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WarnAmber
                                        )
                                    }
                                }
                            }

                            items(stoppableApps) { app ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline
                                    ),
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
                                                .background(Color(app.colorHex).copy(alpha = 0.16f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = app.iconEmoji, fontSize = 17.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = app.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${app.percentage}% battery • ${app.backgroundTime} background",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(WarnAmber.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "RUNNING",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WarnAmber
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(70.dp))
                            }
                        }

                        // CTA Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Button(
                                onClick = { startOptimization() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(listOf(TurboOrange, TurboRose))
                                    )
                                    .testTag("turbo_confirm_button")
                            ) {
                                Text(
                                    text = "🚀 চালু করো (Activate Turbo)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        // Multi-step animated progress view
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (!showSuccessCheck) {
                                CircularProgressIndicator(
                                    color = TurboOrange,
                                    strokeWidth = 4.dp,
                                    modifier = Modifier.size(54.dp)
                                )

                                Spacer(modifier = Modifier.height(28.dp))

                                Text(
                                    text = steps[currentStepIdx],
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    steps.forEachIndexed { idx, _ ->
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (idx <= currentStepIdx) TurboOrange else MaterialTheme.colorScheme.outline
                                                )
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .background(GoodGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Success",
                                        tint = Color.White,
                                        modifier = Modifier.size(56.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "সম্পন্ন!",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoodGreen
                                )

                                Text(
                                    text = "Turbo Battery Saver চালু হয়েছে",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImpactBox(
    metric: String,
    label: String,
    isGradient: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = metric,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGradient) TurboOrange else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.6.sp
            )
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(TurboOrange)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 17.sp
        )
    }
}
