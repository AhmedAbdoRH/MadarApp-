const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";
const SUPERVISOR_UID = "supervisor_admin";
const SUPERVISOR_EMAIL = "ahmedabdoshouq@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// 1. Unauthenticated tests
test("Unauthenticated user: cannot read specialists or applications", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("specialists").get());
  await assertFails(unauthDb.collection("specialist_applications").get());
  await assertFails(unauthDb.collection("conversations").get());
});

// 2. Specialist applications & Supervisor approval flow
test("Applicant can submit PENDING application with qualification, cannot self-approve", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();

  const validApp = {
    userId: ALICE_UID,
    applicantEmail: "alice@example.com",
    fullName: "د. علياء محمد",
    professionalTitle: "أخصائية علاج نفسي إكلينيكي",
    specialtyTags: ["قلق وتوتر", "CBT"],
    experienceYears: 6,
    bio: "خبرة في العلاج المعرفي السلوكي",
    supportsUrgentCare: true,
    qualificationTitle: "ماجستير علم النفس الإكلينيكي",
    qualificationInstitution: "جامعة القاهرة",
    attachmentFileName: "degree.jpg",
    attachmentMimeType: "image/jpeg",
    attachmentSizeBytes: 1024,
    attachmentBase64: "ZmFrZV9iYXNlNjRfaW1hZ2U=",
    status: "PENDING",
    createdAt: new Date(),
  };

  await assertSucceeds(
    aliceDb.collection("specialist_applications").doc(ALICE_UID).set(validApp)
  );

  // Applicant cannot self-approve
  await assertFails(
    aliceDb.collection("specialist_applications").doc(ALICE_UID).update({
      status: "APPROVED",
    })
  );

  // Regular user cannot self-create approved specialist profile or legacy demo specialist docs
  await assertFails(
    aliceDb.collection("specialists").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "د. علياء محمد",
      title: "أخصائية علاج نفسي",
      isOnline: true,
      isVerified: true,
    })
  );
  await assertFails(
    aliceDb.collection("specialists").doc("spec_1").set({
      userId: ALICE_UID,
      name: "أخصائي تجريبي",
      title: "أخصائي",
      isOnline: true,
      isVerified: true,
    })
  );

  // Another user cannot read Alice's application or qualification attachment
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("specialist_applications").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("specialist_applications").get());
});

test("Email spoofing test: unverified supervisor email is rejected", async () => {
  const spoofedAdminDb = testEnv.authenticatedContext("spoof_uid", {
    email: SUPERVISOR_EMAIL,
    email_verified: false,
  }).firestore();

  await assertFails(spoofedAdminDb.collection("specialist_applications").get());
  await assertFails(spoofedAdminDb.collection("reports").get());
});

test("Verified supervisor can inspect applications, approve specialist, and suspend", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("specialist_applications").doc(ALICE_UID).set({
      userId: ALICE_UID,
      applicantEmail: "alice@example.com",
      fullName: "د. علياء محمد",
      professionalTitle: "أخصائية علاج نفسي",
      qualificationTitle: "دكتوراه صحة نفسية",
      attachmentFileName: "cert.pdf",
      attachmentMimeType: "application/pdf",
      attachmentSizeBytes: 2048,
      attachmentBase64: "JVBERi0xLjQK",
      status: "PENDING",
      createdAt: new Date(),
    });
  });

  const adminDb = testEnv.authenticatedContext(SUPERVISOR_UID, {
    email: SUPERVISOR_EMAIL,
    email_verified: true,
  }).firestore();

  // Supervisor lists applications & reads qualification
  await assertSucceeds(adminDb.collection("specialist_applications").get());

  // Supervisor approves application and creates specialist doc
  await assertSucceeds(
    adminDb.collection("specialist_applications").doc(ALICE_UID).update({
      status: "APPROVED",
      reviewerId: SUPERVISOR_UID,
      reviewerEmail: SUPERVISOR_EMAIL,
      reviewerNote: "تمت مراجعة الشهادة واعتماد الحساب",
      reviewedAt: new Date(),
    })
  );

  await assertSucceeds(
    adminDb.collection("specialists").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "د. علياء محمد",
      title: "أخصائية علاج نفسي",
      isOnline: false,
      isVerified: true,
      isSuspended: false,
      qualificationTitle: "دكتوراه صحة نفسية",
      applicationId: ALICE_UID,
      updatedAt: new Date(),
    })
  );

  // Approved specialist can toggle online & update profile fields
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("specialists").doc(ALICE_UID).update({
      isOnline: true,
      updatedAt: new Date(),
    })
  );

  // Specialist cannot modify isVerified, rating, or inject ghost fields
  await assertFails(
    aliceDb.collection("specialists").doc(ALICE_UID).update({
      isSuspended: true,
      isVerified: false,
      rating: 5.0,
    })
  );
  await assertFails(
    aliceDb.collection("specialists").doc(ALICE_UID).update({
      ghostField: "hack",
    })
  );

  // Supervisor suspends specialist
  await assertSucceeds(
    adminDb.collection("specialists").doc(ALICE_UID).update({
      isSuspended: true,
      isOnline: false,
      updatedAt: new Date(),
    })
  );

  // Suspended specialist cannot set themselves online
  await assertFails(
    aliceDb.collection("specialists").doc(ALICE_UID).update({
      isOnline: true,
      updatedAt: new Date(),
    })
  );
});

// 3. Conversation privacy & access
test("Participants can access conversation; non-participants are blocked", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("conversations").doc("conv_123").set({
      specialistId: BOB_UID,
      seekerId: ALICE_UID,
      topic: "فضفضة عامة",
      status: "ACTIVE",
      createdAt: new Date(),
      lastMessage: "مرحبا"
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("conversations").doc("conv_123").get());

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("conversations").doc("conv_123").get());

  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  await assertFails(charlieDb.collection("conversations").doc("conv_123").get());

  // Participants can update conversation with lastMessageSenderId & lastMessageFromSpecialist
  await assertSucceeds(
    bobDb.collection("conversations").doc("conv_123").update({
      lastMessage: "أهلاً بك، أنا معك الآن",
      lastMessageSenderId: BOB_UID,
      lastMessageFromSpecialist: true,
      lastMessageTime: new Date(),
      updatedAt: new Date(),
    })
  );

  // Reject invalid lastMessageFromSpecialist type
  await assertFails(
    aliceDb.collection("conversations").doc("conv_123").update({
      lastMessage: "رسالة جديدة",
      lastMessageSenderId: ALICE_UID,
      lastMessageFromSpecialist: "yes",
    })
  );
});

// 4. Reports oversight
test("Users can submit reports; only supervisor can list and mark reviewed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("reports").doc("rep_1").set({
      conversationId: "conv_123",
      specialistId: BOB_UID,
      userId: ALICE_UID,
      reason: "كلام مش مناسب",
      details: "تفاصيل البلاغ",
      status: "OPEN",
      createdAt: new Date(),
    })
  );

  await assertFails(aliceDb.collection("reports").get());

  const adminDb = testEnv.authenticatedContext(SUPERVISOR_UID, {
    email: SUPERVISOR_EMAIL,
    email_verified: true,
  }).firestore();
  await assertSucceeds(adminDb.collection("reports").get());
  await assertSucceeds(
    adminDb.collection("reports").doc("rep_1").update({
      status: "REVIEWED",
      reviewedNote: "تمت المراجعة واتخاذ اللازم",
    })
  );
});
