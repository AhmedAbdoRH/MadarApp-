package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.config.EmergencyConfig
import com.example.data.config.EmergencyContactNumber
import com.example.ui.theme.SanadTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrgentAssistanceScreen(
    onBack: () -> Unit,
    onStartUrgentChat: () -> Unit,
    onOpenBreathingExercise: () -> Unit,
    onOpenGroundingExercise: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    hasAvailableSpecialists: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showCallSheet by remember { mutableStateOf(false) }
    var showNoSpecialistDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("urgent_assistance_screen"),
        containerColor = SanadTheme.colors.bg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "مساعدة عاجلة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("urgent_screen_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = SanadTheme.colors.text
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SanadTheme.colors.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Disclaimer
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = "مساعدة عاجلة",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.text
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "كلم سند دعم نفسي أولي، مش بديل للطوارئ.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SanadTheme.colors.textSecondary
                )
            }

            // Card 1: في خطر على حياتي أو حياة حد دلوقتي
            UrgentActionCard(
                title = "في خطر على حياتي أو حياة حد دلوقتي",
                subtitle = "أرقام الطوارئ والإسعاف والخطوط الساخنة الرسمية المباشرة",
                icon = Icons.Default.Warning,
                buttonText = "عرض أرقام الطوارئ والاتصال",
                onClick = { showCallSheet = true },
                testTag = "urgent_card_life_threat"
            )

            // Card 2: لازم أتكلم مع حد حالًا
            UrgentActionCard(
                title = "لازم أتكلم مع حد حالًا",
                subtitle = "بدء شات فوري وسري مع أخصائي نفسي متطوع متاح أونلاين الآن",
                icon = Icons.Default.Forum,
                buttonText = "بدء شات فوري عاجل",
                onClick = {
                    if (hasAvailableSpecialists) {
                        onStartUrgentChat()
                    } else {
                        showNoSpecialistDialog = true
                    }
                },
                testTag = "urgent_card_instant_chat"
            )

            // Card 3: تهدئة سريعة
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("urgent_card_calm_down"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                border = BorderStroke(1.dp, SanadTheme.colors.divider)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SanadTheme.colors.accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = null,
                                tint = SanadTheme.colors.onAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تهدئة سريعة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.text
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "تمارين تنفس وتأريض لمساعدتك على استعادة الهدوء فوراً",
                                style = MaterialTheme.typography.bodySmall,
                                color = SanadTheme.colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenBreathingExercise,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SanadTheme.colors.accent,
                                contentColor = SanadTheme.colors.onAccent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("open_breathing_button")
                        ) {
                            Text(
                                text = "تمرين التنفس",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.onAccent
                            )
                        }

                        Button(
                            onClick = onOpenGroundingExercise,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SanadTheme.colors.surface,
                                contentColor = SanadTheme.colors.text
                            ),
                            border = BorderStroke(1.5.dp, SanadTheme.colors.textSecondary),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("open_grounding_button")
                        ) {
                            Text(
                                text = "تمرين التأريض",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.text
                            )
                        }
                    }
                }
            }

            // Card 4: أتواصل مع شخص أثق فيه
            UrgentActionCard(
                title = "أتواصل مع شخص أثق فيه",
                subtitle = "إرسال رسالة جاهزة لشخص قريب منك لطلب الحديث معه",
                icon = Icons.Default.Share,
                buttonText = "مشاركة رسالة مع شخص أثق فيه",
                onClick = {
                    val shareMessage = "ممكن نتكلم شوية؟ عندي ضغط كبير."
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareMessage)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "مشاركة رسالة")

                    try {
                        context.startActivity(shareIntent)
                    } catch (e: Exception) {
                        // Fallback: Copy to clipboard
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("طلب حديث", shareMessage)
                        clipboard.setPrimaryClip(clip)
                        onShowSnackbar("اتنسخت")
                    }
                },
                testTag = "urgent_card_reach_trusted"
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Bottom Sheet: أرقام الطوارئ
    if (showCallSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCallSheet = false },
            sheetState = sheetState,
            containerColor = SanadTheme.colors.surface,
            scrimColor = SanadTheme.colors.bg.copy(alpha = 0.7f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "أرقام الطوارئ الرسمية المباشرة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.text
                        )
                        Text(
                            text = "اضغط على أي رقم للاتصال الفوري المباشر",
                            style = MaterialTheme.typography.bodySmall,
                            color = SanadTheme.colors.textSecondary
                        )
                    }
                    IconButton(onClick = {
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            showCallSheet = false
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = SanadTheme.colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                EmergencyConfig.contacts.forEach { contact ->
                    EmergencyContactRow(
                        contact = contact,
                        onDial = {
                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${contact.number.trim()}")
                            }
                            context.startActivity(dialIntent)
                        }
                    )
                }
            }
        }
    }

    // Modal/Dialog for "مفيش أخصائي متاح دلوقتي"
    if (showNoSpecialistDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showNoSpecialistDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SanadTheme.colors.surface,
                border = BorderStroke(1.dp, SanadTheme.colors.divider),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("no_specialist_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(SanadTheme.colors.surface, CircleShape)
                            .border(1.dp, SanadTheme.colors.divider, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = SanadTheme.colors.accent,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "مفيش أخصائي متاح دلوقتي",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "جميع المتطوعين منشغلون بمحادثات أخرى في هذه اللحظة. إذا كنت في حاجة عاجلة، يُرجى الاتصال بالخطوط الرسمية أدناه فوراً:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SanadTheme.colors.textSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    EmergencyConfig.contacts.forEach { contact ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${contact.number.trim()}")
                                    }
                                    context.startActivity(dialIntent)
                                },
                            shape = RoundedCornerShape(12.dp),
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
                                Text(
                                    text = contact.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SanadTheme.colors.text
                                )
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "اتصال",
                                    tint = SanadTheme.colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showNoSpecialistDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SanadTheme.colors.accent,
                            contentColor = SanadTheme.colors.onAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "إغلاق",
                            fontWeight = FontWeight.Bold,
                            color = SanadTheme.colors.onAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UrgentActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    buttonText: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
        border = BorderStroke(1.dp, SanadTheme.colors.divider)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SanadTheme.colors.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SanadTheme.colors.onAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = SanadTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SanadTheme.colors.accent,
                    contentColor = SanadTheme.colors.onAccent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.onAccent
                )
            }
        }
    }
}

