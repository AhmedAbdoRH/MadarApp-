package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BookingSlot
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.EmergencyContact
import com.example.data.model.MoodCheckIn
import com.example.data.model.Specialist
import com.example.data.model.SpecialistApplication
import com.example.data.model.SpecialistReport
import com.example.data.model.UserRole
import com.example.data.repository.SanadFirestoreRepository
import com.example.data.repository.SanadRepository
import com.example.notifications.SanadNotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp

enum class MainTab {
    HOME,
    CHATS,
    BOOKING,
    WELLBEING,
    ACCOUNT_SETTINGS,
    SPECIALIST_DASHBOARD,
    SPECIALIST_SCHEDULE,
    SPECIALIST_PROFILE,
    SUPERVISOR_APPLICATIONS,
    SUPERVISOR_SPECIALISTS,
    SUPERVISOR_REPORTS
}

data class InAppMessageAlert(
    val conversationId: String,
    val senderName: String,
    val topic: String,
    val messageText: String,
    val isUrgent: Boolean,
    val openAsSpecialist: Boolean,
    val isNewRequest: Boolean = false
)

data class SanadUiState(
    val currentRole: UserRole = UserRole.SEEKER,
    val selectedTab: MainTab = MainTab.HOME,
    val selectedConversationId: String? = null,
    val isEmergencyDialogOpen: Boolean = false,
    val isBookingDialogOpen: Boolean = false,
    val selectedSlotForBooking: BookingSlot? = null,
    val isUrgentConnecting: Boolean = false,
    val activeVentingTopic: String? = null,
    val isTypingResponse: Boolean = false,
    val onlineOnlyFilter: Boolean = false,
    val snackbarMessage: String? = null,
    val volunteerOnlineStatus: Boolean = true,
    val isUrgentScreenOpen: Boolean = false,
    val isPrivacyScreenOpen: Boolean = false,
    val isNoSpecialistScreenOpen: Boolean = false,
    val isWaitingScreenOpen: Boolean = false,
    val isConsentConfirmed: Boolean = false,
    val ageGroup: String? = null,
    val isSpecialistsLoading: Boolean = true,
    val isSupervisor: Boolean = false,
    val isSubmittingApplication: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val hasNotificationPermission: Boolean = true,
    val inAppMessageAlert: InAppMessageAlert? = null,
    val isConversationsLoaded: Boolean = false
)

fun countOnlineSpecialists(specialists: List<Specialist>): Int = specialists.count { it.isOnline }

class SanadViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreRepo = SanadFirestoreRepository(application)

    private val _uiState = MutableStateFlow(SanadUiState())
    val uiState: StateFlow<SanadUiState> = _uiState.asStateFlow()

    private val _allSpecialistsRaw = MutableStateFlow<List<Specialist>>(emptyList())
    val allSpecialistsForSupervisor: StateFlow<List<Specialist>> = _allSpecialistsRaw.asStateFlow()

    private val _specialists = MutableStateFlow<List<Specialist>>(emptyList())
    val specialists: StateFlow<List<Specialist>> = _specialists.asStateFlow()

    val onlineSpecialistsCount: StateFlow<Int> = specialists
        .map { countOnlineSpecialists(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _specialistConversations = MutableStateFlow<List<Conversation>>(emptyList())
    val specialistConversations: StateFlow<List<Conversation>> = _specialistConversations.asStateFlow()

    private val _mySpecialistApplication = MutableStateFlow<SpecialistApplication?>(null)
    val mySpecialistApplication: StateFlow<SpecialistApplication?> = _mySpecialistApplication.asStateFlow()

    private val _allApplications = MutableStateFlow<List<SpecialistApplication>>(emptyList())
    val allApplications: StateFlow<List<SpecialistApplication>> = _allApplications.asStateFlow()

    private val _allReports = MutableStateFlow<List<SpecialistReport>>(emptyList())
    val allReports: StateFlow<List<SpecialistReport>> = _allReports.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    private val _bookingSlots = MutableStateFlow<List<BookingSlot>>(emptyList())
    val bookingSlots: StateFlow<List<BookingSlot>> = _bookingSlots.asStateFlow()

    val emergencyContacts: List<EmergencyContact> = SanadRepository.emergencyContacts
    val moodHistory: StateFlow<List<MoodCheckIn>> = SanadRepository.moodHistory
    val pendingVolunteerRequests: StateFlow<List<Conversation>> = SanadRepository.pendingVolunteerRequests

    private val _volunteerIsOnline = MutableStateFlow(false)
    val volunteerIsOnline: StateFlow<Boolean> = _volunteerIsOnline.asStateFlow()

    private var isAppInForeground: Boolean = true
    private var authStateJob: Job? = null
    private var messagesObservationJob: Job? = null
    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        authStateJob?.cancel()
        val app = getApplication<Application>()
        if (user != null) {
            SanadNotificationHelper.ensureNotificationChannels(app)
            SanadNotificationHelper.scheduleBackgroundMessageSync(app)
            _uiState.value = _uiState.value.copy(
                isSpecialistsLoading = true,
                isConversationsLoaded = false
            )
            startAuthenticatedFirestoreSync(user.uid)
        } else {
            SanadNotificationHelper.cancelBackgroundMessageSync(app)
            _allSpecialistsRaw.value = emptyList()
            _specialists.value = emptyList()
            _conversations.value = emptyList()
            _specialistConversations.value = emptyList()
            _mySpecialistApplication.value = null
            _allApplications.value = emptyList()
            _allReports.value = emptyList()
            _bookingSlots.value = emptyList()
            _messages.value = emptyMap()
            _uiState.value = _uiState.value.copy(
                isSpecialistsLoading = false,
                isConversationsLoaded = false,
                isSupervisor = false,
                currentRole = UserRole.SEEKER,
                selectedTab = MainTab.HOME,
                inAppMessageAlert = null
            )
        }
    }

    private val prefs = application.getSharedPreferences("sanad_consent_prefs", android.content.Context.MODE_PRIVATE)

    init {
        val app = getApplication<Application>()
        SanadNotificationHelper.ensureNotificationChannels(app)
        // Load local consent, ageGroup, and notification state from SharedPreferences
        val hasConsent = prefs.getBoolean("consent_confirmed", false)
        val savedAgeGroup = prefs.getString("age_group", null)
        val hasAuthUser = Firebase.auth.currentUser != null
        _uiState.value = _uiState.value.copy(
            isConsentConfirmed = hasConsent,
            ageGroup = savedAgeGroup,
            isSpecialistsLoading = hasAuthUser,
            notificationsEnabled = SanadNotificationHelper.isNotificationsEnabled(app),
            hasNotificationPermission = SanadNotificationHelper.hasNotificationPermission(app)
        )

        // Listen to Auth State to start Firestore observation only after user is authenticated
        Firebase.auth.addAuthStateListener(authStateListener)
    }

    fun confirmAdultConsent() {
        val nowIso = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(java.util.Date())

        prefs.edit()
            .putBoolean("consent_confirmed", true)
            .putString("consent_version", "v1")
            .putBoolean("age_confirmed", true)
            .putString("consent_at", nowIso)
            .putString("age_group", "adult")
            .apply()

        _uiState.value = _uiState.value.copy(
            isConsentConfirmed = true,
            ageGroup = "adult"
        )
    }

    fun setUnder18AgeGroup() {
        prefs.edit()
            .putString("age_group", "under_18")
            .putBoolean("consent_confirmed", false)
            .apply()

        _uiState.value = _uiState.value.copy(
            isConsentConfirmed = false,
            ageGroup = "under_18"
        )
    }

    fun resetConsentToWelcome() {
        prefs.edit()
            .remove("age_group")
            .remove("consent_confirmed")
            .apply()

        _uiState.value = _uiState.value.copy(
            isConsentConfirmed = false,
            ageGroup = null
        )
    }

    private fun findOwnedSpecialist(list: List<Specialist>, uid: String?): Specialist? {
        if (uid.isNullOrBlank()) return null
        return list.find { it.id == uid || (it.applicationId.isNotBlank() && it.applicationId == uid) }
    }

    private fun startAuthenticatedFirestoreSync(currentUid: String) {
        authStateJob = viewModelScope.launch {
            // Determine supervisor privileges before observing restricted admin collections
            val currentUser = Firebase.auth.currentUser
            val isBootstrapEmail = currentUser?.email.equals("ahmedabdoshouq@gmail.com", ignoreCase = true) &&
                currentUser?.isEmailVerified == true
            val isInAdmins = firestoreRepo.isUserInAdminsCollection(currentUid)
            val isSupervisorUser = isBootstrapEmail || isInAdmins
            _uiState.value = _uiState.value.copy(isSupervisor = isSupervisorUser)

            if (isSupervisorUser) {
                launch {
                    firestoreRepo.cleanupDemoSeedDocsIfAdmin()
                }
            }

            launch {
                firestoreRepo.observeUserBlocks(currentUid)
                    .catch { /* Fallback */ }
                    .collect { blocks ->
                        val blockedIds = blocks.map { it.specialistId }.toSet()
                        for (id in blockedIds) {
                            SanadRepository.blockSpecialist(currentUid, id)
                        }
                        if (_allSpecialistsRaw.value.isNotEmpty()) {
                            _specialists.value = _allSpecialistsRaw.value.filter {
                                it.id !in blockedIds && it.isVerified && !it.isSuspended
                            }
                        }
                    }
            }

            launch {
                firestoreRepo.observeSpecialists()
                    .catch {
                        _allSpecialistsRaw.value = emptyList()
                        _specialists.value = emptyList()
                        _uiState.value = _uiState.value.copy(isSpecialistsLoading = false)
                    }
                    .collect { liveSpecialists ->
                        val verifiedPool = liveSpecialists.filter {
                            it.id !in SanadRepository.DEMO_SPECIALIST_IDS
                        }
                        _allSpecialistsRaw.value = verifiedPool
                        val blocked = SanadRepository.blockedSpecialistIds.value
                        _specialists.value = verifiedPool.filter {
                            it.id !in blocked && it.isVerified && !it.isSuspended
                        }
                        val ownedSpec = findOwnedSpecialist(verifiedPool, currentUid)
                        if (ownedSpec != null) {
                            _volunteerIsOnline.value = ownedSpec.isOnline && !ownedSpec.isSuspended
                            _uiState.value = _uiState.value.copy(
                                volunteerOnlineStatus = ownedSpec.isOnline && !ownedSpec.isSuspended,
                                isSpecialistsLoading = false
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(isSpecialistsLoading = false)
                        }
                    }
            }

            launch {
                firestoreRepo.observeBookingSlots()
                    .catch { /* Ignore transient error */ }
                    .collect { liveSlots ->
                        val blocked = SanadRepository.blockedSpecialistIds.value
                        _bookingSlots.value = liveSlots.filter {
                            it.id !in SanadRepository.DEMO_SLOT_IDS &&
                                it.specialistId !in SanadRepository.DEMO_SPECIALIST_IDS &&
                                it.specialistId !in blocked
                        }
                    }
            }

            var seekerBaselineInitialized = false
            val seenSeekerMsgTimes = mutableMapOf<String, Long>()
            var specialistBaselineInitialized = false
            val seenSpecialistMsgTimes = mutableMapOf<String, Long>()
            val seenSpecialistRequestIds = mutableSetOf<String>()
            var userConvsLoaded = false
            var specConvsLoaded = false

            launch {
                firestoreRepo.observeUserConversations(currentUid)
                    .catch {
                        userConvsLoaded = true
                        if (specConvsLoaded) {
                            _uiState.value = _uiState.value.copy(isConversationsLoaded = true)
                        }
                    }
                    .collect { liveConvs ->
                        val blocked = SanadRepository.blockedSpecialistIds.value
                        val filtered = liveConvs
                            .filter {
                                it.id !in SanadRepository.DEMO_CONVERSATION_IDS &&
                                    it.specialistId !in SanadRepository.DEMO_SPECIALIST_IDS &&
                                    it.specialistId !in blocked
                            }
                            .sortedByDescending {
                                it.lastMessageTime?.toDate()?.time ?: it.createdAt?.toDate()?.time ?: 0L
                            }
                        _conversations.value = filtered

                        val app = getApplication<Application>()
                        if (!seekerBaselineInitialized) {
                            filtered.forEach { conv ->
                                val msgMillis = conv.lastMessageTime?.toDate()?.time ?: 0L
                                seenSeekerMsgTimes[conv.id] = msgMillis
                                SanadNotificationHelper.saveLastNotifiedMillis(app, conv.id, msgMillis)
                            }
                            seekerBaselineInitialized = true
                        } else {
                            filtered.forEach { conv ->
                                val msgMillis = conv.lastMessageTime?.toDate()?.time ?: 0L
                                val prevMillis = seenSeekerMsgTimes[conv.id] ?: 0L
                                val isFromOther = conv.lastMessageSenderId.isNotBlank() &&
                                    conv.lastMessageSenderId != currentUid
                                if (conv.status == "ACTIVE" &&
                                    isFromOther &&
                                    msgMillis > prevMillis &&
                                    conv.lastMessage.isNotBlank()
                                ) {
                                    seenSeekerMsgTimes[conv.id] = msgMillis
                                    dispatchIncomingNotification(
                                        conv = conv,
                                        msgMillis = msgMillis,
                                        openAsSpecialist = false,
                                        isNewRequest = false
                                    )
                                } else if (msgMillis > prevMillis) {
                                    seenSeekerMsgTimes[conv.id] = msgMillis
                                    SanadNotificationHelper.saveLastNotifiedMillis(app, conv.id, msgMillis)
                                }
                            }
                        }
                        userConvsLoaded = true
                        if (specConvsLoaded) {
                            _uiState.value = _uiState.value.copy(isConversationsLoaded = true)
                        }
                    }
            }

            launch {
                firestoreRepo.observeSpecialistConversations(currentUid)
                    .catch {
                        specConvsLoaded = true
                        if (userConvsLoaded) {
                            _uiState.value = _uiState.value.copy(isConversationsLoaded = true)
                        }
                    }
                    .collect { specConvs ->
                        val filtered = specConvs
                            .filter { it.id !in SanadRepository.DEMO_CONVERSATION_IDS }
                            .sortedByDescending {
                                it.lastMessageTime?.toDate()?.time ?: it.createdAt?.toDate()?.time ?: 0L
                            }
                        _specialistConversations.value = filtered

                        val app = getApplication<Application>()
                        if (!specialistBaselineInitialized) {
                            filtered.forEach { conv ->
                                val msgMillis = conv.lastMessageTime?.toDate()?.time
                                    ?: conv.createdAt?.toDate()?.time
                                    ?: 0L
                                seenSpecialistMsgTimes[conv.id] = msgMillis
                                seenSpecialistRequestIds.add(conv.id)
                                SanadNotificationHelper.saveLastNotifiedMillis(app, conv.id, msgMillis)
                            }
                            specialistBaselineInitialized = true
                        } else {
                            filtered.forEach { conv ->
                                val msgMillis = conv.lastMessageTime?.toDate()?.time
                                    ?: conv.createdAt?.toDate()?.time
                                    ?: 0L
                                val prevMillis = seenSpecialistMsgTimes[conv.id] ?: 0L
                                val isNewRequest = conv.status == "ACTIVE" &&
                                    !conv.acceptedBySpecialist &&
                                    conv.id !in seenSpecialistRequestIds
                                val isFromSeeker = conv.lastMessageSenderId.isNotBlank() &&
                                    conv.lastMessageSenderId != currentUid
                                if (conv.status == "ACTIVE" &&
                                    (isNewRequest || (isFromSeeker && msgMillis > prevMillis && conv.lastMessage.isNotBlank()))
                                ) {
                                    seenSpecialistRequestIds.add(conv.id)
                                    val effectiveMillis = maxOf(msgMillis, System.currentTimeMillis())
                                    seenSpecialistMsgTimes[conv.id] = effectiveMillis
                                    dispatchIncomingNotification(
                                        conv = conv,
                                        msgMillis = effectiveMillis,
                                        openAsSpecialist = true,
                                        isNewRequest = isNewRequest
                                    )
                                } else {
                                    seenSpecialistRequestIds.add(conv.id)
                                    if (msgMillis > prevMillis) {
                                        seenSpecialistMsgTimes[conv.id] = msgMillis
                                        SanadNotificationHelper.saveLastNotifiedMillis(app, conv.id, msgMillis)
                                    }
                                }
                            }
                        }
                        specConvsLoaded = true
                        if (userConvsLoaded) {
                            _uiState.value = _uiState.value.copy(isConversationsLoaded = true)
                        }
                    }
            }

            launch {
                firestoreRepo.observeMySpecialistApplication(currentUid)
                    .catch { /* Ignore */ }
                    .collect { myApp ->
                        _mySpecialistApplication.value = myApp
                    }
            }

            if (isSupervisorUser) {
                launch {
                    firestoreRepo.observeAllSpecialistApplications()
                        .catch { /* Ignore */ }
                        .collect { apps ->
                            _allApplications.value = apps
                        }
                }

                launch {
                    firestoreRepo.observeAllReports()
                        .catch { /* Ignore */ }
                        .collect { reports ->
                            _allReports.value = reports
                        }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Firebase.auth.removeAuthStateListener(authStateListener)
        authStateJob?.cancel()
        messagesObservationJob?.cancel()
    }

    // View switching is not specialist registration and does not grant backend access.
    fun switchAccountView(role: UserRole) {
        val uid = Firebase.auth.currentUser?.uid
        val profile = findOwnedSpecialist(_allSpecialistsRaw.value, uid)
        val eligible = com.example.ui.logic.canSwitchSpecialistView(
            !uid.isNullOrBlank(), profile?.isVerified == true, profile?.isSuspended == true
        )
        if (!eligible || role == UserRole.SUPERVISOR) return
        setRole(role)
    }

    fun setRole(role: UserRole) {
        val safeRole = if (role == UserRole.SUPERVISOR && !_uiState.value.isSupervisor) {
            UserRole.SEEKER
        } else {
            role
        }
        val newTab = when (safeRole) {
            UserRole.SEEKER -> MainTab.HOME
            UserRole.SPECIALIST -> MainTab.SPECIALIST_DASHBOARD
            UserRole.SUPERVISOR -> MainTab.SUPERVISOR_APPLICATIONS
        }
        _uiState.value = _uiState.value.copy(
            currentRole = safeRole,
            selectedTab = newTab,
            selectedConversationId = null
        )
    }

    fun selectTab(tab: MainTab) {
        _uiState.value = _uiState.value.copy(
            selectedTab = tab,
            selectedConversationId = null
        )
    }

    fun toggleVolunteerOnline(isOnline: Boolean) {
        val currentUid = Firebase.auth.currentUser?.uid
        val mySpec = findOwnedSpecialist(_allSpecialistsRaw.value, currentUid)
        if (mySpec == null || !mySpec.isVerified) {
            showSnackbar("يجب اعتماد طلبك كأخصائي أولاً لتفعيل التوفر.")
            return
        }
        if (mySpec.isSuspended) {
            showSnackbar("حساب الأخصائي موقوف مؤقتاً من قِبل المشرف.")
            return
        }
        _volunteerIsOnline.value = isOnline
        _uiState.value = _uiState.value.copy(volunteerOnlineStatus = isOnline)
        viewModelScope.launch {
            try {
                firestoreRepo.setSpecialistOnline(mySpec.id, isOnline)
                showSnackbar(if (isOnline) "أنت الآن «أونلاين ومتاح» لاستقبال المحادثات 🟢" else "تم تغيير حالتك إلى غير متاح ⚪")
            } catch (e: Exception) {
                _volunteerIsOnline.value = !isOnline
                _uiState.value = _uiState.value.copy(volunteerOnlineStatus = !isOnline)
                showSnackbar("تعذر تحديث حالة التوفر الآن، يرجى المحاولة مجدداً.")
            }
        }
    }

    private fun dispatchIncomingNotification(
        conv: Conversation,
        msgMillis: Long,
        openAsSpecialist: Boolean,
        isNewRequest: Boolean
    ) {
        val app = getApplication<Application>()
        SanadNotificationHelper.saveLastNotifiedMillis(app, conv.id, msgMillis)

        if (!SanadNotificationHelper.isNotificationsEnabled(app)) return

        val isViewingThisChat = isAppInForeground && _uiState.value.selectedConversationId == conv.id
        if (isViewingThisChat) return

        val senderName = if (openAsSpecialist) {
            conv.seekerName.ifBlank { "مستفيد" }
        } else {
            conv.specialistName.ifBlank { "الأخصائي المتطوع" }
        }

        val postedSystemNotification = SanadNotificationHelper.showIncomingMessageNotification(
            context = app,
            conversationId = conv.id,
            senderName = senderName,
            topic = conv.topic,
            messageText = conv.lastMessage,
            isUrgent = conv.isUrgent,
            openAsSpecialist = openAsSpecialist,
            isNewRequest = isNewRequest
        )

        if (isAppInForeground || !postedSystemNotification) {
            _uiState.value = _uiState.value.copy(
                inAppMessageAlert = InAppMessageAlert(
                    conversationId = conv.id,
                    senderName = senderName,
                    topic = conv.topic,
                    messageText = conv.lastMessage,
                    isUrgent = conv.isUrgent,
                    openAsSpecialist = openAsSpecialist,
                    isNewRequest = isNewRequest
                )
            )
        }
    }

    fun setAppForegroundState(inForeground: Boolean) {
        isAppInForeground = inForeground
    }

    fun refreshNotificationPermissionState() {
        val app = getApplication<Application>()
        _uiState.value = _uiState.value.copy(
            notificationsEnabled = SanadNotificationHelper.isNotificationsEnabled(app),
            hasNotificationPermission = SanadNotificationHelper.hasNotificationPermission(app)
        )
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        val app = getApplication<Application>()
        SanadNotificationHelper.setNotificationsEnabled(app, enabled)
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        showSnackbar(
            if (enabled) "تم تفعيل إشعارات الرسائل 🔔"
            else "تم إيقاف إشعارات الرسائل 🔕"
        )
    }

    fun dismissInAppAlert() {
        _uiState.value = _uiState.value.copy(inAppMessageAlert = null)
    }

    fun openConversationFromNotification(conversationId: String, openAsSpecialist: Boolean) {
        if (conversationId.isBlank()) return
        val targetRole = if (openAsSpecialist) UserRole.SPECIALIST else UserRole.SEEKER
        val targetTab = if (openAsSpecialist) MainTab.SPECIALIST_DASHBOARD else MainTab.CHATS
        _uiState.value = _uiState.value.copy(
            currentRole = targetRole,
            selectedTab = targetTab,
            selectedConversationId = conversationId,
            inAppMessageAlert = null
        )
        SanadNotificationHelper.cancelConversationNotification(getApplication(), conversationId)
        observeMessagesForConversation(conversationId)
    }

    fun openConversation(conversationId: String) {
        val currentAlertId = _uiState.value.inAppMessageAlert?.conversationId
        _uiState.value = _uiState.value.copy(
            selectedConversationId = conversationId,
            inAppMessageAlert = if (currentAlertId == conversationId) null else _uiState.value.inAppMessageAlert
        )
        SanadNotificationHelper.cancelConversationNotification(getApplication(), conversationId)
        observeMessagesForConversation(conversationId)
    }

    fun closeConversation() {
        _uiState.value = _uiState.value.copy(selectedConversationId = null)
        messagesObservationJob?.cancel()
    }

    private fun observeMessagesForConversation(conversationId: String) {
        messagesObservationJob?.cancel()
        messagesObservationJob = viewModelScope.launch {
            firestoreRepo.observeMessages(conversationId)
                .catch { /* Keep existing messages on transient error */ }
                .collect { liveMsgs ->
                    _messages.value = _messages.value + (conversationId to liveMsgs)
                }
        }
    }

    fun startChatWithSpecialist(specialist: Specialist, topic: String = "فضفضة عامة", isUrgent: Boolean = false) {
        viewModelScope.launch {
            val uid = Firebase.auth.currentUser?.uid ?: ""
            val existingLive = _conversations.value.find {
                it.specialistId == specialist.id && it.status == "ACTIVE"
            }
            val conv = existingLive ?: SanadRepository.startOrGetConversation(
                specialist = specialist,
                topic = topic,
                isUrgent = isUrgent,
                seekerId = uid
            )
            if (existingLive == null) {
                _conversations.value = listOf(conv) + _conversations.value
                try {
                    firestoreRepo.createConversation(conv)
                } catch (_: Exception) {
                    // Fallback to in-memory
                }
            }
            openConversation(conv.id)
            _uiState.value = _uiState.value.copy(selectedTab = MainTab.CHATS)
        }
    }

    fun requestUrgentHelp() {
        val onlineSpecialists = specialists.value.filter { it.isOnline }
        if (onlineSpecialists.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isNoSpecialistScreenOpen = true,
                isUrgentConnecting = false
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUrgentConnecting = true)
            delay(800)
            val onlineAvailable = onlineSpecialists.firstOrNull { it.supportsUrgentCare }
                ?: onlineSpecialists.first()
            val uid = Firebase.auth.currentUser?.uid ?: ""
            val conv = SanadRepository.startOrGetConversation(
                specialist = onlineAvailable,
                topic = "دعم نفسي عاجل وطارئ 🚨",
                isUrgent = true,
                seekerId = uid
            )
            if (_conversations.value.none { it.id == conv.id }) {
                _conversations.value = listOf(conv) + _conversations.value
                try {
                    firestoreRepo.createConversation(conv)
                } catch (_: Exception) {
                    // Fallback to in-memory
                }
            }
            _uiState.value = _uiState.value.copy(
                isUrgentConnecting = false,
                isNoSpecialistScreenOpen = false,
                selectedTab = MainTab.CHATS
            )
            openConversation(conv.id)
        }
    }

    fun startVentingByTopic(topicTitle: String) {
        val onlineSpecialists = specialists.value.filter { it.isOnline }
        if (onlineSpecialists.isEmpty()) {
            _uiState.value = _uiState.value.copy(isNoSpecialistScreenOpen = true)
            return
        }

        viewModelScope.launch {
            val onlineAvailable = onlineSpecialists.first()
            val uid = Firebase.auth.currentUser?.uid ?: ""
            val conv = SanadRepository.startOrGetConversation(
                specialist = onlineAvailable,
                topic = topicTitle,
                isUrgent = topicTitle.contains("عاجل"),
                seekerId = uid
            )
            if (_conversations.value.none { it.id == conv.id }) {
                _conversations.value = listOf(conv) + _conversations.value
                try {
                    firestoreRepo.createConversation(conv)
                } catch (_: Exception) {
                    // Fallback to in-memory
                }
            }
            _uiState.value = _uiState.value.copy(
                selectedTab = MainTab.CHATS,
                isNoSpecialistScreenOpen = false
            )
            openConversation(conv.id)
        }
    }

    fun sendMessage(conversationId: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        val currentUid = Firebase.auth.currentUser?.uid
        val conv = (_conversations.value + _specialistConversations.value).find { it.id == conversationId }
        val isSpecialistUser = if (conv != null && !currentUid.isNullOrBlank() &&
            conv.seekerId.isNotBlank() && conv.specialistId.isNotBlank() &&
            conv.seekerId != conv.specialistId
        ) {
            conv.specialistId == currentUid
        } else {
            _uiState.value.currentRole == UserRole.SPECIALIST
        }
        val senderName = if (isSpecialistUser) (conv?.specialistName?.ifBlank { "الأخصائي" } ?: "الأخصائي") else "أنا"
        val receiverId = if (isSpecialistUser) (conv?.seekerId ?: "seeker") else (conv?.specialistId ?: "specialist")

        // 1. Optimistic local update
        val localMsg = SanadRepository.sendMessage(
            conversationId = conversationId,
            text = trimmed,
            isFromSpecialist = isSpecialistUser,
            senderName = senderName,
            senderId = currentUid.orEmpty()
        )
        val existingList = _messages.value[conversationId] ?: emptyList()
        _messages.value = _messages.value + (conversationId to (existingList + localMsg))
        _conversations.value = _conversations.value.map {
            if (it.id == conversationId) it.copy(lastMessage = trimmed, lastMessageTime = Timestamp.now()) else it
        }
        _specialistConversations.value = _specialistConversations.value.map {
            if (it.id == conversationId) it.copy(lastMessage = trimmed, lastMessageTime = Timestamp.now()) else it
        }

        // 2. Sync to Firestore
        viewModelScope.launch {
            try {
                firestoreRepo.sendMessage(
                    conversationId = conversationId,
                    receiverId = receiverId,
                    text = trimmed,
                    isFromSpecialist = isSpecialistUser,
                    senderName = senderName
                )
            } catch (_: Exception) {
                showSnackbar("تعذر إرسال الرسالة، يرجى التحقق من الاتصال.")
            }
        }
    }

    fun openBookingDialog(slot: BookingSlot) {
        _uiState.value = _uiState.value.copy(
            isBookingDialogOpen = true,
            selectedSlotForBooking = slot
        )
    }

    fun closeBookingDialog() {
        _uiState.value = _uiState.value.copy(
            isBookingDialogOpen = false,
            selectedSlotForBooking = null
        )
    }

    fun confirmBooking(seekerName: String = "مستفيد") {
        val slot = _uiState.value.selectedSlotForBooking ?: return
        SanadRepository.bookSlot(slot.id, seekerName)
        _bookingSlots.value = SanadRepository.bookingSlots.value

        viewModelScope.launch {
            try {
                firestoreRepo.bookSlot(slot.id, seekerName)
            } catch (e: Exception) {
                // Kept in local state
            }
        }

        showSnackbar("تم حجز الجلسة بنجاح! موعدك: ${slot.dateText} (${slot.timeText}) 🌿")
        closeBookingDialog()
    }

    fun cancelBooking(slotId: String) {
        SanadRepository.cancelBooking(slotId)
        _bookingSlots.value = SanadRepository.bookingSlots.value

        viewModelScope.launch {
            try {
                firestoreRepo.cancelBooking(slotId)
            } catch (e: Exception) {
                // Kept in local state
            }
        }

        showSnackbar("تم إلغاء موعد الجلسة.")
    }

    fun setEmergencyDialogOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isEmergencyDialogOpen = open)
    }

    fun openUrgentScreen() {
        _uiState.value = _uiState.value.copy(isUrgentScreenOpen = true)
    }

    fun closeUrgentScreen() {
        _uiState.value = _uiState.value.copy(isUrgentScreenOpen = false)
    }

    fun openPrivacyScreen() {
        _uiState.value = _uiState.value.copy(isPrivacyScreenOpen = true)
    }

    fun closePrivacyScreen() {
        _uiState.value = _uiState.value.copy(isPrivacyScreenOpen = false)
    }

    fun openNoSpecialistScreen() {
        _uiState.value = _uiState.value.copy(isNoSpecialistScreenOpen = true)
    }

    fun closeNoSpecialistScreen() {
        _uiState.value = _uiState.value.copy(isNoSpecialistScreenOpen = false)
    }

    fun retrySpecialistSearch() {
        val onlineSpecialists = specialists.value.filter { it.isOnline }
        if (onlineSpecialists.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(isNoSpecialistScreenOpen = false)
            requestUrgentHelp()
        } else {
            showSnackbar("ما زال جميع الأخصائيين غير متاحين حالياً 🌿")
        }
    }

    fun openBreathingExerciseFromNoSpecialist() {
        _uiState.value = _uiState.value.copy(
            isNoSpecialistScreenOpen = false,
            isUrgentScreenOpen = false,
            selectedTab = MainTab.WELLBEING,
            selectedConversationId = null
        )
    }

    fun openGroundingExerciseFromNoSpecialist() {
        _uiState.value = _uiState.value.copy(
            isNoSpecialistScreenOpen = false,
            isUrgentScreenOpen = false,
            selectedTab = MainTab.WELLBEING,
            selectedConversationId = null
        )
    }

    fun openBreathingExerciseFromUrgent() {
        _uiState.value = _uiState.value.copy(
            isUrgentScreenOpen = false,
            isNoSpecialistScreenOpen = false,
            selectedTab = MainTab.WELLBEING,
            selectedConversationId = null
        )
    }

    fun openGroundingExerciseFromUrgent() {
        _uiState.value = _uiState.value.copy(
            isUrgentScreenOpen = false,
            isNoSpecialistScreenOpen = false,
            selectedTab = MainTab.WELLBEING,
            selectedConversationId = null
        )
    }

    fun toggleOnlineFilter() {
        _uiState.value = _uiState.value.copy(onlineOnlyFilter = !_uiState.value.onlineOnlyFilter)
    }

    fun logMood(score: Int, label: String, emoji: String, note: String) {
        SanadRepository.addMood(score, label, emoji, note)
        showSnackbar("تم تسجيل حالتك المزاجية.. شكراً لاهتمامك بمشاعرك 🌿")
    }

    fun endSession(conversationId: String) {
        SanadRepository.endConversation(conversationId)
        _conversations.value = SanadRepository.conversations.value
        _messages.value = SanadRepository.messages.value

        viewModelScope.launch {
            try {
                firestoreRepo.endConversation(conversationId)
            } catch (e: Exception) {
                // Kept in local repository
            }
        }
    }

    fun submitSpecialistReport(conversationId: String, specialistId: String, reason: String, details: String) {
        val currentUid = Firebase.auth.currentUser?.uid ?: "user"
        val report = com.example.data.model.SpecialistReport(
            conversationId = conversationId,
            specialistId = specialistId,
            userId = currentUid,
            reason = reason,
            details = details
        )
        SanadRepository.reportSpecialist(report)

        viewModelScope.launch {
            try {
                firestoreRepo.submitReport(report)
            } catch (e: Exception) {
                // Kept in local state
            }
        }
    }

    fun blockSpecialist(specialistId: String) {
        val currentUid = Firebase.auth.currentUser?.uid ?: "user"
        SanadRepository.blockSpecialist(currentUid, specialistId)
        _specialists.value = _specialists.value.filter { it.id != specialistId }
        _bookingSlots.value = _bookingSlots.value.filter { it.specialistId != specialistId }

        // Close conversation and navigate to conversations list (Chats tab)
        _uiState.value = _uiState.value.copy(
            selectedConversationId = null,
            selectedTab = MainTab.CHATS
        )
        messagesObservationJob?.cancel()

        viewModelScope.launch {
            try {
                firestoreRepo.blockSpecialist(specialistId)
            } catch (e: Exception) {
                // Kept in local repository
            }
        }
        showSnackbar("تم حظر الأخصائي. مش هتظهر لك محادثات معاه تاني.")
    }

    fun recordSessionMood(conversationId: String, moodLabel: String, moodScore: Int, moodEmoji: String) {
        val currentUid = Firebase.auth.currentUser?.uid ?: "user"
        val sessionMood = com.example.data.model.SessionMood(
            conversationId = conversationId,
            userId = currentUid,
            mood = moodLabel,
            moodScore = moodScore,
            moodEmoji = moodEmoji
        )
        SanadRepository.recordSessionMood(sessionMood)
        SanadRepository.addMood(moodScore, moodLabel, moodEmoji, "تقييم بعد الجلسة")

        viewModelScope.launch {
            try {
                firestoreRepo.recordSessionMood(sessionMood)
            } catch (e: Exception) {
                // Kept in local repository
            }
        }

        // Return user to Home screen
        _uiState.value = _uiState.value.copy(
            selectedConversationId = null,
            selectedTab = MainTab.HOME
        )
        messagesObservationJob?.cancel()
    }

    fun returnHomeFromChat() {
        _uiState.value = _uiState.value.copy(
            selectedConversationId = null,
            selectedTab = MainTab.HOME
        )
        messagesObservationJob?.cancel()
    }

    fun acceptPendingVolunteerRequest(request: Conversation) {
        viewModelScope.launch {
            try {
                firestoreRepo.acceptSpecialistConversation(request.id)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                showSnackbar("تعذر قبول المحادثة. لم يتم فتحها، حاول مجدداً.")
                return@launch
            }
            openConversation(request.id)
            showSnackbar("تم قبول المحادثة! أنت الآن متصل مع المستفيد.")
        }
    }

    // --- Specialist Application, Profile & Schedule Actions ---
    fun submitSpecialistApplication(application: SpecialistApplication) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingApplication = true)
            try {
                firestoreRepo.submitSpecialistApplication(application)
                showSnackbar("تم إرسال طلبك والمؤهل المرفق بنجاح للمشرف 🌿")
            } catch (e: Exception) {
                showSnackbar("تعذر إرسال الطلب: تأكد من الاتصال بالإنترنت وحجم المرفق.")
            } finally {
                _uiState.value = _uiState.value.copy(isSubmittingApplication = false)
            }
        }
    }

    fun withdrawSpecialistApplication() {
        viewModelScope.launch {
            try {
                firestoreRepo.withdrawSpecialistApplication()
                showSnackbar("تم سحب طلب التقديم بنجاح.")
            } catch (_: Exception) {
                showSnackbar("تعذر سحب الطلب حالياً.")
            }
        }
    }

    fun updateMySpecialistProfile(
        title: String,
        bio: String,
        tags: List<String>,
        experienceYears: Long,
        supportsUrgentCare: Boolean
    ) {
        val currentUid = Firebase.auth.currentUser?.uid ?: return
        val mySpec = findOwnedSpecialist(_allSpecialistsRaw.value, currentUid) ?: return
        viewModelScope.launch {
            try {
                firestoreRepo.updateMySpecialistProfile(
                    specialistId = mySpec.id,
                    title = title,
                    bio = bio,
                    tags = tags,
                    experienceYears = experienceYears,
                    supportsUrgentCare = supportsUrgentCare
                )
                showSnackbar("تم حفظ تحديثات ملفك المهني بنجاح 🌿")
            } catch (_: Exception) {
                showSnackbar("تعذر حفظ التعديلات حالياً.")
            }
        }
    }

    fun createSpecialistBookingSlot(dateText: String, timeText: String, specialtyTitle: String) {
        val currentUid = Firebase.auth.currentUser?.uid ?: return
        val mySpec = findOwnedSpecialist(_allSpecialistsRaw.value, currentUid)
        val specialistName = mySpec?.name ?: _mySpecialistApplication.value?.fullName ?: "أخصائي معتمد"
        viewModelScope.launch {
            try {
                firestoreRepo.createBookingSlot(
                    specialistName = specialistName,
                    dateText = dateText,
                    timeText = timeText,
                    specialtyTitle = specialtyTitle
                )
                showSnackbar("تمت إضافة الموعد المتاح بنجاح 🗓️")
            } catch (_: Exception) {
                showSnackbar("تعذر إضافة الموعد حالياً.")
            }
        }
    }

    fun deleteSpecialistBookingSlot(slotId: String) {
        viewModelScope.launch {
            try {
                firestoreRepo.deleteBookingSlot(slotId)
                showSnackbar("تم حذف الموعد المتاح.")
            } catch (_: Exception) {
                showSnackbar("تعذر حذف الموعد حالياً.")
            }
        }
    }

    // --- Supervisor Oversight Actions ---
    fun approveApplication(application: SpecialistApplication, reviewerNote: String?) {
        if (!_uiState.value.isSupervisor) return
        viewModelScope.launch {
            try {
                firestoreRepo.approveSpecialistApplication(application, reviewerNote)
                showSnackbar("تم اعتماد الأخصائي ${application.fullName} وتفعيل حسابه بنجاح ✅")
            } catch (_: Exception) {
                showSnackbar("تعذر اعتماد الطلب، تحقق من الصلاحيات والاتصال.")
            }
        }
    }

    fun rejectApplication(applicantUserId: String, reason: String) {
        if (!_uiState.value.isSupervisor) return
        viewModelScope.launch {
            try {
                firestoreRepo.rejectSpecialistApplication(applicantUserId, reason)
                showSnackbar("تم رفض الطلب وإرسال السبب للمتقدم.")
            } catch (_: Exception) {
                showSnackbar("تعذر تحديث حالة الطلب حالياً.")
            }
        }
    }

    fun toggleSpecialistSuspension(specialistId: String, suspend: Boolean) {
        if (!_uiState.value.isSupervisor) return
        viewModelScope.launch {
            try {
                firestoreRepo.setSpecialistSuspended(specialistId, suspend)
                showSnackbar(if (suspend) "تم إيقاف حساب الأخصائي مؤقتاً." else "تمت إعادة تفعيل حساب الأخصائي ✅")
            } catch (_: Exception) {
                showSnackbar("تعذر تغيير حالة الأخصائي حالياً.")
            }
        }
    }

    fun markReportReviewed(reportId: String, status: String, note: String?) {
        if (!_uiState.value.isSupervisor) return
        viewModelScope.launch {
            try {
                firestoreRepo.markReportReviewed(reportId, status, note)
                showSnackbar("تم تحديث حالة البلاغ بنجاح.")
            } catch (_: Exception) {
                showSnackbar("تعذر تحديث البلاغ حالياً.")
            }
        }
    }

    fun suspendSpecialistFromReport(reportId: String, specialistId: String) {
        if (!_uiState.value.isSupervisor) return
        viewModelScope.launch {
            try {
                firestoreRepo.setSpecialistSuspended(specialistId, true)
                firestoreRepo.markReportReviewed(reportId, "ACTION_TAKEN", "تم إيقاف الأخصائي مؤقتاً للمراجعة")
                showSnackbar("تم إيقاف الأخصائي وتحديث البلاغ.")
            } catch (_: Exception) {
                showSnackbar("تعذر تنفيذ الإجراء حالياً.")
            }
        }
    }

    fun showSnackbar(message: String) {
        _uiState.value = _uiState.value.copy(snackbarMessage = message)
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }
}
