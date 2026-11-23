package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SanadTheme

@Composable
fun AccountSettingsScreen(
    canSwitchView: Boolean,
    isSpecialistMode: Boolean,
    onSwitchView: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().padding(20.dp).testTag("account_settings")) {
        Text("إعدادات الحساب", style = MaterialTheme.typography.headlineSmall, color = SanadTheme.colors.text)
        Text("تبديل الواجهة لا يغيّر صلاحيات حسابك أو حالة اعتمادك.",
            style = MaterialTheme.typography.bodyMedium, color = SanadTheme.colors.textSecondary)
        Spacer(Modifier.weight(1f))
        if (canSwitchView) {
            OutlinedButton(
                onClick = onSwitchView,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("account_view_switch")
            ) {
                Text(if (isSpecialistMode) "التبديل لواجهة المستفيد" else "التبديل لواجهة الأخصائي")
            }
        }
    }
}