@Composable
fun EmergencyContactRow(
    contact: EmergencyContactNumber,
    onDial: () -> Unit
) {
    val isPrimary = contact.isPrimaryAmbulance

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDial)
            .testTag("dial_${contact.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) SanadTheme.colors.accent else SanadTheme.colors.surface
        ),
        border = if (isPrimary) null else BorderStroke(1.dp, SanadTheme.colors.divider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = if (isPrimary) 18.dp else 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isPrimary) Icons.Default.LocalHospital else Icons.Default.Call,
                    contentDescription = null,
                    tint = if (isPrimary) SanadTheme.colors.onAccent else SanadTheme.colors.accent,
                    modifier = Modifier.size(if (isPrimary) 28.dp else 22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = contact.label,
                    style = if (isPrimary) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isPrimary) SanadTheme.colors.onAccent else SanadTheme.colors.text
                )
            }

            Box(
                modifier = Modifier
                    .size(if (isPrimary) 44.dp else 38.dp)
                    .clip(CircleShape)
                    .background(if (isPrimary) SanadTheme.colors.onAccent else SanadTheme.colors.accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "اتصال",
                    tint = if (isPrimary) SanadTheme.colors.accent else SanadTheme.colors.onAccent,
                    modifier = Modifier.size(if (isPrimary) 22.dp else 18.dp)
                )
            }
        }
    }
}
