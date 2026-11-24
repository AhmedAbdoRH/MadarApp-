package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.Specialist
import com.example.ui.components.UrgentHelpHeaderButton
import com.example.ui.theme.SanadTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatDetailScreen(
    conversation: Conversation,
    specialist: Specialist?,
    messages: List<ChatMessage>,
    isTyping: Boolean,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit,
    onEmergencyClick: () -> Unit,
    onPrivacyClick: () -> Unit = {},
    onEndSession: (String) -> Unit = {},
    onReportSpecialist: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onBlockSpecialist: (String) -> Unit = {},
    onRecordSessionMood: (String, String, Int, String) -> Unit = { _, _, _, _ -> },
    onReturnHome: () -> Unit = {},
    isSpecialistViewer: Boolean = false,
    currentUserId: String = "",
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val isSessionResolved = conversation.status == "RESOLVED"
    val showScrollToBottom by remember {
        derivedStateOf { listState.canScrollForward }
    }

    // Dialog & Bottom Sheet visibility states
    var showMenuSheet by remember { mutableStateOf(false) }
    var showEndConfirmDialog by remember { mutableStateOf(false) }
    var showBlockConfirmDialog by remember { mutableStateOf(false) }
    var showReportSheet by remember { mutableStateOf(false) }
    var showMoodSheet by remember { mutableStateOf(false) }

    // Report state
    var selectedReportReason by remember { mutableStateOf<String?>(null) }
    var reportDetails by remember { mutableStateOf("") }
    var reportSubmittedMessage by remember { mutableStateOf<String?>(null) }
    var isUrgentRiskReport by remember { mutableStateOf(false) }

    // Mood selection state
    var selectedMoodOption by remember { mutableStateOf<Triple<Int, String, String>?>(null) }
    var moodSubmitted by remember { mutableStateOf(false) }

    val reportReasons = remember {
        listOf(
            "كلام مش مناسب",
            "إساءة أو تنمر",
            "طلب بيانات شخصية أو تواصل برّه التطبيق",
            "فيه خطر على حد",
            "سبب تاني"
        )
    }

    val moodOptions = remember {
        listOf(
            Triple(5, "رائع وممتن", "🌟"),
            Triple(4, "مرتاح وهادئ", "🌿"),
            Triple(3, "عادي ومستقر", "😐"),
            Triple(2, "قلق ومتوتر", "😟"),
            Triple(1, "منهك وحزين", "🌧️")
        )
    }

    val participantName = remember(conversation, isSpecialistViewer) {
        if (isSpecialistViewer) {
            conversation.seekerName.ifBlank { "مستفيد" }
        } else {
            conversation.specialistName.ifBlank { "أخصائي معتمد" }
        }
    }

    val participantInitials = remember(participantName, specialist, isSpecialistViewer) {
        if (isSpecialistViewer) {
            "م"
        } else {
            specialist?.avatarInitials?.takeIf { it.isNotBlank() }
                ?: deriveInitials(participantName)
        }
    }

    val avatarColor = if (specialist != null) Color(specialist.avatarBgHex) else SanadTheme.colors.accentStrong

    // Auto-scroll when new messages arrive
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SanadTheme.colors.bg)
            .testTag("chat_detail_screen")
    ) {
        // 1. Elevated Custom Header
        ChatDetailHeader(
            participantName = participantName,
            participantInitials = participantInitials,
            avatarColor = avatarColor,
            specialistTitle = specialist?.title ?: conversation.specialistTitle,
            conversationTopic = conversation.topic,
            isSpecialistViewer = isSpecialistViewer,
            isSessionResolved = isSessionResolved,
            acceptedBySpecialist = conversation.acceptedBySpecialist,
            isSpecialistOnline = specialist?.isOnline == true,
            isTyping = isTyping,
            onBack = onBack,
            onEmergencyClick = onEmergencyClick,
            onMoreClick = { showMenuSheet = true }
        )

        // 2. Context & Privacy Strip
        ChatContextPrivacyBanner(
            topic = conversation.topic,
            isUrgent = conversation.isUrgent,
            acceptedBySpecialist = conversation.acceptedBySpecialist,
            isSessionResolved = isSessionResolved,
            isSpecialistViewer = isSpecialistViewer,
            onPrivacyClick = onPrivacyClick
        )

        // 3. Messages Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SanadTheme.colors.bg,
                            SanadTheme.colors.surface.copy(alpha = 0.22f),
                            SanadTheme.colors.bg
                        )
                    )
                )
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (messages.isEmpty()) {
                    item(key = "chat_opening_card") {
                        ChatOpeningWelcomeCard(
                            participantName = participantName,
                            topic = conversation.topic,
                            acceptedBySpecialist = conversation.acceptedBySpecialist,
                            isSpecialistViewer = isSpecialistViewer
                        )
                    }
                } else {
                    itemsIndexed(
                        items = messages,
                        key = { index, msg -> msg.id.ifBlank { "${msg.createdAt?.nanoseconds ?: index}_$index" } }
                    ) { index, msg ->
                        val isSystemMessage = msg.senderId == "system" || msg.text == "الجلسة خلصت"
                        if (isSystemMessage) {
                            SessionEndedDivider()
                        } else {
                            val prev = messages.getOrNull(index - 1)
                            val next = messages.getOrNull(index + 1)
                            val prevIsRegular = prev != null && prev.senderId != "system" && prev.text != "الجلسة خلصت"
                            val nextIsRegular = next != null && next.senderId != "system" && next.text != "الجلسة خلصت"

                            val isIncoming = resolveIsIncomingMessage(
                                message = msg,
                                currentUserId = currentUserId,
                                conversation = conversation,
                                isSpecialistViewer = isSpecialistViewer
                            )
                            val prevIncoming = prev?.let {
                                resolveIsIncomingMessage(
                                    message = it,
                                    currentUserId = currentUserId,
                                    conversation = conversation,
                                    isSpecialistViewer = isSpecialistViewer
                                )
                            }
                            val nextIncoming = next?.let {
                                resolveIsIncomingMessage(
                                    message = it,
                                    currentUserId = currentUserId,
                                    conversation = conversation,
                                    isSpecialistViewer = isSpecialistViewer
                                )
                            }

                            val sameSenderAsPrev = prevIsRegular && prevIncoming == isIncoming
                            val sameSenderAsNext = nextIsRegular && nextIncoming == isIncoming

                            if (!sameSenderAsPrev && index > 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            MessageBubble(
                                message = msg,
                                participantInitials = participantInitials,
                                participantName = participantName,
                                avatarColor = avatarColor,
                                isIncomingOverride = isIncoming,
                                isSpecialistViewer = isSpecialistViewer,
                                showSenderName = !sameSenderAsPrev,
                                showAvatar = !sameSenderAsNext,
                                showTimestamp = !sameSenderAsNext,
                                isFirstInGroup = !sameSenderAsPrev,
                                isLastInGroup = !sameSenderAsNext
                            )
                        }
                    }
                }

                if (isTyping && !isSessionResolved && !isSpecialistViewer) {
                    item(key = "typing_indicator") {
                        Spacer(modifier = Modifier.height(8.dp))
                        TypingIndicatorBubble(
                            specialistName = participantName,
                            avatarInitials = participantInitials,
                            avatarColor = avatarColor
                        )
                    }
                }
            }

            // Floating Scroll-to-Bottom Pill
            androidx.compose.animation.AnimatedVisibility(
                visible = showScrollToBottom && messages.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        scope.launch {
                            if (messages.isNotEmpty()) {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    },
                    containerColor = SanadTheme.colors.surface,
                    contentColor = SanadTheme.colors.accent,
                    shape = CircleShape,
                    modifier = Modifier
                        .border(1.dp, SanadTheme.colors.divider, CircleShape)
                        .testTag("scroll_to_bottom_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "الانتقال لآخر رسالة"
                    )
                }
            }
        }

        // 4. Modern Composer Bar or Session Resolved Banner
        if (isSessionResolved) {
            SessionResolvedFooter(
                isSpecialistViewer = isSpecialistViewer,
                onReturnAction = if (isSpecialistViewer) onBack else onReturnHome
            )
        } else {
            ChatComposerBar(
                inputText = inputText,
                isSpecialistViewer = isSpecialistViewer,
                onInputChange = { inputText = it },
                onSend = {
                    val trimmed = inputText.trim()
                    if (trimmed.isNotEmpty()) {
                        inputText = ""
                        onSendMessage(trimmed)
                    }
                }
            )
        }
    }

    // --- Safety Modals & Bottom Sheets ---
    if (showMenuSheet) {
        ChatMenuBottomSheet(
            isSpecialistViewer = isSpecialistViewer,
            isSessionResolved = isSessionResolved,
            onDismiss = { showMenuSheet = false },
            onEndSessionClick = {
                showMenuSheet = false
                showEndConfirmDialog = true
            },
            onReportClick = {
                showMenuSheet = false
                selectedReportReason = null
                reportDetails = ""
                reportSubmittedMessage = null
                isUrgentRiskReport = false
                showReportSheet = true
            },
            onBlockClick = {
                showMenuSheet = false
                showBlockConfirmDialog = true
            }
        )
    }

    if (showEndConfirmDialog) {
        EndSessionConfirmDialog(
            onConfirm = {
                showEndConfirmDialog = false
                onEndSession(conversation.id)
                if (isSpecialistViewer) {
                    onBack()
                } else {
                    showMoodSheet = true
                }
            },
            onDismiss = { showEndConfirmDialog = false }
        )
    }

    if (showBlockConfirmDialog) {
        BlockSpecialistConfirmDialog(
            onConfirm = {
                showBlockConfirmDialog = false
                onBlockSpecialist(conversation.specialistId)
            },
            onDismiss = { showBlockConfirmDialog = false }
        )
    }

    if (showReportSheet) {
        ReportSpecialistBottomSheet(
            reportReasons = reportReasons,
            selectedReason = selectedReportReason,
            reportDetails = reportDetails,
            submittedMessage = reportSubmittedMessage,
            isUrgentRisk = isUrgentRiskReport,
            onSelectReason = { selectedReportReason = it },
            onDetailsChange = { reportDetails = it },
            onSubmit = {
                val reason = selectedReportReason ?: return@ReportSpecialistBottomSheet
                onReportSpecialist(conversation.id, conversation.specialistId, reason, reportDetails)
                isUrgentRiskReport = (reason == "فيه خطر على حد")
                reportSubmittedMessage = "وصل البلاغ، شكرًا."
            },
            onUrgentHelpClick = {
                showReportSheet = false
                onEmergencyClick()
            },
            onDismiss = { showReportSheet = false }
        )
    }

    if (showMoodSheet) {
        PostSessionMoodBottomSheet(
            moodOptions = moodOptions,
            selectedMoodOption = selectedMoodOption,
            moodSubmitted = moodSubmitted,
            onSelectMood = { option ->
                selectedMoodOption = option
                onRecordSessionMood(conversation.id, option.second, option.first, option.third)
                moodSubmitted = true
            },
            onSkip = {
                showMoodSheet = false
                onReturnHome()
            },
            onFinish = {
                showMoodSheet = false
                onReturnHome()
            },
            onDismiss = {
                showMoodSheet = false
                onReturnHome()
            }
        )
    }
}

