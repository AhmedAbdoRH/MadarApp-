package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SanadTheme

/**
 * زر ثابت «مساعدة عاجلة»
 * - شكله: خلفية var(--text) وكلمته var(--bg)، بدون أحمر.
 * - ارتفاعه مش أقل من 44px، وما يغطيش أي محتوى.
 * - مزود بأيقونة مناسبة (قلب/حلقة نجاة).
 */
@Composable
fun UrgentHelpHeaderButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(SanadTheme.colors.text)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp)
            .testTag("urgent_help_header_button"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = SanadTheme.colors.bg,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "مساعدة عاجلة",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = SanadTheme.colors.bg
            )
        }
    }
}
