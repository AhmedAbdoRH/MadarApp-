package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Specialist
import com.example.data.model.SpecialistApplication
import com.example.ui.components.QualificationViewerDialog
import com.example.ui.theme.SanadTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpecialistProfileScreen(
    specialist: Specialist,
    application: SpecialistApplication?,
    onSaveProfile: (title: String, bio: String, tags: List<String>, experienceYears: Long, supportsUrgentCare: Boolean) -> Unit,
    onSwitchToSeeker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember(specialist.id, specialist.title) { mutableStateOf(specialist.title) }
    var bio by remember(specialist.id, specialist.bio) { mutableStateOf(specialist.bio) }
    var expText by remember(specialist.id, specialist.experienceYears) { mutableStateOf(specialist.experienceYears.toString()) }
    var supportsUrgentCare by remember(specialist.id, specialist.supportsUrgentCare) { mutableStateOf(specialist.supportsUrgentCare) }
    var selectedTags by remember(specialist.id, specialist.tags) { mutableStateOf(specialist.tags.toSet()) }
    var showQualificationDialog by remember { mutableStateOf(false) }

    val availableTags = listOf("قلق وتوتر", "اكتئاب", "CBT", "صدمات", "علاقات وأسرة", "ضغوط عمل ودراسة", "دعم عاجل")

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = SanadTheme.colors.text,
        unfocusedTextColor = SanadTheme.colors.text,
        focusedBorderColor = SanadTheme.colors.accent,
        unfocusedBorderColor = SanadTheme.colors.borderInput,
        focusedLabelColor = SanadTheme.colors.accent,
        unfocusedLabelColor = SanadTheme.colors.textSecondary
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("specialist_profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                border = BorderStroke(1.dp, SanadTheme.colors.divider)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(SanadTheme.colors.accent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = specialist.avatarInitials,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.onAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = specialist.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.text
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "حساب موثق",
                                tint = SanadTheme.colors.online,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "حساب أخصائي معتمد وموثق",
                            style = MaterialTheme.typography.labelSmall,
                            color = SanadTheme.colors.online,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Qualification Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                border = BorderStroke(1.dp, SanadTheme.colors.divider)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = SanadTheme.colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "المؤهل المعتمد",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.text
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = specialist.qualificationTitle.ifBlank {
                            application?.qualificationTitle ?: "مؤهل أكاديمي ومهني معتمد"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = SanadTheme.colors.text
                    )
                    if (application != null && application.attachmentBase64.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { showQualificationDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SanadTheme.colors.accent)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = SanadTheme.colors.accent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("عرض وثيقة المؤهل المرفقة", color = SanadTheme.colors.accent, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Editable Fields
        item {
            Text(
                text = "تعديل البيانات المهنية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.text
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("المسمى المهني") },
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = expText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() } && it.length <= 2) expText = it },
                    label = { Text("سنوات الخبرة") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = fieldColors,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("النبذة المهنية") },
                    minLines = 3,
                    maxLines = 5,
                    colors = fieldColors,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableTags.forEach { tag ->
                        val isSelected = tag in selectedTags
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTags = if (isSelected) selectedTags - tag else selectedTags + tag
                            },
                            label = { Text(tag) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SanadTheme.colors.surface,
                                labelColor = SanadTheme.colors.textSecondary,
                                selectedContainerColor = SanadTheme.colors.accent,
                                selectedLabelColor = SanadTheme.colors.onAccent
                            )
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "استقبال طلبات الدعم العاجل",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.text
                            )
                            Text(
                                text = "ظهورك ضمن المستجيبين للحالات الطارئة",
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

                Button(
                    onClick = {
                        onSaveProfile(
                            title.trim(),
                            bio.trim(),
                            selectedTags.toList().ifEmpty { listOf("دعم نفسي") },
                            expText.toLongOrNull() ?: specialist.experienceYears,
                            supportsUrgentCare
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .testTag("save_specialist_profile_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SanadTheme.colors.accent,
                        contentColor = SanadTheme.colors.onAccent
                    )
                ) {
                    Text("حفظ التحديثات", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onSwitchToSeeker,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SanadTheme.colors.divider)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = SanadTheme.colors.text)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("التبديل لوضع المستفيد", color = SanadTheme.colors.text)
                }
            }
        }
    }

    if (showQualificationDialog && application != null) {
        QualificationViewerDialog(
            qualificationTitle = application.qualificationTitle,
            qualificationInstitution = application.qualificationInstitution,
            fileName = application.attachmentFileName,
            mimeType = application.attachmentMimeType,
            sizeBytes = application.attachmentSizeBytes,
            base64Content = application.attachmentBase64,
            onDismiss = { showQualificationDialog = false }
        )
    }
}
