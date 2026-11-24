package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MoodCheckIn
import com.example.ui.theme.SanadTheme
import kotlinx.coroutines.delay

@Composable
fun WellbeingScreen(
    moodHistory: List<MoodCheckIn>,
    onLogMood: (Int, String, String, String) -> Unit,
    onEmergencyClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Mood selection state
    var selectedScore by remember { mutableStateOf<Int?>(null) }
    var moodNote by remember { mutableStateOf("") }

    val moodOptions = listOf(
        Triple(5, "رائع وممتن", "🌟"),
        Triple(4, "مرتاح وهادئ", "🌿"),
        Triple(3, "عادي ومستقر", "😐"),
        Triple(2, "قلق ومتوتر", "😟"),
        Triple(1, "منهك وحزين", "🌧️")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("wellbeing_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header with Urgent Help Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = "مساحة الراحة النفسية",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text,
                        softWrap = true,
                        maxLines = 2
                    )
                    Text(
                        text = "تمارين استرخاء سريعة ومتابعة يومية لسلامك الداخلي",
                        style = MaterialTheme.typography.bodySmall,
                        color = SanadTheme.colors.textSecondary,
                        softWrap = true
                    )
                }
                com.example.ui.components.UrgentHelpHeaderButton(onClick = onEmergencyClick)
            }
        }

        // 2. Interactive 4-7-8 Breathing Circle
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("breathing_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                border = BorderStroke(1.dp, SanadTheme.colors.divider),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تمرين التنفس \u200E4-7-8\u200E",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.text
                        )
                        Box(
                            modifier = Modifier
                                .background(SanadTheme.colors.surface, RoundedCornerShape(12.dp))
                                .border(1.dp, SanadTheme.colors.divider, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "مهدئ للأعصاب والهلع",
                                style = MaterialTheme.typography.labelSmall,
                                color = SanadTheme.colors.accent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Reusable Breathing Exercise Component
                    com.example.ui.components.BreathingExercise(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 3. Mood Tracker & Check-in
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mood_tracker_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                border = BorderStroke(1.dp, SanadTheme.colors.divider),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "كيف تشعر في هذه اللحظة؟",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text
                    )
                    Text(
                        text = "تسجيل مشاعرك بصدق هو أولى خطوات التعافي والسلام",
                        style = MaterialTheme.typography.bodySmall,
                        color = SanadTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5 Mood Emoji choices
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        moodOptions.forEach { (score, label, emoji) ->
                            val isSelected = selectedScore == score
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedScore = score }
                                    .background(
                                        if (isSelected) SanadTheme.colors.surface else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) SanadTheme.colors.accent else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(8.dp)
                                    .testTag("mood_option_$score")
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 28.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label.split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) SanadTheme.colors.accent else SanadTheme.colors.textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = moodNote,
                        onValueChange = { moodNote = it },
                        placeholder = {
                            Text(
                                text = "ملاحظة صغيرة حول ما تشعر به (اختياري)...",
                                color = SanadTheme.colors.textSecondary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mood_note_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SanadTheme.colors.text,
                            unfocusedTextColor = SanadTheme.colors.text,
                            focusedContainerColor = SanadTheme.colors.surface,
                            unfocusedContainerColor = SanadTheme.colors.surface,
                            focusedBorderColor = SanadTheme.colors.accent,
                            unfocusedBorderColor = SanadTheme.colors.borderInput,
                            cursorColor = SanadTheme.colors.accent
                        ),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        enabled = selectedScore != null,
                        onClick = {
                            val option = moodOptions.find { it.first == selectedScore }
                            if (option != null) {
                                onLogMood(option.first, option.second, option.third, moodNote)
                                moodNote = ""
                                selectedScore = null
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_mood_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SanadTheme.colors.accent,
                            contentColor = SanadTheme.colors.onAccent
                        )
                    ) {
                        Text(
                            text = if (selectedScore == null) "اختر شعورك أولاً" else "حفظ حالتي المزاجية 🌿",
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.onAccent
                        )
                    }
                }
            }
        }

        // 4. Grounding Guide 5-4-3-2-1
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                border = BorderStroke(1.dp, SanadTheme.colors.divider)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تمرين التأريض بالحواس (\u200E5-4-3-2-1\u200E)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "عندما يشتد القلق والهلع، أعد عقلك للواقع عبر هذه الخطوات الخمس:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SanadTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "👁️ 5 أشياء يمكنك رؤيتها بوضوح في غرفتك", style = MaterialTheme.typography.bodySmall, color = SanadTheme.colors.text)
                    Text(text = "✋ 4 أشياء يمكنك لمس ملمسها بيدك الآن", style = MaterialTheme.typography.bodySmall, color = SanadTheme.colors.text)
                    Text(text = "👂 3 أصوات تستطيع تمييزها حولك في المكان", style = MaterialTheme.typography.bodySmall, color = SanadTheme.colors.text)
                    Text(text = "👃 شيئان تستطيع شم رائحتهما الآن", style = MaterialTheme.typography.bodySmall, color = SanadTheme.colors.text)
                    Text(text = "👅 شيء واحد يمكنك تذوقه أو شرب رشفة ماء بارد", style = MaterialTheme.typography.bodySmall, color = SanadTheme.colors.text)
                }
            }
        }
    }
}
