package com.example.ui.components

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SanadTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

// Constants for Breathing Exercise (4-7-8)
const val INHALE_SECONDS = 4
const val HOLD_SECONDS = 7
const val EXHALE_SECONDS = 8
const val TOTAL_CYCLES = 4

enum class BreathingPhase {
    IDLE,
    INHALE, // شهيق (4s)
    HOLD,   // حبس (7s)
    EXHALE, // زفير (8s)
    COMPLETED
}

/**
 * مكوّن واحد قابل لإعادة الاستخدام: BreathingExercise
 * يحرك دائرة التنفس 4-7-8 مع حلقة التقدم وعدد الدورات (4 دورات).
 * يدعم prefers-reduced-motion من إعدادات النظام بحيث يعطل التكبير والتصغير مع بقاء الكلمات والحلقة.
 */
@Composable
fun BreathingExercise(
    modifier: Modifier = Modifier,
    onCompletedDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val isReducedMotion = remember(context) {
        try {
            val animatorScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            animatorScale == 0.0f
        } catch (e: Exception) {
            false
        }
    }

    var isRunning by remember { mutableStateOf(false) }
    var currentCycle by remember { mutableIntStateOf(1) }
    var currentPhase by remember { mutableStateOf(BreathingPhase.IDLE) }
    var phaseProgress by remember { mutableFloatStateOf(0f) }

    // Animated scale for the circle (1.0 to 1.35)
    val circleScaleAnim = remember { Animatable(1f) }

    // Coroutine controller for the 4-7-8 timer and phases
    LaunchedEffect(isRunning) {
        if (!isRunning) {
            currentPhase = BreathingPhase.IDLE
            phaseProgress = 0f
            circleScaleAnim.snapTo(1f)
            return@LaunchedEffect
        }

        try {
            for (cycle in 1..TOTAL_CYCLES) {
                if (!isActive || !isRunning) break
                currentCycle = cycle

                // 1. Inhale (شهيق 4 ثواني) -> scale expands 1.0f to 1.35f
                currentPhase = BreathingPhase.INHALE
                val inhaleMs = INHALE_SECONDS * 1000
                val inhaleSteps = 40
                val inhaleInterval = inhaleMs / inhaleSteps

                for (step in 1..inhaleSteps) {
                    if (!isActive || !isRunning) break
                    phaseProgress = step.toFloat() / inhaleSteps.toFloat()
                    val targetScale = if (isReducedMotion) 1f else (1f + 0.35f * phaseProgress)
                    circleScaleAnim.snapTo(targetScale)
                    delay(inhaleInterval.toLong())
                }

                if (!isActive || !isRunning) break

                // 2. Hold (حبس 7 ثواني) -> scale remains fixed at 1.35f
                currentPhase = BreathingPhase.HOLD
                val holdMs = HOLD_SECONDS * 1000
                val holdSteps = 70
                val holdInterval = holdMs / holdSteps

                for (step in 1..holdSteps) {
                    if (!isActive || !isRunning) break
                    phaseProgress = step.toFloat() / holdSteps.toFloat()
                    delay(holdInterval.toLong())
                }

                if (!isActive || !isRunning) break

                // 3. Exhale (زفير 8 ثواني) -> scale contracts back to 1.0f
                currentPhase = BreathingPhase.EXHALE
                val exhaleMs = EXHALE_SECONDS * 1000
                val exhaleSteps = 80
                val exhaleInterval = exhaleMs / exhaleSteps

                for (step in 1..exhaleSteps) {
                    if (!isActive || !isRunning) break
                    phaseProgress = step.toFloat() / exhaleSteps.toFloat()
                    val targetScale = if (isReducedMotion) 1f else (1.35f - 0.35f * phaseProgress)
                    circleScaleAnim.snapTo(targetScale)
                    delay(exhaleInterval.toLong())
                }
            }

            if (isRunning) {
                // Completed all 4 cycles
                currentPhase = BreathingPhase.COMPLETED
                phaseProgress = 1f
                circleScaleAnim.snapTo(1f)
            }
        } catch (e: CancellationException) {
            // Clean up upon cancellation
            currentPhase = BreathingPhase.IDLE
            phaseProgress = 0f
            circleScaleAnim.snapTo(1f)
        }
    }

    // Clean up when leaving composition
    DisposableEffect(Unit) {
        onDispose {
            isRunning = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("breathing_exercise_component"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentPhase == BreathingPhase.COMPLETED) {
            // Screen after 4 cycles completed
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🌿",
                fontSize = 44.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "خلصنا. الوقت وقتك.",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.text,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "أخذت 4 دورات تنفس كاملة مهدئة للجهاز العصبي.",
                style = MaterialTheme.typography.bodyMedium,
                color = SanadTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        // Restart another 4 cycles
                        currentCycle = 1
                        currentPhase = BreathingPhase.IDLE
                        phaseProgress = 0f
                        isRunning = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("breathing_repeat_cycle_btn"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, SanadTheme.colors.accent)
                ) {
                    Text(
                        text = "دورة كمان",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.accent
                    )
                }

                Button(
                    onClick = {
                        // Reset to idle start state
                        isRunning = false
                        currentCycle = 1
                        currentPhase = BreathingPhase.IDLE
                        phaseProgress = 0f
                        onCompletedDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("breathing_finish_ok_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SanadTheme.colors.accent,
                        contentColor = SanadTheme.colors.onAccent
                    )
                ) {
                    Text(
                        text = "تمام",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Cycle count indicator above circle during exercise
            if (isRunning) {
                Text(
                    text = "دورة $currentCycle من $TOTAL_CYCLES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.accent,
                    modifier = Modifier
                        .padding(bottom = 14.dp)
                        .testTag("breathing_cycle_indicator")
                )
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Outer container with Progress Ring & Animated Breathing Circle
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clickable {
                        // Clicking circle toggles exercise
                        if (isRunning) {
                            isRunning = false
                        } else {
                            currentCycle = 1
                            isRunning = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Outer progress ring in --accent (filling smoothly per phase)
                if (isRunning) {
                    CircularProgressIndicator(
                        progress = { phaseProgress },
                        modifier = Modifier.size(200.dp),
                        color = SanadTheme.colors.accent,
                        trackColor = SanadTheme.colors.divider,
                        strokeWidth = 3.dp
                    )
                }

                // Inner Breathing Circle: scales up and down, background gradient + border --accent
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .scale(circleScaleAnim.value)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    SanadTheme.colors.accent.copy(alpha = 0.2f),
                                    SanadTheme.colors.accent.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                        .border(3.dp, SanadTheme.colors.accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = SanadTheme.colors.accent,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (currentPhase) {
                                BreathingPhase.IDLE -> "ابدأ التمرين"
                                BreathingPhase.INHALE -> "شهيق"
                                BreathingPhase.HOLD -> "حبس"
                                BreathingPhase.EXHALE -> "زفير"
                                BreathingPhase.COMPLETED -> "خلصنا"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.text,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Action Button: «ابدأ الآن» or «إيقاف»
            Button(
                onClick = {
                    if (isRunning) {
                        // Stop and reset to idle start state
                        isRunning = false
                        currentCycle = 1
                        currentPhase = BreathingPhase.IDLE
                        phaseProgress = 0f
                    } else {
                        // Start exercise from cycle 1
                        currentCycle = 1
                        isRunning = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(48.dp)
                    .testTag("toggle_breathing_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) SanadTheme.colors.surface else SanadTheme.colors.accent,
                    contentColor = if (isRunning) SanadTheme.colors.text else SanadTheme.colors.onAccent
                ),
                border = if (isRunning) BorderStroke(1.5.dp, SanadTheme.colors.divider) else null
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isRunning) SanadTheme.colors.text else SanadTheme.colors.onAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "إيقاف" else "ابدأ الآن",
                    color = if (isRunning) SanadTheme.colors.text else SanadTheme.colors.onAccent,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
