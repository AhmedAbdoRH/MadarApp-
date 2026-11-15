package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Conversation
import com.example.data.model.Specialist
import com.example.ui.theme.SanadTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatsListScreen(
    conversations: List<Conversation>,
    specialists: List<Specialist>,
    onSelectConversation: (String) -> Unit,
    onStartNewChat: () -> Unit,
    onEmergencyClick: () -> Unit = {},
    isSpecialistMode: Boolean = false,
    notificationsEnabled: Boolean = true,
    hasNotificationPermission: Boolean = true,
    onRequestNotificationPermission: () -> Unit = {},
    onToggleNotifications: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.forLanguageTag("ar")) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("chats_list_screen")
    ) {
        // Screen Header with Urgent Help Button
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
                    text = if (isSpecialistMode) "محادثات المستفيدين" else "محادثات الدعم النفسي",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.text,
                    softWrap = true,
                    maxLines = 2
                )
                Text(
                    text = if (isSpecialistMode) "الحالات الجارية والطلبات المقبولة" else "مساحتك الخاصة للتواصل مع المتطوعين",
                    style = MaterialTheme.typography.bodySmall,
                    color = SanadTheme.colors.textSecondary,
                    softWrap = true
                )
            }
            com.example.ui.components.UrgentHelpHeaderButton(onClick = onEmergencyClick)
        }

        // Message Notification Status & Control Strip
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("chats_notification_strip"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
            border = BorderStroke(
                1.dp,
                if (!hasNotificationPermission) SanadTheme.colors.accent else SanadTheme.colors.divider
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (hasNotificationPermission && notificationsEnabled) {
                            Icons.Default.NotificationsActive
                        } else {
                            Icons.Default.NotificationsOff
                        },
                        contentDescription = null,
                        tint = if (hasNotificationPermission && notificationsEnabled) {
                            SanadTheme.colors.accent
                        } else {
                            SanadTheme.colors.textSecondary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = when {
                                !hasNotificationPermission -> "تفعيل إشعارات الرسائل"
                                notificationsEnabled -> "إشعارات الرسائل مفعّلة"
                                else -> "إشعارات الرسائل متوقفة"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.text
                        )
                        Text(
                            text = when {
                                !hasNotificationPermission -> "اسمح بالإشعارات لتنبيهك فور وصول أي رسالة"
                                notificationsEnabled -> "ستصلك تنبيهات فورية عند وصول رسائل جديدة"
                                else -> "قم بتشغيلها لتلقي تنبيهات الرسائل الواردة"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = SanadTheme.colors.textSecondary
                        )
                    }
                }

                if (!hasNotificationPermission) {
                    TextButton(
                        onClick = onRequestNotificationPermission,
                        modifier = Modifier.testTag("enable_notifications_permission_btn")
                    ) {
                        Text(
                            text = "تفعيل",
                            color = SanadTheme.colors.accent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onToggleNotifications,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SanadTheme.colors.onAccent,
                            checkedTrackColor = SanadTheme.colors.accent,
                            uncheckedThumbColor = SanadTheme.colors.textSecondary,
                            uncheckedTrackColor = SanadTheme.colors.bg
                        ),
                        modifier = Modifier.testTag("chats_notification_toggle")
                    )
                }
            }
        }

        if (conversations.isEmpty()) {
            EmptyChatsView(
                isSpecialistMode = isSpecialistMode,
                onStartNewChat = onStartNewChat
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(conversations, key = { it.id }) { conv ->
                    val specialist = specialists.find { it.id == conv.specialistId }
                    ConversationItemCard(
                        conversation = conv,
                        specialist = specialist,
                        isSpecialistMode = isSpecialistMode,
                        formattedTime = timeFormatter.format(conv.lastMessageTime?.toDate() ?: Date()),
                        onClick = { onSelectConversation(conv.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItemCard(
    conversation: Conversation,
    specialist: Specialist?,
    formattedTime: String,
    isSpecialistMode: Boolean = false,
    onClick: () -> Unit
) {
    val displayTitle = if (isSpecialistMode) conversation.seekerName.ifBlank { "مستفيد" } else conversation.specialistName
    val fallbackInitials = remember(conversation.specialistName) {
        val parts = conversation.specialistName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].first()}.${parts[1].first()}"
            parts.size == 1 -> parts[0].take(2)
            else -> "أ"
        }
    }
    val avatarInitials = if (isSpecialistMode) "م.د" else (specialist?.avatarInitials?.ifBlank { fallbackInitials } ?: fallbackInitials)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("conversation_item_${conversation.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
        border = BorderStroke(
            1.dp,
            if (isSpecialistMode && conversation.isUrgent && conversation.status == "ACTIVE")
                SanadTheme.colors.danger else SanadTheme.colors.divider
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color(specialist?.avatarBgHex ?: 0xFF134E4A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarInitials,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.text
                        )
                        if (!isSpecialistMode) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "معتمد",
                                tint = SanadTheme.colors.accent,
                                modifier = Modifier.size(14.dp)
                            )
                        } else if (!conversation.acceptedBySpecialist && conversation.status == "ACTIVE") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(SanadTheme.colors.bg, RoundedCornerShape(6.dp))
                                    .border(1.dp, SanadTheme.colors.accent, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "جديد",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SanadTheme.colors.accent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = SanadTheme.colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = conversation.topic,
                    style = MaterialTheme.typography.labelSmall,
                    color = SanadTheme.colors.accent,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = conversation.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = SanadTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (conversation.unreadCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(SanadTheme.colors.accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${conversation.unreadCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SanadTheme.colors.onAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyChatsView(
    isSpecialistMode: Boolean = false,
    onStartNewChat: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(SanadTheme.colors.surface, CircleShape)
                .border(1.dp, SanadTheme.colors.divider, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                tint = SanadTheme.colors.accent,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "لا توجد محادثات نشطة حالياً",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SanadTheme.colors.text
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isSpecialistMode)
                "ستظهر هنا محادثاتك مع المستفيدين فور قبول الطلبات من لوحة الأخصائي."
            else
                "الأخصائيون المتطوعون بانتظار الاستماع إليك بكل محبة. خذ خطوة وابدأ محادثتك المجانية الآن.",
            style = MaterialTheme.typography.bodySmall,
            color = SanadTheme.colors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onStartNewChat,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SanadTheme.colors.accent,
                contentColor = SanadTheme.colors.onAccent
            ),
            modifier = Modifier.testTag("empty_start_chat_button")
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                tint = SanadTheme.colors.onAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSpecialistMode) "العودة للوحة الأخصائي" else "بدء محادثة جديدة الآن",
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.onAccent
            )
        }
    }
}
