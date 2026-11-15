package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BookingSlot
import com.example.ui.theme.SanadTheme

@Composable
fun SpecialistScheduleScreen(
    specialistName: String,
    mySlots: List<BookingSlot>,
    onCreateSlot: (dateText: String, timeText: String, specialtyTitle: String) -> Unit,
    onDeleteSlot: (slotId: String) -> Unit,
    onCancelBookedSlot: (slotId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val bookedSlots = mySlots.filter { it.isBooked }
    val openSlots = mySlots.filter { !it.isBooked }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("specialist_schedule_screen"),
        containerColor = SanadTheme.colors.bg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SanadTheme.colors.accent,
                contentColor = SanadTheme.colors.onAccent,
                modifier = Modifier.testTag("add_booking_slot_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة موعد متاح")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "إدارة جدول المواعيد",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text
                    )
                    Text(
                        text = "أضف أوقات فراغك لتظهر للمستفيدين في صفحة حجز المواعيد",
                        style = MaterialTheme.typography.bodySmall,
                        color = SanadTheme.colors.textSecondary
                    )
                }
            }

            // Booked sessions with seekers
            item {
                Text(
                    text = "الجلسات المحجوزة (${bookedSlots.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.text
                )
            }

            if (bookedSlots.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                        border = BorderStroke(1.dp, SanadTheme.colors.divider)
                    ) {
                        Text(
                            text = "لا توجد جلسات محجوزة مع مستفيدين حالياً.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SanadTheme.colors.textSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(bookedSlots, key = { "booked_${it.id}" }) { slot ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                        border = BorderStroke(1.dp, SanadTheme.colors.online)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = SanadTheme.colors.online,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "المستفيد: ${slot.bookedByName ?: "مستفيد"}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SanadTheme.colors.text
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(SanadTheme.colors.bg, RoundedCornerShape(8.dp))
                                        .border(1.dp, SanadTheme.colors.online, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "محجوزة",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SanadTheme.colors.online,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = slot.specialtyTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = SanadTheme.colors.accent
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SanadTheme.colors.textSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(slot.dateText, style = MaterialTheme.typography.bodySmall, color = SanadTheme.colors.text)
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = SanadTheme.colors.textSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(slot.timeText, style = MaterialTheme.typography.bodySmall, color = SanadTheme.colors.text)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { onCancelBookedSlot(slot.id) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("إلغاء الحجز وإعادة فتح الموعد", color = SanadTheme.colors.danger)
                            }
                        }
                    }
                }
            }

            // Open available slots
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "المواعيد المتاحة للحجز (${openSlots.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.text
                )
            }

            if (openSlots.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                        border = BorderStroke(1.dp, SanadTheme.colors.divider)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = SanadTheme.colors.accent,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "لم تضف مواعيد متاحة بعد",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.text
                            )
                            Text(
                                text = "اضغط على زر (+) لإضافة موعد جديد ليتمكن المستفيدون من حجزه",
                                style = MaterialTheme.typography.bodySmall,
                                color = SanadTheme.colors.textSecondary
                            )
                        }
                    }
                }
            } else {
                items(openSlots, key = { "open_${it.id}" }) { slot ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                        border = BorderStroke(1.dp, SanadTheme.colors.divider)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${slot.dateText} • ${slot.timeText}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SanadTheme.colors.text
                                )
                                Text(
                                    text = slot.specialtyTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SanadTheme.colors.textSecondary
                                )
                            }
                            IconButton(
                                onClick = { onDeleteSlot(slot.id) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("delete_slot_${slot.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "حذف الموعد",
                                    tint = SanadTheme.colors.danger
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    if (showAddDialog) {
        AddBookingSlotDialog(
            specialistName = specialistName,
            onDismiss = { showAddDialog = false },
            onConfirm = { dateText, timeText, specialtyTitle ->
                onCreateSlot(dateText, timeText, specialtyTitle)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddBookingSlotDialog(
    specialistName: String,
    onDismiss: () -> Unit,
    onConfirm: (dateText: String, timeText: String, specialtyTitle: String) -> Unit
) {
    var dateText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }
    var specialtyTitle by remember { mutableStateOf("جلسة استشارة ودعم نفسي (45 دقيقة)") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = SanadTheme.colors.text,
        unfocusedTextColor = SanadTheme.colors.text,
        focusedBorderColor = SanadTheme.colors.accent,
        unfocusedBorderColor = SanadTheme.colors.borderInput,
        focusedLabelColor = SanadTheme.colors.accent,
        unfocusedLabelColor = SanadTheme.colors.textSecondary
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SanadTheme.colors.surface,
        title = {
            Text(
                text = "إضافة موعد جلسة جديد",
                color = SanadTheme.colors.text,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "الأخصائي: $specialistName",
                    style = MaterialTheme.typography.labelMedium,
                    color = SanadTheme.colors.accent
                )
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("اليوم والتاريخ") },
                    placeholder = { Text("مثال: غداً - الخميس") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slot_date_input")
                )
                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = { Text("الوقت") },
                    placeholder = { Text("مثال: 07:00 م - 07:45 م") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slot_time_input")
                )
                OutlinedTextField(
                    value = specialtyTitle,
                    onValueChange = { specialtyTitle = it },
                    label = { Text("موضوع / مسار الجلسة") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slot_topic_input")
                )
                if (errorMsg != null) {
                    Text(errorMsg!!, color = SanadTheme.colors.danger, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (dateText.isBlank() || timeText.isBlank() || specialtyTitle.isBlank()) {
                        errorMsg = "يرجى استكمال اليوم والوقت وموضوع الجلسة"
                    } else {
                        onConfirm(dateText.trim(), timeText.trim(), specialtyTitle.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SanadTheme.colors.accent,
                    contentColor = SanadTheme.colors.onAccent
                ),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("حفظ ونشر الموعد", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = SanadTheme.colors.textSecondary)
            }
        }
    )
}
