package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
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
import androidx.compose.material3.TextButton
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
import com.example.ui.components.BreathingExerciseBottomSheet
import com.example.ui.components.EmergencyNumbersBottomSheet
import com.example.ui.components.GroundingExerciseBottomSheet
import com.example.ui.components.UrgentHelpHeaderButton
import com.example.ui.theme.SanadTheme

/**
 * شاشة «أقل من 18»
 * تظهر عند اختيار «عمري أقل من 18 سنة» في شاشة الموافقة أو عند فتح التطبيق والمستخدم مسجل كـ under_18
 * يمنع منها تماماً الوصول للرئيسية أو الشات أو قائمة الأخصائيين أو الحجز.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Under18Screen(
    onBackToWelcome: () -> Unit,
    onEmergencyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEmergencyNumbersSheet by remember { mutableStateOf(false) }
    var showBreathingSheet by remember { mutableStateOf(false) }
    var showGroundingSheet by remember { mutableStateOf(false) }

    // Intercept back button to return to /welcome screen
    BackHandler(enabled = true) {
        onBackToWelcome()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("under_18_screen"),
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
                        onClick = onEmergencyClick,
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
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Title: «الخدمة دي للي عمره 18 سنة أو أكثر»
            Text(
                text = "الخدمة دي للي عمره 18 سنة أو أكثر",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.text,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Body text: «بس مش لازم الوقت ده يعدّي عليك لوحدك. الكلام مع شخص كبير موثوق بيساعد، وفيه خطوط مساعدة ممكن الاتصال بيها.»
            Text(
                text = "بس مش لازم الوقت ده يعدّي عليك لوحدك. الكلام مع شخص كبير موثوق بيساعد، وفيه خطوط مساعدة ممكن الاتصال بيها.",
                style = MaterialTheme.typography.bodyMedium,
                color = SanadTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Action Buttons:
            // 1. «أرقام المساعدة»
            Under18ActionCard(
                title = "أرقام المساعدة",
                subtitle = "خطوط الدعم النفسي والمساندة الرسمية المجانية",
                icon = Icons.Default.Call,
                onClick = { showEmergencyNumbersSheet = true },
                testTag = "under_18_help_numbers_btn"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. «تمرين تنفس»
            Under18ActionCard(
                title = "تمرين تنفس",
                subtitle = "تمرين \u200E4-7-8\u200E لتهدئة ضربات القلب والجهاز العصبي",
                icon = Icons.Default.Spa,
                onClick = { showBreathingSheet = true },
                testTag = "under_18_breathing_btn"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. «تمرين تأريض»
            Under18ActionCard(
                title = "تمرين تأريض",
                subtitle = "تمرين الحواس \u200E5-4-3-2-1\u200E لاستعادة الهدوء والتوازن",
                icon = Icons.Default.SelfImprovement,
                onClick = { showGroundingSheet = true },
                testTag = "under_18_grounding_btn"
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Text link «رجوع» to return to /welcome
            TextButton(
                onClick = onBackToWelcome,
                modifier = Modifier.testTag("under_18_back_link")
            ) {
                Text(
                    text = "رجوع",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = SanadTheme.colors.accent
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Bottom Sheets
    if (showEmergencyNumbersSheet) {
        EmergencyNumbersBottomSheet(
            onDismiss = { showEmergencyNumbersSheet = false }
        )
    }

    if (showBreathingSheet) {
        BreathingExerciseBottomSheet(
            onDismiss = { showBreathingSheet = false }
        )
    }

    if (showGroundingSheet) {
        GroundingExerciseBottomSheet(
            onDismiss = { showGroundingSheet = false }
        )
    }
}

@Composable
fun Under18ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
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
