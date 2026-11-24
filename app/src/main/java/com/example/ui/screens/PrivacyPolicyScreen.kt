package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SanadTheme

// TODO: استبدال الصفحة بالنص القانوني النهائي قبل أي إطلاق.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("privacy_policy_screen"),
        containerColor = SanadTheme.colors.bg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "سياسة الخصوصية",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SanadTheme.colors.text
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("privacy_back_button")
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "سياسة الخصوصية",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.text
            )
            Text(
                text = "السياسة غير مكتملة، وليست سياسة خصوصية نهائية. لا تعتبر استخدام التطبيق ضماناً للسرية المطلقة.",
                style = MaterialTheme.typography.bodyLarge,
                color = SanadTheme.colors.textSecondary,
                lineHeight = 24.sp
            )
            Text(
                text = "قبل مشاركة معلومات حساسة",
                style = MaterialTheme.typography.titleMedium,
                color = SanadTheme.colors.text
            )
            Text(
                text = "لم تُنشر بعد تفاصيل الجهة المسؤولة ووسيلة التواصل، ومن يمكنه الاطلاع على البيانات، ومدة الاحتفاظ بها وطريقة طلب حذفها، وحدود السرية. تجنب مشاركة بيانات تعريفية أو معلومات حساسة لا تحتاجها المحادثة إلى أن تتضح هذه التفاصيل.",
                style = MaterialTheme.typography.bodyLarge,
                color = SanadTheme.colors.text,
                lineHeight = 26.sp
            )
        }
    }
}
