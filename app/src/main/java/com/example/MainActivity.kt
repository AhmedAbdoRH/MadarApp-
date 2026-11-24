package com.example

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.ApplicationStatus
import com.example.data.model.UserRole
import com.example.notifications.SanadNotificationHelper
import com.example.ui.MainTab
import com.example.ui.SanadViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.components.BookingConfirmDialog
import com.example.ui.components.EmergencyCrisisDialog
import com.example.ui.components.InAppNotificationBanner
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatsListScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NoSpecialistAvailableScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.SessionsBookingScreen
import com.example.ui.screens.SpecialistApplicationScreen
import com.example.ui.screens.SpecialistDashboardScreen
import com.example.ui.screens.SpecialistProfileScreen
import com.example.ui.screens.SpecialistScheduleScreen
import com.example.ui.screens.SupervisorApplicationsScreen
import com.example.ui.screens.SupervisorReportsScreen
import com.example.ui.screens.SupervisorSpecialistsScreen
import com.example.ui.screens.Under18Screen
import com.example.ui.screens.UrgentAssistanceScreen
import com.example.ui.screens.WelcomeConsentScreen
import com.example.ui.screens.WellbeingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SanadTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: SanadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNotificationIntent(intent)
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    SanadRoot(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val convId = intent?.getStringExtra(SanadNotificationHelper.EXTRA_CONVERSATION_ID)
        if (!convId.isNullOrBlank()) {
            val openAsSpecialist = intent.getBooleanExtra(
                SanadNotificationHelper.EXTRA_OPEN_AS_SPECIALIST,
                false
            )
            viewModel.openConversationFromNotification(convId, openAsSpecialist)
            intent.removeExtra(SanadNotificationHelper.EXTRA_CONVERSATION_ID)
            intent.removeExtra(SanadNotificationHelper.EXTRA_OPEN_AS_SPECIALIST)
        }
    }
}

@Composable
fun SanadRoot(viewModel: SanadViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    val uiState by viewModel.uiState.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        SanadNotificationHelper.markPermissionPrompted(context)
        viewModel.refreshNotificationPermissionState()
        if (granted) {
            viewModel.showSnackbar("تم تفعيل إشعارات الرسائل بنجاح 🔔")
        }
    }

    LaunchedEffect(currentUser, uiState.isConsentConfirmed) {
        if (currentUser != null && uiState.isConsentConfirmed &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !SanadNotificationHelper.hasNotificationPermission(context) &&
            !SanadNotificationHelper.wasPermissionPrompted(context)
        ) {
            SanadNotificationHelper.markPermissionPrompted(context)
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        val credentialManager = CredentialManager.create(context)
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { currentUser = Firebase.auth.currentUser },
            onUnauthenticated = {},
            scope = scope
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    viewModel.setAppForegroundState(true)
                    viewModel.refreshNotificationPermissionState()
                }
                Lifecycle.Event.ON_STOP -> {
                    viewModel.setAppForegroundState(false)
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    // Route Protection Hierarchy:
    // 1. Exceptions that stay open without any condition: Urgent Assistance, Privacy Policy
    if (uiState.isUrgentScreenOpen) {
        val onlineCount by viewModel.onlineSpecialistsCount.collectAsState()
        val hasAvailableSpecialists = onlineCount > 0

        UrgentAssistanceScreen(
            onBack = { viewModel.closeUrgentScreen() },
            onStartUrgentChat = {
                viewModel.closeUrgentScreen()
                viewModel.requestUrgentHelp()
            },
            onOpenBreathingExercise = {
                viewModel.openBreathingExerciseFromUrgent()
            },
            onOpenGroundingExercise = {
                viewModel.openGroundingExerciseFromUrgent()
            },
            onShowSnackbar = { msg ->
                viewModel.showSnackbar(msg)
            },
            hasAvailableSpecialists = hasAvailableSpecialists
        )
    } else if (uiState.isPrivacyScreenOpen) {
        PrivacyPolicyScreen(
            onBack = { viewModel.closePrivacyScreen() }
        )
    } else if (uiState.ageGroup == "under_18") {
        // Screen 2: Under 18 Screen (No access to home, chat, specialists, or booking)
        Under18Screen(
            onBackToWelcome = { viewModel.resetConsentToWelcome() },
            onEmergencyClick = { viewModel.openUrgentScreen() }
        )
    } else if (!uiState.isConsentConfirmed) {
        // Screen 1: Welcome Consent Screen (/welcome)
        WelcomeConsentScreen(
            onConfirmAdultConsent = { viewModel.confirmAdultConsent() },
            onSelectUnder18 = { viewModel.setUnder18AgeGroup() },
            onEmergencyClick = { viewModel.openUrgentScreen() },
            onPrivacyClick = { viewModel.openPrivacyScreen() }
        )
    } else if (uiState.isNoSpecialistScreenOpen) {
        NoSpecialistAvailableScreen(
            onBack = { viewModel.closeNoSpecialistScreen() },
            onOpenBreathingExercise = { viewModel.openBreathingExerciseFromNoSpecialist() },
            onOpenGroundingExercise = { viewModel.openGroundingExerciseFromNoSpecialist() },
            onRetry = { viewModel.retrySpecialistSearch() },
            onOpenEmergencyHelp = { viewModel.openUrgentScreen() }
        )
    } else if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = { currentUser = Firebase.auth.currentUser },
            onEmergencyClick = { viewModel.openUrgentScreen() }
        )
    } else {
        val uid = currentUser!!.uid
        key(uid) {
            SanadApp(viewModel = viewModel)
        }
    }
}

