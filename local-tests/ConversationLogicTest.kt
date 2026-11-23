package com.example.localtests

import com.example.ui.logic.*
import java.time.ZonedDateTime
import java.util.TimeZone

fun main() {
    var checks = 0
    fun verify(condition: Boolean, label: String) { check(condition) { label }; checks++ }
    fun millis(s: String) = ZonedDateTime.parse(s).toInstant().toEpochMilli()
    val cairo = TimeZone.getTimeZone("Africa/Cairo")
    val now = millis("2026-01-01T00:05:00+02:00[Africa/Cairo]")
    verify(formatConversationTimestamp(millis("2026-01-01T00:01:00+02:00[Africa/Cairo]"), now, cairo).startsWith("اليوم"), "today")
    verify(formatConversationTimestamp(millis("2025-12-31T23:59:00+02:00[Africa/Cairo]"), now, cairo).startsWith("أمس"), "year boundary yesterday")
    val beforeMidnight = millis("2025-12-31T23:58:00+02:00[Africa/Cairo]")
    val message = millis("2025-12-31T18:30:00+02:00[Africa/Cairo]")
    verify(formatConversationTimestamp(message, beforeMidnight, cairo).startsWith("اليوم"), "before midnight")
    verify(formatConversationTimestamp(message, now, cairo).startsWith("أمس"), "after midnight refresh")
    val older = formatConversationTimestamp(millis("2024-06-15T14:30:00+03:00[Africa/Cairo]"), now, cairo)
    verify(older.contains("٢٠٢٤"), "older year is explicit")
    verify(older.contains("٠٢:٣٠"), "local time preserved")
    val zoneMessage = millis("2026-01-01T00:30:00+02:00[Africa/Cairo]")
    verify(formatConversationTimestamp(zoneMessage, now, cairo).startsWith("اليوم"), "Cairo calendar zone")
    verify(formatConversationTimestamp(null, now, cairo) == "التاريخ غير متاح", "missing")
    verify(formatConversationTimestamp(0, now, cairo) == "التاريخ غير متاح", "zero")
    verify(formatConversationTimestamp(-1, now, cairo) == "التاريخ غير متاح", "negative")
    verify(ticketLifecycleLabel("ACTIVE", false).contains("بانتظار القبول"), "awaiting acceptance")
    verify(ticketLifecycleLabel("ACTIVE", true).contains("مقبولة"), "accepted")
    verify(ticketLifecycleLabel("RESOLVED", false) == "منتهية", "resolved overrides acceptance")
    verify(ticketLifecycleLabel("unexpected", true).contains("غير معروفة"), "unknown lifecycle")
    verify(UNKNOWN_READ_STATE.contains("غير متاحة"), "unknown does not mean unopened")
    for (signedIn in listOf(false, true)) for (owned in listOf(false, true)) for (suspended in listOf(false, true)) {
        verify(canSwitchSpecialistView(signedIn, owned, suspended) == (signedIn && owned && !suspended), "eligibility truth table")
    }
    println("PASS: $checks pure Kotlin/JVM assertions; no Firebase, accounts, or network used by tests")
}
