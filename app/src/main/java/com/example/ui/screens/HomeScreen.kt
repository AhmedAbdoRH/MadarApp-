package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Specialist
import com.example.data.model.UserRole
import com.example.data.repository.SanadRepository
import com.example.ui.MainTab
import com.example.ui.SanadUiState
import com.example.ui.SanadViewModel
import com.example.ui.theme.SanadTheme

@Composable
fun HomeScreen(
    viewModel: SanadViewModel,
    uiState: SanadUiState,
    specialists: List<Specialist>,
    onNavigateToTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val baseSpecialists = if (uiState.onlineOnlyFilter) {
        specialists.filter { it.isOnline }
    } else {
        specialists
    }
    val filteredSpecialists = baseSpecialists.sortedWith(
        compareByDescending<Specialist> { it.isOnline }.thenBy { it.name }
    )

    val onlineCount by viewModel.onlineSpecialistsCount.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Role switcher & Top status
        item {
            RoleSwitchBar(
                currentRole = uiState.currentRole,
                isSupervisor = uiState.isSupervisor,
                onRoleChange = { viewModel.setRole(it) },
                onEmergencyClick = { viewModel.openUrgentScreen() }
            )
        }

        // 2. Compassionate Hero Banner
        item {
            HeroBanner(
                onlineCount = onlineCount,
                isLoading = uiState.isSpecialistsLoading,
                onUrgentClick = { viewModel.requestUrgentHelp() },
                isConnecting = uiState.isUrgentConnecting
            )
        }

        // 3. Urgent Psychological First Aid Card
        item {
            UrgentHelpCta(
                isConnecting = uiState.isUrgentConnecting,
                onClick = { viewModel.requestUrgentHelp() }
            )
        }

        // 4. Venting Topics Grid
        item {
            SectionHeader(
                title = "عن ماذا تريد الفضفضة اليوم؟",
                subtitle = "أو اختر موضوعاً لطلب محادثة. الرد حسب توافر المتطوعين."
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(SanadRepository.ventingTopics, key = { it.id }) { topic ->
                    VentingTopicCard(
                        title = topic.title,
                        description = topic.description,
                        onClick = { viewModel.startVentingByTopic(topic.title) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 5. Online Volunteer Specialists Header & Filter
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "الأخصائيون النفسيون المتطوعون",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.text
                            )
                            if (!uiState.isSpecialistsLoading) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(SanadTheme.colors.surface, RoundedCornerShape(12.dp))
                                        .border(1.dp, SanadTheme.colors.divider, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .testTag("specialists_online_count_badge")
                                ) {
                                    Text(
                                        text = "$onlineCount متاح الآن",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SanadTheme.colors.online,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "جميع المحادثات مجانية",
                            style = MaterialTheme.typography.bodySmall,
                            color = SanadTheme.colors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilterChip(
                        selected = uiState.onlineOnlyFilter,
                        onClick = { viewModel.toggleOnlineFilter() },
                        label = {
                            Text(
                                text = "المتاحون فقط",
                                color = if (uiState.onlineOnlyFilter) SanadTheme.colors.accent else SanadTheme.colors.textSecondary
                            )
                        },
                        leadingIcon = {
                            if (uiState.onlineOnlyFilter) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SanadTheme.colors.accent,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.FilterAlt,
                                    contentDescription = null,
                                    tint = SanadTheme.colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SanadTheme.colors.surface,
                            labelColor = SanadTheme.colors.textSecondary,
                            selectedContainerColor = SanadTheme.colors.surface,
                            selectedLabelColor = SanadTheme.colors.accent
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = uiState.onlineOnlyFilter,
                            borderColor = if (uiState.onlineOnlyFilter) SanadTheme.colors.accent else SanadTheme.colors.divider
                        ),
                        modifier = Modifier.testTag("filter_online_chip")
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // 6. Specialists List or Loading / Empty State
        if (uiState.isSpecialistsLoading && filteredSpecialists.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("specialists_loading_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                    border = BorderStroke(1.dp, SanadTheme.colors.divider)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = SanadTheme.colors.accent,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "جارٍ تحميل قائمة الأخصائيين المعتمدين...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SanadTheme.colors.textSecondary
                        )
                    }
                }
            }
        } else if (filteredSpecialists.isEmpty()) {
            item {
                EmptySpecialistsStateCard(
                    hasAnyApprovedSpecialists = specialists.isNotEmpty(),
                    onlineOnlyFilter = uiState.onlineOnlyFilter,
                    onShowAllClick = { viewModel.toggleOnlineFilter() },
                    onApplyAsSpecialist = { viewModel.setRole(UserRole.SPECIALIST) },
                    onOpenEmergency = { viewModel.openUrgentScreen() },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        } else {
            items(filteredSpecialists, key = { it.id }) { specialist ->
                SpecialistCard(
                    specialist = specialist,
                    onStartChat = { viewModel.startChatWithSpecialist(specialist, "فضفضة واستشارة نفسية") },
                    onBookSession = { onNavigateToTab(MainTab.BOOKING) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // 7. Quick Wellness Affordances
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = "أدوات مهدئة سريعة",
                subtitle = "تمارين وتفريغ نفسي يساعدك على استعادة التوازن"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickToolCard(
                    title = "تمرين التنفس \u200E4-7-8\u200E",
                    subtitle = "يهدئ نبضات القلب والهلع",
                    icon = Icons.Default.SelfImprovement,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(MainTab.WELLBEING) }
                )
                QuickToolCard(
                    title = "مساحة تسجيل المزاج",
                    subtitle = "فرّغ أفكارك يومياً",
                    icon = Icons.Default.Hearing,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(MainTab.WELLBEING) }
                )
            }
        }
    }
}

@Composable
fun RoleSwitchBar(
    currentRole: UserRole,
    isSupervisor: Boolean = false,
    onRoleChange: (UserRole) -> Unit,
    onEmergencyClick: () -> Unit
) {
    Surface(
        color = SanadTheme.colors.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Title & Tagline
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SanadTheme.colors.accent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = "سند",
                            tint = SanadTheme.colors.onAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "كلم سند",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.text
                        )
                        Text(
                            text = "دعم نفسي وفضفضة مجانية",
                            style = MaterialTheme.typography.labelSmall,
                            color = SanadTheme.colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Role toggle & Single Urgent Help button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Urgent Help Header Button (Single button, no red phone)
                    com.example.ui.components.UrgentHelpHeaderButton(
                        onClick = onEmergencyClick
                    )
                }
            }

            if (isSupervisor) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SanadTheme.colors.bg)
                        .border(BorderStroke(0.5.dp, SanadTheme.colors.divider))
                        .clickable { onRoleChange(UserRole.SUPERVISOR) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("supervisor_portal_banner_btn"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🛡️ وضع المشرف متاح: مراجعة طلبات الاعتماد والمؤهلات",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.accent
                    )
                    Text(
                        text = "دخول ◀",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text
                    )
                }
            }
        }
    }
}

