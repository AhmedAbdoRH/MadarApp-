package com.example.data.repository

import com.example.data.model.BookingSlot
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.EmergencyContact
import com.example.data.model.MoodCheckIn
import com.example.data.model.Specialist
import com.example.data.model.VentingTopic
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object SanadRepository {

    val DEMO_SPECIALIST_IDS = setOf("spec_1", "spec_2", "spec_3", "spec_4", "spec_5")
    val DEMO_SLOT_IDS = setOf("slot_1", "slot_2", "slot_3", "slot_4", "slot_5")
    val DEMO_CONVERSATION_IDS = setOf("conv_welcome", "req_101", "req_102")

    val emergencyContacts = listOf(
        EmergencyContact(
            id = "em_eg",
            country = "مصر",
            flag = "🇪🇬",
            organization = "الأمانة العامة للصحة النفسية وعلاج الإدمان",
            phoneNumber = "08008880700",
            availableHours = "متاح 24 ساعة - مجاناً",
            note = "دعم واستشارات نفسية فورية من وزارة الصحة والسكان المصرية (خط بديل: 0220816831)"
        ),
        EmergencyContact(
            id = "em_sa",
            country = "المملكة العربية السعودية",
            flag = "🇸🇦",
            organization = "المركز الوطني لتعزيز الصحة النفسية",
            phoneNumber = "920033360",
            availableHours = "متاح يومياً (من 8 ص حتى 8 م)",
            note = "استشارات نفسية هاتفية يقدمها نخبة من المختصين المعتمدين بالمملكة"
        ),
        EmergencyContact(
            id = "em_ae",
            country = "الإمارات العربية المتحدة",
            flag = "🇦🇪",
            organization = "الخط الساخن الوطني للدعم النفسي (أمل)",
            phoneNumber = "8004673",
            availableHours = "متاح 24/7 طوال الأسبوع",
            note = "مبادرة البرنامج الوطني للسعادة وجودة الحياة لتقديم الدعم النفسي السري"
        ),
        EmergencyContact(
            id = "em_jo",
            country = "الأردن",
            flag = "🇯🇴",
            organization = "الخط الوطني للإرشاد والدعم النفسي",
            phoneNumber = "110",
            availableHours = "متاح 24 ساعة",
            note = "خط ساخن لتقديم الدعم النفسي والاجتماعي العاجل (خط بديل: 065330055)"
        ),
        EmergencyContact(
            id = "em_kw",
            country = "الكويت",
            flag = "🇰🇼",
            organization = "خط المساندة النفسية - مركز الكويت للصحة النفسية",
            phoneNumber = "147",
            availableHours = "متاح يومياً على مدار الساعة",
            note = "دعم نفسي واستجابة سريعة للأزمات النفسية والطارئة"
        ),
        EmergencyContact(
            id = "em_qa",
            country = "قطر",
            flag = "🇶🇦",
            organization = "خط المساعدة للصحة النفسية - مؤسسة حمد الطبية",
            phoneNumber = "16000",
            availableHours = "من 7 ص حتى 3 م (الخيار رقم 4)",
            note = "تقييم ودعم نفسي سري من متخصصين سريريين"
        )
    )

    val ventingTopics = listOf(
        VentingTopic("v_urgent", "دعم عاجل الآن 🚨", "favorite", "أشعر بضيق شديد أو نوبة هلع وأحتاج متطوعاً فوراً"),
        VentingTopic("v_talk", "فضفضة حرة 💬", "chat", "أحتاج أفرغ مشاعري بدون أحكام ومع من يفهمني"),
        VentingTopic("v_anxiety", "قلق وتوتر 🌿", "spa", "تسارع أفكار، خوف من المستقبل أو قلق مستمر"),
        VentingTopic("v_sadness", "حزن وفقد 🌧️", "cloud", "أشعر بثقل في قلبي، شعور بالوحدة أو تجربة فقد"),
        VentingTopic("v_burnout", "ضغوط وعمل 💼", "work", "إنهاك دراسي أو وظيفي وفقدان الشغف"),
        VentingTopic("v_relations", "علاقات وانفصال 🤝", "people", "تحديات عاطفية، خلافات عائلية أو تخطي علاقة")
    )

    private val _specialists = MutableStateFlow<List<Specialist>>(emptyList())
    val specialists: StateFlow<List<Specialist>> = _specialists.asStateFlow()

    private val _bookingSlots = MutableStateFlow<List<BookingSlot>>(emptyList())
    val bookingSlots: StateFlow<List<BookingSlot>> = _bookingSlots.asStateFlow()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    private val _moodHistory = MutableStateFlow<List<MoodCheckIn>>(emptyList())
    val moodHistory: StateFlow<List<MoodCheckIn>> = _moodHistory.asStateFlow()

    private val _volunteerIsOnline = MutableStateFlow(false)
    val volunteerIsOnline: StateFlow<Boolean> = _volunteerIsOnline.asStateFlow()

    private val _pendingVolunteerRequests = MutableStateFlow<List<Conversation>>(emptyList())
    val pendingVolunteerRequests: StateFlow<List<Conversation>> = _pendingVolunteerRequests.asStateFlow()

    fun setVolunteerOnlineStatus(isOnline: Boolean) {
        _volunteerIsOnline.value = isOnline
    }

    fun startOrGetConversation(
        specialist: Specialist,
        topic: String,
        isUrgent: Boolean,
        seekerId: String = "",
        seekerName: String = "مجهول (باحث عن دعم)"
    ): Conversation {
        val existing = _conversations.value.find {
            it.specialistId == specialist.id && it.status == "ACTIVE"
        }
        if (existing != null) {
            return existing
        }

        val convId = "conv_" + UUID.randomUUID().toString().take(8)
        val previewText = if (isUrgent) {
            "طلب دعم نفسي عاجل — $topic"
        } else {
            "تم فتح مساحة المحادثة حول: $topic"
        }

        val newConv = Conversation(
            id = convId,
            specialistId = specialist.id,
            specialistName = specialist.name,
            specialistTitle = specialist.title,
            seekerId = seekerId,
            seekerName = seekerName,
            topic = topic,
            status = "ACTIVE",
            acceptedBySpecialist = false,
            lastMessage = previewText,
            lastMessageTime = Timestamp.now(),
            isUrgent = isUrgent,
            unreadCount = 0L
        )

        _conversations.value = listOf(newConv) + _conversations.value
        return newConv
    }

    fun sendMessage(
        conversationId: String,
        text: String,
        isFromSpecialist: Boolean = false,
        senderName: String = "أنا",
        senderId: String = ""
    ): ChatMessage {
        val msgId = "msg_" + UUID.randomUUID().toString().take(8)
        val now = System.currentTimeMillis()
        val resolvedSenderId = senderId.ifBlank { if (isFromSpecialist) "spec_user" else "seeker_user" }
        val message = ChatMessage(
            id = msgId,
            conversationId = conversationId,
            senderId = resolvedSenderId,
            senderName = senderName,
            isFromSpecialist = isFromSpecialist,
            text = text,
            createdAt = Timestamp.now(),
            timestamp = now
        )

        val currentList = _messages.value[conversationId] ?: emptyList()
        _messages.value = _messages.value + (conversationId to (currentList + message))

        _conversations.value = _conversations.value.map {
            if (it.id == conversationId) {
                it.copy(
                    lastMessage = text,
                    lastMessageTime = Timestamp.now(),
                    unreadCount = if (isFromSpecialist) 1L else 0L
                )
            } else it
        }

        return message
    }

    fun bookSlot(slotId: String, seekerName: String): Boolean {
        var success = false
        _bookingSlots.value = _bookingSlots.value.map {
            if (it.id == slotId && !it.isBooked) {
                success = true
                it.copy(isBooked = true, bookedByName = seekerName)
            } else it
        }
        return success
    }

    fun cancelBooking(slotId: String) {
        _bookingSlots.value = _bookingSlots.value.map {
            if (it.id == slotId) it.copy(isBooked = false, bookedByName = null) else it
        }
    }

    fun addMood(score: Int, label: String, emoji: String, note: String) {
        val entry = MoodCheckIn(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            score = score,
            label = label,
            emoji = emoji,
            note = note
        )
        _moodHistory.value = listOf(entry) + _moodHistory.value
    }

    private val _blockedSpecialistIds = MutableStateFlow<Set<String>>(emptySet())
    val blockedSpecialistIds: StateFlow<Set<String>> = _blockedSpecialistIds.asStateFlow()

    private val _reports = MutableStateFlow<List<com.example.data.model.SpecialistReport>>(emptyList())
    val reports: StateFlow<List<com.example.data.model.SpecialistReport>> = _reports.asStateFlow()

    private val _sessionMoods = MutableStateFlow<List<com.example.data.model.SessionMood>>(emptyList())
    val sessionMoods: StateFlow<List<com.example.data.model.SessionMood>> = _sessionMoods.asStateFlow()

    fun blockSpecialist(userId: String, specialistId: String) {
        _blockedSpecialistIds.value = _blockedSpecialistIds.value + specialistId
        _specialists.value = _specialists.value.filter { it.id != specialistId }
    }

    fun isSpecialistBlocked(specialistId: String): Boolean {
        return _blockedSpecialistIds.value.contains(specialistId)
    }

    fun reportSpecialist(report: com.example.data.model.SpecialistReport) {
        _reports.value = listOf(report) + _reports.value
    }

    fun recordSessionMood(sessionMood: com.example.data.model.SessionMood) {
        _sessionMoods.value = listOf(sessionMood) + _sessionMoods.value
    }

    fun endConversation(conversationId: String) {
        _conversations.value = _conversations.value.map {
            if (it.id == conversationId) {
                it.copy(status = "RESOLVED", updatedAt = Timestamp.now())
            } else it
        }
        val endSystemMsg = ChatMessage(
            id = "msg_" + UUID.randomUUID().toString().take(8),
            conversationId = conversationId,
            senderId = "system",
            senderName = "سند",
            isFromSpecialist = false,
            text = "الجلسة خلصت",
            createdAt = Timestamp.now(),
            timestamp = System.currentTimeMillis()
        )
        val currentList = _messages.value[conversationId] ?: emptyList()
        _messages.value = _messages.value + (conversationId to (currentList + endSystemMsg))
    }

    fun acceptVolunteerRequest(request: Conversation): Conversation {
        _pendingVolunteerRequests.value = _pendingVolunteerRequests.value.filter { it.id != request.id }
        _conversations.value = listOf(request) + _conversations.value
        return request
    }
}
