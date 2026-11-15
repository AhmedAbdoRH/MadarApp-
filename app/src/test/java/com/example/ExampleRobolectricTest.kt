package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Specialist
import com.example.data.model.UserRole
import com.example.data.repository.SanadRepository
import com.example.ui.SanadViewModel
import com.example.base.FirestoreEmulatorTestBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest : FirestoreEmulatorTestBase() {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("كلم سند", appName)
  }

  @Test
  fun `verify no demo specialists or demo slots are preloaded`() {
    assertTrue("Should not ship hardcoded demo specialists", SanadRepository.specialists.value.isEmpty())
    assertTrue("Should not ship hardcoded demo booking slots", SanadRepository.bookingSlots.value.isEmpty())
    assertTrue(
      "Should not ship hardcoded welcome or demo conversations",
      SanadRepository.conversations.value.none { it.id in SanadRepository.DEMO_CONVERSATION_IDS }
    )
  }

  @Test
  fun `verify conversation creation and message flow for approved specialist`() {
    val approvedSpec = Specialist(
      id = "uid_approved_1",
      userId = "uid_approved_1",
      name = "د. أحمد حسن",
      title = "أخصائي دعم نفسي",
      isOnline = true,
      isVerified = true,
      isSuspended = false
    )
    val conv = SanadRepository.startOrGetConversation(
      specialist = approvedSpec,
      topic = "قلق وتوتر",
      isUrgent = false,
      seekerId = "seeker_1"
    )
    assertFalse("New conversation should await specialist acceptance", conv.acceptedBySpecialist)

    val sentMsg = SanadRepository.sendMessage(
      conversationId = conv.id,
      text = "أحتاج مساحة للفضفضة اليوم",
      isFromSpecialist = false,
      senderName = "أنا"
    )
    assertEquals("أحتاج مساحة للفضفضة اليوم", sentMsg.text)

    SanadRepository.endConversation(conv.id)
    val updatedConv = SanadRepository.conversations.value.first { it.id == conv.id }
    assertEquals("RESOLVED", updatedConv.status)
  }

  @Test
  fun `verify role switching behavior`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = SanadViewModel(app)
    viewModel.setRole(UserRole.SPECIALIST)
    assertEquals(UserRole.SPECIALIST, viewModel.uiState.value.currentRole)
    assertFalse("Volunteer online status defaults to false without approved profile", viewModel.volunteerIsOnline.value)
  }

  @Test
  fun `verify notification helper preferences, timestamps, and channels`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.notifications.SanadNotificationHelper.ensureNotificationChannels(context)
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    val messagesChannel = manager.getNotificationChannel(com.example.notifications.SanadNotificationHelper.CHANNEL_MESSAGES)
    val urgentChannel = manager.getNotificationChannel(com.example.notifications.SanadNotificationHelper.CHANNEL_URGENT)
    assertTrue("Messages channel should exist", messagesChannel != null)
    assertTrue("Urgent channel should exist", urgentChannel != null)

    assertTrue("Notifications should be enabled by default", com.example.notifications.SanadNotificationHelper.isNotificationsEnabled(context))
    com.example.notifications.SanadNotificationHelper.setNotificationsEnabled(context, false)
    assertFalse("Notifications toggle should persist disabled state", com.example.notifications.SanadNotificationHelper.isNotificationsEnabled(context))
    com.example.notifications.SanadNotificationHelper.setNotificationsEnabled(context, true)
    assertTrue("Notifications toggle should persist enabled state", com.example.notifications.SanadNotificationHelper.isNotificationsEnabled(context))

    com.example.notifications.SanadNotificationHelper.saveLastNotifiedMillis(context, "conv_notif_test", 1500L)
    assertEquals(1500L, com.example.notifications.SanadNotificationHelper.getLastNotifiedMillis(context, "conv_notif_test"))
    // Earlier timestamp must not overwrite newer timestamp
    com.example.notifications.SanadNotificationHelper.saveLastNotifiedMillis(context, "conv_notif_test", 1000L)
    assertEquals(1500L, com.example.notifications.SanadNotificationHelper.getLastNotifiedMillis(context, "conv_notif_test"))
  }

  @Test
  fun `verify ViewModel notification deep link and toggle updates uiState`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = SanadViewModel(app)

    viewModel.setNotificationsEnabled(false)
    assertFalse(viewModel.uiState.value.notificationsEnabled)
    viewModel.setNotificationsEnabled(true)
    assertTrue(viewModel.uiState.value.notificationsEnabled)

    viewModel.openConversationFromNotification(
      conversationId = "conv_deep_link_1",
      openAsSpecialist = true
    )
    assertEquals(UserRole.SPECIALIST, viewModel.uiState.value.currentRole)
    assertEquals(com.example.ui.MainTab.SPECIALIST_DASHBOARD, viewModel.uiState.value.selectedTab)
    assertEquals("conv_deep_link_1", viewModel.uiState.value.selectedConversationId)

    viewModel.openConversationFromNotification(
      conversationId = "conv_deep_link_2",
      openAsSpecialist = false
    )
    assertEquals(UserRole.SEEKER, viewModel.uiState.value.currentRole)
    assertEquals(com.example.ui.MainTab.CHATS, viewModel.uiState.value.selectedTab)
    assertEquals("conv_deep_link_2", viewModel.uiState.value.selectedConversationId)
  }
}
