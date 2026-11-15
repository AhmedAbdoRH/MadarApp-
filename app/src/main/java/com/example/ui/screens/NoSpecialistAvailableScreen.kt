package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmergencyNumbersBottomSheet
import com.example.ui.components.UrgentHelpHeaderButton
import com.example.ui.theme.SanadTheme

/**
 * شاشة حالة «مفيش أخصائي متاح»
 * تظهر عندما يكون جميع الأخصائيين غير متاحين أو مشغولين عند طلب شات فوري.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoSpecialistAvailableScreen(
    onBack: () -> Unit,
    onOpenBreathingExercise: () -> Unit,
    onOpenGroundingExercise: () -> Unit,
    onRetry: () -> Unit,
    onOpenEmergencyHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEmergencyBottomSheet by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("no_specialist_available_screen"),
        containerColor = SanadTheme.colors.bg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "كلم سند",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text
                    )
                },
                actions = {
                    UrgentHelpHeaderButton(
                        onClick = onOpenEmergencyHelp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Header titles
            Text(
                text = "مفيش أخصائي متاح دلوقتي",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.text,
                textAlign = TextAlign.Center
            )

            Text(
                text = "لحد ما حد يتاح، دي حاجات ممكن تهدّي.",
                style = MaterialTheme.typography.bodyMedium,
                color = SanadTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Option 1: تمرين تنفس
            NoSpecialistActionRow(
                title = "تمرين تنفس",
                subtitle = "تمرين \u200E4-7-8\u200E لتهدئة ضربات القلب والجهاز العصبي",
                icon = Icons.Default.Spa,
                onClick = onOpenBreathingExercise,
                testTag = "no_specialist_breathing_btn"
            )

            // Option 2: تمرين تأريض
            NoSpecialistActionRow(
                title = "تمرين تأريض",
                subtitle = "تمرين الحواس \u200E5-4-3-2-1\u200E للرجوع للحظة الحالية",
                icon = Icons.Default.SelfImprovement,
                onClick = onOpenGroundingExercise,
                testTag = "no_specialist_grounding_btn"
            )

            // Option 3: أرقام المساعدة
            NoSpecialistActionRow(
                title = "أرقام المساعدة",
                subtitle = "الاتصال المباشر بخطوط الدعم والطوارئ الرسمية بمصر",
                icon = Icons.Default.Call,
                onClick = { showEmergencyBottomSheet = true },
                testTag = "no_specialist_emergency_numbers_btn"
            )

            // Option 4: إعادة المحاولة
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SanadTheme.colors.accent,
                    contentColor = SanadTheme.colors.onAccent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("no_specialist_retry_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = SanadTheme.colors.onAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "إعادة المحاولة",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.onAccent
                )
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))
            Spacer(modifier = Modifier.height(24.dp))

            // Footer note
            Text(
                text = "كلم سند دعم نفسي أولي، مش بديل للطوارئ.",
                style = MaterialTheme.typography.labelSmall,
                color = SanadTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showEmergencyBottomSheet) {
        EmergencyNumbersBottomSheet(
            onDismiss = { showEmergencyBottomSheet = false }
        )
    }
}

@Composable
private fun NoSpecialistActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
        border = BorderStroke(1.dp, SanadTheme.colors.divider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SanadTheme.colors.accent,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.size(14.dp))
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
    }
}
