package com.example.ui.logic

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// No Android/Firebase dependency: usable in isolated JVM tests.
fun formatConversationTimestamp(millis: Long?, nowMillis: Long, zone: TimeZone): String {
    if (millis == null || millis <= 0) return "التاريخ غير متاح"
    val locale = Locale.forLanguageTag("ar")
    fun calendar(value: Long) = Calendar.getInstance(zone).apply { timeInMillis = value }
    val message = calendar(millis)
    val today = calendar(nowMillis)
    val yesterday = calendar(nowMillis).apply { add(Calendar.DAY_OF_YEAR, -1) }
    fun same(a: Calendar, b: Calendar) = a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    fun format(pattern: String) = SimpleDateFormat(pattern, locale).apply { timeZone = zone }.format(Date(millis))
    val day = when {
        same(message, today) -> "اليوم"
        same(message, yesterday) -> "أمس"
        message.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> format("d MMMM")
        else -> format("d MMMM yyyy")
    }
    return "$day، ${format("hh:mm a")}"
}

fun ticketLifecycleLabel(status: String, accepted: Boolean): String = when (status) {
    "RESOLVED" -> "منتهية"
    "ACTIVE" -> if (accepted) "مقبولة · جارية" else "طلب جديد · بانتظار القبول"
    else -> "حالة التذكرة غير معروفة"
}

// This repository revision has no reliable per-viewer read/open evidence.
// Do not manufacture unread counts or an unopened claim from absent data.
const val UNKNOWN_READ_STATE = "حالة القراءة غير متاحة"
fun canSwitchSpecialistView(signedIn: Boolean, ownedVerifiedProfile: Boolean, suspended: Boolean): Boolean =
    signedIn && ownedVerifiedProfile && !suspended