@Composable
fun SanadApp(viewModel: SanadViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val specialists by viewModel.specialists.collectAsState()
    val allSpecialistsForSupervisor by viewModel.allSpecialistsForSupervisor.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val specialistConversations by viewModel.specialistConversations.collectAsState()
    val myApplication by viewModel.mySpecialistApplication.collectAsState()
    val allApplications by viewModel.allApplications.collectAsState()
    val allReports by viewModel.allReports.collectAsState()
    val messagesMap by viewModel.messages.collectAsState()
    val bookingSlots by viewModel.bookingSlots.collectAsState()
    val moodHistory by viewModel.moodHistory.collectAsState()
    val volunteerIsOnline by viewModel.volunteerIsOnline.collectAsState()

    val currentUid = Firebase.auth.currentUser?.uid
    val mySpecialistProfile = remember(allSpecialistsForSupervisor, currentUid) {
        if (currentUid.isNullOrBlank()) null
        else allSpecialistsForSupervisor.find {
            it.id == currentUid || (it.applicationId.isNotBlank() && it.applicationId == currentUid)
        }
    }
    val isApprovedSpecialist = mySpecialistProfile != null && mySpecialistProfile.isVerified && !mySpecialistProfile.isSuspended
    val isSuspendedSpecialist = mySpecialistProfile?.isSuspended == true ||
        ApplicationStatus.fromValue(myApplication?.status) == ApplicationStatus.SUSPENDED

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val settingsOrPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.refreshNotificationPermissionState()
        if (!granted) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
    }

    // Auto-dismiss in-app notification banner after 4.5s
    LaunchedEffect(uiState.inAppMessageAlert) {
        if (uiState.inAppMessageAlert != null) {
            delay(4500L)
            viewModel.dismissInAppAlert()
        }
    }

    // Handle snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Handle system back navigation
    BackHandler(enabled = uiState.selectedConversationId != null) {
        viewModel.closeConversation()
    }
    BackHandler(enabled = uiState.selectedConversationId == null && uiState.currentRole != UserRole.SEEKER) {
        viewModel.setRole(UserRole.SEEKER)
    }

    val showBottomBar = uiState.selectedConversationId == null &&
        !(uiState.currentRole == UserRole.SPECIALIST && !isApprovedSpecialist)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    SanadBottomBar(
                        selectedTab = uiState.selectedTab,
                        currentRole = uiState.currentRole,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }
            }
        ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        if (uiState.selectedConversationId != null) {
            val conv = (conversations + specialistConversations).find { it.id == uiState.selectedConversationId }
            if (conv != null) {
                val isSpecialistViewer = if (!currentUid.isNullOrBlank() &&
                    conv.seekerId.isNotBlank() &&
                    conv.specialistId.isNotBlank() &&
                    conv.seekerId != conv.specialistId
                ) {
                    conv.specialistId == currentUid
                } else {
                    uiState.currentRole == UserRole.SPECIALIST
                }
                val specialist = allSpecialistsForSupervisor.find { it.id == conv.specialistId }
                    ?: specialists.find { it.id == conv.specialistId }
                val messages = messagesMap[conv.id] ?: emptyList()

                ChatDetailScreen(
                    conversation = conv,
                    specialist = specialist,
                    messages = messages,
                    isTyping = uiState.isTypingResponse,
                    onSendMessage = { text -> viewModel.sendMessage(conv.id, text) },
                    onBack = { viewModel.closeConversation() },
                    onEmergencyClick = { viewModel.openUrgentScreen() },
                    onPrivacyClick = { viewModel.openPrivacyScreen() },
                    onEndSession = { convId -> viewModel.endSession(convId) },
                    onReportSpecialist = { convId, specId, reason, details ->
                        viewModel.submitSpecialistReport(convId, specId, reason, details)
                    },
                    onBlockSpecialist = { specId -> viewModel.blockSpecialist(specId) },
                    onRecordSessionMood = { convId, mood, score, emoji ->
                        viewModel.recordSessionMood(convId, mood, score, emoji)
                    },
                    onReturnHome = { viewModel.returnHomeFromChat() },
                    isSpecialistViewer = isSpecialistViewer,
                    currentUserId = currentUid.orEmpty(),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                )
            } else if (!uiState.isConversationsLoaded) {
                Box(
                    modifier = contentModifier,
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = SanadTheme.colors.accent)
                        Text(
                            text = "جارٍ فتح المحادثة...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SanadTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = { viewModel.closeConversation() }) {
                            Text(
                                text = "رجوع",
                                color = SanadTheme.colors.accent
                            )
                        }
                    }
                }
            } else {
                LaunchedEffect(uiState.selectedConversationId, uiState.isConversationsLoaded) {
                    viewModel.closeConversation()
                }
            }
        } else if (uiState.currentRole == UserRole.SPECIALIST && !isApprovedSpecialist) {
            SpecialistApplicationScreen(
                application = myApplication,
                isSuspendedSpecialist = isSuspendedSpecialist,
                isSubmitting = uiState.isSubmittingApplication,
                onSubmitApplication = { app -> viewModel.submitSpecialistApplication(app) },
                onWithdrawApplication = { viewModel.withdrawSpecialistApplication() },
                onBackToSeeker = { viewModel.setRole(UserRole.SEEKER) },
                modifier = contentModifier
            )
        } else if (uiState.currentRole == UserRole.SUPERVISOR && uiState.isSupervisor) {
            when (uiState.selectedTab) {
                MainTab.SUPERVISOR_SPECIALISTS -> {
                    SupervisorSpecialistsScreen(
                        specialists = allSpecialistsForSupervisor,
                        applications = allApplications,
                        onToggleSuspend = { specId, suspend ->
                            viewModel.toggleSpecialistSuspension(specId, suspend)
                        },
                        onSwitchToSeeker = { viewModel.setRole(UserRole.SEEKER) },
                        modifier = contentModifier
                    )
                }

                MainTab.SUPERVISOR_REPORTS -> {
                    SupervisorReportsScreen(
                        reports = allReports,
                        specialists = allSpecialistsForSupervisor,
                        onMarkReviewed = { reportId, status, note ->
                            viewModel.markReportReviewed(reportId, status, note)
                        },
                        onSuspendSpecialistFromReport = { reportId, specId ->
                            viewModel.suspendSpecialistFromReport(reportId, specId)
                        },
                        onSwitchToSeeker = { viewModel.setRole(UserRole.SEEKER) },
                        modifier = contentModifier
                    )
                }

                else -> {
                    SupervisorApplicationsScreen(
                        applications = allApplications,
                        onApproveApplication = { app, note -> viewModel.approveApplication(app, note) },
                        onRejectApplication = { uid, reason -> viewModel.rejectApplication(uid, reason) },
                        onSwitchToSeeker = { viewModel.setRole(UserRole.SEEKER) },
                        modifier = contentModifier
                    )
                }
            }
        } else {
            val isSpecialistMode = uiState.currentRole == UserRole.SPECIALIST
            val mySpecialistSlots = bookingSlots.filter {
                !currentUid.isNullOrBlank() && it.specialistId == currentUid
            }

            when (uiState.selectedTab) {
                MainTab.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        specialists = specialists,
                        onNavigateToTab = { viewModel.selectTab(it) },
                        modifier = contentModifier
                    )
                }

                MainTab.CHATS -> {
                    ChatsListScreen(
                        conversations = if (isSpecialistMode) specialistConversations else conversations,
                        specialists = allSpecialistsForSupervisor.ifEmpty { specialists },
                        onSelectConversation = { convId -> viewModel.openConversation(convId) },
                        onStartNewChat = {
                            viewModel.selectTab(if (isSpecialistMode) MainTab.SPECIALIST_DASHBOARD else MainTab.HOME)
                        },
                        onEmergencyClick = { viewModel.openUrgentScreen() },
                        isSpecialistMode = isSpecialistMode,
                        notificationsEnabled = uiState.notificationsEnabled,
                        hasNotificationPermission = uiState.hasNotificationPermission,
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                settingsOrPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onToggleNotifications = { enabled ->
                            viewModel.setNotificationsEnabled(enabled)
                        },
                        modifier = contentModifier
                    )
                }

                MainTab.BOOKING -> {
                    SessionsBookingScreen(
                        slots = bookingSlots,
                        onOpenBookingDialog = { slot -> viewModel.openBookingDialog(slot) },
                        onCancelBooking = { slotId -> viewModel.requestBookingCancellation(slotId) },
                        onEmergencyClick = { viewModel.openUrgentScreen() },
                        modifier = contentModifier
                    )
                }

                MainTab.ACCOUNT_SETTINGS -> {
                    com.example.ui.screens.AccountSettingsScreen(
                        canSwitchView = com.example.ui.logic.canSwitchSpecialistView(
                            !currentUid.isNullOrBlank(), mySpecialistProfile?.isVerified == true,
                            mySpecialistProfile?.isSuspended == true
                        ),
                        isSpecialistMode = isSpecialistMode,
                        onSwitchView = {
                            viewModel.switchAccountView(if (isSpecialistMode) UserRole.SEEKER else UserRole.SPECIALIST)
                        },
                        modifier = contentModifier
                    )
                }

                MainTab.WELLBEING -> {
                    WellbeingScreen(
                        moodHistory = moodHistory,
                        onLogMood = { score, label, emoji, note ->
                            viewModel.logMood(score, label, emoji, note)
                        },
                        onEmergencyClick = { viewModel.openUrgentScreen() },
                        modifier = contentModifier
                    )
                }

                MainTab.SPECIALIST_DASHBOARD -> {
                    val incomingRequests = specialistConversations.filter {
                        !it.acceptedBySpecialist && it.status == "ACTIVE"
                    }
                    val acceptedOrActive = specialistConversations.filter {
                        it.acceptedBySpecialist
                    }
                    SpecialistDashboardScreen(
                        specialist = mySpecialistProfile,
                        isOnline = volunteerIsOnline,
                        pendingRequests = incomingRequests,
                        activeConversations = acceptedOrActive,
                        myBookingSlots = mySpecialistSlots,
                        onToggleOnline = { isOnline -> viewModel.toggleVolunteerOnline(isOnline) },
                        onAcceptRequest = { req -> viewModel.acceptPendingVolunteerRequest(req) },
                        onOpenConversation = { convId -> viewModel.openConversation(convId) },
                        onNavigateToSchedule = { viewModel.selectTab(MainTab.SPECIALIST_SCHEDULE) },
                        onSwitchToSeeker = { viewModel.setRole(UserRole.SEEKER) },
                        modifier = contentModifier
                    )
                }

                MainTab.SPECIALIST_SCHEDULE -> {
                    SpecialistScheduleScreen(
                        specialistName = mySpecialistProfile?.name ?: myApplication?.fullName ?: "الأخصائي",
                        mySlots = mySpecialistSlots,
                        onCreateSlot = { dateText, timeText, specialtyTitle ->
                            viewModel.createSpecialistBookingSlot(dateText, timeText, specialtyTitle)
                        },
                        onDeleteSlot = { slotId -> viewModel.deleteSpecialistBookingSlot(slotId) },
                        onCancelBookedSlot = { slotId -> viewModel.requestBookingCancellation(slotId) },
                        modifier = contentModifier
                    )
                }

                MainTab.SPECIALIST_PROFILE -> {
                    mySpecialistProfile?.let { spec ->
                        SpecialistProfileScreen(
                            specialist = spec,
                            application = myApplication,
                            onSaveProfile = { title, bio, tags, years, urgent ->
                                viewModel.updateMySpecialistProfile(title, bio, tags, years, urgent)
                            },
                            onSwitchToSeeker = { viewModel.selectTab(MainTab.ACCOUNT_SETTINGS) },
                            modifier = contentModifier
                        )
                    }
                }

                else -> {
                    HomeScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        specialists = specialists,
                        onNavigateToTab = { viewModel.selectTab(it) },
                        modifier = contentModifier
                    )
                }
            }
        }

        // Emergency Hotlines Dialog (fallback)
        if (uiState.isEmergencyDialogOpen) {
            EmergencyCrisisDialog(
                contacts = viewModel.emergencyContacts,
                onDismiss = { viewModel.setEmergencyDialogOpen(false) }
            )
        }

        if (uiState.bookingCancellationSlotId != null) {
            val slot = bookingSlots.firstOrNull { it.id == uiState.bookingCancellationSlotId }
            AlertDialog(
                onDismissRequest = { viewModel.dismissBookingCancellation() },
                title = { Text("إلغاء حجز الجلسة؟") },
                text = {
                    Text(if (slot == null) "هل تريد إلغاء هذا الحجز؟" else
                        "${slot.specialistName}\n${slot.dateText} | ${slot.timeText}\nلن يتم الإلغاء إلا بعد تأكيد الخادم.")
                },
                confirmButton = {
                    TextButton(
                        enabled = !uiState.isBookingBusy,
                        onClick = { viewModel.confirmBookingCancellation() }
                    ) { Text(if (uiState.isBookingBusy) "جارٍ الإلغاء..." else "نعم، ألغِ الحجز") }
                },
                dismissButton = {
                    TextButton(
                        enabled = !uiState.isBookingBusy,
                        onClick = { viewModel.dismissBookingCancellation() }
                    ) { Text("احتفظ بالحجز") }
                }
            )
        }

        // Booking confirmation dialog
        if (uiState.isBookingDialogOpen && uiState.selectedSlotForBooking != null) {
            BookingConfirmDialog(
                slot = uiState.selectedSlotForBooking!!,
                isSubmitting = uiState.isBookingBusy,
                onConfirm = { seekerName -> viewModel.confirmBooking(seekerName) },
                onDismiss = { viewModel.closeBookingDialog() }
            )
        }
    }

        // Top Floating In-App Message Notification Banner
        InAppNotificationBanner(
            alert = uiState.inAppMessageAlert,
            onOpenConversation = { alert ->
                viewModel.openConversationFromNotification(
                    conversationId = alert.conversationId,
                    openAsSpecialist = alert.openAsSpecialist
                )
            },
            onDismiss = { viewModel.dismissInAppAlert() },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun SanadBottomBar(
    selectedTab: MainTab,
    currentRole: UserRole,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("sanad_bottom_nav")
    ) {
        val navItems = when (currentRole) {
            UserRole.SEEKER -> listOf(
                NavigationItem(MainTab.HOME, "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home),
                NavigationItem(MainTab.CHATS, "محادثاتي", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat),
                NavigationItem(MainTab.BOOKING, "حجز موعد", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
                NavigationItem(MainTab.WELLBEING, "راحتك", Icons.Filled.SelfImprovement, Icons.Outlined.SelfImprovement),
                NavigationItem(MainTab.ACCOUNT_SETTINGS, "حسابي", Icons.Filled.Person, Icons.Outlined.Person)
            )

            UserRole.SPECIALIST -> listOf(
                NavigationItem(MainTab.SPECIALIST_DASHBOARD, "لوحتي", Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism),
                NavigationItem(MainTab.CHATS, "المحادثات", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat),
                NavigationItem(MainTab.SPECIALIST_SCHEDULE, "مواعيدي", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
                NavigationItem(MainTab.SPECIALIST_PROFILE, "ملفي", Icons.Filled.Person, Icons.Outlined.Person),
                NavigationItem(MainTab.ACCOUNT_SETTINGS, "حسابي", Icons.Filled.Person, Icons.Outlined.Person)
            )

            UserRole.SUPERVISOR -> listOf(
                NavigationItem(MainTab.SUPERVISOR_APPLICATIONS, "الاعتماد", Icons.Filled.VerifiedUser, Icons.Outlined.VerifiedUser),
                NavigationItem(MainTab.SUPERVISOR_SPECIALISTS, "الأخصائيون", Icons.Filled.People, Icons.Outlined.People),
                NavigationItem(MainTab.SUPERVISOR_REPORTS, "البلاغات", Icons.Filled.Flag, Icons.Outlined.Flag)
            )
        }

        navItems.forEach { item ->
            val isSelected = selectedTab == item.tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp
                        ),
                        color = if (isSelected) SanadTheme.colors.onAccent else SanadTheme.colors.textSecondary,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SanadTheme.colors.onAccent,
                    selectedTextColor = SanadTheme.colors.onAccent,
                    indicatorColor = SanadTheme.colors.accent,
                    unselectedIconColor = SanadTheme.colors.textSecondary,
                    unselectedTextColor = SanadTheme.colors.textSecondary
                ),
                modifier = Modifier.testTag("nav_item_${item.tab.name.lowercase()}")
            )
        }
    }
}

data class NavigationItem(
    val tab: MainTab,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
