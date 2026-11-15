package com.example.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SanadFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val conversationId = data["conversationId"].orEmpty()
            .ifBlank { "fcm_${System.currentTimeMillis()}" }
        val senderName = data["senderName"]
            ?: notification?.title
            ?: "رسالة جديدة في كلم سند"
        val body = data["body"]
            ?: notification?.body
            ?: "لديك رسالة جديدة"
        val topic = data["topic"].orEmpty()
        val isUrgent = data["isUrgent"]?.equals("true", ignoreCase = true) == true
        val openAsSpecialist = data["openAsSpecialist"]?.equals("true", ignoreCase = true) == true
        val isNewRequest = data["isNewRequest"]?.equals("true", ignoreCase = true) == true

        SanadNotificationHelper.showIncomingMessageNotification(
            context = applicationContext,
            conversationId = conversationId,
            senderName = senderName,
            topic = topic,
            messageText = body,
            isUrgent = isUrgent,
            openAsSpecialist = openAsSpecialist,
            isNewRequest = isNewRequest
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token refresh hook ready for server-side push integration
    }
}
