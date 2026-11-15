package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.ApplicationStatus
import com.example.data.model.Conversation
import com.example.data.model.SpecialistApplication
import com.example.data.model.SpecialistReport
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class SanadRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private suspend fun verifyCurrentUserEmailInEmulator() = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: error("No user signed in")
        val url = URL("http://$EMULATOR_HOST:$AUTH_PORT/identitytoolkit.googleapis.com/v1/projects/$PROJECT_ID/accounts:update")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer owner")
            doOutput = true
        }
        val body = """{"localId":"${user.uid}","emailVerified":true}"""
        conn.outputStream.use { it.write(body.toByteArray()) }
        val responseCode = conn.responseCode
        val responseBody = (if (responseCode in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader()?.use { it.readText() } ?: ""
        conn.disconnect()
        assertEquals("Auth emulator admin update failed: $responseBody", 200, responseCode)
        user.reload().await()
        assertTrue("Expected emailVerified=true after reload, got false", user.isEmailVerified)
        user.getIdToken(true).await()
    }

    @Test
    fun testSpecialistApplicationSupervisorApprovalAndSuspensionFlow() = runBlocking {
        // 1. Applicant submits qualification application
        val applicantEmail = "applicant_${UUID.randomUUID().toString().take(6)}@test.com"
        val applicantUid = signInTestUser(applicantEmail)
        val applicantRepo = SanadFirestoreRepository(firestore)

        val application = SpecialistApplication(
            userId = applicantUid,
            applicantEmail = applicantEmail,
            fullName = "د. علياء محمد",
            professionalTitle = "أخصائية علاج نفسي إكلينيكي",
            specialtyTags = listOf("قلق وتوتر", "CBT"),
            experienceYears = 7L,
            bio = "متخصصة في الدعم النفسي الأولي والعلاج المعرفي السلوكي",
            supportsUrgentCare = true,
            qualificationTitle = "ماجستير علم النفس الإكلينيكي",
            qualificationInstitution = "جامعة القاهرة",
            attachmentFileName = "qualification.jpg",
            attachmentMimeType = "image/jpeg",
            attachmentSizeBytes = 1234L,
            attachmentBase64 = "ZmFrZV9xdWFsaWZpY2F0aW9uX2ltYWdl"
        )
        withTimeout(5000L) {
            applicantRepo.submitSpecialistApplication(application)
        }

        val observedApp = withTimeout(4000L) {
            applicantRepo.observeMySpecialistApplication(applicantUid).first { it != null }
        }
        assertNotNull(observedApp)
        assertEquals(ApplicationStatus.PENDING.value, observedApp?.status)

        // Applicant cannot self-create approved specialist profile
        try {
            withTimeout(5000L) {
                firestore.collection("specialists").document(applicantUid).set(
                    mapOf(
                        "userId" to applicantUid,
                        "name" to "د. علياء محمد",
                        "title" to "أخصائية",
                        "isOnline" to true,
                        "isVerified" to true
                    )
                ).await()
            }
            fail("Non-supervisor must not be allowed to publish specialists/{uid}")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }

        // 2. Supervisor signs in (verified email) and approves after reviewing qualification
        auth.signOut()
        signInTestUser("ahmedabdoshouq@gmail.com")
        verifyCurrentUserEmailInEmulator()
        val supervisorRepo = SanadFirestoreRepository(firestore)

        val allApps = withTimeout(4000L) {
            supervisorRepo.observeAllSpecialistApplications().first { list -> list.any { it.userId == applicantUid } }
        }
        val targetApp = allApps.first { it.userId == applicantUid }
        assertEquals("ZmFrZV9xdWFsaWZpY2F0aW9uX2ltYWdl", targetApp.attachmentBase64)

        withTimeout(5000L) {
            supervisorRepo.approveSpecialistApplication(targetApp, "تم الاطلاع على شهادة الماجستير واعتماد الحساب")
        }

        // 3. Approved specialist signs back in and toggles online status & manages slots
        auth.signOut()
        signInTestUser(applicantEmail)
        val specialistRepo = SanadFirestoreRepository(firestore)

        withTimeout(5000L) {
            specialistRepo.setSpecialistOnline(applicantUid, true)
        }
        val specDoc = withTimeout(4000L) {
            firestore.collection("specialists").document(applicantUid).get().await()
        }
        assertEquals(true, specDoc.getBoolean("isOnline"))
        assertEquals(true, specDoc.getBoolean("isVerified"))

        val slotId = withTimeout(5000L) {
            specialistRepo.createBookingSlot(
                specialistName = "د. علياء محمد",
                dateText = "غداً - الخميس",
                timeText = "07:00 م - 07:45 م",
                specialtyTitle = "جلسة دعم القلق"
            )
        }
        assertTrue(slotId.isNotBlank())
        withTimeout(5000L) {
            specialistRepo.deleteBookingSlot(slotId)
        }

        // 4. Supervisor suspends specialist; specialist can no longer set themselves online
        auth.signOut()
        signInTestUser("ahmedabdoshouq@gmail.com")
        verifyCurrentUserEmailInEmulator()
        withTimeout(5000L) {
            supervisorRepo.setSpecialistSuspended(applicantUid, true)
        }

        auth.signOut()
        signInTestUser(applicantEmail)
        try {
            withTimeout(5000L) {
                specialistRepo.setSpecialistOnline(applicantUid, true)
            }
            fail("Suspended specialist must not be allowed to go online")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    @Test
    fun testConversationMessageAndReportOversightFlow() = runBlocking {
        val specEmail = "spec_${UUID.randomUUID().toString().take(6)}@test.com"
        val specUid = signInTestUser(specEmail)
        auth.signOut()

        val seekerUid = signInTestUser("seeker_${UUID.randomUUID().toString().take(6)}@test.com")
        val seekerRepo = SanadFirestoreRepository(firestore)

        val convId = "conv_${UUID.randomUUID().toString().take(6)}"
        val conv = Conversation(
            id = convId,
            specialistId = specUid,
            specialistName = "د. سارة",
            seekerId = seekerUid,
            topic = "فضفضة وقلق",
            status = "ACTIVE"
        )
        val createdId = withTimeout(5000L) { seekerRepo.createConversation(conv) }
        assertEquals(convId, createdId)

        val msgId = withTimeout(5000L) {
            seekerRepo.sendMessage(
                conversationId = convId,
                receiverId = specUid,
                text = "أشعر بضيق مفاجئ وأحتاج للمساعدة",
                isFromSpecialist = false,
                senderName = "باحث عن دعم"
            )
        }
        assertNotNull(msgId)

        // Specialist replies in the same conversation
        auth.signOut()
        signInTestUser(specEmail)
        val specRepo = SanadFirestoreRepository(firestore)
        withTimeout(5000L) {
            specRepo.sendMessage(
                conversationId = convId,
                receiverId = seekerUid,
                text = "أهلاً بك، أنا هنا للاستماع إليك",
                isFromSpecialist = true,
                senderName = "د. سارة"
            )
        }

        val msgs = withTimeout(4000L) {
            specRepo.observeMessages(convId).first { it.size >= 2 }
        }
        val seekerMsg = msgs.first { it.text.contains("أشعر بضيق") }
        val specialistMsg = msgs.first { it.text.contains("أهلاً بك") }
        assertEquals(seekerUid, seekerMsg.senderId)
        assertEquals(false, seekerMsg.isFromSpecialist)
        assertEquals(specUid, specialistMsg.senderId)
        assertEquals(true, specialistMsg.isFromSpecialist)

        auth.signOut()
        signInTestUser("seeker_${UUID.randomUUID().toString().take(6)}@test.com")
        val repo = SanadFirestoreRepository(firestore)

        val reportId = withTimeout(5000L) {
            repo.submitReport(
                SpecialistReport(
                    conversationId = convId,
                    specialistId = "spec_volunteer_1",
                    userId = seekerUid,
                    reason = "كلام مش مناسب",
                    details = "تفاصيل تجريبية"
                )
            )
        }
        assertTrue(reportId.isNotBlank())

        // Supervisor can observe and mark report reviewed
        auth.signOut()
        signInTestUser("ahmedabdoshouq@gmail.com")
        verifyCurrentUserEmailInEmulator()
        val supervisorRepo = SanadFirestoreRepository(firestore)
        val reports = withTimeout(4000L) {
            supervisorRepo.observeAllReports().first { list -> list.any { it.id == reportId } }
        }
        assertTrue(reports.any { it.id == reportId })
        withTimeout(5000L) {
            supervisorRepo.markReportReviewed(reportId, "REVIEWED", "تم الفحص")
        }
    }

    @Test
    fun testUnauthenticatedAccessFails() = runBlocking {
        auth.signOut()
        val repo = SanadFirestoreRepository(firestore)

        try {
            repo.requireUserId()
            assertTrue("Should throw for unauthenticated user", false)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("تسجيل الدخول") == true)
        }
    }
}
