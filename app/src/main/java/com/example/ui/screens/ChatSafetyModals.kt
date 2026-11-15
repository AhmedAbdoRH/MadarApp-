package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SanadTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMenuBottomSheet(
    isSpecialistViewer: Boolean,
    isSessionResolved: Boolean,
    onDismiss: () -> Unit,
    onEndSessionClick: () -> Unit,
    onReportClick: () -> Unit,
    onBlockClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SanadTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "خيارات المحادثة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.text,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (!isSessionResolved) {
                ChatMenuRow(
                    icon = Icons.Default.StopCircle,
                    label = "إنهاء الجلسة",
                    tint = SanadTheme.colors.text,
                    onClick = onEndSessionClick
                )
            }

            if (!isSpecialistViewer) {
                ChatMenuRow(
                    icon = Icons.Default.Flag,
                    label = "إبلاغ عن المحادثة",
                    tint = SanadTheme.colors.text,
                    onClick = onReportClick
                )

                ChatMenuRow(
                    icon = Icons.Default.Block,
                    label = "حظر الأخصائي",
                    tint = SanadTheme.colors.danger,
                    onClick = onBlockClick
                )
            }
        }
    }
}

@Composable
private fun ChatMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = tint
        )
    }
}

@Composable
fun EndSessionConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SanadTheme.colors.surface,
        title = {
            Text(
                text = "إنهاء الجلسة؟",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.text
            )
        },
        text = {
            Text(
                text = "المحادثة هتتقفل ولن تتمكن من إرسال رسائل جديدة فيها.",
                style = MaterialTheme.typography.bodyMedium,
                color = SanadTheme.colors.textSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SanadTheme.colors.accent,
                    contentColor = SanadTheme.colors.onAccent
                ),
                modifier = Modifier.testTag("confirm_end_session_button")
            ) {
                Text("إنهاء", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_end_session_button")
            ) {
                Text("رجوع", color = SanadTheme.colors.text)
            }
        }
    )
}

@Composable
fun BlockSpecialistConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SanadTheme.colors.surface,
        title = {
            Text(
                text = "حظر الأخصائي؟",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.text
            )
        },
        text = {
            Text(
                text = "مش هتظهر لك محادثات معاه تاني.",
                style = MaterialTheme.typography.bodyMedium,
                color = SanadTheme.colors.textSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SanadTheme.colors.danger,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("confirm_block_specialist_button")
            ) {
                Text("حظر", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_block_specialist_button")
            ) {
                Text("رجوع", color = SanadTheme.colors.text)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSpecialistBottomSheet(
    reportReasons: List<String>,
    selectedReason: String?,
    reportDetails: String,
    submittedMessage: String?,
    isUrgentRisk: Boolean,
    onSelectReason: (String) -> Unit,
    onDetailsChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onUrgentHelpClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SanadTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
        ) {
            if (submittedMessage == null) {
                Text(
                    text = "إبلاغ عن محادثة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.text,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "اختر سبب البلاغ للمساعدة في الحفاظ على أمان المنصة:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SanadTheme.colors.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                reportReasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .selectable(
                                selected = (selectedReason == reason),
                                onClick = { onSelectReason(reason) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedReason == reason),
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = SanadTheme.colors.accent,
                                unselectedColor = SanadTheme.colors.borderInput
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SanadTheme.colors.text
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reportDetails,
                    onValueChange = onDetailsChange,
                    placeholder = {
                        Text(
                            text = "تفاصيل (اختياري)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SanadTheme.colors.textSecondary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_details_field"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SanadTheme.colors.text,
                        unfocusedTextColor = SanadTheme.colors.text,
                        focusedContainerColor = SanadTheme.colors.bg,
                        unfocusedContainerColor = SanadTheme.colors.bg,
                        focusedBorderColor = SanadTheme.colors.accent,
                        unfocusedBorderColor = SanadTheme.colors.borderInput
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSubmit,
                    enabled = selectedReason != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_report_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SanadTheme.colors.accent,
                        contentColor = SanadTheme.colors.onAccent,
                        disabledContainerColor = SanadTheme.colors.borderInput,
                        disabledContentColor = SanadTheme.colors.textSecondary
                    )
                ) {
                    Text(
                        text = "إرسال البلاغ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = submittedMessage,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text,
                        textAlign = TextAlign.Center
                    )

                    if (isUrgentRisk) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "إذا كان هناك شخص في خطر فوري، ننصحك بالرجوع لخطوط المساندة والطوارئ فوراً.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SanadTheme.colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onUrgentHelpClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("report_urgent_help_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SanadTheme.colors.text,
                                contentColor = SanadTheme.colors.bg
                            )
                        ) {
                            Text("مساعدة عاجلة", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("dismiss_report_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SanadTheme.colors.accent,
                                contentColor = SanadTheme.colors.onAccent
                            )
                        ) {
                            Text("تمام", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostSessionMoodBottomSheet(
    moodOptions: List<Triple<Int, String, String>>,
    selectedMoodOption: Triple<Int, String, String>?,
    moodSubmitted: Boolean,
    onSelectMood: (Triple<Int, String, String>) -> Unit,
    onSkip: () -> Unit,
    onFinish: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SanadTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!moodSubmitted) {
                Text(
                    text = "الإحساس دلوقتي عامل إزاي؟",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.text,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "مشاركتك بتساعدنا نطمن عليك بعد المحادثة 🌿",
                    style = MaterialTheme.typography.bodySmall,
                    color = SanadTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    moodOptions.forEach { option ->
                        val isSelected = selectedMoodOption?.first == option.first
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) SanadTheme.colors.accent.copy(alpha = 0.2f) else Color.Transparent
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) SanadTheme.colors.accent else SanadTheme.colors.divider,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelectMood(option) }
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                                .testTag("mood_option_${option.first}")
                        ) {
                            Text(text = option.third, fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (option.first) {
                                    5 -> "رائع"
                                    4 -> "مرتاح"
                                    3 -> "عادي"
                                    2 -> "قلق"
                                    else -> "منهك"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) SanadTheme.colors.accent else SanadTheme.colors.textSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "تخطي",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SanadTheme.colors.textSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onSkip)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("skip_session_mood_button")
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = selectedMoodOption?.third ?: "🌿",
                    fontSize = 44.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "شكرًا إنك كنت هنا.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.text,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "نتمنى تكون المحادثة ساعدتك تهدى شوية. بابنا مفتوح ليك دايماً.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SanadTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("finish_mood_return_home_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SanadTheme.colors.accent,
                        contentColor = SanadTheme.colors.onAccent
                    )
                ) {
                    Text(
                        text = "تمام",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
