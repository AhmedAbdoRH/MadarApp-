package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApplicationStatus
import com.example.data.model.Specialist
import com.example.data.model.SpecialistApplication
import com.example.data.model.SpecialistReport
import com.example.ui.components.QualificationViewerDialog
import com.example.ui.theme.SanadTheme
import com.example.util.QualificationAttachmentHelper

@Composable
private fun SupervisorTopHeader(
    title: String,
    subtitle: String,
    onSwitchToSeeker: () -> Unit
) {
    Surface(
        color = SanadTheme.colors.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SanadTheme.colors.accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = SanadTheme.colors.onAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = SanadTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onSwitchToSeeker,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SanadTheme.colors.divider),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("supervisor_switch_to_seeker_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = SanadTheme.colors.text,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "المستفيد",
                    style = MaterialTheme.typography.labelMedium,
                    color = SanadTheme.colors.text,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SupervisorApplicationsScreen(
    applications: List<SpecialistApplication>,
    onApproveApplication: (SpecialistApplication, String?) -> Unit,
    onRejectApplication: (String, String) -> Unit,
    onSwitchToSeeker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("PENDING") }
    var inspectedApplication by remember { mutableStateOf<SpecialistApplication?>(null) }
    var approveTarget by remember { mutableStateOf<SpecialistApplication?>(null) }
    var rejectTarget by remember { mutableStateOf<SpecialistApplication?>(null) }

    val pendingCount = applications.count { ApplicationStatus.fromValue(it.status) == ApplicationStatus.PENDING }
    val approvedCount = applications.count { ApplicationStatus.fromValue(it.status) == ApplicationStatus.APPROVED }
    val rejectedCount = applications.count { ApplicationStatus.fromValue(it.status) == ApplicationStatus.REJECTED }

    val filteredApps = when (selectedFilter) {
        "PENDING" -> applications.filter { ApplicationStatus.fromValue(it.status) == ApplicationStatus.PENDING }
        "APPROVED" -> applications.filter { ApplicationStatus.fromValue(it.status) == ApplicationStatus.APPROVED }
        "REJECTED" -> applications.filter { ApplicationStatus.fromValue(it.status) == ApplicationStatus.REJECTED }
        else -> applications
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SanadTheme.colors.bg)
            .testTag("supervisor_applications_screen")
    ) {
        SupervisorTopHeader(
            title = "بوابة المشرف — الاعتماد",
            subtitle = "الاطلاع على المؤهلات المرفقة واعتماد الأخصائيين",
            onSwitchToSeeker = onSwitchToSeeker
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SupervisorSummaryChip(
                        label = "بانتظار المراجعة",
                        count = pendingCount,
                        color = SanadTheme.colors.busy,
                        modifier = Modifier.weight(1f)
                    )
                    SupervisorSummaryChip(
                        label = "معتمدون",
                        count = approvedCount,
                        color = SanadTheme.colors.online,
                        modifier = Modifier.weight(1f)
                    )
                    SupervisorSummaryChip(
                        label = "مرفوضون",
                        count = rejectedCount,
                        color = SanadTheme.colors.danger,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Filter Chips
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "PENDING" to "قيد المراجعة ($pendingCount)",
                        "APPROVED" to "معتمد ($approvedCount)",
                        "REJECTED" to "مرفوض ($rejectedCount)",
                        "ALL" to "الكل (${applications.size})"
                    )
                    filters.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SanadTheme.colors.surface,
                                labelColor = SanadTheme.colors.textSecondary,
                                selectedContainerColor = SanadTheme.colors.accent,
                                selectedLabelColor = SanadTheme.colors.onAccent
                            ),
                            modifier = Modifier.testTag("supervisor_filter_$key")
                        )
                    }
                }
            }

            if (filteredApps.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                        border = BorderStroke(1.dp, SanadTheme.colors.divider)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = SanadTheme.colors.accent,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "لا توجد طلبات في هذا التصنيف حالياً",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.text
                            )
                        }
                    }
                }
            } else {
                items(filteredApps, key = { it.userId.ifBlank { it.id } }) { app ->
                    val statusEnum = ApplicationStatus.fromValue(app.status)
                    val statusColor = when (statusEnum) {
                        ApplicationStatus.APPROVED -> SanadTheme.colors.online
                        ApplicationStatus.REJECTED, ApplicationStatus.SUSPENDED -> SanadTheme.colors.danger
                        ApplicationStatus.PENDING -> SanadTheme.colors.busy
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("supervisor_app_card_${app.userId.ifBlank { app.id }}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                        border = BorderStroke(1.dp, SanadTheme.colors.divider)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.fullName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SanadTheme.colors.text
                                    )
                                    Text(
                                        text = "${app.professionalTitle} • خبرة ${app.experienceYears} سنوات",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SanadTheme.colors.textSecondary
                                    )
                                    if (app.applicantEmail.isNotBlank()) {
                                        Text(
                                            text = app.applicantEmail,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SanadTheme.colors.accent
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .background(SanadTheme.colors.bg, RoundedCornerShape(10.dp))
                                        .border(1.dp, statusColor, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = statusEnum.labelAr,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }

                            if (app.bio.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = app.bio,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SanadTheme.colors.textSecondary,
                                    lineHeight = 19.sp
                                )
                            }

                            if (app.specialtyTags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    app.specialtyTags.forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .background(SanadTheme.colors.bg, RoundedCornerShape(8.dp))
                                                .border(1.dp, SanadTheme.colors.divider, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = tag,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SanadTheme.colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Qualification Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SanadTheme.colors.bg, RoundedCornerShape(14.dp))
                                    .border(1.dp, SanadTheme.colors.divider, RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = SanadTheme.colors.accent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = app.qualificationTitle,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SanadTheme.colors.text
                                        )
                                    }
                                    if (app.qualificationInstitution.isNotBlank()) {
                                        Text(
                                            text = "الجهة المانحة: ${app.qualificationInstitution}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SanadTheme.colors.textSecondary
                                        )
                                    }
                                    Text(
                                        text = "المرفق: ${app.attachmentFileName} (${QualificationAttachmentHelper.formatFileSize(app.attachmentSizeBytes)})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SanadTheme.colors.accent
                                    )
                                }
                            }

                            if (!app.rejectionReason.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "سبب الرفض: ${app.rejectionReason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SanadTheme.colors.danger
                                )
                            } else if (!app.reviewerNote.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "ملاحظة المشرف: ${app.reviewerNote}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SanadTheme.colors.online
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Inspect Qualification Button
                            OutlinedButton(
                                onClick = { inspectedApplication = app },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("inspect_qualification_btn_${app.userId.ifBlank { app.id }}"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, SanadTheme.colors.accent)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = SanadTheme.colors.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "الاطلاع على المؤهل المرفق",
                                    color = SanadTheme.colors.accent,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (statusEnum == ApplicationStatus.PENDING || statusEnum == ApplicationStatus.REJECTED) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { approveTarget = app },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("approve_app_btn_${app.userId.ifBlank { app.id }}"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SanadTheme.colors.online,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("موافقة واعتماد", fontWeight = FontWeight.Bold)
                                    }

                                    if (statusEnum == ApplicationStatus.PENDING) {
                                        OutlinedButton(
                                            onClick = { rejectTarget = app },
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .testTag("reject_app_btn_${app.userId.ifBlank { app.id }}"),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, SanadTheme.colors.danger)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = SanadTheme.colors.danger, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("رفض الطلب", color = SanadTheme.colors.danger, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    inspectedApplication?.let { app ->
        QualificationViewerDialog(
            qualificationTitle = app.qualificationTitle,
            qualificationInstitution = app.qualificationInstitution,
            fileName = app.attachmentFileName,
            mimeType = app.attachmentMimeType,
            sizeBytes = app.attachmentSizeBytes,
            base64Content = app.attachmentBase64,
            onDismiss = { inspectedApplication = null }
        )
    }

    approveTarget?.let { app ->
        var note by remember(app.userId) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { approveTarget = null },
            containerColor = SanadTheme.colors.surface,
            title = {
                Text(
                    text = "اعتماد الأخصائي ${app.fullName}",
                    color = SanadTheme.colors.text,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "بالضغط على تأكيد الاعتماد، سيتم تفعيل حساب الأخصائي وظهوره للمستفيدين.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SanadTheme.colors.textSecondary
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("ملاحظة الاعتماد (اختياري)") },
                        placeholder = { Text("مثال: تم التحقق من الشهادة المعتمدة") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SanadTheme.colors.text,
                            unfocusedTextColor = SanadTheme.colors.text,
                            focusedBorderColor = SanadTheme.colors.accent,
                            unfocusedBorderColor = SanadTheme.colors.borderInput
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onApproveApplication(app, note.ifBlank { null })
                        approveTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SanadTheme.colors.online,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("تأكيد الاعتماد", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { approveTarget = null }) {
                    Text("إلغاء", color = SanadTheme.colors.textSecondary)
                }
            }
        )
    }

    rejectTarget?.let { app ->
        var reason by remember(app.userId) { mutableStateOf("") }
        var reasonError by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { rejectTarget = null },
            containerColor = SanadTheme.colors.surface,
            title = {
                Text(
                    text = "رفض طلب ${app.fullName}",
                    color = SanadTheme.colors.danger,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "يرجى توضيح سبب الرفض ليتمكن المتقدم من تصحيحه وإعادة التقديم:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SanadTheme.colors.textSecondary
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = {
                            reason = it
                            reasonError = false
                        },
                        label = { Text("سبب الرفض (إلزامي)") },
                        placeholder = { Text("مثال: صورة الشهادة غير واضحة أو التخصص غير مطابق") },
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SanadTheme.colors.text,
                            unfocusedTextColor = SanadTheme.colors.text,
                            focusedBorderColor = SanadTheme.colors.danger,
                            unfocusedBorderColor = SanadTheme.colors.borderInput
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (reasonError) {
                        Text(
                            text = "يجب كتابة سبب الرفض أولاً",
                            style = MaterialTheme.typography.labelSmall,
                            color = SanadTheme.colors.danger
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reason.trim().length < 3) {
                            reasonError = true
                        } else {
                            onRejectApplication(app.userId.ifBlank { app.id }, reason.trim())
                            rejectTarget = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SanadTheme.colors.danger,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("تأكيد الرفض", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectTarget = null }) {
                    Text("إلغاء", color = SanadTheme.colors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun SupervisorSummaryChip(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
        border = BorderStroke(1.dp, SanadTheme.colors.divider)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SanadTheme.colors.textSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SupervisorSpecialistsScreen(
    specialists: List<Specialist>,
    applications: List<SpecialistApplication>,
    onToggleSuspend: (specialistId: String, suspend: Boolean) -> Unit,
    onSwitchToSeeker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inspectedApp by remember { mutableStateOf<SpecialistApplication?>(null) }
    var suspendConfirmTarget by remember { mutableStateOf<Specialist?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SanadTheme.colors.bg)
            .testTag("supervisor_specialists_screen")
    ) {
        SupervisorTopHeader(
            title = "الأخصائيون المعتمدون",
            subtitle = "الإشراف على الحسابات النشطة والموقوفة",
            onSwitchToSeeker = onSwitchToSeeker
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (specialists.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                        border = BorderStroke(1.dp, SanadTheme.colors.divider)
                    ) {
                        Text(
                            text = "لا يوجد أخصائيون مسجلون حالياً.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SanadTheme.colors.textSecondary,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            } else {
                items(specialists, key = { it.id }) { spec ->
                    val matchingApp = applications.find {
                        it.userId == spec.id || it.id == spec.applicationId || it.userId == spec.userId
                    }?.takeIf { it.attachmentBase64.isNotBlank() }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("supervisor_spec_card_${spec.id}"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                        border = BorderStroke(
                            1.dp,
                            if (spec.isSuspended) SanadTheme.colors.danger else SanadTheme.colors.divider
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = spec.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SanadTheme.colors.text
                                        )
                                        if (spec.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "معتمد",
                                                tint = SanadTheme.colors.online,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = spec.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SanadTheme.colors.textSecondary
                                    )
                                    if (spec.qualificationTitle.isNotBlank()) {
                                        Text(
                                            text = "المؤهل: ${spec.qualificationTitle}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SanadTheme.colors.accent
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .background(SanadTheme.colors.bg, RoundedCornerShape(10.dp))
                                        .border(
                                            1.dp,
                                            when {
                                                spec.isSuspended -> SanadTheme.colors.danger
                                                spec.isOnline -> SanadTheme.colors.online
                                                else -> SanadTheme.colors.divider
                                            },
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = when {
                                            spec.isSuspended -> "موقوف"
                                            spec.isOnline -> "متصل"
                                            else -> "غير متاح"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            spec.isSuspended -> SanadTheme.colors.danger
                                            spec.isOnline -> SanadTheme.colors.online
                                            else -> SanadTheme.colors.textSecondary
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "خبرة ${spec.experienceYears} سنوات • جلسات مكتملة: ${spec.completedChats} • التقييم: ${spec.rating}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SanadTheme.colors.textSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (matchingApp != null) {
                                    OutlinedButton(
                                        onClick = { inspectedApp = matchingApp },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, SanadTheme.colors.accent)
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, tint = SanadTheme.colors.accent, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("عرض المؤهل", color = SanadTheme.colors.accent)
                                    }
                                }

                                Button(
                                    onClick = { suspendConfirmTarget = spec },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("toggle_suspend_${spec.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (spec.isSuspended) SanadTheme.colors.online else SanadTheme.colors.danger,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (spec.isSuspended) Icons.Default.PlayCircleOutline else Icons.Default.Block,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (spec.isSuspended) "إعادة تفعيل" else "إيقاف مؤقت",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    inspectedApp?.let { app ->
        QualificationViewerDialog(
            qualificationTitle = app.qualificationTitle,
            qualificationInstitution = app.qualificationInstitution,
            fileName = app.attachmentFileName,
            mimeType = app.attachmentMimeType,
            sizeBytes = app.attachmentSizeBytes,
            base64Content = app.attachmentBase64,
            onDismiss = { inspectedApp = null }
        )
    }

    suspendConfirmTarget?.let { spec ->
        val willSuspend = !spec.isSuspended
        AlertDialog(
            onDismissRequest = { suspendConfirmTarget = null },
            containerColor = SanadTheme.colors.surface,
            title = {
                Text(
                    text = if (willSuspend) "إيقاف ${spec.name} مؤقتاً؟" else "إعادة تفعيل ${spec.name}؟",
                    color = SanadTheme.colors.text,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (willSuspend)
                        "لن يظهر هذا الأخصائي للمستفيدين ولن يتمكن من استقبال محادثات جديدة حتى تعيد تفعيله."
                    else
                        "سيتمكن الأخصائي من الظهور مجدداً واستقبال طلبات الدعم النفسي.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SanadTheme.colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleSuspend(spec.id, willSuspend)
                        suspendConfirmTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (willSuspend) SanadTheme.colors.danger else SanadTheme.colors.online,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(if (willSuspend) "تأكيد الإيقاف" else "تأكيد التفعيل", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { suspendConfirmTarget = null }) {
                    Text("إلغاء", color = SanadTheme.colors.textSecondary)
                }
            }
        )
    }
}

@Composable
fun SupervisorReportsScreen(
    reports: List<SpecialistReport>,
    specialists: List<Specialist>,
    onMarkReviewed: (reportId: String, status: String, note: String?) -> Unit,
    onSuspendSpecialistFromReport: (reportId: String, specialistId: String) -> Unit,
    onSwitchToSeeker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var reviewTarget by remember { mutableStateOf<SpecialistReport?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SanadTheme.colors.bg)
            .testTag("supervisor_reports_screen")
    ) {
        SupervisorTopHeader(
            title = "بلاغات الأمان والجلسات",
            subtitle = "مراجعة بلاغات المستفيدين وحماية جودة الدعم",
            onSwitchToSeeker = onSwitchToSeeker
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (reports.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                        border = BorderStroke(1.dp, SanadTheme.colors.divider)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = SanadTheme.colors.accent,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "لا توجد بلاغات مسجلة حتى الآن 🌿",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.text
                            )
                        }
                    }
                }
            } else {
                items(reports, key = { it.id }) { report ->
                    val targetSpec = specialists.find { it.id == report.specialistId || it.userId == report.specialistId }
                    val isOpen = report.status == "OPEN"
                    val badgeColor = if (isOpen) SanadTheme.colors.danger else SanadTheme.colors.online
                    val badgeLabel = when (report.status) {
                        "ACTION_TAKEN" -> "تم اتخاذ إجراء"
                        "REVIEWED" -> "تمت المراجعة"
                        else -> "بلاغ مفتوح"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("supervisor_report_card_${report.id}"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                        border = BorderStroke(1.dp, if (isOpen) SanadTheme.colors.danger else SanadTheme.colors.divider)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = report.reason,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SanadTheme.colors.text,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .background(SanadTheme.colors.bg, RoundedCornerShape(10.dp))
                                        .border(1.dp, badgeColor, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = badgeLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "الأخصائي المُبلَّغ عنه: ${targetSpec?.name ?: report.specialistId}",
                                style = MaterialTheme.typography.labelMedium,
                                color = SanadTheme.colors.accent
                            )
                            if (report.details.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "التفاصيل: ${report.details}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SanadTheme.colors.textSecondary
                                )
                            }
                            if (!report.reviewedNote.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "ملاحظة المشرف: ${report.reviewedNote}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SanadTheme.colors.online
                                )
                            }

                            if (isOpen) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { reviewTarget = report },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("mark_report_reviewed_${report.id}"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SanadTheme.colors.accent,
                                            contentColor = SanadTheme.colors.onAccent
                                        )
                                    ) {
                                        Text("تحديد كمُراجع", fontWeight = FontWeight.Bold)
                                    }

                                    if (targetSpec != null && !targetSpec.isSuspended) {
                                        OutlinedButton(
                                            onClick = {
                                                onSuspendSpecialistFromReport(report.id, targetSpec.id)
                                            },
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("suspend_from_report_${report.id}"),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, SanadTheme.colors.danger)
                                        ) {
                                            Text("إيقاف الأخصائي", color = SanadTheme.colors.danger)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    reviewTarget?.let { report ->
        var note by remember(report.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { reviewTarget = null },
            containerColor = SanadTheme.colors.surface,
            title = {
                Text(
                    text = "إغلاق ومراجعة البلاغ",
                    color = SanadTheme.colors.text,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("ملاحظة المشرف (اختياري)") },
                    placeholder = { Text("مثال: تم التواصل والتحقق من المحادثة") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SanadTheme.colors.text,
                        unfocusedTextColor = SanadTheme.colors.text,
                        focusedBorderColor = SanadTheme.colors.accent,
                        unfocusedBorderColor = SanadTheme.colors.borderInput
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onMarkReviewed(report.id, "REVIEWED", note.ifBlank { null })
                        reviewTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SanadTheme.colors.accent,
                        contentColor = SanadTheme.colors.onAccent
                    ),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("حفظ المراجعة", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewTarget = null }) {
                    Text("إلغاء", color = SanadTheme.colors.textSecondary)
                }
            }
        )
    }
}