@Composable
private fun ChatDetailHeader(
    participantName: String,
    participantInitials: String,
    avatarColor: Color,
    specialistTitle: String,
    conversationTopic: String,
    isSpecialistViewer: Boolean,
    isSessionResolved: Boolean,
    acceptedBySpecialist: Boolean,
    isSpecialistOnline: Boolean,
    isTyping: Boolean,
    onBack: () -> Unit,
    onEmergencyClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val statusDotColor = when {
        isSessionResolved -> SanadTheme.colors.textSecondary
        !acceptedBySpecialist -> SanadTheme.colors.busy
        isSpecialistOnline || isSpecialistViewer -> SanadTheme.colors.online
        else -> SanadTheme.colors.busy
    }

    val statusText = when {
        isSessionResolved -> "الجلسة منتهية"
        isTyping && !isSpecialistViewer -> "يكتب رداً مهدئاً..."
        !acceptedBySpecialist && !isSpecialistViewer -> "في انتظار انضمام الأخصائي"
        isSpecialistViewer -> conversationTopic.ifBlank { "جلسة دعم نفسي" }
        isSpecialistOnline -> "متصل ومستعد للاستماع"
        else -> specialistTitle.ifBlank { "أخصائي معتمد" }
    }

    Surface(
        color = SanadTheme.colors.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 64.dp)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("chat_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = SanadTheme.colors.text
                    )
                }

                // Avatar with Status Ring & Indicator
                Box(modifier = Modifier.padding(end = 10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(avatarColor)
                            .border(1.5.dp, statusDotColor.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = participantInitials,
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(statusDotColor, CircleShape)
                            .border(2.dp, SanadTheme.colors.surface, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                // Name + Status Subtitle
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = participantName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (!isSpecialistViewer) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "أخصائي معتمد",
                                tint = SanadTheme.colors.accent,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isTyping && !isSpecialistViewer) {
                            SanadTheme.colors.accent
                        } else if (!acceptedBySpecialist && !isSessionResolved) {
                            SanadTheme.colors.busy
                        } else {
                            SanadTheme.colors.textSecondary
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                UrgentHelpHeaderButton(
                    onClick = onEmergencyClick,
                    modifier = Modifier.padding(end = 2.dp)
                )

                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("chat_more_menu_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "المزيد",
                        tint = SanadTheme.colors.text,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            HorizontalDivider(thickness = 0.75.dp, color = SanadTheme.colors.divider)
        }
    }
}

@Composable
private fun ChatContextPrivacyBanner(
    topic: String,
    isUrgent: Boolean,
    acceptedBySpecialist: Boolean,
    isSessionResolved: Boolean,
    isSpecialistViewer: Boolean,
    onPrivacyClick: () -> Unit
) {
    val accentColor = SanadTheme.colors.accent
    val privacyAnnotatedText = remember(accentColor) {
        buildAnnotatedString {
            append("سياسة الخصوصية لم تكتمل بعد. راجع الحالة الحالية في ")
            withLink(
                LinkAnnotation.Clickable(
                    tag = "privacy",
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        )
                    ),
                    linkInteractionListener = { onPrivacyClick() }
                )
            ) {
                append("سياسة الخصوصية")
            }
            append(".")
        }
    }

    Surface(
        color = SanadTheme.colors.surface.copy(alpha = 0.65f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (topic.isNotBlank() || (!acceptedBySpecialist && !isSessionResolved && !isSpecialistViewer)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (topic.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isUrgent) SanadTheme.colors.danger.copy(alpha = 0.14f)
                                        else SanadTheme.colors.bg
                                    )
                                    .border(
                                        1.dp,
                                        if (isUrgent) SanadTheme.colors.danger.copy(alpha = 0.5f)
                                        else SanadTheme.colors.divider,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = topic,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isUrgent) SanadTheme.colors.danger else SanadTheme.colors.text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (!acceptedBySpecialist && !isSessionResolved && !isSpecialistViewer) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = SanadTheme.colors.busy,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "طلب جديد",
                                style = MaterialTheme.typography.labelSmall,
                                color = SanadTheme.colors.busy,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SanadTheme.colors.accent,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = privacyAnnotatedText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SanadTheme.colors.textSecondary
                    ),
                    modifier = Modifier.testTag("chat_privacy_policy_link")
                )
            }
            HorizontalDivider(thickness = 0.75.dp, color = SanadTheme.colors.divider)
        }
    }
}

