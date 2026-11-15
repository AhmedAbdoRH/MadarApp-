package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApplicationStatus
import com.example.data.model.SpecialistApplication
import com.example.ui.components.QualificationViewerDialog
import com.example.ui.theme.SanadTheme
import com.example.util.AttachmentReadResult
import com.example.util.PickedQualificationFile
import com.example.util.QualificationAttachmentHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpecialistApplicationScreen(
    application: SpecialistApplication?,
    isSuspendedSpecialist: Boolean,
    isSubmitting: Boolean,
    onSubmitApplication: (SpecialistApplication) -> Unit,
    onWithdrawApplication: () -> Unit,
    onBackToSeeker: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToSeeker() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentStatus = ApplicationStatus.fromValue(application?.status)

    var isEditing by remember(application?.id, application?.status) {
        mutableStateOf(application == null)
    }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    // Form fields
    var fullName by remember(application) { mutableStateOf(application?.fullName ?: "") }
    var professionalTitle by remember(application) { mutableStateOf(application?.professionalTitle ?: "") }
    var experienceYearsText by remember(application) {
        mutableStateOf(if ((application?.experienceYears ?: 0L) > 0L) application?.experienceYears.toString() else "")
    }
    var bio by remember(application) { mutableStateOf(application?.bio ?: "") }
    var supportsUrgentCare by remember(application) { mutableStateOf(application?.supportsUrgentCare ?: true) }
    var qualificationTitle by remember(application) { mutableStateOf(application?.qualificationTitle ?: "") }
    var qualificationInstitution by remember(application) { mutableStateOf(application?.qualificationInstitution ?: "") }

    val availableTags = listOf("قلق وتوتر", "اكتئاب", "CBT", "صدمات", "علاقات وأسرة", "ضغوط عمل ودراسة", "دعم عاجل")
    var selectedTags by remember(application) {
        mutableStateOf(application?.specialtyTags?.toSet() ?: setOf("قلق وتوتر"))
    }

    var pickedFile by remember(application) {
        mutableStateOf(
            if (application != null && application.attachmentBase64.isNotBlank()) {
                PickedQualificationFile(
                    fileName = application.attachmentFileName,
                    mimeType = application.attachmentMimeType,
                    sizeBytes = application.attachmentSizeBytes,
                    base64Content = application.attachmentBase64
                )
            } else null
        )
    }
    var isReadingFile by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isReadingFile = true
                formError = null
                when (val result = QualificationAttachmentHelper.readAndPrepareAttachment(context, uri)) {
                    is AttachmentReadResult.Success -> {
                        pickedFile = result.file
                    }
                    is AttachmentReadResult.Error -> {
                        formError = result.message
                    }
                }
                isReadingFile = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SanadTheme.colors.bg)
            .testTag("specialist_application_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SanadTheme.colors.surface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                IconButton(
                    onClick = onBackToSeeker,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("app_back_to_seeker_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "العودة لوضع المستفيد",
                        tint = SanadTheme.colors.text
                    )
                }
                Column {
                    Text(
                        text = "الانضمام كأخصائي متطوع",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text
                    )
                    Text(
                        text = "يتطلب مراجعة المؤهل وموافقة المشرف",
                        style = MaterialTheme.typography.labelSmall,
                        color = SanadTheme.colors.textSecondary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (isSuspendedSpecialist || currentStatus == ApplicationStatus.SUSPENDED) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                        border = BorderStroke(1.5.dp, SanadTheme.colors.danger)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = SanadTheme.colors.danger,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "تم إيقاف حساب الأخصائي مؤقتاً",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SanadTheme.colors.danger
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "قام مشرف المنصة بإيقاف ظهورك كأخصائي متاح مؤقتاً للمراجعة. يمكنك التواصل مع المشرف أو العودة لاستخدام التطبيق كمستفيد.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SanadTheme.colors.textSecondary,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onBackToSeeker,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SanadTheme.colors.accent,
                                    contentColor = SanadTheme.colors.onAccent
                                )
                            ) {
                                Text("العودة لوضع المستفيد", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (application != null && !isEditing) {
                // Status Card (PENDING or REJECTED)
                item {
                    val isPending = currentStatus == ApplicationStatus.PENDING
                    val statusColor = if (isPending) SanadTheme.colors.busy else SanadTheme.colors.danger
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("application_status_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                        border = BorderStroke(1.5.dp, statusColor)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(statusColor.copy(alpha = 0.16f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPending) Icons.Default.HourglassTop else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isPending) "طلبك قيد المراجعة" else "لم يتم اعتماد الطلب",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SanadTheme.colors.text
                                    )
                                    Text(
                                        text = if (isPending)
                                            "سيقوم المشرف بالاطلاع على مؤهلك المرفق والرد قريباً"
                                        else
                                            "يرجى مراجعة ملاحظة المشرف وتحديث بياناتك",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SanadTheme.colors.textSecondary
                                    )
                                }
                            }

                            if (!isPending && !application.rejectionReason.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SanadTheme.colors.bg, RoundedCornerShape(12.dp))
                                        .border(1.dp, SanadTheme.colors.danger.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "سبب الرفض من المشرف: ${application.rejectionReason}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SanadTheme.colors.danger,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "الاسم: ${application.fullName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.text
                            )
                            Text(
                                text = "المسمى: ${application.professionalTitle} • خبرة ${application.experienceYears} سنوات",
                                style = MaterialTheme.typography.bodySmall,
                                color = SanadTheme.colors.textSecondary
                            )
                            Text(
                                text = "المؤهل: ${application.qualificationTitle}" +
                                    if (application.qualificationInstitution.isNotBlank()) " (${application.qualificationInstitution})" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = SanadTheme.colors.accent
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = { showPreviewDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("applicant_preview_qualification_btn"),
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
                                    text = "معاينة المؤهل المرفق (${application.attachmentFileName})",
                                    color = SanadTheme.colors.accent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { isEditing = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("edit_application_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SanadTheme.colors.accent,
                                        contentColor = SanadTheme.colors.onAccent
                                    )
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPending) "تعديل الطلب" else "تعديل وإعادة التقديم",
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showWithdrawDialog = true },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("withdraw_application_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, SanadTheme.colors.danger)
                                ) {
                                    Text("سحب الطلب", color = SanadTheme.colors.danger)
                                }
                            }
                        }
                    }
                }
            } else {
                // Application Form
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                        border = BorderStroke(1.dp, SanadTheme.colors.divider)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = SanadTheme.colors.accent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "لحماية المستفيدين، يتم تفعيل حساب الأخصائي فقط بعد اطلاع المشرف على المؤهل العلمي أو الترخيص المرفق والموافقة عليه.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SanadTheme.colors.textSecondary,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                item {
                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SanadTheme.colors.text,
                        unfocusedTextColor = SanadTheme.colors.text,
                        focusedBorderColor = SanadTheme.colors.accent,
                        unfocusedBorderColor = SanadTheme.colors.borderInput,
                        focusedLabelColor = SanadTheme.colors.accent,
                        unfocusedLabelColor = SanadTheme.colors.textSecondary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("الاسم الكامل (كما يظهر للمستفيدين)") },
                            placeholder = { Text("مثال: د. سارة العمري") },
                            singleLine = true,
                            colors = fieldColors,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_full_name_input")
                        )

                        OutlinedTextField(
                            value = professionalTitle,
                            onValueChange = { professionalTitle = it },
                            label = { Text("المسمى المهني والتخصص") },
                            placeholder = { Text("مثال: أخصائي إرشاد نفسي وعلاج معرفي سلوكي") },
                            singleLine = true,
                            colors = fieldColors,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_title_input")
                        )

                        OutlinedTextField(
                            value = experienceYearsText,
                            onValueChange = { newVal ->
                                if (newVal.all { it.isDigit() } && newVal.length <= 2) {
                                    experienceYearsText = newVal
                                }
                            },
                            label = { Text("سنوات الخبرة") },
                            placeholder = { Text("مثال: 5") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = fieldColors,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_experience_input")
                        )

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("النبذة المهنية ومنهجية الدعم") },
                            placeholder = { Text("عرّف المستفيدين بمجالات دعمك وأسلوبك في الإصغاء...") },
                            minLines = 3,
                            maxLines = 5,
                            colors = fieldColors,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_bio_input")
                        )

                        Text(
                            text = "التخصصات الدقيقة",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.text
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableTags.forEach { tag ->
                                val selected = tag in selectedTags
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedTags = if (selected) selectedTags - tag else selectedTags + tag
                                    },
                                    label = { Text(tag) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = SanadTheme.colors.surface,
                                        labelColor = SanadTheme.colors.textSecondary,
                                        selectedContainerColor = SanadTheme.colors.accent,
                                        selectedLabelColor = SanadTheme.colors.onAccent
                                    ),
                                    modifier = Modifier.testTag("apply_tag_$tag")
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                            border = BorderStroke(1.dp, SanadTheme.colors.divider)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "استقبال طلبات الدعم النفسي العاجل",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SanadTheme.colors.text
                                    )
                                    Text(
                                        text = "الظهور ضمن المتطوعين المستجيبين لنوبات القلق والضيق الحاد",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SanadTheme.colors.textSecondary
                                    )
                                }
                                Switch(
                                    checked = supportsUrgentCare,
                                    onCheckedChange = { supportsUrgentCare = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SanadTheme.colors.onAccent,
                                        checkedTrackColor = SanadTheme.colors.accent
                                    )
                                )
                            }
                        }

                        // Qualification Section
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = SanadTheme.colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "المؤهل العلمي والترخيص المهني (إلزامي)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.text
                            )
                        }

                        OutlinedTextField(
                            value = qualificationTitle,
                            onValueChange = { qualificationTitle = it },
                            label = { Text("اسم المؤهل أو الترخيص") },
                            placeholder = { Text("مثال: بكالوريوس / ماجستير علم النفس - ترخيص مزاولة") },
                            singleLine = true,
                            colors = fieldColors,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_qualification_title_input")
                        )

                        OutlinedTextField(
                            value = qualificationInstitution,
                            onValueChange = { qualificationInstitution = it },
                            label = { Text("الجهة المانحة (اختياري)") },
                            placeholder = { Text("مثال: جامعة القاهرة / نقابة الأخصائيين النفسيين") },
                            singleLine = true,
                            colors = fieldColors,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_qualification_inst_input")
                        )

                        // File Picker Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                            border = BorderStroke(
                                1.dp,
                                if (pickedFile != null) SanadTheme.colors.online else SanadTheme.colors.accent
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (pickedFile == null) {
                                    Text(
                                        text = "إرفاق صورة المؤهل أو شهادة الاعتماد (JPG / PNG / PDF)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SanadTheme.colors.text
                                    )
                                    Text(
                                        text = "سيطلع المشرف على المستند المرفق للتحقق قبل اعتماد الحساب",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SanadTheme.colors.textSecondary
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (pickedFile!!.mimeType == "application/pdf")
                                                Icons.Default.PictureAsPdf else Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = SanadTheme.colors.online,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = pickedFile!!.fileName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SanadTheme.colors.text
                                            )
                                            Text(
                                                text = QualificationAttachmentHelper.formatFileSize(pickedFile!!.sizeBytes),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SanadTheme.colors.textSecondary
                                            )
                                        }
                                        TextButton(
                                            onClick = { showPreviewDialog = true },
                                            modifier = Modifier.testTag("preview_picked_file_btn")
                                        ) {
                                            Text("معاينة", color = SanadTheme.colors.accent, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = {
                                        documentPickerLauncher.launch(arrayOf("image/*", "application/pdf"))
                                    },
                                    enabled = !isReadingFile && !isSubmitting,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("pick_qualification_file_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, SanadTheme.colors.accent)
                                ) {
                                    if (isReadingFile) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = SanadTheme.colors.accent,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("جاري تجهيز المرفق...", color = SanadTheme.colors.accent)
                                    } else {
                                        Icon(
                                            imageVector = if (pickedFile == null) Icons.Default.UploadFile else Icons.Default.AttachFile,
                                            contentDescription = null,
                                            tint = SanadTheme.colors.accent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (pickedFile == null) "اختيار ملف المؤهل (صورة أو PDF)" else "تغيير الملف المرفق",
                                            color = SanadTheme.colors.accent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        if (formError != null) {
                            Text(
                                text = formError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = SanadTheme.colors.danger,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                val years = experienceYearsText.toLongOrNull() ?: 0L
                                when {
                                    fullName.trim().length < 2 ->
                                        formError = "يرجى كتابة الاسم الكامل"
                                    professionalTitle.trim().length < 2 ->
                                        formError = "يرجى كتابة المسمى المهني"
                                    qualificationTitle.trim().length < 2 ->
                                        formError = "يرجى كتابة اسم المؤهل العلمي أو الترخيص"
                                    pickedFile == null ->
                                        formError = "يرجى إرفاق صورة أو ملف PDF للمؤهل للاطلاع عليه من المشرف"
                                    else -> {
                                        formError = null
                                        val file = pickedFile!!
                                        onSubmitApplication(
                                            SpecialistApplication(
                                                fullName = fullName.trim(),
                                                professionalTitle = professionalTitle.trim(),
                                                specialtyTags = selectedTags.toList().ifEmpty { listOf("دعم نفسي") },
                                                experienceYears = years,
                                                bio = bio.trim(),
                                                supportsUrgentCare = supportsUrgentCare,
                                                qualificationTitle = qualificationTitle.trim(),
                                                qualificationInstitution = qualificationInstitution.trim(),
                                                attachmentFileName = file.fileName,
                                                attachmentMimeType = file.mimeType,
                                                attachmentSizeBytes = file.sizeBytes,
                                                attachmentBase64 = file.base64Content
                                            )
                                        )
                                        isEditing = false
                                    }
                                }
                            },
                            enabled = !isSubmitting && !isReadingFile,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("submit_application_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SanadTheme.colors.accent,
                                contentColor = SanadTheme.colors.onAccent
                            )
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = SanadTheme.colors.onAccent,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("جاري إرسال الطلب...", fontWeight = FontWeight.Bold)
                            } else {
                                Text("إرسال الطلب للمراجعة والاعتماد", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (application != null) {
                            OutlinedButton(
                                onClick = { isEditing = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("cancel_edit_application_btn"),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, SanadTheme.colors.divider)
                            ) {
                                Text("إلغاء التعديل", color = SanadTheme.colors.textSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showPreviewDialog && (pickedFile != null || application != null)) {
        QualificationViewerDialog(
            qualificationTitle = qualificationTitle.ifBlank { application?.qualificationTitle ?: "" },
            qualificationInstitution = qualificationInstitution.ifBlank { application?.qualificationInstitution ?: "" },
            fileName = pickedFile?.fileName ?: application?.attachmentFileName ?: "",
            mimeType = pickedFile?.mimeType ?: application?.attachmentMimeType ?: "image/jpeg",
            sizeBytes = pickedFile?.sizeBytes ?: application?.attachmentSizeBytes ?: 0L,
            base64Content = pickedFile?.base64Content ?: application?.attachmentBase64 ?: "",
            onDismiss = { showPreviewDialog = false }
        )
    }

    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            containerColor = SanadTheme.colors.surface,
            title = {
                Text("سحب طلب الانضمام", color = SanadTheme.colors.text, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "هل تريد سحب طلب الانضمام كأخصائي متطوع وحذف المرفق؟ يمكنك التقديم مجدداً في أي وقت.",
                    color = SanadTheme.colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWithdrawDialog = false
                        onWithdrawApplication()
                    }
                ) {
                    Text("نعم، سحب الطلب", color = SanadTheme.colors.danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text("تراجع", color = SanadTheme.colors.textSecondary)
                }
            }
        )
    }
}