@Composable
fun HeroBanner(
    onlineCount: Int,
    isLoading: Boolean,
    onUrgentClick: () -> Unit,
    isConnecting: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
        border = BorderStroke(1.dp, SanadTheme.colors.divider),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.sanad_hero_1794126502005),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )

            // Enhanced Dark gradient scrim for crystal clear readability
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                SanadTheme.colors.bg.copy(alpha = 0.5f),
                                SanadTheme.colors.bg.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(18.dp)
            ) {
                if (!isLoading && onlineCount > 0) {
                    Box(
                        modifier = Modifier
                            .background(SanadTheme.colors.surface.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                            .border(1.dp, SanadTheme.colors.divider, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("hero_availability_badge")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(SanadTheme.colors.online, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "متطوعون متاحون الآن: $onlineCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = SanadTheme.colors.text,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = "مشاعرك مهمة.. وتستحق من يصغي إليها",
                    style = MaterialTheme.typography.titleLarge,
                    color = SanadTheme.colors.text,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ابدأ بطلب دعم، أو اختر موضوعاً أو أخصائياً من القائمة. الرد حسب التوافر.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SanadTheme.colors.textSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun UrgentHelpCta(
    isConnecting: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(enabled = !isConnecting, onClick = onClick)
            .testTag("urgent_help_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
        border = BorderStroke(1.dp, SanadTheme.colors.danger)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(SanadTheme.colors.danger, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "عاجل",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = if (isConnecting) "جارٍ ربطك بمتطوع متاح..." else "طلب دعم نفسي",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.danger
                    )
                    Text(
                        text = if (isConnecting) "مدة الانتظار حسب توافر المتطوعين." else "نبحث عن متطوع للرد. الخدمة ليست بديلاً للطوارئ.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SanadTheme.colors.textSecondary
                    )
                }
            }

            Button(
                onClick = onClick,
                enabled = !isConnecting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SanadTheme.colors.accent,
                    contentColor = SanadTheme.colors.onAccent
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("urgent_chat_now_button")
            ) {
                Text(
                    text = "اطلب الدعم",
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.onAccent
                )
            }
        }
    }
}

@Composable
fun VentingTopicCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .clickable(onClick = onClick)
            .testTag("topic_card_${title.take(6)}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
        border = BorderStroke(1.dp, SanadTheme.colors.divider),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.text
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SanadTheme.colors.textSecondary,
                maxLines = 2,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun SpecialistCard(
    specialist: Specialist,
    onStartChat: () -> Unit,
    onBookSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("specialist_card_${specialist.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
        border = BorderStroke(1.dp, SanadTheme.colors.divider),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Avatar, Name, Title, Online status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with online dot indicator
                Box {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(specialist.avatarBgHex), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = specialist.avatarInitials,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (specialist.isOnline) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(SanadTheme.colors.online, CircleShape)
                                .border(2.dp, SanadTheme.colors.surface, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = specialist.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.text
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "أخصائي معتمد",
                            tint = SanadTheme.colors.accent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = specialist.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = SanadTheme.colors.textSecondary
                    )
                }

                // Online / Status Badge
                if (specialist.isOnline) {
                    Box(
                        modifier = Modifier
                            .background(SanadTheme.colors.surface, RoundedCornerShape(12.dp))
                            .border(1.dp, SanadTheme.colors.online, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "متصل 🟢",
                            style = MaterialTheme.typography.labelSmall,
                            color = SanadTheme.colors.online,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(SanadTheme.colors.surface, RoundedCornerShape(12.dp))
                            .border(1.dp, SanadTheme.colors.divider, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "مشغول",
                            style = MaterialTheme.typography.labelSmall,
                            color = SanadTheme.colors.busy,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bio
            Text(
                text = specialist.bio,
                style = MaterialTheme.typography.bodySmall,
                color = SanadTheme.colors.textSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                specialist.tags.take(3).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(SanadTheme.colors.surface, RoundedCornerShape(8.dp))
                            .border(1.dp, SanadTheme.colors.divider, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = SanadTheme.colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartChat,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_with_${specialist.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SanadTheme.colors.accent,
                        contentColor = SanadTheme.colors.onAccent
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = null,
                        tint = SanadTheme.colors.onAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (specialist.isOnline) "طلب محادثة مجانية" else "إرسال رسالة",
                        color = SanadTheme.colors.onAccent,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onBookSession,
                    modifier = Modifier.testTag("book_with_${specialist.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SanadTheme.colors.text
                    ),
                    border = BorderStroke(1.5.dp, SanadTheme.colors.textSecondary)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = SanadTheme.colors.text,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "حجز جلسة",
                        color = SanadTheme.colors.text
                    )
                }
            }
        }
    }
}

@Composable
fun QuickToolCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("tool_card_${title.take(5)}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
        border = BorderStroke(1.dp, SanadTheme.colors.divider)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SanadTheme.colors.accent,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.text
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SanadTheme.colors.textSecondary
            )
        }
    }
}

@Composable
fun EmptySpecialistsStateCard(
    hasAnyApprovedSpecialists: Boolean,
    onlineOnlyFilter: Boolean,
    onShowAllClick: () -> Unit,
    onApplyAsSpecialist: () -> Unit,
    onOpenEmergency: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("empty_specialists_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
        border = BorderStroke(1.dp, SanadTheme.colors.divider)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(SanadTheme.colors.bg, CircleShape)
                    .border(1.dp, SanadTheme.colors.divider, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VolunteerActivism,
                    contentDescription = null,
                    tint = SanadTheme.colors.accent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (hasAnyApprovedSpecialists && onlineOnlyFilter) {
                    "لا يوجد أخصائيون متصلون الآن"
                } else {
                    "لا يوجد أخصائيون معتمدون حالياً"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.text,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (hasAnyApprovedSpecialists && onlineOnlyFilter) {
                    "يمكنك إلغاء تصفية «المتاحون فقط» لعرض جميع الأخصائيين المعتمدين وإرسال رسالة أو حجز موعد."
                } else {
                    "تظهر هنا حسابات الأخصائيين الحقيقيين فقط بعد مراجعة مؤهلاتهم واعتمادهم من المشرف."
                },
                style = MaterialTheme.typography.bodySmall,
                color = SanadTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (hasAnyApprovedSpecialists && onlineOnlyFilter) {
                Button(
                    onClick = onShowAllClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SanadTheme.colors.accent,
                        contentColor = SanadTheme.colors.onAccent
                    ),
                    modifier = Modifier.testTag("show_all_specialists_button")
                ) {
                    Text(
                        text = "عرض كل الأخصائيين",
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.onAccent
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onApplyAsSpecialist,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_as_specialist_empty_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SanadTheme.colors.accent,
                            contentColor = SanadTheme.colors.onAccent
                        )
                    ) {
                        Text(
                            text = "التقديم كأخصائي",
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.onAccent
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenEmergency,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("emergency_empty_specialists_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, SanadTheme.colors.divider)
                    ) {
                        Text(
                            text = "أرقام المساعدة",
                            fontWeight = FontWeight.SemiBold,
                            color = SanadTheme.colors.text
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SanadTheme.colors.text
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = SanadTheme.colors.textSecondary
        )
    }
}