@Composable
private fun ChatOpeningWelcomeCard(
    participantName: String,
    topic: String,
    acceptedBySpecialist: Boolean,
    isSpecialistViewer: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 8.dp),
        shape = RoundedCornerShape(22.dp),
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
                    .size(52.dp)
                    .background(SanadTheme.colors.bg, CircleShape)
                    .border(1.dp, SanadTheme.colors.accent.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VolunteerActivism,
                    contentDescription = null,
                    tint = SanadTheme.colors.accent,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = when {
                    isSpecialistViewer -> "مساحة دعم جديدة حول: ${topic.ifBlank { "فضفضة عامة" }}"
                    !acceptedBySpecialist -> "تم فتح المحادثة مع $participantName"
                    else -> "محادثتك مع $participantName"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.text,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when {
                    isSpecialistViewer -> "ابدأ بترحيب دافئ ومتفهم للاستماع للمستفيد."
                    !acceptedBySpecialist -> "يمكنك كتابة ما تشعر به الآن بحرية؛ سيصلك الرد من الأخصائي بمجرد قبوله للطلب."
                    else -> "خذ وقتك تماماً وابدأ بالحديث عما يشغل بالك دون أي أحكام."
                },
                style = MaterialTheme.typography.bodySmall,
                color = SanadTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun SessionEndedDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = SanadTheme.colors.divider
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .background(SanadTheme.colors.surface, RoundedCornerShape(12.dp))
                .border(1.dp, SanadTheme.colors.divider, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "الجلسة خلصت",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = SanadTheme.colors.textSecondary
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = SanadTheme.colors.divider
        )
    }
}

