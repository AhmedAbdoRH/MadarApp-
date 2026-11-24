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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingSlot
import com.example.ui.theme.SanadTheme

@Composable
fun SessionsBookingScreen(
    slots: List<BookingSlot>,
    onOpenBookingDialog: (BookingSlot) -> Unit,
    onCancelBooking: (String) -> Unit,
    onEmergencyClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("المواعيد المتاحة 📅", "جلساتي المحجوزة ⏳")

    val availableSlots = slots.filter { !it.isBooked }
    val myBookedSlots = slots.filter { it.isBooked }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("sessions_booking_screen")
    ) {
        // Header with Urgent Help Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = "حجز الجلسات المجدولة",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.text,
                    softWrap = true,
                    maxLines = 2
                )
                Text(
                    text = "جلسات استشارية فردية متعمقة (45 دقيقة) مع أخصائيين متطوعين",
                    style = MaterialTheme.typography.bodySmall,
                    color = SanadTheme.colors.textSecondary,
                    softWrap = true
                )
            }
            com.example.ui.components.UrgentHelpHeaderButton(onClick = onEmergencyClick)
        }

        // Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
            border = BorderStroke(1.dp, SanadTheme.colors.divider)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SanadTheme.colors.accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "الخدمة الأساسية في «كلم سند» هي المحادثة المجانية حسب توافر المتطوعين، وهذه الجلسات المجدولة مخصصة لمن يرغب في متابعة منتظمة ومحددة مسبقاً.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SanadTheme.colors.textSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = SanadTheme.colors.surface,
            contentColor = SanadTheme.colors.accent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = SanadTheme.colors.accent
                )
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .border(1.dp, SanadTheme.colors.divider, RoundedCornerShape(12.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = if (index == 1 && myBookedSlots.isNotEmpty()) "$title (${myBookedSlots.size})" else title,
                            color = if (isSelected) SanadTheme.colors.accent else SanadTheme.colors.textSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("booking_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        if (selectedTabIndex == 0) {
            if (availableSlots.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد مواعيد متاحة حالياً، يتم تحديث الجداول بشكل مستمر من المتطوعين.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SanadTheme.colors.textSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(availableSlots, key = { it.id }) { slot ->
                        AvailableSlotCard(
                            slot = slot,
                            onBook = { onOpenBookingDialog(slot) }
                        )
                    }
                }
            }
        } else {
            if (myBookedSlots.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = SanadTheme.colors.accent,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لم تقم بحجز أي جلسة حتى الآن",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.text
                        )
                        Text(
                            text = "يمكنك حجز جلسة قادمة من قائمة المواعيد المتاحة أعلاه.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SanadTheme.colors.textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myBookedSlots, key = { it.id }) { slot ->
                        BookedSlotCard(
                            slot = slot,
                            onCancel = { onCancelBooking(slot.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AvailableSlotCard(
    slot: BookingSlot,
    onBook: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("slot_item_${slot.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
        border = BorderStroke(1.dp, SanadTheme.colors.divider),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = slot.specialistName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.text
                )
                Box(
                    modifier = Modifier
                        .background(SanadTheme.colors.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, SanadTheme.colors.online, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "متاح للحجز",
                        style = MaterialTheme.typography.labelSmall,
                        color = SanadTheme.colors.online,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = slot.specialtyTitle,
                style = MaterialTheme.typography.bodySmall,
                color = SanadTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = SanadTheme.colors.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = slot.dateText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SanadTheme.colors.text
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = SanadTheme.colors.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = slot.timeText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SanadTheme.colors.text
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onBook,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("book_button_${slot.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SanadTheme.colors.accent,
                    contentColor = SanadTheme.colors.onAccent
                )
            ) {
                Text(
                    text = "حجز هذا الموعد مجاناً",
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.onAccent
                )
            }
        }
    }
}

@Composable
fun BookedSlotCard(
    slot: BookingSlot,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("booked_slot_${slot.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
        border = BorderStroke(1.dp, SanadTheme.colors.divider),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SanadTheme.colors.online,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "جلسة مؤكدة",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.online
                    )
                }

                Text(
                    text = slot.dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.text
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "مع: ${slot.specialistName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.text
            )
            Text(
                text = slot.specialtyTitle,
                style = MaterialTheme.typography.bodySmall,
                color = SanadTheme.colors.textSecondary
            )
            Text(
                text = "الوقت المحدد: ${slot.timeText}",
                style = MaterialTheme.typography.bodyMedium,
                color = SanadTheme.colors.accent,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cancel_booking_${slot.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SanadTheme.colors.danger
                ),
                border = BorderStroke(1.5.dp, SanadTheme.colors.danger)
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = SanadTheme.colors.danger
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "إلغاء الموعد",
                    color = SanadTheme.colors.danger
                )
            }
        }
    }
}
