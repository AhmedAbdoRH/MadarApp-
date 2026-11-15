package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.UrgentHelpHeaderButton
import com.example.ui.theme.SanadTheme

enum class AgeOption {
    ADULT,     // عمري 18 سنة أو أكثر
    UNDER_18   // عمري أقل من 18 سنة
}

/**
 * شاشة «قبل ما نبدأ» (موافقة + تأكيد السن) - route: /welcome
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeConsentScreen(
    onConfirmAdultConsent: () -> Unit,
    onSelectUnder18: () -> Unit,
    onEmergencyClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // No option selected by default (بطاقتين Radio من غير أي خيار محدد مسبقاً)
    var selectedAgeOption by remember { mutableStateOf<AgeOption?>(null) }

    // Intercept back button to prevent skipping consent screen
    BackHandler(enabled = true) {
        // Do nothing / keep user on welcome consent screen
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("welcome_consent_screen"),
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
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle App Name
            Text(
                text = "كلم سند",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.accent,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Title: «قبل ما نبدأ»
            Text(
                text = "قبل ما نبدأ",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.text,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3 rows with small icon and text
            ConsentInfoRow(
                icon = Icons.Default.Info,
                text = "كلم سند دعم نفسي أولي، مش علاج ولا بديل للطوارئ."
            )

            Spacer(modifier = Modifier.height(14.dp))

            ConsentPrivacyRow(
                onPrivacyClick = onPrivacyClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            ConsentInfoRow(
                icon = Icons.Default.VerifiedUser,
                text = "الخدمة لمن عمره 18 سنة أو أكثر."
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Age Confirmation Selection Cards (Radio Cards)
            Text(
                text = "تأكيد السن:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.text,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            // Card 1: عمري 18 سنة أو أكثر
            AgeSelectCard(
                title = "عمري 18 سنة أو أكثر",
                isSelected = selectedAgeOption == AgeOption.ADULT,
                onClick = { selectedAgeOption = AgeOption.ADULT },
                testTag = "age_card_adult"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Card 2: عمري أقل من 18 سنة
            AgeSelectCard(
                title = "عمري أقل من 18 سنة",
                isSelected = selectedAgeOption == AgeOption.UNDER_18,
                onClick = { selectedAgeOption = AgeOption.UNDER_18 },
                testTag = "age_card_under_18"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Small text above button: «الضغط على «موافق ونكمل» معناه الموافقة على اللي فوق.»
            Text(
                text = "الضغط على «موافق ونكمل» معناه الموافقة على اللي فوق.",
                style = MaterialTheme.typography.bodySmall,
                color = SanadTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Primary Button «موافق ونكمل», disabled until user chooses
            val isButtonEnabled = selectedAgeOption != null
            Button(
                onClick = {
                    when (selectedAgeOption) {
                        AgeOption.ADULT -> onConfirmAdultConsent()
                        AgeOption.UNDER_18 -> onSelectUnder18()
                        null -> { /* disabled */ }
                    }
                },
                enabled = isButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("confirm_consent_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SanadTheme.colors.accent,
                    contentColor = SanadTheme.colors.onAccent,
                    disabledContainerColor = SanadTheme.colors.surface,
                    disabledContentColor = SanadTheme.colors.textSecondary
                )
            ) {
                Text(
                    text = "موافق ونكمل",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ConsentInfoRow(
    icon: ImageVector,
    text: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
        border = BorderStroke(1.dp, SanadTheme.colors.divider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(SanadTheme.colors.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SanadTheme.colors.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = SanadTheme.colors.text,
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ConsentPrivacyRow(
    onPrivacyClick: () -> Unit
) {
    val privacyText = androidx.compose.ui.text.buildAnnotatedString {
        append("بنحترم خصوصيتك، وتفاصيل التعامل مع البيانات في ")
        pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
        withStyle(
            style = androidx.compose.ui.text.SpanStyle(
                color = SanadTheme.colors.accent,
                fontWeight = FontWeight.Bold
            )
        ) {
            append("سياسة الخصوصية")
        }
        pop()
        append(".")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
        border = BorderStroke(1.dp, SanadTheme.colors.divider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(SanadTheme.colors.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SanadTheme.colors.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            androidx.compose.foundation.text.ClickableText(
                text = privacyText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SanadTheme.colors.text,
                    lineHeight = 22.sp
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("welcome_privacy_policy_link"),
                onClick = { offset ->
                    privacyText.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                        .firstOrNull()?.let {
                            onPrivacyClick()
                        }
                }
            )
        }
    }
}

@Composable
fun AgeSelectCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SanadTheme.colors.surface else SanadTheme.colors.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) SanadTheme.colors.accent else SanadTheme.colors.divider
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) SanadTheme.colors.accent else SanadTheme.colors.text
            )

            // Visual radio indicator: filled circle with checkmark vs hollow circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) SanadTheme.colors.accent else Color.Transparent
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) SanadTheme.colors.accent else SanadTheme.colors.borderInput,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "محدد",
                        tint = SanadTheme.colors.onAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