internal fun resolveIsIncomingMessage(
    message: ChatMessage,
    currentUserId: String,
    conversation: Conversation,
    isSpecialistViewer: Boolean
): Boolean {
    if (currentUserId.isNotBlank() &&
        message.senderId.isNotBlank() &&
        message.senderId != "system" &&
        conversation.seekerId.isNotBlank() &&
        conversation.specialistId.isNotBlank() &&
        conversation.seekerId != conversation.specialistId
    ) {
        return message.senderId != currentUserId
    }
    return if (isSpecialistViewer) !message.isFromSpecialist else message.isFromSpecialist
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    participantInitials: String,
    participantName: String,
    avatarColor: Color,
    isIncomingOverride: Boolean? = null,
    isSpecialistViewer: Boolean = false,
    showSenderName: Boolean = true,
    showAvatar: Boolean = true,
    showTimestamp: Boolean = true,
    isFirstInGroup: Boolean = true,
    isLastInGroup: Boolean = true
) {
    val isIncoming = isIncomingOverride
        ?: if (isSpecialistViewer) !message.isFromSpecialist else message.isFromSpecialist
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.forLanguageTag("ar")) }
    val effectiveMillis = message.createdAt?.toDate()?.time ?: message.timestamp
    val formattedTime = remember(effectiveMillis) { timeFormatter.format(Date(effectiveMillis)) }

    val bubbleShape = if (isIncoming) {
        RoundedCornerShape(
            topStart = if (isFirstInGroup) 18.dp else 8.dp,
            topEnd = 18.dp,
            bottomStart = if (isLastInGroup) 4.dp else 8.dp,
            bottomEnd = 18.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 18.dp,
            topEnd = if (isFirstInGroup) 18.dp else 8.dp,
            bottomStart = 18.dp,
            bottomEnd = if (isLastInGroup) 4.dp else 8.dp
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isIncoming) Arrangement.Start else Arrangement.End
    ) {
        if (isIncoming) {
            if (showAvatar) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(avatarColor, CircleShape)
                        .align(Alignment.Bottom),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = participantInitials,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(30.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 286.dp),
            horizontalAlignment = if (isIncoming) Alignment.Start else Alignment.End
        ) {
            if (isIncoming && showSenderName) {
                Text(
                    text = participantName,
                    style = MaterialTheme.typography.labelSmall,
                    color = SanadTheme.colors.textSecondary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 3.dp)
                )
            }

            Surface(
                shape = bubbleShape,
                color = if (isIncoming) SanadTheme.colors.surface else SanadTheme.colors.bubbleUser,
                border = if (isIncoming) BorderStroke(1.dp, SanadTheme.colors.divider) else null,
                tonalElevation = if (isIncoming) 1.dp else 0.dp
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SanadTheme.colors.text,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }

            if (showTimestamp) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = SanadTheme.colors.textSecondary,
                        fontSize = 10.sp
                    )
                    if (!isIncoming) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "تم التسليم",
                            tint = SanadTheme.colors.accent,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble(
    specialistName: String,
    avatarInitials: String = "أ",
    avatarColor: Color = SanadTheme.colors.accentStrong
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typingDots")
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, delayMillis = 160, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, delayMillis = 320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("typing_indicator"),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(avatarColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarInitials,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp),
            color = SanadTheme.colors.surface,
            border = BorderStroke(1.dp, SanadTheme.colors.divider)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$specialistName يكتب الآن",
                    style = MaterialTheme.typography.labelSmall,
                    color = SanadTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(dot1Alpha, dot2Alpha, dot3Alpha).forEach { alphaVal ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .alpha(alphaVal)
                                .background(SanadTheme.colors.accent, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatComposerBar(
    inputText: String,
    isSpecialistViewer: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val canSend = inputText.isNotBlank()
    val sendBgColor by animateColorAsState(
        targetValue = if (canSend) SanadTheme.colors.accent else SanadTheme.colors.bg,
        animationSpec = tween(200),
        label = "sendBgColor"
    )
    val sendBorderColor by animateColorAsState(
        targetValue = if (canSend) SanadTheme.colors.accent else SanadTheme.colors.divider,
        animationSpec = tween(200),
        label = "sendBorderColor"
    )
    val sendIconTint by animateColorAsState(
        targetValue = if (canSend) SanadTheme.colors.onAccent else SanadTheme.colors.textSecondary,
        animationSpec = tween(200),
        label = "sendIconTint"
    )
    val sendScale by animateFloatAsState(
        targetValue = if (canSend) 1f else 0.94f,
        animationSpec = tween(180),
        label = "sendScale"
    )

    var isFocused by remember { mutableStateOf(false) }
    val inputBorderColor by animateColorAsState(
        targetValue = if (isFocused) SanadTheme.colors.accent else SanadTheme.colors.divider,
        animationSpec = tween(180),
        label = "inputBorderColor"
    )

    Surface(
        color = SanadTheme.colors.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
        ) {
            HorizontalDivider(thickness = 0.75.dp, color = SanadTheme.colors.divider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .onFocusChanged { isFocused = it.isFocused }
                        .testTag("chat_input_field"),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = SanadTheme.colors.text
                    ),
                    cursorBrush = SolidColor(SanadTheme.colors.accent),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { if (canSend) onSend() }
                    ),
                    maxLines = 4,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(SanadTheme.colors.bg)
                                .border(1.dp, inputBorderColor, RoundedCornerShape(24.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (inputText.isEmpty()) {
                                Text(
                                    text = if (isSpecialistViewer) {
                                        "اكتب ردك الداعم للمستفيد..."
                                    } else {
                                        "اكتب ما تشعر به هنا بكل حرية..."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SanadTheme.colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .scale(sendScale)
                        .clip(CircleShape)
                        .background(sendBgColor)
                        .border(1.dp, sendBorderColor, CircleShape)
                        .clickable(enabled = canSend, onClick = onSend)
                        .testTag("send_message_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "إرسال",
                        tint = sendIconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionResolvedFooter(
    isSpecialistViewer: Boolean,
    onReturnAction: () -> Unit
) {
    Surface(
        color = SanadTheme.colors.surface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
        ) {
            HorizontalDivider(thickness = 0.75.dp, color = SanadTheme.colors.divider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "هذه الجلسة منتهية",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text
                    )
                    Text(
                        text = if (isSpecialistViewer) {
                            "تم إغلاق المحادثة وحفظها في سجل الجلسات."
                        } else {
                            "يمكنك بدء محادثة جديدة في أي وقت تحتاج فيه للدعم."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = SanadTheme.colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onReturnAction,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SanadTheme.colors.accent,
                        contentColor = SanadTheme.colors.onAccent
                    ),
                    modifier = Modifier.testTag("resolved_return_home_btn")
                ) {
                    Text(
                        text = if (isSpecialistViewer) "رجوع" else "الرئيسية",
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.onAccent
                    )
                }
            }
        }
    }
}

private fun deriveInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first()}.${parts[1].first()}"
        parts.size == 1 -> parts[0].take(2)
        else -> "أ"
    }
}
