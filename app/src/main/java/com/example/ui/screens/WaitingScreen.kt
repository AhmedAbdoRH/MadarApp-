package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.UrgentHelpHeaderButton
import com.example.ui.theme.SanadTheme
import kotlinx.coroutines.delay

const val WAIT_TIMEOUT_SECONDS: Int = 75

/**
 * شاشة الانتظار لطلب شات فوري
 * - تظهر أثناء انتظار قبول الأخصائي للطلب
 * - دايرة تنفس هادية تكبر خلال 4 ثواني (شهيق) وتصغر خلال 6 ثواني (زفير)
 * - بعد 75 ثانية بدون قبول: يظهر اقتراح «لسه مفيش رد. نجرب أخصائي تاني؟»
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaitingScreen(
    onCancel: () -> Unit,
    onTryAnotherSpecialist: () -> Unit,
    onKeepWaiting: () -> Unit = {},
    onOpenEmergencyHelp: () -> Unit = {},
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var isTimedOut by remember { mutableStateOf(false) }
    var breathingPhase by remember { mutableStateOf("شهيق") }

    BackHandler(onBack = onCancel)

    // Breathing phase loop: 4s inhale, 6s exhale (10s total cycle)
    LaunchedEffect(Unit) {
        while (true) {
            breathingPhase = "شهيق"
            delay(4000L)
            breathingPhase = "زفير"
            delay(6000L)
        }
    }

    // Timeout counter
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            elapsedSeconds++
            if (elapsedSeconds >= WAIT_TIMEOUT_SECONDS) {
                isTimedOut = true
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("waiting_screen"),
        containerColor = SanadTheme.colors.bg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "كلم سند",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text
                    )
                },
                actions = {
                    UrgentHelpHeaderButton(
                        onClick = onOpenEmergencyHelp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SanadTheme.colors.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Main breathing animated circle
            val infiniteTransition = rememberInfiniteTransition(label = "waiting_breathing")
            val animatedScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.35f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "breathing_circle_scale"
            )

            val circleScale = if (reduceMotion) 1f else animatedScale

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(circleScale)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                SanadTheme.colors.accent.copy(alpha = 0.22f),
                                SanadTheme.colors.accent.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
                    .border(2.5.dp, SanadTheme.colors.accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = breathingPhase,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.text,
                    fontSize = 26.sp
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            if (!isTimedOut) {
                Text(
                    text = "بنوصّلك بأخصائي…",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.text,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "مدة الانتظار غير محددة وتعتمد على توافر المتطوعين.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SanadTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Cancel button (secondary style)
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, SanadTheme.colors.textSecondary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SanadTheme.colors.text
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(48.dp)
                        .testTag("waiting_cancel_btn")
                ) {
                    Text(
                        text = "إلغاء",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text
                    )
                }
            } else {
                // Timeout after 75s
                Text(
                    text = "لسه مفيش رد. نجرب أخصائي تاني؟",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.text,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Try another specialist (Primary style)
                    Button(
                        onClick = onTryAnotherSpecialist,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SanadTheme.colors.accent,
                            contentColor = SanadTheme.colors.onAccent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("try_another_specialist_btn")
                    ) {
                        Text(
                            text = "نجرب أخصائي تاني",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.onAccent
                        )
                    }

                    // Keep waiting (Secondary style)
                    OutlinedButton(
                        onClick = {
                            isTimedOut = false
                            elapsedSeconds = 0
                            onKeepWaiting()
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, SanadTheme.colors.textSecondary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SanadTheme.colors.text
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("keep_waiting_btn")
                    ) {
                        Text(
                            text = "نكمل ننتظر",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.text
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SanadTheme.colors.divider),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SanadTheme.colors.textSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(44.dp)
                ) {
                    Text(
                        text = "إلغاء",
                        style = MaterialTheme.typography.labelMedium,
                        color = SanadTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
