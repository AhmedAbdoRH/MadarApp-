package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun emergencyConfig_containsAccurateEgyptHotlines() {
    assertEquals("EG", com.example.data.config.EmergencyConfig.COUNTRY)
    val contacts = com.example.data.config.EmergencyConfig.contacts
    assertEquals(4, contacts.size)

    val ambulance = contacts.first { it.id == "ambulance" }
    assertEquals("123", ambulance.number)
    assertEquals("الإسعاف: 123", ambulance.label)
    assertTrue(ambulance.isPrimaryAmbulance)

    val mentalHealth = contacts.first { it.id == "mental_health_hotline" }
    assertEquals("16328", mentalHealth.number)

    val addiction = contacts.first { it.id == "addiction_hotline" }
    assertEquals("16023", addiction.number)

    val secretariat = contacts.first { it.id == "mental_health_secretariat" }
    assertEquals("0220816831", secretariat.number)
  }

  @Test
  fun waitingScreen_hasExactWaitTimeoutConstant() {
    assertEquals(75, com.example.ui.screens.WAIT_TIMEOUT_SECONDS)
  }

  @Test
  fun welcomeConsent_ageOptionsIntegrity() {
    val adultOption = com.example.ui.screens.AgeOption.ADULT
    val under18Option = com.example.ui.screens.AgeOption.UNDER_18
    assertEquals("ADULT", adultOption.name)
    assertEquals("UNDER_18", under18Option.name)
  }

  @Test
  fun breathingExercise_constantsVerification() {
    assertEquals(4, com.example.ui.components.INHALE_SECONDS)
    assertEquals(7, com.example.ui.components.HOLD_SECONDS)
    assertEquals(8, com.example.ui.components.EXHALE_SECONDS)
    assertEquals(4, com.example.ui.components.TOTAL_CYCLES)
  }

  @Test
  fun countOnlineSpecialists_reflectsLiveAvailabilityAccurately() {
    val allOffline = listOf(
      com.example.data.model.Specialist(id = "1", name = "A", isOnline = false),
      com.example.data.model.Specialist(id = "2", name = "B", isOnline = false)
    )
    assertEquals(0, com.example.ui.countOnlineSpecialists(allOffline))
    assertEquals(0, com.example.ui.countOnlineSpecialists(emptyList()))

    val mixed = listOf(
      com.example.data.model.Specialist(id = "1", name = "A", isOnline = true),
      com.example.data.model.Specialist(id = "2", name = "B", isOnline = false),
      com.example.data.model.Specialist(id = "3", name = "C", isOnline = true)
    )
    assertEquals(2, com.example.ui.countOnlineSpecialists(mixed))
  }

  @Test
  fun applicationStatus_parsesAllLifecycleStates() {
    assertEquals(
      com.example.data.model.ApplicationStatus.PENDING,
      com.example.data.model.ApplicationStatus.fromValue("PENDING")
    )
    assertEquals(
      com.example.data.model.ApplicationStatus.APPROVED,
      com.example.data.model.ApplicationStatus.fromValue("approved")
    )
    assertEquals(
      com.example.data.model.ApplicationStatus.REJECTED,
      com.example.data.model.ApplicationStatus.fromValue("REJECTED")
    )
    assertEquals(
      com.example.data.model.ApplicationStatus.SUSPENDED,
      com.example.data.model.ApplicationStatus.fromValue("SUSPENDED")
    )
    assertEquals(
      com.example.data.model.ApplicationStatus.PENDING,
      com.example.data.model.ApplicationStatus.fromValue(null)
    )
  }

  @Test
  fun seekerVisibleSpecialists_excludesSuspendedAndUnverified() {
    val raw = listOf(
      com.example.data.model.Specialist(id = "s1", name = "د. سارة", isOnline = true, isVerified = true, isSuspended = false),
      com.example.data.model.Specialist(id = "s2", name = "د. علي", isOnline = true, isVerified = false, isSuspended = false),
      com.example.data.model.Specialist(id = "s3", name = "د. نور", isOnline = true, isVerified = true, isSuspended = true)
    )
    val visible = raw.filter { it.isVerified && !it.isSuspended }
    assertEquals(1, visible.size)
    assertEquals("s1", visible.first().id)
    assertEquals(1, com.example.ui.countOnlineSpecialists(visible))
  }

  @Test
  fun sanadRepository_hasNoPreloadedDemoSpecialistsOrSlots() {
    assertTrue(com.example.data.repository.SanadRepository.DEMO_SPECIALIST_IDS.contains("spec_1"))
    assertTrue(com.example.data.repository.SanadRepository.DEMO_SPECIALIST_IDS.contains("spec_5"))
    assertTrue(com.example.data.repository.SanadRepository.specialists.value.isEmpty())
    assertTrue(com.example.data.repository.SanadRepository.bookingSlots.value.isEmpty())
  }

  @Test
  fun resolveIsIncomingMessage_separatesSenderAndReceiverInBothDirections() {
    val conv = com.example.data.model.Conversation(
      id = "conv_dir_1",
      seekerId = "seeker_uid",
      specialistId = "spec_uid"
    )
    val seekerMsg = com.example.data.model.ChatMessage(
      id = "m1",
      conversationId = conv.id,
      senderId = "seeker_uid",
      isFromSpecialist = false,
      text = "مرحبا"
    )
    val specialistMsg = com.example.data.model.ChatMessage(
      id = "m2",
      conversationId = conv.id,
      senderId = "spec_uid",
      isFromSpecialist = true,
      text = "أهلاً بك"
    )
    val legacySpecialistMsgWithoutFlag = specialistMsg.copy(id = "m3", isFromSpecialist = false)

    // Seeker viewing conversation
    assertFalse(
      com.example.ui.screens.resolveIsIncomingMessage(seekerMsg, "seeker_uid", conv, isSpecialistViewer = false)
    )
    assertTrue(
      com.example.ui.screens.resolveIsIncomingMessage(specialistMsg, "seeker_uid", conv, isSpecialistViewer = false)
    )
    // Even if legacy doc failed to deserialize isFromSpecialist, senderId distinguishes it!
    assertTrue(
      com.example.ui.screens.resolveIsIncomingMessage(legacySpecialistMsgWithoutFlag, "seeker_uid", conv, isSpecialistViewer = false)
    )

    // Specialist viewing conversation
    assertTrue(
      com.example.ui.screens.resolveIsIncomingMessage(seekerMsg, "spec_uid", conv, isSpecialistViewer = true)
    )
    assertFalse(
      com.example.ui.screens.resolveIsIncomingMessage(specialistMsg, "spec_uid", conv, isSpecialistViewer = true)
    )
  }
}
