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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingSlot
import com.example.data.model.Conversation
import com.example.data.model.Specialist
import com.example.ui.theme.SanadTheme

@Composable
fun SpecialistDashboardScreen(
    specialist: Specialist?,
    isOnline: Boolean,
    pendingRequests: List<Conversation>,
    activeConversations: List<Conversation>,
    myBookingSlots: List<BookingSlot>,
    onToggleOnline: (Boolean) -> Unit,
    onAcceptRequest: (Conversation) -> Unit,
    onOpenConversation: (String) -> Unit,
    onNavigateToSchedule: () -> Unit,
    onSwitchToSeeker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCount = activeConversations.count { it.status == "ACTIVE" }
    val resolvedCount = activeConversations.count { it.status == "RESOLVED" }
    val bookedSlotsCount = myBookingSlots.count { it.isBooked }
    val ratingText = specialist?.let { String.format(java.util.Locale.US, "%.1f ⭐", it.rating) } ?: "5.0 ⭐"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("specialist_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with verified badge & switch to seeker
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SanadTheme.colors.accent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = null,
                            tint = SanadTheme.colors.onAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = specialist?.name ?: "بوابة الأخصائي المتطوع",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.text
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "أخصائي معتمد",
                                tint = SanadTheme.colors.online,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = specialist?.title?.ifBlank { "أخصائي نفسي متطوع معتمد" } ?: "أخصائي نفسي متطوع معتمد",
                            style = MaterialTheme.typography.bodySmall,
                            color = SanadTheme.colors.textSecondary
                        )
                    }
                }


            }
        }

        // 1. Online / Available Toggle Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("volunteer_status_toggle_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                border = BorderStroke(
                    1.dp,
                    if (isOnline) SanadTheme.colors.online else SanadTheme.colors.divider
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        if (isOnline) SanadTheme.colors.online else SanadTheme.colors.textSecondary,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isOnline) "أنت الآن: «أونلاين ومتاح»" else "أنت الآن: «غير متاح / استراحة»",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.text
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isOnline)
                                "تظهر الآن للمستفيدين في القائمة ويمكنهم مراسلتك فوراً."
                            else
                                "لن يتم تحويل طلبات شات جديدة إليك حتى تفعل التوفر.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SanadTheme.colors.textSecondary
                        )
                    }

                    Switch(
                        checked = isOnline,
                        onCheckedChange = onToggleOnline,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SanadTheme.colors.onAccent,
                            checkedTrackColor = SanadTheme.colors.online,
                            uncheckedThumbColor = SanadTheme.colors.textSecondary,
                            uncheckedTrackColor = SanadTheme.colors.divider
                        ),
                        modifier = Modifier.testTag("volunteer_online_switch")
                    )
                }
            }
        }

        // 2. Real Live Statistics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VolunteerStatCard(
                    title = "محادثات نشطة",
                    value = "$activeCount",
                    icon = Icons.Default.ChatBubble,
                    modifier = Modifier.weight(1f)
                )
                VolunteerStatCard(
                    title = "جلسات منجزة",
                    value = "${(specialist?.completedChats ?: 0L) + resolvedCount}",
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
                VolunteerStatCard(
                    title = "مواعيد محجوزة",
                    value = "$bookedSlotsCount",
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.weight(1f)
                )
                VolunteerStatCard(
                    title = "التقييم",
                    value = ratingText,
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Pending Requests Queue
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "طلبات الدعم الواردة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text
                    )
                    Text(
                        text = "طلبات المستفيدين بانتظار قبولك لبدء الاستماع والدعم",
                        style = MaterialTheme.typography.bodySmall,
                        color = SanadTheme.colors.textSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .background(SanadTheme.colors.surface, RoundedCornerShape(10.dp))
                        .border(1.dp, SanadTheme.colors.divider, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${pendingRequests.size} طلبات",
                        style = MaterialTheme.typography.labelSmall,
                        color = SanadTheme.colors.accent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (pendingRequests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                    border = BorderStroke(1.dp, SanadTheme.colors.divider)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد طلبات معلقة حالياً في قائمة الانتظار 🌿",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SanadTheme.colors.textSecondary
                        )
                    }
                }
            }
        } else {
            items(pendingRequests, key = { it.id }) { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("request_card_${req.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                    border = BorderStroke(
                        1.dp,
                        if (req.isUrgent) SanadTheme.colors.danger else SanadTheme.colors.divider
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = req.seekerName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.text
                            )
                            if (req.isUrgent) {
                                Box(
                                    modifier = Modifier
                                        .background(SanadTheme.colors.bg, RoundedCornerShape(8.dp))
                                        .border(1.dp, SanadTheme.colors.danger, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "عاجل 🚨",
                                        color = SanadTheme.colors.danger,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "الموضوع: ${req.topic}",
                            style = MaterialTheme.typography.labelMedium,
                            color = SanadTheme.colors.accent,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (req.lastMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "«${req.lastMessage}»",
                                style = MaterialTheme.typography.bodySmall,
                                color = SanadTheme.colors.textSecondary,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onAcceptRequest(req) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("accept_request_${req.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SanadTheme.colors.accent,
                                contentColor = SanadTheme.colors.onAccent
                            )
                        ) {
                            Text(
                                text = "قبول المحادثة وبدء الاستماع والدعم",
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.onAccent
                            )
                        }
                    }
                }
            }
        }

        // 4. Active Conversations Quick Access
        if (activeConversations.isNotEmpty()) {
            item {
                Text(
                    text = "المحادثات الجارية (${activeConversations.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.text
                )
            }

            items(activeConversations.take(4), key = { "dash_${it.id}" }) { conv ->
                Card(
                    onClick = { onOpenConversation(conv.id) },
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
                                text = conv.seekerName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.text
                            )
                            Text(
                                text = conv.topic,
                                style = MaterialTheme.typography.labelSmall,
                                color = SanadTheme.colors.accent
                            )
                            if (conv.lastMessage.isNotBlank()) {
                                Text(
                                    text = conv.lastMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SanadTheme.colors.textSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (conv.status == "RESOLVED") "منتهية" else "متابعة",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (conv.status == "RESOLVED") SanadTheme.colors.textSecondary else SanadTheme.colors.online,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 5. Schedule Quick Action + PFA Charter
        item {
            OutlinedButton(
                onClick = onNavigateToSchedule,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("dashboard_manage_schedule_btn"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SanadTheme.colors.accent)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = SanadTheme.colors.accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "إدارة جدول المواعيد المتاحة ($bookedSlotsCount محجوزة)",
                    color = SanadTheme.colors.accent,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                border = BorderStroke(1.dp, SanadTheme.colors.divider)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SanadTheme.colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ميثاق المتطوع والإسعاف النفسي الأولي (PFA)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.text
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• احترام الخصوصية: مشاعر المستفيدين أمانة أخلاقية.\n• الإصغاء الوجداني: تقبّل المشاعر دون مقاطعة أو أحكام.\n• الحالات الحرجة: وجّه المستفيد فوراً لخطوط الطوارئ المعتمدة عند وجود خطر إيذاء النفس.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SanadTheme.colors.textSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun VolunteerStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
        border = BorderStroke(1.dp, SanadTheme.colors.divider),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SanadTheme.colors.accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.text
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = SanadTheme.colors.textSecondary,
                maxLines = 1
            )
        }
    }
}
