package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

enum class UserRole {
    SEEKER,      // مستفيد / محتاج دعم نفسي أو فضفضة
    SPECIALIST,  // أخصائي نفسي متطوع
    SUPERVISOR   // مشرف منصة لاعتماد الأخصائيين ومراجعة المؤهلات والبلاغات
}

enum class ConversationStatus {
    ACTIVE,
    RESOLVED
}

enum class ApplicationStatus(val value: String, val labelAr: String) {
    PENDING("PENDING", "قيد المراجعة"),
    APPROVED("APPROVED", "معتمد ومقبول"),
    REJECTED("REJECTED", "مرفوض"),
    SUSPENDED("SUSPENDED", "موقوف مؤقتاً");

    companion object {
        fun fromValue(value: String?): ApplicationStatus {
            return entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

@IgnoreExtraProperties
data class Specialist(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val title: String = "",
    val bio: String = "",
    @get:PropertyName("isOnline") @field:PropertyName("isOnline")
    val isOnline: Boolean = true,
    @get:PropertyName("isVerified") @field:PropertyName("isVerified")
    val isVerified: Boolean = true,
    @get:PropertyName("isSuspended") @field:PropertyName("isSuspended")
    val isSuspended: Boolean = false,
    val qualificationTitle: String = "",
    val applicationId: String = "",
    val rating: Double = 4.9,
    val completedChats: Long = 0L,
    val experienceYears: Long = 5L,
    val tags: List<String> = emptyList(),
    val avatarInitials: String = "س",
    val avatarBgHex: Long = 0xFF134E4A,
    val supportsUrgentCare: Boolean = true,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SpecialistApplication(
    @DocumentId val id: String = "",
    val userId: String = "",
    val applicantEmail: String = "",
    val fullName: String = "",
    val professionalTitle: String = "",
    val specialtyTags: List<String> = emptyList(),
    val experienceYears: Long = 0L,
    val bio: String = "",
    val supportsUrgentCare: Boolean = true,
    val qualificationTitle: String = "",
    val qualificationInstitution: String = "",
    val attachmentFileName: String = "",
    val attachmentMimeType: String = "",
    val attachmentSizeBytes: Long = 0L,
    val attachmentBase64: String = "",
    val status: String = ApplicationStatus.PENDING.value,
    val reviewerId: String? = null,
    val reviewerEmail: String? = null,
    val reviewerNote: String? = null,
    val rejectionReason: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    @ServerTimestamp val reviewedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    @get:PropertyName("isFromSpecialist") @field:PropertyName("isFromSpecialist")
    val isFromSpecialist: Boolean = false,
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val timestamp: Long = System.currentTimeMillis(),
    @get:PropertyName("isQuickAction") @field:PropertyName("isQuickAction")
    val isQuickAction: Boolean = false,
    @get:PropertyName("isVoiceNote") @field:PropertyName("isVoiceNote")
    val isVoiceNote: Boolean = false,
    val voiceDurationSec: Int = 0
)

@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val specialistId: String = "",
    val specialistName: String = "",
    val specialistTitle: String = "",
    val seekerId: String = "",
    val seekerName: String = "مجهول (باحث عن دعم)",
    val topic: String = "",
    val status: String = "ACTIVE",
    val acceptedBySpecialist: Boolean = true,
    val lastMessage: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageFromSpecialist: Boolean = false,
    @ServerTimestamp val lastMessageTime: Timestamp? = null,
    @get:PropertyName("isUrgent") @field:PropertyName("isUrgent")
    val isUrgent: Boolean = false,
    val unreadCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class BookingSlot(
    @DocumentId val id: String = "",
    val specialistId: String = "",
    val specialistName: String = "",
    val dateText: String = "",
    val timeText: String = "",
    val specialtyTitle: String = "",
    @get:PropertyName("isBooked") @field:PropertyName("isBooked")
    val isBooked: Boolean = false,
    val sessionType: String = "جلسة مجدولة مجانية (45 دقيقة)",
    val bookedById: String? = null,
    val bookedByName: String? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class EmergencyContact(
    val id: String,
    val country: String,
    val flag: String,
    val organization: String,
    val phoneNumber: String,
    val availableHours: String,
    val note: String
)

data class MoodCheckIn(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val score: Int = 3,
    val label: String = "",
    val emoji: String = "",
    val note: String = ""
)

data class VentingTopic(
    val id: String,
    val title: String,
    val icon: String,
    val description: String
)

@IgnoreExtraProperties
data class SpecialistReport(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val specialistId: String = "",
    val userId: String = "",
    val reason: String = "",
    val details: String = "",
    val status: String = "OPEN",
    val reviewedNote: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SpecialistBlock(
    @DocumentId val id: String = "",
    val userId: String = "",
    val specialistId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SessionMood(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val userId: String = "",
    val mood: String = "",
    val moodScore: Int = 3,
    val moodEmoji: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
