package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BookingSlot
import com.example.ui.theme.SanadTheme

@Composable
fun BookingConfirmDialog(
    slot: BookingSlot,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    isSubmitting: Boolean = false
) {
    var nameInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { if (!isSubmitting) onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, SanadTheme.colors.divider, RoundedCornerShape(24.dp))
                .testTag("booking_confirm_dialog"),
            color = SanadTheme.colors.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SanadTheme.colors.accent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = SanadTheme.colors.onAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "تأكيد حجز جلسة استشارية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.text
                        )
                    }
                    IconButton(onClick = onDismiss, enabled = !isSubmitting) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = SanadTheme.colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SanadTheme.colors.divider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = slot.specialistName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.text
                        )
                        Text(
                            text = slot.specialtyTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = SanadTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SanadTheme.colors.online,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${slot.dateText} | ${slot.timeText}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SanadTheme.colors.text
                            )
                        }
                        Text(
                            text = "الخدمة مجانية بالكامل ومقدمة من أخصائي متطوع 🌿",
                            style = MaterialTheme.typography.labelSmall,
                            color = SanadTheme.colors.online,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    enabled = !isSubmitting,
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("اسمك أو اسم مستعار (اختياري للخصوصية)") },
                    placeholder = {
                        Text(
                            text = "مثال: أحمد أو باحث عن دعم",
                            color = SanadTheme.colors.textSecondary
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SanadTheme.colors.text,
                        unfocusedTextColor = SanadTheme.colors.text,
                        focusedLabelColor = SanadTheme.colors.accent,
                        unfocusedLabelColor = SanadTheme.colors.textSecondary,
                        focusedBorderColor = SanadTheme.colors.accent,
                        unfocusedBorderColor = SanadTheme.colors.borderInput,
                        cursorColor = SanadTheme.colors.accent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("booking_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                        Text(
                            text = "إلغاء",
                            color = SanadTheme.colors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        enabled = !isSubmitting,
                        onClick = {
                            val finalName = if (nameInput.isNotBlank()) nameInput.trim() else "مستفيد"
                            onConfirm(finalName)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SanadTheme.colors.accent,
                            contentColor = SanadTheme.colors.onAccent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("confirm_booking_button")
                    ) {
                        Text(
                            text = if (isSubmitting) "جارٍ تأكيد الحجز..." else "تأكيد الحجز مجاناً",
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.onAccent
                        )
                    }
                }
            }
        }
    }
}
