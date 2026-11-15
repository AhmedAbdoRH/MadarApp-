package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firestore.OperationType
import com.example.data.firestore.handleFirestoreError
import com.example.data.model.ApplicationStatus
import com.example.data.model.BookingSlot
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.Specialist
import com.example.data.model.SpecialistApplication
import com.example.data.model.SpecialistReport
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SanadFirestoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            FirebaseApp.getInstance(),
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("يجب تسجيل الدخول باستخدام حساب Google أولاً.")
    }

    // --- Specialists ---
    fun observeSpecialists(): Flow<List<Specialist>> {
        val path = "specialists"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Specialist::class.java)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun setSpecialistOnline(specialistId: String, isOnline: Boolean) {
        val uid = requireUserId()
        val path = "specialists/$specialistId"
        try {
            db.collection("specialists").document(specialistId).update(
                mapOf(
                    "isOnline" to isOnline,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    suspend fun cleanupDemoSeedDocsIfAdmin() {
        for (specId in SanadRepository.DEMO_SPECIALIST_IDS) {
            try {
                db.collection("specialists").document(specId).delete().await()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Best effort cleanup for already-deleted or permission-restricted docs
            }
        }
        for (slotId in SanadRepository.DEMO_SLOT_IDS) {
            try {
                db.collection("bookingSlots").document(slotId).delete().await()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Best effort cleanup
            }
        }
    }

    // --- Conversations ---
    fun observeUserConversations(userId: String): Flow<List<Conversation>> {
        val path = "conversations"
        return db.collection(path)
            .whereEqualTo("seekerId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Conversation::class.java)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeSpecialistConversations(specialistId: String): Flow<List<Conversation>> {
        val path = "conversations"
        return db.collection(path)
            .whereEqualTo("specialistId", specialistId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Conversation::class.java)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun createConversation(conv: Conversation): String {
        val uid = requireUserId()
        val docRef = if (conv.id.isNotBlank()) {
            db.collection("conversations").document(conv.id)
        } else {
            db.collection("conversations").document()
        }
        val path = "conversations/${docRef.id}"
        val payload = mapOf(
            "specialistId" to conv.specialistId,
            "specialistName" to conv.specialistName,
            "specialistTitle" to conv.specialistTitle,
            "seekerId" to uid,
            "seekerName" to conv.seekerName,
            "topic" to conv.topic,
            "status" to conv.status,
            "acceptedBySpecialist" to conv.acceptedBySpecialist,
            "lastMessage" to conv.lastMessage,
            "lastMessageSenderId" to uid,
            "lastMessageFromSpecialist" to false,
            "lastMessageTime" to FieldValue.serverTimestamp(),
            "isUrgent" to conv.isUrgent,
            "unreadCount" to 0L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun acceptSpecialistConversation(conversationId: String) {
        requireUserId()
        val path = "conversations/$conversationId"
        try {
            db.collection("conversations").document(conversationId).update(
                mapOf(
                    "acceptedBySpecialist" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    // --- Messages ---
    fun observeMessages(conversationId: String): Flow<List<ChatMessage>> {
        val path = "conversations/$conversationId/messages"
        return db.collection("conversations").document(conversationId).collection("messages")
            .orderBy("createdAt")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ChatMessage::class.java)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun sendMessage(
        conversationId: String,
        receiverId: String,
        text: String,
        isFromSpecialist: Boolean,
        senderName: String
    ): String {
        val uid = requireUserId()
        val msgRef = db.collection("conversations").document(conversationId).collection("messages").document()
        val path = "conversations/$conversationId/messages/${msgRef.id}"
        val payload = mapOf(
            "conversationId" to conversationId,
            "senderId" to uid,
            "senderName" to senderName,
            "receiverId" to receiverId,
            "text" to text,
            "isFromSpecialist" to isFromSpecialist,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            msgRef.set(payload).await()
            // Update parent conversation
            db.collection("conversations").document(conversationId).update(
                mapOf(
                    "lastMessage" to text,
                    "lastMessageSenderId" to uid,
                    "lastMessageFromSpecialist" to isFromSpecialist,
                    "lastMessageTime" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            return msgRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    // --- Booking Slots ---
    fun observeBookingSlots(): Flow<List<BookingSlot>> {
        val path = "bookingSlots"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(BookingSlot::class.java)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun bookSlot(slotId: String, seekerName: String) {
        val uid = requireUserId()
        val path = "bookingSlots/$slotId"
        try {
            db.collection("bookingSlots").document(slotId).update(
                mapOf(
                    "isBooked" to true,
                    "bookedById" to uid,
                    "bookedByName" to seekerName,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    suspend fun cancelBooking(slotId: String) {
        val uid = requireUserId()
        val path = "bookingSlots/$slotId"
        try {
            db.collection("bookingSlots").document(slotId).update(
                mapOf(
                    "isBooked" to false,
                    "bookedById" to null,
                    "bookedByName" to null,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }



    // --- End Conversation ---
    suspend fun endConversation(conversationId: String) {
        val path = "conversations/$conversationId"
        try {
            db.collection("conversations").document(conversationId).update(
                mapOf(
                    "status" to "RESOLVED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val msgRef = db.collection("conversations").document(conversationId).collection("messages").document()
            msgRef.set(
                mapOf(
                    "conversationId" to conversationId,
                    "senderId" to requireUserId(),
                    "senderName" to "سند",
                    "receiverId" to "",
                    "text" to "الجلسة خلصت",
                    "isFromSpecialist" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    // --- Reports ---
    suspend fun submitReport(report: com.example.data.model.SpecialistReport): String {
        val uid = requireUserId()
        val docRef = db.collection("reports").document()
        val path = "reports/${docRef.id}"
        val payload = mapOf(
            "conversationId" to report.conversationId,
            "specialistId" to report.specialistId,
            "userId" to uid,
            "reason" to report.reason,
            "details" to report.details,
            "status" to "OPEN",
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    // --- Blocks ---
    suspend fun blockSpecialist(specialistId: String): String {
        val uid = requireUserId()
        val docRef = db.collection("blocks").document()
        val path = "blocks/${docRef.id}"
        val payload = mapOf(
            "userId" to uid,
            "specialistId" to specialistId,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    fun observeUserBlocks(userId: String): Flow<List<com.example.data.model.SpecialistBlock>> {
        val path = "blocks"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(com.example.data.model.SpecialistBlock::class.java)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    // --- Session Moods ---
    suspend fun recordSessionMood(sessionMood: com.example.data.model.SessionMood): String {
        val uid = requireUserId()
        val docRef = db.collection("session_moods").document()
        val path = "session_moods/${docRef.id}"
        val payload = mapOf(
            "conversationId" to sessionMood.conversationId,
            "userId" to uid,
            "mood" to sessionMood.mood,
            "moodScore" to sessionMood.moodScore,
            "moodEmoji" to sessionMood.moodEmoji,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    // --- Specialist Applications & Qualification Review ---
    fun observeMySpecialistApplication(userId: String): Flow<SpecialistApplication?> {
        val path = "specialist_applications/$userId"
        return db.collection("specialist_applications").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) snapshot.toObject(SpecialistApplication::class.java) else null
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.GET, path)
                }
                throw error
            }
    }

    suspend fun submitSpecialistApplication(application: SpecialistApplication) {
        val uid = requireUserId()
        val applicantEmail = auth.currentUser?.email ?: application.applicantEmail
        val docRef = db.collection("specialist_applications").document(uid)
        val path = "specialist_applications/$uid"
        try {
            val existingSnap = docRef.get().await()
            val baseFields = mutableMapOf<String, Any?>(
                "userId" to uid,
                "applicantEmail" to applicantEmail,
                "fullName" to application.fullName.trim(),
                "professionalTitle" to application.professionalTitle.trim(),
                "specialtyTags" to application.specialtyTags,
                "experienceYears" to application.experienceYears,
                "bio" to application.bio.trim(),
                "supportsUrgentCare" to application.supportsUrgentCare,
                "qualificationTitle" to application.qualificationTitle.trim(),
                "qualificationInstitution" to application.qualificationInstitution.trim(),
                "attachmentFileName" to application.attachmentFileName,
                "attachmentMimeType" to application.attachmentMimeType,
                "attachmentSizeBytes" to application.attachmentSizeBytes,
                "attachmentBase64" to application.attachmentBase64,
                "status" to ApplicationStatus.PENDING.value,
                "reviewerId" to null,
                "reviewerEmail" to null,
                "reviewerNote" to null,
                "rejectionReason" to null,
                "reviewedAt" to null,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (existingSnap.exists()) {
                docRef.update(baseFields).await()
            } else {
                baseFields["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(baseFields.filterValues { it != null }).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    suspend fun withdrawSpecialistApplication() {
        val uid = requireUserId()
        val path = "specialist_applications/$uid"
        try {
            db.collection("specialist_applications").document(uid).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    fun observeAllSpecialistApplications(): Flow<List<SpecialistApplication>> {
        val path = "specialist_applications"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SpecialistApplication::class.java)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun approveSpecialistApplication(
        application: SpecialistApplication,
        reviewerNote: String? = null
    ) {
        val supervisorUid = requireUserId()
        val supervisorEmail = auth.currentUser?.email ?: ""
        val targetUid = application.userId.ifBlank { application.id }
        val appPath = "specialist_applications/$targetUid"
        try {
            val appRef = db.collection("specialist_applications").document(targetUid)
            val specRef = db.collection("specialists").document(targetUid)
            val initials = buildAvatarInitials(application.fullName)

            val batch = db.batch()
            batch.update(
                appRef,
                mapOf(
                    "status" to ApplicationStatus.APPROVED.value,
                    "reviewerId" to supervisorUid,
                    "reviewerEmail" to supervisorEmail,
                    "reviewerNote" to reviewerNote?.trim()?.ifBlank { null },
                    "rejectionReason" to null,
                    "reviewedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                specRef,
                mapOf(
                    "userId" to targetUid,
                    "name" to application.fullName,
                    "title" to application.professionalTitle,
                    "bio" to application.bio,
                    "isOnline" to false,
                    "isVerified" to true,
                    "isSuspended" to false,
                    "qualificationTitle" to application.qualificationTitle,
                    "applicationId" to targetUid,
                    "rating" to 5.0,
                    "completedChats" to 0L,
                    "experienceYears" to application.experienceYears,
                    "tags" to application.specialtyTags,
                    "avatarInitials" to initials,
                    "avatarBgHex" to 0xFF134E4AL,
                    "supportsUrgentCare" to application.supportsUrgentCare,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, appPath)
            throw e
        }
    }

    suspend fun rejectSpecialistApplication(
        applicantUserId: String,
        reason: String
    ) {
        val supervisorUid = requireUserId()
        val supervisorEmail = auth.currentUser?.email ?: ""
        val path = "specialist_applications/$applicantUserId"
        try {
            db.collection("specialist_applications").document(applicantUserId).update(
                mapOf(
                    "status" to ApplicationStatus.REJECTED.value,
                    "rejectionReason" to reason.trim(),
                    "reviewerId" to supervisorUid,
                    "reviewerEmail" to supervisorEmail,
                    "reviewedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    suspend fun setSpecialistSuspended(specialistId: String, suspended: Boolean) {
        val supervisorUid = requireUserId()
        val supervisorEmail = auth.currentUser?.email ?: ""
        val specPath = "specialists/$specialistId"
        try {
            val specUpdates = mutableMapOf<String, Any>(
                "isSuspended" to suspended,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (suspended) {
                specUpdates["isOnline"] = false
            }
            db.collection("specialists").document(specialistId).update(specUpdates).await()

            val appRef = db.collection("specialist_applications").document(specialistId)
            val appSnap = appRef.get().await()
            if (appSnap.exists()) {
                appRef.update(
                    mapOf(
                        "status" to if (suspended) ApplicationStatus.SUSPENDED.value else ApplicationStatus.APPROVED.value,
                        "reviewerId" to supervisorUid,
                        "reviewerEmail" to supervisorEmail,
                        "reviewedAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, specPath)
            throw e
        }
    }

    suspend fun updateMySpecialistProfile(
        specialistId: String,
        title: String,
        bio: String,
        tags: List<String>,
        experienceYears: Long,
        supportsUrgentCare: Boolean
    ) {
        requireUserId()
        val path = "specialists/$specialistId"
        try {
            db.collection("specialists").document(specialistId).update(
                mapOf(
                    "title" to title.trim(),
                    "bio" to bio.trim(),
                    "tags" to tags,
                    "experienceYears" to experienceYears,
                    "supportsUrgentCare" to supportsUrgentCare,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    // --- Specialist Booking Slot Management ---
    suspend fun createBookingSlot(
        specialistName: String,
        dateText: String,
        timeText: String,
        specialtyTitle: String
    ): String {
        val uid = requireUserId()
        val docRef = db.collection("bookingSlots").document()
        val path = "bookingSlots/${docRef.id}"
        val payload = mapOf(
            "specialistId" to uid,
            "specialistName" to specialistName.trim(),
            "dateText" to dateText.trim(),
            "timeText" to timeText.trim(),
            "specialtyTitle" to specialtyTitle.trim(),
            "isBooked" to false,
            "bookedById" to null,
            "bookedByName" to null,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun deleteBookingSlot(slotId: String) {
        requireUserId()
        val path = "bookingSlots/$slotId"
        try {
            db.collection("bookingSlots").document(slotId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    // --- Supervisor Reports & Admin Check ---
    fun observeAllReports(): Flow<List<SpecialistReport>> {
        val path = "reports"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SpecialistReport::class.java)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun markReportReviewed(reportId: String, status: String, note: String?) {
        requireUserId()
        val path = "reports/$reportId"
        try {
            db.collection("reports").document(reportId).update(
                mapOf(
                    "status" to status,
                    "reviewedNote" to note?.trim()?.ifBlank { null }
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    suspend fun fetchConversationsForBackgroundCheck(uid: String): Pair<List<Conversation>, List<Conversation>> {
        val asSeeker = db.collection("conversations")
            .whereEqualTo("seekerId", uid)
            .get()
            .await()
            .toObjects(Conversation::class.java)
        val asSpecialist = db.collection("conversations")
            .whereEqualTo("specialistId", uid)
            .get()
            .await()
            .toObjects(Conversation::class.java)
        return asSeeker to asSpecialist
    }

    suspend fun isUserInAdminsCollection(uid: String): Boolean {
        return try {
            db.collection("admins").document(uid).get().await().exists()
        } catch (_: Exception) {
            false
        }
    }

    private fun buildAvatarInitials(fullName: String): String {
        val parts = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            parts.size >= 2 -> "${parts[0].first()}.${parts[1].first()}"
            parts.size == 1 -> parts[0].take(2)
            else -> "أ"
        }.take(6)
    }
}
