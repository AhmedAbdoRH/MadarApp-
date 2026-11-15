package com.example.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.repository.SanadFirestoreRepository
import com.example.data.repository.SanadRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class SanadMessageCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val currentUser = Firebase.auth.currentUser ?: return Result.success()
        val uid = currentUser.uid

        if (!SanadNotificationHelper.isNotificationsEnabled(applicationContext) ||
            !SanadNotificationHelper.hasNotificationPermission(applicationContext)
        ) {
            return Result.success()
        }

        return try {
            val repo = SanadFirestoreRepository(applicationContext)
            val (asSeeker, asSpecialist) = repo.fetchConversationsForBackgroundCheck(uid)

            asSeeker
                .filter {
                    it.id !in SanadRepository.DEMO_CONVERSATION_IDS &&
                        it.specialistId !in SanadRepository.DEMO_SPECIALIST_IDS &&
                        it.status == "ACTIVE"
                }
                .forEach { conv ->
                    val msgMillis = conv.lastMessageTime?.toDate()?.time
                        ?: conv.updatedAt?.toDate()?.time
                        ?: 0L
                    val lastSeen = SanadNotificationHelper.getLastNotifiedMillis(applicationContext, conv.id)
                    val isFromOther = conv.lastMessageSenderId.isNotBlank() &&
                        conv.lastMessageSenderId != uid
                    if (isFromOther && msgMillis > lastSeen && conv.lastMessage.isNotBlank()) {
                        SanadNotificationHelper.showIncomingMessageNotification(
                            context = applicationContext,
                            conversationId = conv.id,
                            senderName = conv.specialistName.ifBlank { "الأخصائي المتطوع" },
                            topic = conv.topic,
                            messageText = conv.lastMessage,
                            isUrgent = conv.isUrgent,
                            openAsSpecialist = false
                        )
                        SanadNotificationHelper.saveLastNotifiedMillis(applicationContext, conv.id, msgMillis)
                    }
                }

            asSpecialist
                .filter {
                    it.id !in SanadRepository.DEMO_CONVERSATION_IDS &&
                        it.status == "ACTIVE"
                }
                .forEach { conv ->
                    val msgMillis = conv.lastMessageTime?.toDate()?.time
                        ?: conv.updatedAt?.toDate()?.time
                        ?: conv.createdAt?.toDate()?.time
                        ?: 0L
                    val lastSeen = SanadNotificationHelper.getLastNotifiedMillis(applicationContext, conv.id)
                    val isNewRequest = !conv.acceptedBySpecialist && lastSeen == 0L
                    val isFromSeeker = conv.lastMessageSenderId.isNotBlank() &&
                        conv.lastMessageSenderId != uid
                    if ((isNewRequest || isFromSeeker) && msgMillis > lastSeen) {
                        SanadNotificationHelper.showIncomingMessageNotification(
                            context = applicationContext,
                            conversationId = conv.id,
                            senderName = conv.seekerName.ifBlank { "مستفيد" },
                            topic = conv.topic,
                            messageText = conv.lastMessage,
                            isUrgent = conv.isUrgent,
                            openAsSpecialist = true,
                            isNewRequest = isNewRequest
                        )
                        SanadNotificationHelper.saveLastNotifiedMillis(applicationContext, conv.id, msgMillis)
                    }
                }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
